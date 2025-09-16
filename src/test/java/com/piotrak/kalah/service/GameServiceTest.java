package com.piotrak.kalah.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.piotrak.kalah.exception.ForbiddenOperationException;
import com.piotrak.kalah.model.Board;
import com.piotrak.kalah.model.Game;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameServiceTest {

  private static final String URL = "url";

  private final GameService service = new GameService();

  private Game game;

  @BeforeEach
  public void before() {
    game = service.createGame(URL);
  }

  @Test
  public void test_createGame() {
    Game game2 = service.createGame(URL);

    assertNotEquals(game.getUrl(), game2.getUrl());
  }

  @Test()
  public void test_makeMove_noGame() {
    assertThrows(IllegalArgumentException.class, () -> service.makeMove(2, 1));
  }

  @Test()
  public void test_makeMove_notYourTurn() {
    game.setPlayerOneMove(true);

    assertThrows(ForbiddenOperationException.class, () -> service.makeMove(game.getId(), 8));
  }

  @Test()
  public void test_makeMove_fromEmptyPit() {
    // Set pit 1 to 0
    Map<Integer, Integer> customStatus = new HashMap<>(game.getStatus());
    customStatus.put(1, 0);
    game.setBoard(new Board(customStatus));

    assertThrows(ForbiddenOperationException.class, () -> service.makeMove(game.getId(), 1));
  }

  @Test()
  public void test_makeMove_fromKalah() {
    assertThrows(ForbiddenOperationException.class, () -> service.makeMove(game.getId(), 7));
  }

  @Test()
  public void test_makeMove_endInKalah() {
    Game updatedGame = service.makeMove(game.getId(), 1);

    assertTrue(updatedGame.isPlayerOneMove());
  }

  @Test()
  public void test_makeMove_endInPit() {
    Game updatedGame = service.makeMove(game.getId(), 5);

    assertEquals(0, updatedGame.getStatus().get(5).intValue());
    assertFalse(updatedGame.isPlayerOneMove());
  }

  @Test()
  public void test_makeMove_endInOpponentKalah() {
    // Set pit 6 to 8
    Map<Integer, Integer> customStatus = new HashMap<>(game.getStatus());
    customStatus.put(6, 8);
    game.setBoard(new Board(customStatus));
    int opponentKalah = game.getStatus().get(14);

    Game updatedGame = service.makeMove(game.getId(), 6);

    assertEquals(0, updatedGame.getStatus().get(6).intValue());
    assertEquals(opponentKalah, updatedGame.getStatus().get(14).intValue());
  }

  @Test()
  public void test_makeMove_endInEmptyPit() {
    // Set pit 6 to 13
    Map<Integer, Integer> customStatus = new HashMap<>(game.getStatus());
    customStatus.put(6, 13);
    game.setBoard(new Board(customStatus));

    Game updatedGame = service.makeMove(game.getId(), 6);

    assertEquals(0, updatedGame.getStatus().get(6).intValue());
    assertEquals(0, updatedGame.getStatus().get(8).intValue());
    assertEquals(9, updatedGame.getStatus().get(7).intValue());
  }

  @Test()
  public void test_makeMove_playerOneWins() {
    Map<Integer, Integer> customStatus = new HashMap<>();
    customStatus.put(1, 0);
    customStatus.put(2, 0);
    customStatus.put(3, 0);
    customStatus.put(4, 0);
    customStatus.put(5, 0);
    customStatus.put(6, 1);
    customStatus.put(7, 10);
    customStatus.put(8, 1);
    customStatus.put(9, 1);
    customStatus.put(10, 1);
    customStatus.put(11, 1);
    customStatus.put(12, 1);
    customStatus.put(13, 1);
    customStatus.put(14, 1);
    game.setBoard(new Board(customStatus));

    Game updatedGame = service.makeMove(game.getId(), 6);

    assertEquals(0, updatedGame.getStatus().get(6).intValue());
    assertEquals(0, updatedGame.getStatus().get(8).intValue());
    assertEquals(11, updatedGame.getStatus().get(7).intValue());
    assertEquals(7, updatedGame.getStatus().get(14).intValue());
    assertTrue(updatedGame.getMessage().contains("Player One"));
  }

  @Test
  public void test_makeMove_differentGames() {
    Game game2 = service.createGame(URL);

    Game updatedGame = service.makeMove(game.getId(), 1);
    Game updatedGame2 = service.makeMove(game2.getId(), 2);

    assertNotEquals(updatedGame.getStatus().get(1), updatedGame2.getStatus().get(1));
  }
}
