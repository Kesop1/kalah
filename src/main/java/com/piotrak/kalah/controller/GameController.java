package com.piotrak.kalah.controller;

import com.piotrak.kalah.model.Game;
import com.piotrak.kalah.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
@Tag(name = "kalah", description = "Kalah game operations")
public class GameController {

  private final GameService gameService;

  @Operation(summary = "Create a new game", description = "This method will create a new game of Kalah")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "201", description = "Game created")})
  @RequestMapping(method = RequestMethod.POST, produces = "application/json")
  @ResponseBody
  public ResponseEntity<Game> createGame(HttpServletRequest request) {
    String baseUrl = request.getRequestURL().toString();
    Game game = gameService.createGame(baseUrl);
    return new ResponseEntity<>(game, HttpStatus.CREATED);
  }

  @Operation(summary = "Make a move", description = "This method will make a move in the game of Kalah")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Move made"),
    @ApiResponse(responseCode = "400", description = "Bad request")})
  @RequestMapping(value = "{gameId}/pits/{pitId}", method = RequestMethod.PUT, produces = "application/json")
  @ResponseBody
  public ResponseEntity<Game> makeMove(
    @Parameter(description = "Id of the kalah game", example = "1") @PathVariable(name = "gameId") Integer gameId,
    @Parameter(description = "Id of the pit from which to move", example = "1") @PathVariable(name = "pitId") Integer pitId) {
    Game game = gameService.makeMove(gameId, pitId);
    return new ResponseEntity<>(game, HttpStatus.OK);
  }
}
