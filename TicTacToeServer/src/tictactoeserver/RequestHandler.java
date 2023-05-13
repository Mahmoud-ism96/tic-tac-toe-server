/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tictactoeserver;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fg
 */
public class RequestHandler extends Thread {

    private DataInputStream inputStream;
    private PrintStream printStream;
    private BufferedReader bufferedReader;
    private String clientName;
    static Vector<RequestHandler> clients = new Vector<RequestHandler>();
    private Player player;
    private boolean isRunning;

    public RequestHandler(Socket newClientSocket) {
        try {
            inputStream = new DataInputStream(newClientSocket.getInputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            printStream = new PrintStream(newClientSocket.getOutputStream());
            clientName = newClientSocket.getInetAddress().toString();
            RequestHandler.clients.add(this);
            isRunning = true;

            start();
        } catch (IOException ex) {
            closeConnection();
        }
    }

    public void run() {

        try {
            while (isRunning) {
                String jsonString = bufferedReader.readLine();
                if (jsonString != null) {

                    Gson gson = new Gson();
                    JsonElement jsonElement = gson.fromJson(jsonString, JsonElement.class);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    try {
                        handleRequset(jsonObject);
                    } catch (SQLException ex) {
                        Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (inputStream.available() == 0) {
                    closeConnection();
                }
            }
        } catch (IOException ex) {
            closeConnection();
        }

    }

    private void handleRequset(JsonObject jsonObject) throws SQLException {

        JsonElement requestElement = jsonObject.get("request");
        JsonObject requestObject = requestElement.getAsJsonObject();
        String request = requestObject.get("request").getAsString();

        switch (request) {
            case "SIGNIN": {
                signInRequest(jsonObject);
            }
            break;
            case "SIGNUP": {
                signUpRequest(jsonObject);
            }
            break;
            case "SIGNOUT": {
                signOutRequest();
            }
            break;
            case "ONLINEPLAYERLIST": {
                OnlinePlayersList();
            }
            break;
            case "SENDGAMEREQUEST": {
                sendGameRequest(jsonObject);
            }
            break;
            case "GAMEACCEPTED": {
                gameAccepted(jsonObject);
            }
            break;
            case "GAMEREJECTED": {
                gameRejected(jsonObject);
            }
            break;
            case "GAMEMOVE": {
                gameMove(jsonObject);
            }
            break;
            case "UPDATESCORE": {
                updateScore(jsonObject);
            }
            break;
            case "GAMEHISTORY": {
                gameHistory(jsonObject);
            }
            break;
            case "PLAYERLEFT": {
                playerLeft(jsonObject);
            }
        }

    }

    private void OnlinePlayersList() throws SQLException {
        List<Player> players = new ArrayList<>();
        for (RequestHandler client : clients) {
            Player player = client.player;
            if (player != null && !players.contains(player) && player.isPlaying == false) {
                players.add(player);
            }
        }

        players.remove(player);

        JsonObject responseList = new JsonObject();
        JsonObject onlinePlayersJson = new JsonObject();
        JsonObject request = new JsonObject();

        request.addProperty("request", "ONLINEPLAYERLIST");
        onlinePlayersJson.add("players", new Gson().toJsonTree(players));
        responseList.add("request", request);
        responseList.add("data", onlinePlayersJson);
        sendResponseToClient(responseList);
    }

    private void signInRequest(JsonObject jsonObject) throws SQLException {

        boolean checker = true;
        JsonElement signInElement = jsonObject.get("data");
        JsonObject signInObject = signInElement.getAsJsonObject();
        String userName = signInObject.get("username").getAsString();
        String password = signInObject.get("password").getAsString();

        JsonObject response = DataAccessLayer.getInstance().getPlayerByID(userName, password);

        JsonObject responseObject = response.get("response").getAsJsonObject();
        String responsestate = responseObject.get("response").getAsString();

        for (RequestHandler client : clients) {
            if (client.player != null) {
                if (client.player.getUser_id().equals(userName)) {
                    checker = false;
                    response.remove("response");
                    JsonObject responseUpdate = new JsonObject();
                    responseUpdate.addProperty("response", "exist");

                    response.add("response", responseUpdate);
                }
            }
        }

        if (responsestate.equals("success") && checker) {
            JsonElement responseDataElement = response.get("data").getAsJsonObject();
            JsonObject responseDataObject = responseDataElement.getAsJsonObject();
            String user_id = responseDataObject.get("userId").getAsString();
            String display_name = responseDataObject.get("displayName").getAsString();
            int score = responseDataObject.get("totalScore").getAsInt();

            player = new Player(user_id, display_name, score);
        }

        System.out.println(response);
        sendResponseToClient(response);
    }

    private void signUpRequest(JsonObject jsonObject) throws SQLException {
        JsonElement signInElement = jsonObject.get("data");
        JsonObject signUpObject = signInElement.getAsJsonObject();
        String userName = signUpObject.get("username").getAsString();
        String displayname = signUpObject.get("displayname").getAsString();
        String password = signUpObject.get("password").getAsString();

        JsonObject response = DataAccessLayer.getInstance().insert(userName, displayname, password);
        JsonObject responseObject = response.get("response").getAsJsonObject();
        String responsestate = responseObject.get("response").getAsString();

        if (responsestate.equals("success")) {
            JsonElement responseDataElement = response.get("data").getAsJsonObject();
            JsonObject responseDataObject = responseDataElement.getAsJsonObject();
            String user_id = responseDataObject.get("userId").getAsString();
            String display_name = responseDataObject.get("displayName").getAsString();
            int score = responseDataObject.get("totalScore").getAsInt();

            player = new Player(user_id, display_name, score);
        }

        sendResponseToClient(response);
    }

    private void signOutRequest() {
        closeConnection();
    }

    private void sendGameRequest(JsonObject jsonObject) {
        JsonObject replyObject = new JsonObject();
        JsonElement gameElement = jsonObject.get("data");
        JsonObject sendGameObject = gameElement.getAsJsonObject();
        JsonObject requestData = new JsonObject();
        requestData.addProperty("request", "GAMEREQUEST");
        String player2 = sendGameObject.get("player2").getAsString();

        replyObject.add("data", sendGameObject);
        replyObject.add("request", requestData);

        for (RequestHandler client : clients) {
            if (client.player.getUser_id().equals(player2)) {
                client.sendResponseToClient(replyObject);
            }
        }

    }

    private void gameAccepted(JsonObject jsonObject) {
        JsonObject replyObject = new JsonObject();
        JsonElement gameElement = jsonObject.get("data");
        JsonObject sendGameObject = gameElement.getAsJsonObject();
        JsonObject requestData = new JsonObject();
        requestData.addProperty("request", "GAMEACCEPTED");
        String player1 = sendGameObject.get("player1").getAsString();

        replyObject.add("data", sendGameObject);
        replyObject.add("request", requestData);

        player.isPlaying = true;

        for (RequestHandler client : clients) {
            if (client.player.getUser_id().equals(player1)) {
                client.player.isPlaying = true;
                client.sendResponseToClient(replyObject);
            }
        }

    }

    private void gameRejected(JsonObject jsonObject) {
        JsonObject replyObject = new JsonObject();
        JsonElement gameElement = jsonObject.get("data");
        JsonObject sendGameObject = gameElement.getAsJsonObject();
        JsonObject requestData = new JsonObject();
        requestData.addProperty("request", "GAMEREJECTED");
        String player1 = sendGameObject.get("player1").getAsString();

        replyObject.add("data", sendGameObject);
        replyObject.add("request", requestData);

        for (RequestHandler client : clients) {
            if (client.player.getUser_id().equals(player1)) {
                client.sendResponseToClient(replyObject);
            }
        }

    }

    private void gameMove(JsonObject jsonObject) {

        JsonObject replyObject = new JsonObject();
        JsonElement gameElement = jsonObject.get("data");
        JsonElement indexElement = jsonObject.get("index");
        JsonObject sendGameObject = gameElement.getAsJsonObject();
        JsonObject gameIndex = indexElement.getAsJsonObject();
        JsonObject requestData = new JsonObject();
        requestData.addProperty("request", "GAMEMOVE");
        String vsPlayer = sendGameObject.get("vsPlayer").getAsString();

        replyObject.add("data", sendGameObject);
        replyObject.add("request", requestData);
        replyObject.add("index", gameIndex);

        System.out.println(replyObject);
        for (RequestHandler client : clients) {
            if (client.player.getUser_id().equals(vsPlayer)) {
                client.sendResponseToClient(replyObject);
            }
        }

    }

    private void updateScore(JsonObject jsonObject) throws SQLException {
        JsonElement gameElement = jsonObject.get("data");
        JsonObject sendGameObject = gameElement.getAsJsonObject();
        String winner = sendGameObject.get("winner").getAsString();
        String loser = sendGameObject.get("loser").getAsString();
        String moves = sendGameObject.get("moves").getAsString();

        player.isPlaying = false;

        for (RequestHandler client : clients) {
            if (client.player.getUser_id().equals(loser)) {
                client.player.isPlaying = false;
            }
        }

        DataAccessLayer.getInstance().updateWinnerScore(winner, loser, moves);
    }

    private void gameHistory(JsonObject jsonObject) throws SQLException {
        JsonElement gameElement = jsonObject.get("data");
        JsonObject sendGameObject = gameElement.getAsJsonObject();
        String user = sendGameObject.get("user").getAsString();
        JsonObject requestData = new JsonObject();
        requestData.addProperty("request", "GAMEHISTORY");

        JsonObject json = new JsonObject();
        json.add("data", DataAccessLayer.getInstance().getAllGames(user));
        json.add("request", requestData);

        sendResponseToClient(json);
    }

    private void playerLeft(JsonObject jsonObject) {
        JsonElement gameElement = jsonObject.get("data");
        JsonObject sendGameObject = gameElement.getAsJsonObject();
        String user = sendGameObject.get("playing").getAsString();
        JsonObject requestData = new JsonObject();
        requestData.addProperty("request", "PLAYERLEFT");

        JsonObject json = new JsonObject();
        json.add("data", sendGameObject);
        json.add("request", requestData);

        player.isPlaying = false;

        for (RequestHandler client : clients) {
            if (client.player.getUser_id().equals(user)) {
                client.player.isPlaying = false;
                client.sendResponseToClient(json);
            }
        }

    }

    private void sendResponseToClient(JsonObject message) {
        try {
            printStream.println(message.toString());
        } catch (Exception ex) {
            System.out.println("Error sending message: " + ex.getMessage());
        }
    }

    private void closeConnection() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (printStream != null) {
                printStream.close();
            }
            isRunning = false;
            RequestHandler.clients.remove(this);

        } catch (IOException ex1) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex1);
        }
    }

    public static int countOnline() {
        int count = 0;
        for (RequestHandler client : clients) {
            if (client.player != null) {
                count++;
            }
        }
        return count;
    }
}
