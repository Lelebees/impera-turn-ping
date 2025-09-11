package com.lelebees.imperabot.bot.application.runnable;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GameService;
import com.lelebees.imperabot.bot.domain.game.exception.GameNotFoundException;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.presentation.game.GameDTO;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.game.exception.ImperaGameNotFoundException;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGamePlayerDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.history.HistoryActionName;
import discord4j.core.object.entity.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class CheckTurns implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(CheckTurns.class);
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
            checkTurns();
        } catch (RuntimeException e) {
            logger.error("Handled Error: " + e.getMessage(), e);
        }
    }

    private void checkTurns() {
        List<GameDTO> games = gameService.getAllGames();
        logger.info("Checking turns for " + games.size() + " games.");
        int skippedGames = 0;
        int handledGames = 0;
        for (GameDTO game : games) {
            ImperaGameViewDTO imperaGame;
            try {
                imperaGame = imperaService.getGame(game.id());
            } catch (ImperaGameNotFoundException e) {
                logger.error("Game in memory could not be found on the Impera server. Skipping game.", e);
                skippedGames++;
                continue;
            }
            if (gameLinkService.findLinksByGame(game.id()).isEmpty()) {
                logger.debug("Skipping and removing game " + game.id() + " (" + imperaGame.name() + ") because it has no channels.");
                gameService.deleteGame(game.id());
                skippedGames++;
                continue;
            }
            if (imperaGame.hasYetToStart()) {
                skippedGames++;
                continue;
            }
            boolean turnHasChanged = game.currentTurn() != imperaGame.turnCounter();
            boolean halfOfTurnTimeHasPassed = imperaGame.timeoutSecondsLeft() <= (imperaGame.options().timeoutInSeconds() / 2);
            boolean shouldSendHalfTimeNotice = halfOfTurnTimeHasPassed && !game.halfTimeNotice();
            // If the turn hasn't changed, and we don't need to send a half-time notice, and the game hasn't ended,
            if (!turnHasChanged && !shouldSendHalfTimeNotice && !imperaGame.hasEnded()) {
                // Skip this game
                skippedGames++;
                continue;
            }

            List<Channel> channels = getChannelsToNotify(game);
            logger.debug("Found " + channels.size() + " channels to notify.");

            HashMap<String, HistoryActionName> playersThatAreNoLongerPlaying = imperaService.getPlayersThatAreNoLongerPlaying(game.id(), game.currentTurn(), imperaGame.turnCounter() + 1);
            logger.debug("Found " + playersThatAreNoLongerPlaying.size() + " players that are no longer playing.");
            playersThatAreNoLongerPlaying.forEach((player, outcome) -> discordService.sendLoserMessage(channels, imperaGame.findPlayerById(player), imperaGame, outcome));

            // There is probably a better way to do this, but I'm not sure what it is.
            if (imperaGame.hasEnded()) {
                logger.debug("Game " + game.id() + " has ended!");
                sendVictoryNotice(game, imperaGame, channels);
            } else if (turnHasChanged) {
                notifyNextUser(game, imperaGame, channels);
            } else {
                sendHalfTimeNotice(game, imperaGame, channels);
            }
            handledGames++;
        }
        logger.info("Handled " + handledGames + " games, skipped " + skippedGames + " games.");
    }

    private void sendVictoryNotice(GameDTO game, ImperaGameViewDTO imperaGame, List<Channel> channels) {
        logger.info("Sending victory notice for " + imperaGame.name() + " (" + imperaGame.id() + ")!");
        List<ImperaGamePlayerDTO> winningPlayers = imperaGame.getWinningPlayers();
        // Send a message to all channels that are tracking this game, who won
        discordService.sendVictorsMessage(channels, winningPlayers, imperaGame);
        winningPlayers.forEach(winner -> discordService.giveWinnerRole(game, winner));
        gameService.deleteGame(game.id());
    }

    private void notifyNextUser(GameDTO game, ImperaGameViewDTO imperaGame, List<Channel> channels) {
        logger.info("Sending turn notice for " + imperaGame.name() + " (" + imperaGame.id() + ")!");
        discordService.sendNewTurnMessage(channels, imperaGame.currentPlayer(), imperaGame);
        try {
            gameService.changeTurn(game.id(), imperaGame.turnCounter());
        } catch (GameNotFoundException e) {
            logger.error("Game from database could not be found in database.", e);
        }
    }

    private void sendHalfTimeNotice(GameDTO game, ImperaGameViewDTO imperaGame, List<Channel> channels) {
        logger.info("Sending half time notice for " + imperaGame.name() + " (" + imperaGame.id() + ")!");
        discordService.sendHalfTimeMessage(channels, imperaGame.currentPlayer(), imperaGame);
        try {
            gameService.setHalfTimeNoticeForGame(game.id());
        } catch (GameNotFoundException e) {
            logger.error("Game from database could not be found in database.", e);
        }
    }

    private List<Channel> getChannelsToNotify(GameDTO game) {
        return gameLinkService.findLinksByGame(game.id())
                .stream()
                .map(GameChannelLink::getChannelId)
                .map(discordService::getChannelById)
                .toList();
    }
}
