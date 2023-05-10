package tictactoeserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public int insert(String userName, String displayName, String password) throws SQLException {

        int result = 0;
        PreparedStatement preStatement = databaseConnection.prepareStatement("INSERT INTO PLAYER (user_id,Display_name,password)" + "VALUES (?,?,?)");
        preStatement.setString(1, userName);
        preStatement.setString(2, displayName);
        preStatement.setString(3, password);
        result = preStatement.executeUpdate();
        return result;
    }

    public boolean getPlayerByID(String userName, String password) throws SQLException {
        boolean result;
        PreparedStatement preStatement = databaseConnection.prepareStatement("SELECT * FROM Player WHERE user_id = ? AND password = ?");
        preStatement.setString(1, userName);
        preStatement.setString(2, password);

        ResultSet rSet = preStatement.executeQuery();

        result = rSet.next();
        /*String name = rSet.getString("DISPLAY_NAME");
        String email = rSet.getString("USER_ID");*/
        return result;
    }

    /*public boolean checkPlayerExist(String email) throws SQLException {
        boolean isExist;
        preStatement preStatement = databaseConnection.prepareStatement("SELECT * FROM player WHERE EMAIL = ?");
        preStatement.setString(1, email);

        rSet rSet = preStatement.executeQuery();

        isExist = rSet.next();

        return isExist;
    }*/

    /**
     *
     * @return
     * @throws SQLException
     */


    public int getAllplayerList() throws SQLException {
    
        PreparedStatement preStatement = databaseConnection.prepareStatement("SELECT * FROM player");
        int  userCount=0;
        ResultSet rSet = preStatement.executeQuery();

        while (rSet.next()) {
            userCount++;
        }
        return userCount;
    }

    /*public ArrayList<Integer> getOnlineplayerList() throws SQLException {
        ArrayList<Integer> playerList = new ArrayList<>();

        PreparedStatement preStatement = databaseConnection.prepareStatement("SELECT * FROM player WHERE STATUS = 1");
        ResultSet rSet = preStatement.executeQuery();
        while (rSet.next()) {
            playerList.add(rSet.getInt(4));
        }

        return playerList;
    }
     public ArrayList<Integer> getOfflineplayerList() throws SQLException {
        ArrayList<Integer> playerList = new ArrayList<>();

        PreparedStatement preStatement = databaseConnection.prepareStatement("SELECT * FROM player WHERE STATUS = 0");
        ResultSet rSet = preStatement.executeQuery();

        while (rSet.next()) {
            playerList.add(rSet.getInt(4));
        }

        return playerList;
    }*/

    /*public int changePlayStatus(Player player) throws SQLException {
        int result = 0;
        preStatement preStatement = databaseConnection.prepareStatement("UPDATE player SET ISPLAYING = ? WHERE PLAYERID = ?");
        preStatement.setBoolean(1, !player.isIsPlaying());
        preStatement.setInt(2, player.getPlayerId());
        result = preStatement.executeUpdate();

        return result;
    }*/
}
