package common;

import java.io.Serializable;

public class User implements Serializable {
    // TODO: 06/01/2023 Capire che vuol dire serialVersionUID = 1L in User
    //private static final long serialVersionUID = 1L;
    private final String ONLINE = "Online";
    private final String OFFLINE = "Offline";
    private String username;
    private String password;
    private String status;

    public User(String u, String p) {
        username = u;
        password = p;
        status = OFFLINE;
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

    public void setOnline() {
        status = ONLINE;
    }

    public void setOffline() {
        status = OFFLINE;
    }
}
