package ui;

import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


public class SercerFacade {
    private final String serverUrl;
    private final Gson gson;

    public SercerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
        this.gson = new Gson();
    }

    public String register(String username, String password, String email) throws Exception{
        var path = "/user";
        var request = new RegisterRequest(username, password, email);

        var response = makeRequest("POST", path, request, null, AuthResponse.class);
        return response.authToken();
    }

    public <T> T makeRequest(String method, String path, Object request, String authToken, Class<T> responseClass) throws Exception {
        try{
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            writeHeads(http, authToken);
            writeBody(request, http);
            http.connect();


        }
    }
    private void writeHeads(HttpURLConnection http, String authToken)
    {
        if (authToken ==null)
        {
            http.addRequestProperty("Authorization", authToken);
        }
        http.addRequestProperty("Content-Type", "application/json");
    }
    private void writeBody(Object request,HttpURLConnection http) throws Exception
    {
        if(request != null)
        {
            String requestData = gson.toJson(request);
            try (OutputStream body = http.getOutputStream())
            {
                body.write(requestData.getBytes());
            }
        }
    }

    public boolean logout(String authtoken) throws Exception
    {
        var path = "/session";
        makeRequest("DELETE",path, null,authtoken,null);
        return true;
    }

    public int createGame(String gameName, String authToken)
    {
        var path = "/game";
        var request = new CreateGameRec();

    }



    private record RegisterRequest(String username, String password, String email) {
    }

    private record AuthResponse(String authToken, String username) {
    }

    private record CreateGameRec(String gameName, String authToken){}

}
