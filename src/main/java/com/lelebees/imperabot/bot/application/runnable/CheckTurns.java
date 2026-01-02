package com.lelebees.imperabot.bot.application.runnable;

import com.lelebees.imperabot.bot.application.game.GameService;
import com.lelebees.imperabot.bot.application.game.exception.GameNotFoundException;
import com.lelebees.imperabot.bot.presentation.game.ChannelDTO;
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
    private final DiscordService discordService;

    public CheckTurns(ImperaService imperaService, GameService gameService, DiscordService discordService) {
        this.imperaService = imperaService;
        this.gameService = gameService;
        this.discordService = discordService;
    }

    @Override
    public void run() {
        try {
            checkTurns();
        } catch (RuntimeException e) {
            logger.error("Handled Error: {}", e.getMessage(), e);
        }
    }

    private void checkTurns() {
        List<GameDTO> games = gameService.getAllGames();
        logger.info("Checking turns for {} games.", games.size());
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
            if (!imperaGame.hasStarted()) {
                skippedGames++;
                continue;
            }
            if (game.trackingChannels().isEmpty()) {
                logger.debug("Skipping and removing game {} ({}) because it is not being tracked.", game.id(), imperaGame.name());
                gameService.deleteGame(game.id());
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
            logger.debug("Found {} channels to notify.", channels.size());

            HashMap<String, HistoryActionName> playersThatAreNoLongerPlaying = imperaService.getPlayersThatAreNoLongerPlaying(game.id(), game.currentTurn(), imperaGame.turnCounter() + 1);
            logger.debug("Found {} players that are no longer playing.", playersThatAreNoLongerPlaying.size());
            playersThatAreNoLongerPlaying.forEach((player, outcome) -> discordService.sendLoserMessage(channels, imperaGame.findPlayerById(player), imperaGame, outcome));

            // There is probably a better way to do this, but I'm not sure what it is.
            if (imperaGame.hasEnded()) {
                logger.debug("Game {} has ended!", game.id());
                sendVictoryNotice(game, imperaGame, channels);
            } else if (turnHasChanged) {
                notifyNextUser(game, imperaGame, channels);
            } else {
                sendHalfTimeNotice(game, imperaGame, channels);
            }
            handledGames++;
        }
        logger.info("Handled {} games, skipped {} games.", handledGames, skippedGames);
    }

    private void sendVictoryNotice(GameDTO game, ImperaGameViewDTO imperaGame, List<Channel> channels) {
        logger.info("Sending victory notice for {} ({})!", imperaGame.name(), imperaGame.id());
        List<ImperaGamePlayerDTO> winningPlayers = imperaGame.getWinningPlayers();
        // Send a message to all channels that are tracking this game, who won
        discordService.sendVictorsMessage(channels, winningPlayers, imperaGame);
        winningPlayers.forEach(winner -> discordService.giveWinnerRole(game, winner));
        gameService.deleteGame(game.id());
    }

    private void notifyNextUser(GameDTO game, ImperaGameViewDTO imperaGame, List<Channel> channels) {
        logger.info("Sending turn notice for {} ({})!", imperaGame.name(), imperaGame.id());
        discordService.sendNewTurnMessage(channels, imperaGame.currentPlayer(), imperaGame);
        try {
            gameService.changeTurn(game.id(), imperaGame.turnCounter());
        } catch (GameNotFoundException e) {
            logger.error("Game from database could not be found in database.", e);
        }
    }

    private void sendHalfTimeNotice(GameDTO game, ImperaGameViewDTO imperaGame, List<Channel> channels) {
        logger.info("Sending half time notice for {} ({})!", imperaGame.name(), imperaGame.id());
        discordService.sendHalfTimeMessage(channels, imperaGame.currentPlayer(), imperaGame);
        try {
            gameService.setHalfTimeNoticeForGame(game.id());
        } catch (GameNotFoundException e) {
            logger.error("Game from database could not be found in database.", e);
        }
    }

    private List<Channel> getChannelsToNotify(GameDTO game) {
        return game.trackingChannels()
                .stream()
                .map(ChannelDTO::id)
                .map(discordService::getChannelById)
                .toList();
    }
}
