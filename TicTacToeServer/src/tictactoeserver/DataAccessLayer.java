/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servertest;


import java.sql.*;
import org.apache.derby.jdbc.ClientDriver;
import org.json.simple.JSONObject;

/**
 *
 * @author HP
 */
public class DataAccessLayer {
    
    public void connect() throws SQLException{
        DriverManager.registerDriver(new ClientDriver());
        Connection con;
        con = DriverManager.getConnection("jdbc:derby://localhost:1527/Player","Root","root");
    }
    public static int insert(JSONObject json) throws SQLException{
        int result=0;
        DriverManager.registerDriver(new ClientDriver());
        Connection con;
        con = DriverManager.getConnection("jdbc:derby://localhost:1527/Player","Root","root");
        String sql = "INSERT INTO PLAYER (user_id,Display_name,password)" +"VALUES (?,?,?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, (String)json.get("name"));
            pst.setString(2, (String)json.get("name"));
            pst.setString(3, (String)json.get("password"));
            pst.executeUpdate();
        }
        con.close();
        return result;
    }
    
}
