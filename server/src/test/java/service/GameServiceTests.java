package service;

import model.UserData;
import org.junit.jupiter.api.Test;
import dataaccess.*;
import handler.*;
import server.*;
import service.*;
import model.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class GameServiceTests {
    private GameService gameService;

    @BeforeEach
    void setup() {
        gameService = new GameService();
        gameService.clear();
    }

    @Test
    void posGameCreate() {
        int gameID = gameService.createGame("new game");
        assertTrue(gameID > 0);
        Collection<GameData> games = gameService.listGames();
        assertTrue(games.stream().anyMatch(g -> g.getGameName().equals("new game")));

    }

    @Test
    void negGameCreate() {
        int gameID = gameService.createGame("");
        assertTrue(gameID > 0);

    }

    @Test
    void posListGames() {
        gameService.createGame("Chess");
        Collection<GameData> games = gameService.listGames();
        assertEquals(1, games.size());

    }

    @Test
    void negListGames() {
        Collection<GameData> games = gameService.listGames();
        assertTrue(games.isEmpty());

    }

    @Test
    void posJoinGame() throws Exception {
        int gameID = gameService.createGame("new game");
        assertTrue(gameService.joinGame(String.valueOf(gameID), "bob", "WHITE"));

    }

    @Test
    void negJoinGame() throws Exception {
        int gameID = gameService.createGame("new game");
        assertThrows(Exception.class, () -> gameService.joinGame("not valid id", "bob", "WHITE"));
    }

    @Test
    void clear() {
        gameService.createGame("short game");
        gameService.clear();
        assertTrue(gameService.listGames().isEmpty());

    }
}
