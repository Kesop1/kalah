# kalah
Java RESTful Web Service for the Kalah game

rules of the game are described here: https://en.wikipedia.org/wiki/Kalah
This is a 6-stones variation of the game

To create a game run :
curl --header "Content-Type: application/json" \
--request POST \
http://<host>:<port>/games
  
to make a move:
curl --header "Content-Type: application/json" \
--request PUT \
http://<host>:<port>/games/{gameId}/pits/{pitId}
