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
       
            while (true) {
                 try {
                String jsonString = bufferedReader.readLine();
                if(jsonString != null)
                {

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
            Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
            
        }
    }
    }

    private void handleRequset(JsonObject jsonObject) throws SQLException {

        JsonElement requestElement = jsonObject.get("request");
        JsonObject requestObject = requestElement.getAsJsonObject();
        String request = requestObject.get("request").getAsString();

        switch (request) {
            case "SIGNIN": 
                signInRequest(jsonObject);
                break;
            case "SIGNUP":
                signUpRequest(jsonObject);
                break;
        }

    }

private void signInRequest( JsonObject jsonObject) throws SQLException {
    JsonElement signInElement = jsonObject.get("data");
    JsonObject signInObject = signInElement.getAsJsonObject();
    String userName = signInObject.get("username").getAsString();
    String password = signInObject.get("password").getAsString();
    Connection connection = null;
    
    try {
        connection = DriverManager.getConnection("jdbc:derby://localhost:1527/GameDataBase", "root", "root");

<<<<<<< Updated upstream
        String query = "SELECT * FROM Player WHERE DISPLAY_NAME = ? AND password = ? ";
=======
         try {
    Connection connection = DriverManager.
            getConnection("jdbc:derby://localhost:1527/Player", "Root", "root");

        String query = "SELECT * FROM Player WHERE user_id = ? AND password = ?";
>>>>>>> Stashed changes
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, userName);
        statement.setString(2, password);

        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            System.out.println("username " + userName);
            System.out.println("password " + password);
            
            String displayName = resultSet.getString("DISPLAY_NAME");
            int totalScore = resultSet.getInt("SCORE");
            int UserId = resultSet.getInt("USER_ID");

            
            JsonObject response = new JsonObject();
            response.addProperty("response", "success");
            response.addProperty("displayName", displayName);
            response.addProperty("totalScore", totalScore);
            response.addProperty("userId", UserId);

            
            sendResponseToClient(response);
            System.out.println("User exists in the database");
        } else {
            JsonObject response = new JsonObject();
            response.addProperty("response", "failure");
            System.out.println("User does not exist in the database");
        }
    } catch (Exception ex) {
        
        System.out.println(ex.getLocalizedMessage());
    } finally {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}

<<<<<<< Updated upstream
private void sendResponseToClient(JsonObject message) {
    try {
        printStream.println(message.toString());
    } catch (Exception ex) {
        System.out.println("Error sending message: " + ex.getMessage());
    }
}
    
=======
    private void signUpRequest(JsonObject jsonObject) {
            
             JsonElement signUpElement = jsonObject.get("data");
             JsonObject signUpObject = signUpElement.getAsJsonObject();
             String userName = signUpObject.get("username").getAsString();
             String displayname=signUpObject.get("displayname").getAsString();
             String password = signUpObject.get("password").getAsString();
             
            try {
                int result=0;
                Connection connection = DriverManager.
                  getConnection("jdbc:derby://localhost:1527/Player", "Root", "root");

              String query = "INSERT INTO PLAYER (user_id,Display_name,password)" +"VALUES (?,?,?)";
              PreparedStatement statement = connection.prepareStatement(query);
              statement.setString(1, userName);
              statement.setString(2, displayname);
              statement.setString(3, password);

             result = statement.executeUpdate();


              if (result!=0) {

                  System.out.println("insert in the database");
              } else {

                  System.out.println("fail to insert in the database");
              }

          }   catch (SQLException ex) {
                  Logger.getLogger(RequestHandler.class.getName()).log(Level.SEVERE, null, ex);
              }
}
>>>>>>> Stashed changes
}


