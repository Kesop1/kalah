package com.piotrak.kalah.controller;

import com.piotrak.kalah.controller.response.ErrorResponse;
import com.piotrak.kalah.model.Game;
import com.piotrak.kalah.service.GameService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/games")
@AllArgsConstructor
public class GameController {

  private final GameService gameService;

  @ApiOperation(value = "Create a new game", notes = "This method will create a new game of Kalah",
    response = Game.class, tags = {"kalah"})
  @ApiResponses(value = {
    @ApiResponse(code = 201, message = "Game", response = Game.class)})
  @RequestMapping(method = RequestMethod.POST, produces = "application/json")
  @ResponseBody
  public ResponseEntity<Game> createGame(HttpServletRequest request) {
    String baseUrl = request.getRequestURL().toString();
    Game game = gameService.createGame(baseUrl);
    return new ResponseEntity<>(game, HttpStatus.CREATED);
  }

  @ApiOperation(value = "Make a move", notes = "This method will make a move in the game of Kalah",
    response = Game.class, tags = {"kalah"})
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Game", response = Game.class),
    @ApiResponse(code = 400, message = "ErrorResponse", response = ErrorResponse.class)})
  @RequestMapping(value = "{gameId}/pits/{pitId}", method = RequestMethod.PUT, produces = "application/json")
  @ResponseBody
  public ResponseEntity<Game> makeMove(
    @ApiParam(value = "Id of the kalah game", required = true, example = "1") @PathVariable(name = "gameId") Integer gameId,
    @ApiParam(value = "Id of the pit from which to move", required = true, example = "1") @PathVariable(name = "pitId") Integer pitId) {
    Game game = gameService.makeMove(gameId, pitId);
    return new ResponseEntity<>(game, HttpStatus.OK);
  }
}
