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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fg
 */
public class RequestHandler  extends Thread {
     private DataInputStream inputStream;
    private PrintStream printStream;
    private BufferedReader bufferedReader;
    private String clientName;
    static Vector<RequestHandler> clients = new Vector<RequestHandler>();

    public RequestHandler(Socket newClientSocket) throws IOException {
        inputStream = new DataInputStream(newClientSocket.getInputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        printStream = new PrintStream(newClientSocket.getOutputStream());
        clientName = newClientSocket.getInetAddress().toString();
        RequestHandler.clients.add(this);
        start();
    }

    public void run() {
        try {
            while (true) {
                String jsonString = bufferedReader.readLine();

                Gson gson = new Gson();
                JsonElement jsonElement = gson.fromJson(jsonString, JsonElement.class);
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                handleRequset(jsonObject);

            }

        } catch (IOException ex) {
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleRequset(JsonObject jsonObject) {

        JsonElement requestElement = jsonObject.get("request");
        JsonObject requestObject = requestElement.getAsJsonObject();
        String request = requestObject.get("request").getAsString();

        switch (request) {
            case "SIGNIN": {
                signInRequest(jsonObject);
            }
        }

    }

    private void signInRequest(JsonObject jsonObject) {
        JsonElement signInElement = jsonObject.get("data");
        JsonObject signInObject = signInElement.getAsJsonObject();
        String userName = signInObject.get("username").getAsString();
        String password = signInObject.get("password").getAsString();

    }
}


