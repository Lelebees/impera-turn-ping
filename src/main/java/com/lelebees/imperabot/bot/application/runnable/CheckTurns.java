package com.lelebees.imperabot.bot.application.runnable;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GameService;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGamePlayerDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.history.HistoryActionName;
import discord4j.core.object.entity.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import static com.lelebees.imperabot.impera.domain.history.HistoryActionName.*;

public class CheckTurns implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CheckTurns.class);
    private final ImperaService imperaService;
    private final GameService gameService;
    private final GameLinkService gameLinkService;
    private final DiscordService discordService;

    public CheckTurns(ImperaService imperaService, GameService gameService, GameLinkService gameLinkService, DiscordService discordService) {
        this.imperaService = imperaService;
        this.gameService = gameService;
        this.gameLinkService = gameLinkService;
        this.discordService = discordService;
    }

    @Override
    public void run() {
        try {
            List<Game> trackedGames = gameService.findAllGames();
            logger.info("Checking turns for " + trackedGames.size() + " games.");
            int skippedGames = 0;
            int handledGames = 0;
            for (Game game : trackedGames) {
                ImperaGameViewDTO imperaGame = imperaService.getGame(game.getId());
                if (gameLinkService.findLinksByGame(game.getId()).isEmpty()) {
                    logger.debug("Skipping and removing game " + game.getId() + " (" + imperaGame.name() + ") because it has no channels.");
                    gameService.deleteGame(game.getId());
                    skippedGames++;
                    continue;
                }

                boolean gameEnded = imperaGame.hasEnded();
                boolean gameHasYetToStart = imperaGame.hasYetToStart();
                boolean turnHasChanged = game.currentTurn != imperaGame.turnCounter();
                boolean halfOfTurnTimeHasPassed = imperaGame.timeoutSecondsLeft() <= (imperaGame.options().timeoutInSeconds() / 2);
                // If the turn hasn't changed, and it has not been half-time OR the notice has already been sent, and the game hasn't ended, or the game is open
                if ((!turnHasChanged && (!halfOfTurnTimeHasPassed || game.halfTimeNotice) && !gameEnded) || gameHasYetToStart) {
                    // Skip this game
                    skippedGames++;
                    continue;
                }

                HashMap<HistoryActionName, List<String>> playersThatAreNoLongerPlaying = imperaService.getPlayersThatAreNoLongerPlaying(game.getId(), game.currentTurn, imperaGame.turnCounter() + 1);
                // Get all players that have been defeated since the last turn change
                List<ImperaGamePlayerDTO> defeatedPlayers = playersThatAreNoLongerPlaying.get(LOST)
                        .stream().map(imperaGame::findPlayerByGameId)
                        .toList();
                logger.debug("Found " + defeatedPlayers.size() + " defeated players.");
                // Get all players that surrendered in the last turn(s)
                List<ImperaGamePlayerDTO> surrenderedPlayers = playersThatAreNoLongerPlaying.get(SURRENDERED)
                        .stream().map(imperaGame::findPlayerByGameId)
                        .toList();
                logger.debug("Found " + surrenderedPlayers.size() + " surrendered players.");

                List<ImperaGamePlayerDTO> timedOutPlayers = playersThatAreNoLongerPlaying.get(TIMED_OUT)
                        .stream().map(imperaGame::findPlayerByGameId)
                        .toList();
                logger.debug("Found " + timedOutPlayers.size() + " timed out players.");

                // Get all channels that want to receive updates for this game.
                List<Channel> channels = gameLinkService.findLinksByGame(game.getId())
                        .stream()
                        .map(GameChannelLink::getChannelId)
                        .map(discordService::getChannelById)
                        .toList();
                logger.debug("Found " + channels.size() + " channels to notify.");

                defeatedPlayers.forEach(player -> {
                    logger.info("Sending defeated notice for " + imperaGame.name() + " (" + imperaGame.id() + ")!");
                    discordService.sendDefeatedMessage(channels, player, imperaGame);
                });
                surrenderedPlayers.forEach(gamePlayer -> {
                    logger.info("Sending surrendered notice for " + imperaGame.name() + " (" + imperaGame.id() + ")!");
                    discordService.sendSurrenderMessage(channels, gamePlayer, imperaGame);
                });
                timedOutPlayers.forEach(gamePlayer -> {
                    logger.info("Sending timed out notice for " + imperaGame.name() + " (" + imperaGame.id() + ")!");
                    discordService.sendTimedOutMessage(channels, gamePlayer, imperaGame);
                });
                // There is probably a better way to do this, but I'm not sure what it is.
                if (gameEnded) {
                    logger.debug("Game " + game.getId() + " has ended!");
                    logger.info("Sending victory notice for " + imperaGame.name() + " (" + imperaGame.id() + ")!");
                    List<ImperaGamePlayerDTO> winningPlayers = imperaGame.teams().stream()
                            .filter(team -> team.players.stream().anyMatch(ImperaGamePlayerDTO::hasWon))
                            .map(team -> team.players.stream()
                                    .filter(ImperaGamePlayerDTO::hasWon)
                                    .toList())
                            .flatMap(Collection::stream)
                            .toList();
                    // Send a message to all channels that are tracking this game, who won
                    discordService.sendVictorsMessage(channels, winningPlayers, imperaGame);
                    winningPlayers.forEach(winner -> discordService.giveWinnerRole(game, winner));
//                    winningPlayers.forEach(winner -> discordService.sendVictorMessage(channels, winner, imperaGame));
                    gameService.deleteGame(game.getId());
                } else if (turnHasChanged) {
                    //Send notice
                    logger.info("Sending turn notice for " + imperaGame.name() + " (" + imperaGame.id() + ")!");
                    discordService.sendNewTurnMessage(channels, imperaGame.currentPlayer(), imperaGame);
                    gameService.turnChanged(game.getId(), imperaGame.turnCounter());
                } else {
                    //Send half-time notice
                    logger.info("Sending half time notice for " + imperaGame.name() + " (" + imperaGame.id() + ")!");
                    discordService.sendHalfTimeMessage(channels, imperaGame.currentPlayer(), imperaGame);
                    gameService.setHalfTimeNoticeForGame(game.getId());
                }
                handledGames++;
            }
            logger.info("Handled " + handledGames + " games, skipped " + skippedGames + " games.");
        } catch (Exception e) {
            logger.error("Handled Error: " + e.getMessage(), e);
        }
    }
}
