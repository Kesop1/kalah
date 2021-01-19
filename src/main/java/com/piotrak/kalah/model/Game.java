package com.piotrak.kalah.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Game {

  public static final String MESSAGE_PLAYER_ONE_MOVE = "It's player One's move";
  public static final String MESSAGE_PLAYER_TWO_MOVE = "It's player Two's move";
  public static final String MESSAGE_MOVE_AGAIN = "One more time: ";
  public static final String MESSAGE_GAME_CREATED = "A new Kalah game was created. " + MESSAGE_PLAYER_ONE_MOVE;
  public static final String MESSAGE_PLAYER_ONE_WON = "Player One is the Victor: %s to %s";
  public static final String MESSAGE_PLAYER_TWO_WON = "Player Two won: %s to %s";
  public static final String MESSAGE_DRAW = "Draw";

  private final Integer id;
  private final String url;
  private final Map<Integer, Integer> status;
  private String message;
  @JsonIgnore
  private boolean playerOneMove;

  public void setPlayerOneMove(boolean playerOneMove) {
    this.playerOneMove = playerOneMove;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
