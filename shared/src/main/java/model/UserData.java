package model;


public class UserData {
    public static String setUserName;
    public static String setPassword;
    private String userName;
    private String password;
    private String email;

    public UserData() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UserData(String userName, String password, String email) {
        this.userName = userName;
        this.password = password;
        this.email = email;

    }
}
