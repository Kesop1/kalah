package com.piotrak.kalah.service;

import static com.piotrak.kalah.model.Game.MESSAGE_DRAW;
import static com.piotrak.kalah.model.Game.MESSAGE_MOVE_AGAIN;
import static com.piotrak.kalah.model.Game.MESSAGE_PLAYER_ONE_MOVE;
import static com.piotrak.kalah.model.Game.MESSAGE_PLAYER_ONE_WON;
import static com.piotrak.kalah.model.Game.MESSAGE_PLAYER_TWO_MOVE;
import static com.piotrak.kalah.model.Game.MESSAGE_PLAYER_TWO_WON;
import static java.util.Objects.isNull;

import com.piotrak.kalah.exception.ForbiddenOperationException;
import com.piotrak.kalah.model.Board;
import com.piotrak.kalah.model.Game;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GameService {

  private static final int PLAYER_PIT_NUMBER = 6;
  private static final int INITIAL_ROCKS_NUMBER = 6;
  private static final int BOARD_SIZE = (PLAYER_PIT_NUMBER + 1) * 2;

  private static final int PLAYER_ONE_START_PIT = 1;
  private static final int PLAYER_ONE_KALAH = PLAYER_PIT_NUMBER + 1;
  private static final int PLAYER_TWO_START_PIT = PLAYER_ONE_KALAH + 1;
  private static final int PLAYER_TWO_KALAH = PLAYER_ONE_KALAH * 2;

  private static final Map<Integer, Integer> NEW_BOARD_STATUS = new HashMap<>(BOARD_SIZE);

  static {
    for (int i = PLAYER_ONE_START_PIT; i <= BOARD_SIZE; i++) {
      int rocks = 0;
      if (i != PLAYER_ONE_KALAH && i != PLAYER_TWO_KALAH) {
        rocks = INITIAL_ROCKS_NUMBER;
      }
      NEW_BOARD_STATUS.put(i, rocks);
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
    Board board = createNewBoard();
    Game game = Game.builder()
      .id(gameId)
      .url(baseUrl + "/" + gameId)
      .board(board)
      .message(Game.MESSAGE_GAME_CREATED)
      .playerOneMove(true)
      .build();
    gameMap.put(gameId, game);
    return game;
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
    validateMove(game.getBoard(), pitId, game.isPlayerOneMove());

    MoveResult moveResult = executeMove(game.getBoard(), pitId, game.isPlayerOneMove());
    Board updatedBoard = moveResult.board();

    GameFinishResult finishResult = checkGameFinished(updatedBoard);
    if (finishResult.finished()) {
      updatedBoard = calculateFinishedGameOutcome(game, finishResult);
    } else {
      int lastPit = moveResult.lastPit();
      calculateNextMove(game, lastPit);
    }

    // Update the game with new board
    Game updatedGame = Game.builder()
      .id(game.getId())
      .url(game.getUrl())
      .board(updatedBoard)
      .message(game.getMessage())
      .playerOneMove(game.isPlayerOneMove())
      .build();
    gameMap.put(gameId, updatedGame);
    return updatedGame;
  }

  private void calculateNextMove(Game game, int lastPit) {
    boolean nextPlayerOne = determineNextPlayer(lastPit, game.isPlayerOneMove());
    game.setPlayerOneMove(nextPlayerOne);
    game.setMessage(nextPlayerOne ? MESSAGE_PLAYER_ONE_MOVE : MESSAGE_PLAYER_TWO_MOVE);
    if (game.isPlayerOneMove() == (lastPit == PLAYER_ONE_KALAH) || !game.isPlayerOneMove() == (lastPit == PLAYER_TWO_KALAH)) {
      game.setMessage(MESSAGE_MOVE_AGAIN + game.getMessage());
    }
  }

  private Board calculateFinishedGameOutcome(Game game, GameFinishResult finishResult) {
    Board updatedBoard;
    updatedBoard = finishResult.board();
    // Determine winner and set message
    Map<Integer, Integer> status = updatedBoard.status();
    int playerOneScore = status.get(PLAYER_ONE_KALAH);
    int playerTwoScore = status.get(PLAYER_TWO_KALAH);
    if (playerOneScore > playerTwoScore) {
      game.setMessage(String.format(MESSAGE_PLAYER_ONE_WON, playerOneScore, playerTwoScore));
    } else if (playerOneScore < playerTwoScore) {
      game.setMessage(String.format(MESSAGE_PLAYER_TWO_WON, playerTwoScore, playerOneScore));
    } else {
      game.setMessage(MESSAGE_DRAW);
    }
    game.setPlayerOneMove(true); // Game over, but set to player one for consistency
    return updatedBoard;
  }

  /**
   * Creates a new game board.
   * @return a new Board
   */
  private Board createNewBoard() {
    return new Board(NEW_BOARD_STATUS);
  }

  /**
   * Validates if a move is allowed.
   * @param board the game board
   * @param pitId the pit to move from
   * @param playerOneMove true if it's player one's turn
   */
  private void validateMove(Board board, int pitId, boolean playerOneMove) {
    checkMovingFromEmptyPit(board, pitId);
    checkMovingFromKalah(pitId);
    verifyPlayerTurn(playerOneMove, pitId);
  }

  private void checkMovingFromKalah(int pitId) {
    if (pitId == PLAYER_ONE_KALAH || pitId == PLAYER_TWO_KALAH) {
      throw new ForbiddenOperationException("Unable to move rocks from Kalah!");
    }
  }

  private void checkMovingFromEmptyPit(Board board, int pitId) {
    if (board.isEmpty(pitId)) {
      throw new ForbiddenOperationException("Unable to move rocks from an empty pit!");
    }
  }

  /**
   * Executes a move on the board.
   * @param board the current board
   * @param pitId the pit to move from
   * @param playerOneMove true if it's player one's turn
   * @return the updated board and the last pit number
   */
  private MoveResult executeMove(Board board, int pitId, boolean playerOneMove) {
    MoveResult moveResult = moveTheRocks(board, pitId, playerOneMove);
    Board boardAfterMove = moveResult.board();
    int lastPit = moveResult.lastPit();
    Board updatedBoard = captureOpponentsRocks(boardAfterMove, lastPit, playerOneMove);
    return new MoveResult(updatedBoard, lastPit);
  }

  /**
   * Checks if the game is finished and updates the board accordingly.
   * @param board the current board
   * @return the result of the check
   */
  private GameFinishResult checkGameFinished(Board board) {
    Map<Integer, Integer> status = new HashMap<>(board.status());
    boolean finished = false;
    int playerOneRocksOnBoard = 0;
    for (int i = PLAYER_ONE_START_PIT; i < PLAYER_ONE_KALAH; i++) {
      playerOneRocksOnBoard += status.get(i);
    }

    int playerTwoRocksOnBoard = 0;
    for (int i = PLAYER_TWO_START_PIT; i < PLAYER_TWO_KALAH; i++) {
      playerTwoRocksOnBoard += status.get(i);
    }

    if (playerOneRocksOnBoard == 0) {
      movePlayerTwoRemainingRocksToKalah(status, playerTwoRocksOnBoard);
      finished = true;
    } else if (playerTwoRocksOnBoard == 0) {
      movePlayerOneRemainingRocksToKalah(status, playerOneRocksOnBoard);
      finished = true;
    }

    Board updatedBoard = new Board(status);
    return new GameFinishResult(updatedBoard, finished);
  }

  private void movePlayerOneRemainingRocksToKalah(Map<Integer, Integer> status, int playerOneRocksOnBoard) {
    status.put(PLAYER_ONE_KALAH, status.get(PLAYER_ONE_KALAH) + playerOneRocksOnBoard);
    for (int i = PLAYER_ONE_START_PIT; i < PLAYER_ONE_KALAH; i++) {
      status.put(i, 0);
    }
  }

  private void movePlayerTwoRemainingRocksToKalah(Map<Integer, Integer> status, int playerTwoRocksOnBoard) {
    status.put(PLAYER_TWO_KALAH, status.get(PLAYER_TWO_KALAH) + playerTwoRocksOnBoard);
    for (int i = PLAYER_TWO_START_PIT; i < PLAYER_TWO_KALAH; i++) {
      status.put(i, 0);
    }
  }

  /**
   * Determines the next player's turn.
   * @param lastPitNumber the last pit where a rock was placed
   * @param playerOneMove true if it was player one's turn
   * @return true if it's still player one's turn
   */
  private boolean determineNextPlayer(int lastPitNumber, boolean playerOneMove) {
    if (playerOneMove && lastPitNumber != PLAYER_ONE_KALAH
        || !playerOneMove && lastPitNumber != PLAYER_TWO_KALAH) {
      return !playerOneMove;
    }
    return playerOneMove;
  }

  private MoveResult moveTheRocks(Board board, int pitId, boolean playerOneMove) {
    int otherPlayersKalah = playerOneMove ? PLAYER_TWO_KALAH : PLAYER_ONE_KALAH;
    Map<Integer, Integer> status = new HashMap<>(board.status());
    int rocksNumber = status.get(pitId);
    status.put(pitId, 0);
    int currentPit = pitId;
    // Move the rocks to the next pits
    for (int rocksMoved = 0; rocksMoved < rocksNumber; rocksMoved++) {
      currentPit++;
      // Skip the opponent's kalah
      if (currentPit == otherPlayersKalah) {
        currentPit++;
      }
      // Wrap around the board if needed
      if (currentPit > BOARD_SIZE) {
        currentPit = PLAYER_ONE_START_PIT;
      }
      status.put(currentPit, status.get(currentPit) + 1);
    }
    return new MoveResult(new Board(status), currentPit);
  }

  private Board captureOpponentsRocks(Board board, int lastPitNumber, boolean playerOneMove) {
    Map<Integer, Integer> status = new HashMap<>(board.status());
    // Only capture opponent's rocks if the last rock was placed in an empty pit on the player's side
    if (status.get(lastPitNumber) != 1) {
      return board;
    }
    int playersKalah;
    int otherPlayersKalah;
    if (playerOneMove) {
      if (lastPitNumber >= PLAYER_ONE_KALAH) {
        return board;
      }
      playersKalah = PLAYER_ONE_KALAH;
      otherPlayersKalah = PLAYER_TWO_KALAH;
    } else {
      // If last rock was placed on the other player's side or in their kalah, do not capture
      if (lastPitNumber == PLAYER_TWO_KALAH || lastPitNumber <= PLAYER_ONE_KALAH) {
        return board;
      }
      playersKalah = PLAYER_TWO_KALAH;
      otherPlayersKalah = PLAYER_ONE_KALAH;
    }

    int captured = status.get(lastPitNumber);
    status.put(lastPitNumber, 0);
    status.put(playersKalah, status.get(playersKalah) + captured);

    // Capture the opponent's rocks from opposite pit
    int step = lastPitNumber % (BOARD_SIZE / 2);
    int opponentsPit = otherPlayersKalah - step;

    captured = status.get(opponentsPit);
    status.put(opponentsPit, 0);
    status.put(playersKalah, status.get(playersKalah) + captured);

    return new Board(status);
  }

  private void verifyPlayerTurn(boolean playerOneMove, int pitId) {
    boolean playerOneMakingMove = isMovingFromFirstHalfOfTheBoard(pitId);
    if (playerOneMove && !playerOneMakingMove || !playerOneMove && playerOneMakingMove) {
      throw new ForbiddenOperationException("It is the other players turn!");
    }
  }

  private boolean isMovingFromFirstHalfOfTheBoard(int pitId) {
    return pitId <= BOARD_SIZE / 2;
  }

  public record MoveResult(Board board, int lastPit) {}
  public record GameFinishResult(Board board, boolean finished) {}
}
