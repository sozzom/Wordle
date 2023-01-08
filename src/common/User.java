package common;

import Gioco.Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.function.ToDoubleBiFunction;

public class User implements Serializable {

    //TODO: 06/01/2023 Capire che vuol dire serialVersionUID = 1L in User
    //private static final long serialVersionUID = 1L;
    private final String ONLINE = "Online";
    private final String OFFLINE = "Offline";
    private String username;
    private String password;
    private String status;
    public ArrayList<String> playedWords;
    public int score;
    public int played;
    public int wins;

    public User(String u, String p) {
        username = u;
        password = p;
        status = OFFLINE;
        score = 0;
        played = 0;
        wins = 0;
        playedWords = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    public int getScore() {
        return score;
    }

    public void setOnline() {
        status = ONLINE;
    }

    public void setOffline() {
        status = OFFLINE;
    }

    @Override
    public String toString() {
        return " User: " + "username = '" + username + '\'' + ", status = '" + status + '\'' + "\n";
    }
}
