/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tictactoeserver;

/**
 *
 * @author HP
 */
public class Player {
    protected String user_id;
    protected String display_name;
    protected boolean status;
    protected int score;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    protected String game_moves;

    public Player() {
    }

    public Player(String user_id, String display_name, int score) {
        this.user_id = user_id;
        this.display_name = display_name;
        this.score = score;
        this.status = false;
    }
    
    

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getGame_moves() {
        return game_moves;
    }

    public void setGame_moves(String game_moves) {
        this.game_moves = game_moves;
    }
    
    
    
    
}
