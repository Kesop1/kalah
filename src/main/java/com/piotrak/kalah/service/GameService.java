package com.piotrak.kalah.service;

import static com.piotrak.kalah.model.Game.MESSAGE_DRAW;
import static com.piotrak.kalah.model.Game.MESSAGE_GAME_CREATED;
import static com.piotrak.kalah.model.Game.MESSAGE_MOVE_AGAIN;
import static com.piotrak.kalah.model.Game.MESSAGE_PLAYER_ONE_MOVE;
import static com.piotrak.kalah.model.Game.MESSAGE_PLAYER_ONE_WON;
import static com.piotrak.kalah.model.Game.MESSAGE_PLAYER_TWO_MOVE;
import static com.piotrak.kalah.model.Game.MESSAGE_PLAYER_TWO_WON;
import static java.util.Objects.isNull;
import com.piotrak.kalah.exception.ForbiddenOperationException;
import com.piotrak.kalah.model.Game;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GameService {

  private static final int PLAYER_PIT_NUMBER = 6;
  private static final int INITIAL_ROCKS_NUMBER = 6;
  private static final int BOARD_SIZE = (PLAYER_PIT_NUMBER + 1) * 2;
  private static final int PLAYER_ONE_KALAH = PLAYER_PIT_NUMBER + 1;
  private static final int PLAYER_TWO_KALAH = PLAYER_ONE_KALAH * 2;

  private static final Map<Integer, Integer> NEW_BOARD = new HashMap<>(BOARD_SIZE);

  static {
    for (int i = 1; i <= BOARD_SIZE; i++) {
      int rocks = (i == PLAYER_ONE_KALAH || i == PLAYER_TWO_KALAH) ? 0 : INITIAL_ROCKS_NUMBER;
      NEW_BOARD.put(i, rocks);
    }
  }

  private final Map<Integer, Game> gameMap = new HashMap<>();

  /**
   * Create a new Kalah game
   * @param baseUrl Server's Url
   * @return Kalah game
   */
  public Game createGame(String baseUrl) {
    Integer gameId = gameMap.size() + 1;
    Game game = Game.builder()
      .id(gameId)
      .url(baseUrl + "/" + gameId)
      .status(createNewGameBoard())
      .message(MESSAGE_GAME_CREATED)
      .playerOneMove(true)
      .build();
    gameMap.put(gameId, game);
    return game;
  }

  private Map<Integer, Integer> createNewGameBoard() {
    return new HashMap<>(NEW_BOARD);
  }

  /**
   * Make a move in the Kalah game
   * @param gameId Kalah game id
   * @param pitId pit from which to move
   * @return Kalah game
   */
  public Game makeMove(Integer gameId, Integer pitId) {
    Game game = gameMap.get(gameId);
    if (isNull(game)) {
      throw new IllegalArgumentException(
        String.format("Unable to find a game with id: %s", gameId));
    }
    verifyPlayerTurn(game.isPlayerOneMove(), pitId);
    moveRocksFromPit(game, pitId);
    return game;
  }

  /**
   * Moves the rocks form a pit.
   * The player who begins picks up all the stones in any of their own pits, and sows the stones on to the right, one in
   * each of the following pits, including his own Kalah. No stones are put in the opponent's' Kalah. If the players last
   * stone lands in his own Kalah, he gets another turn. This can be repeated any number of times before it's the other
   * player's turn.
   * @param game Kalah game
   * @param pitId pit from which to move
   */
  private void moveRocksFromPit(Game game, Integer pitId) {
    if (game.getStatus().get(pitId) == 0) {
      throw new ForbiddenOperationException("Unable to move rocks from an empty pit!");
    }
    if (pitId == PLAYER_ONE_KALAH || pitId == PLAYER_TWO_KALAH) {
      throw new ForbiddenOperationException("Unable to move rocks from Kalah!");
    }

    int lastPitNumber = moveTheRocks(game, pitId);
    captureOpponentsRocks(game, lastPitNumber);
    boolean gameFinished = checkGameFinished(game);
    if (!gameFinished) {
      setPlayerMove(game, lastPitNumber);
    }
  }

  /**
   * Checks if the game is over.
   * The game is over as soon as one of the sides run out of stones. The player who still has stones in his/her pits keeps
   * them and puts them in his/hers Kalah. The winner of the game is the player who has the most stones in his Kalah.
   * @param game Kalah game
   * @return true if game is finished
   */
  private boolean checkGameFinished(Game game) {
    Map<Integer, Integer> status = game.getStatus();
    boolean finished = false;
    int sumPlayerOne = 0;
    for (int i = 1; i < 7; i++) {
      sumPlayerOne += status.get(i);
    }

    int sumPlayerTwo = 0;
    for (int i = 8; i < BOARD_SIZE; i++) {
      sumPlayerTwo += status.get(i);
    }

    if (sumPlayerOne == 0) {
      status.put(PLAYER_TWO_KALAH, status.get(PLAYER_TWO_KALAH) + sumPlayerTwo);
      for (int i = 8; i < BOARD_SIZE; i++) {
        status.put(i, 0);
      }
      finished = true;
      game.setPlayerOneMove(true);
    } else if (sumPlayerTwo == 0) {
      status.put(PLAYER_ONE_KALAH, status.get(PLAYER_ONE_KALAH) + sumPlayerOne);
      for (int i = 1; i < 7; i++) {
        status.put(i, 0);
      }
      finished = true;
      game.setPlayerOneMove(false);
    }

    if (finished) {
      int playerOneScore = status.get(PLAYER_ONE_KALAH);
      int playerTwoScore = status.get(PLAYER_TWO_KALAH);
      if (playerOneScore > playerTwoScore) {
        game.setMessage(String.format(MESSAGE_PLAYER_ONE_WON, playerOneScore, playerTwoScore));
      } else if (playerOneScore < playerTwoScore) {
        game.setMessage(String.format(MESSAGE_PLAYER_TWO_WON, playerTwoScore, playerOneScore));
      } else {
        game.setMessage(MESSAGE_DRAW);
      }
    }
    return finished;
  }

  /**
   * Checks if opponents rocks are to be captured.
   * When the last stone lands in an own empty pit, the player captures this stone and all stones in the opposite pit (the
   * other players' pit) and puts them in his own Kalah.
   * @param game Kalah game
   * @param lastPitNumber the pit where the last stone was put
   */
  private void captureOpponentsRocks(Game game, int lastPitNumber) {
    int playersKalah;
    int otherPlayersKalah;
    Map<Integer, Integer> status = game.getStatus();
    if (status.get(lastPitNumber) != 1) {
      return;
    }
    if (game.isPlayerOneMove()) {
      if (lastPitNumber >= PLAYER_ONE_KALAH) {
        return;
      }
      playersKalah = PLAYER_ONE_KALAH;
      otherPlayersKalah = PLAYER_TWO_KALAH;
    } else {
      if (lastPitNumber == PLAYER_TWO_KALAH || lastPitNumber <= PLAYER_ONE_KALAH) {
        return;
      }
      playersKalah = PLAYER_TWO_KALAH;
      otherPlayersKalah = PLAYER_ONE_KALAH;
    }

    status.put(playersKalah, status.get(playersKalah) + status.put(lastPitNumber, 0));

    int step = lastPitNumber % (BOARD_SIZE / 2);
    int opponentsPit = otherPlayersKalah - step;
    status.put(playersKalah, status.get(playersKalah) + status.put(opponentsPit, 0));
  }

  /**
   * Switches the players turn if the last stone was not put in player's Kalah
   * @param game Kalah game
   * @param lastPitNumber the pit where the last stone was put
   */
  private void setPlayerMove(Game game, int lastPitNumber) {
    if (game.isPlayerOneMove() && lastPitNumber != PLAYER_ONE_KALAH
      || !game.isPlayerOneMove() && lastPitNumber != PLAYER_TWO_KALAH) {
      game.setPlayerOneMove(!game.isPlayerOneMove());
      game.setMessage(game.isPlayerOneMove() ? MESSAGE_PLAYER_ONE_MOVE : MESSAGE_PLAYER_TWO_MOVE);
    } else {
      game.setMessage(MESSAGE_MOVE_AGAIN + (game.isPlayerOneMove() ? MESSAGE_PLAYER_ONE_MOVE
        : MESSAGE_PLAYER_TWO_MOVE));
    }
  }

  /**
   * Moves the rocks from the pit.
   * @param game Kalah game
   * @param pitId pit from which to move
   * @return number of the pit where the last stone was put
   */
  private int moveTheRocks(Game game, Integer pitId) {
    int otherPlayersKalah = game.isPlayerOneMove() ? PLAYER_TWO_KALAH : PLAYER_ONE_KALAH;
    Map<Integer, Integer> status = game.getStatus();
    int rocksNumber = status.put(pitId, 0);
    int currentPit = pitId;
    for (int rocksMoved = 0; rocksMoved < rocksNumber; rocksMoved++) {
      currentPit++;

      if (currentPit == otherPlayersKalah) {
        currentPit++;
      }

      if (currentPit > BOARD_SIZE) {
        currentPit = 1;
      }

      status.put(currentPit, status.get(currentPit) + 1);
    }
    return currentPit;
  }

  /**
   * checks if it is the player's turn to move.
   * @param playerOneMove true if it is Player One's turn
   * @param pitId pit from which to move
   */
  private void verifyPlayerTurn(boolean playerOneMove, int pitId) {
    boolean playerOneMakingMove = pitId <= BOARD_SIZE / 2;
    if (playerOneMove && !playerOneMakingMove || !playerOneMove && playerOneMakingMove) {
      throw new ForbiddenOperationException("It is the other players turn!");
    }
  }
}
