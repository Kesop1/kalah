package com.piotrak.kalah.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.piotrak.kalah.exception.ForbiddenOperationException;
import com.piotrak.kalah.model.Game;
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
    game.getStatus().put(1, 0);

    assertThrows(ForbiddenOperationException.class, () -> service.makeMove(game.getId(), 1));
  }

  @Test()
  public void test_makeMove_fromKalah() {
    assertThrows(ForbiddenOperationException.class, () -> service.makeMove(game.getId(), 7));
  }

  @Test()
  public void test_makeMove_endInKalah() {
    service.makeMove(game.getId(), 1);

    assertTrue(game.isPlayerOneMove());
  }

  @Test()
  public void test_makeMove_endInPit() {
    service.makeMove(game.getId(), 5);

    assertEquals(0, game.getStatus().get(5).intValue());
    assertFalse(game.isPlayerOneMove());
  }

  @Test()
  public void test_makeMove_endInOpponentKalah() {
    game.getStatus().put(6, 8);
    int opponentKalah = game.getStatus().get(14);

    service.makeMove(game.getId(), 6);

    assertEquals(0, game.getStatus().get(6).intValue());
    assertEquals(opponentKalah, game.getStatus().get(14).intValue());
  }

  @Test()
  public void test_makeMove_endInEmptyPit() {
    game.getStatus().put(6, 13);

    service.makeMove(game.getId(), 6);

    assertEquals(0, game.getStatus().get(6).intValue());
    assertEquals(0, game.getStatus().get(8).intValue());
    assertEquals(9, game.getStatus().get(7).intValue());
  }

  @Test()
  public void test_makeMove_playerOneWins() {
    game.getStatus().put(1, 0);
    game.getStatus().put(2, 0);
    game.getStatus().put(3, 0);
    game.getStatus().put(4, 0);
    game.getStatus().put(5, 0);
    game.getStatus().put(6, 1);
    game.getStatus().put(7, 10);

    game.getStatus().put(8, 1);
    game.getStatus().put(9, 1);
    game.getStatus().put(10, 1);
    game.getStatus().put(11, 1);
    game.getStatus().put(12, 1);
    game.getStatus().put(13, 1);
    game.getStatus().put(14, 1);

    service.makeMove(game.getId(), 6);

    assertEquals(0, game.getStatus().get(6).intValue());
    assertEquals(0, game.getStatus().get(8).intValue());
    assertEquals(11, game.getStatus().get(7).intValue());
    assertEquals(7, game.getStatus().get(14).intValue());
    assertTrue(game.getMessage().contains("Player One"));
  }

  @Test
  public void test_makeMove_differentGames() {
    Game game2 = service.createGame(URL);

    service.makeMove(game.getId(), 1);
    service.makeMove(game2.getId(), 2);

    assertNotEquals(game.getStatus().get(1), game2.getStatus().get(1));
  }
}
