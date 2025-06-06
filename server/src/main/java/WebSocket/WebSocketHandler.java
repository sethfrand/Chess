package WebSocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.AuthService;
import service.GameService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


@WebSocket
public class WebSocketHandler {
    private static final ConcurrentHashMap<Integer, CopyOnWriteArrayList<Session>> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, String> sessionToAuth = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Session, String> sessionToGame = new ConcurrentHashMap<>();

    private final Gson gson = new Gson();
    private final GameService gameService;
    private final AuthService authService;

    public WebSocketHandler() throws Exception {
        this.gameService = new GameService();
        this.authService = new AuthService();

    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws Exception {

    }


    @OnWebSocketConnect
    public void onConnect() {

    }

    @OnWebSocketClose
    public void onClose() {
        
    }

}
