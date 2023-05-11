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
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public RequestHandler(Socket newClientSocket) {
        try {
            inputStream = new DataInputStream(newClientSocket.getInputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            printStream = new PrintStream(newClientSocket.getOutputStream());
            clientName = newClientSocket.getInetAddress().toString();
            for (RequestHandler client : RequestHandler.clients) {
                if (client.player != null && client.player.equals(player)) {
                    client.closeConnection();
                    RequestHandler.clients.remove(client);
                    break;
                }
            }

            if (!RequestHandler.clients.contains(this)) {
                RequestHandler.clients.add(this);
            }

            start();
        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            closeConnection();
        }
    }

    public void run() {

        while (true) {
            try {
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
                }
            } catch (IOException ex) {
                closeConnection();
            }
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
            case "ONLINEPLAYERLIST": {
                OnlinePlayersList();
            }
            break;

        }

    }

    private void OnlinePlayersList() throws SQLException {
        List<Player> players = new ArrayList<>();
        for (RequestHandler client : clients) {
            Player player = client.player;
            if (player != null && !players.contains(player)) {
                players.add(player);
            }
        }

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
        JsonElement signInElement = jsonObject.get("data");
        JsonObject signInObject = signInElement.getAsJsonObject();
        String userName = signInObject.get("username").getAsString();
        String password = signInObject.get("password").getAsString();

        JsonObject response = DataAccessLayer.getInstance().getPlayerByID(userName, password);

        JsonObject responseObject = response.get("response").getAsJsonObject();
        String responsestate = responseObject.get("response").getAsString();
        System.out.println(responseObject);
        if (responsestate.equals("success")) {
            JsonElement responseDataElement = response.get("data").getAsJsonObject();
            System.out.println(responseDataElement);
            JsonObject responseDataObject = responseDataElement.getAsJsonObject();
            System.out.println(responseDataObject);
            String user_id = responseDataObject.get("userId").getAsString();
            String display_name = responseDataObject.get("displayName").getAsString();
            int score = responseDataObject.get("totalScore").getAsInt();

            player = new Player(user_id, display_name, score);
        }
        sendResponseToClient(response);
        System.out.println("User exists in the database");
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
            RequestHandler.clients.remove(this);

        } catch (IOException ex1) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex1);
        }
    }
}
