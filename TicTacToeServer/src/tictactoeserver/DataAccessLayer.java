package tictactoeserver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.derby.jdbc.ClientDriver;
//import tictactoeserver.model.Player;

public final class DataAccessLayer {

    Connection databaseConnection;
    private static DataAccessLayer instance;

    private DataAccessLayer() throws SQLException {
        DriverManager.registerDriver(new ClientDriver());
        databaseConnection = DriverManager.getConnection("jdbc:derby://localhost:1527/Player", "Root", "root");
    }

    public static synchronized DataAccessLayer getInstance() throws SQLException {
        if (instance == null) {
            instance = new DataAccessLayer();
        }
        return instance;
    }

    public JsonObject insert(String userName, String displayName, String password) throws SQLException {

        int result = 0;
        JsonObject playerData = new JsonObject();
        PreparedStatement preStatement = databaseConnection.prepareStatement("INSERT INTO PLAYER (user_id,Display_name,password)" + "VALUES (?,?,?)");
        preStatement.setString(1, userName);
        preStatement.setString(2, displayName);
        preStatement.setString(3, password);

        JsonObject signinObject = new JsonObject();
        JsonObject requestData = new JsonObject();
        JsonObject responseData = new JsonObject();

        try {
            result = preStatement.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            result = -1;
            responseData.addProperty("response", "exist");
        }

        if (result > 0) {
            responseData.addProperty("response", "success");
            signinObject.addProperty("displayName", displayName);
            signinObject.addProperty("totalScore", 0);
            signinObject.addProperty("userId", userName);
            playerData.add("data", signinObject);
        } else if (result == 0) {
            responseData.addProperty("response", "fail");
        }

        requestData.addProperty("request", "SIGNUP");

        playerData.add("request", requestData);
        playerData.add("response", responseData);

        System.out.println(playerData);

        return playerData;
    }

    public JsonObject getPlayerByID(String userName, String password) throws SQLException {
        boolean result = false;
        JsonObject playerData = new JsonObject();
        PreparedStatement preStatement = databaseConnection.prepareStatement("SELECT * FROM Player WHERE user_id = ? AND password = ?");
        preStatement.setString(1, userName);
        preStatement.setString(2, password);

        ResultSet rSet = preStatement.executeQuery();

        result = rSet.next();

        JsonObject signInObject = new JsonObject();
        JsonObject requestData = new JsonObject();
        JsonObject responseData = new JsonObject();

        if (result) {
            responseData.addProperty("response", "success");
            signInObject.addProperty("displayName", rSet.getString(2));
            signInObject.addProperty("totalScore", rSet.getInt(4));
            signInObject.addProperty("userId", userName);
            playerData.add("data", signInObject);
        } else {
            responseData.addProperty("response", "fail");
        }

        requestData.addProperty("request", "SIGNIN");

        playerData.add("request", requestData);
        playerData.add("response", responseData);
        System.out.println("Player Logedin");
        return playerData;
    }

    public int getAllplayerList() throws SQLException {

        PreparedStatement preStatement = databaseConnection.prepareStatement("SELECT * FROM player");
        int userCount = 0;
        ResultSet rSet = preStatement.executeQuery();

        while (rSet.next()) {
            userCount++;
        }
        return userCount;
    }

    public JsonObject getAllGames(String user) throws SQLException {

        String sql = "SELECT Game.*, Player1.Display_Name AS Winner_Name, Player2.Display_Name AS Loser_Name "
                + "FROM Game "
                + "INNER JOIN Player AS Player1 ON Game.Winner_id = Player1.User_Id "
                + "INNER JOIN Player AS Player2 ON Game.Loser_id = Player2.User_Id "
                + "WHERE Winner_id = ? OR Loser_id = ?";

        PreparedStatement preStatement = databaseConnection.prepareStatement(sql);
        preStatement.setString(1, user);
        preStatement.setString(2, user);
        ResultSet rSet = preStatement.executeQuery();

        List<Map<String, Object>> resultList = new ArrayList<>();
        ResultSetMetaData metaData = rSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        while (rSet.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                Object columnValue = rSet.getObject(i);
                row.put(columnName, columnValue);
            }
            resultList.add(row);
        }

        JsonObject result = new JsonObject();
        result.add("data", new Gson().toJsonTree(resultList));

        return result;
    }

    public void updateWinnerScore(String winnerId, String loserId, String gameMoves) throws SQLException {
        String sql = "INSERT INTO Game (Winner_id, Loser_id, Game_Moves, Is_Draw, Game_Date) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement preStatement = databaseConnection.prepareStatement(sql);

        java.util.Date now = new java.util.Date();
        java.sql.Timestamp timestamp = new java.sql.Timestamp(now.getTime());

        preStatement.setString(1, winnerId);
        preStatement.setString(2, loserId);
        preStatement.setString(3, gameMoves);
        preStatement.setInt(4, 0);
        preStatement.setTimestamp(5, timestamp);

        preStatement.executeUpdate();

        String updateQuery = "UPDATE Player SET Score = Score + 5 WHERE User_Id = ?";
        try (PreparedStatement stmt = databaseConnection.prepareStatement(updateQuery)) {
            stmt.setString(1, winnerId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println(rowsAffected + " rows updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
