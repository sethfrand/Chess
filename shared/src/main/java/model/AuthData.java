package model;

public class AuthData {
    private String authToken;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private String userName;

    public AuthData(String authToken, String userName) {
        this.authToken = authToken;
        this.userName = userName;
    }
}
