### /link

1. Create or find a user in our database with the same id as the discord id of the user executing this command
2. If this user already has an impera account id, tell the user to unlink first
3. else, return the user's secret code and tell them to send it back to us over the Impera message system
4. Once we've received the secret code, set the user's impera account id to the id of the message sender
5. Tell the user on discord that they've been linked.
6. [OPTIONAL] make a new secret code.

### /unlink

1. find the user in our database with the same id as the discord id of the user executing this command
2. If the user cannot be found, tell them they're not in the database
3. If this user has no impera account id, tell the user that there is nothing to unlink
4. else, set the impera account id to null.
5. make a new secret code.

### /track

1. if this command is issued in a guild, check if the user has manage channel permissions.
2. find the user in our database with the same id as the discord id of the user executing this command
3. if the user cannot be found, tell them the game cannot be tracked as they're not linked to an impera account
4. if the user can be found, but has no impera id, tell them the game cannot be tracked because they're not linked to an
   impera account
5. if the user can be found and has an impera id, find the game they want to play
6. if the game cannot be found, tell the user the game cannot be tracked because it does not exist
7. if the game can be found, find the impera-id of the user in the player list of the game
8. if the player is not in the game, tell the user the game cannot be tracked because they're not a part of it
9. if the bot is not yet in the game, join the game
10. if the game cannot be joined, tell the user the game cannot be tracked because (the password is incorrect || there
    is no room for the bot to join || other)
11. else, add the game to the list of games to track

12. Once the game starts, surrender at the earliest possible moment
13. Once the game starts, add all players that use the bot's private channels to the list of channelLinks, if they are
    not in there yet.