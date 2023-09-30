package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGamePlayerDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import discord4j.core.object.entity.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Service
public class SchedulerService {
    private final ImperaService imperaService;
    private final UserService userService;
    private final GameService gameService;
    private final GameLinkService gameLinkService;
    private final DiscordService discordService;
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    public SchedulerService(ImperaService imperaService, UserService userService, GameService gameService, GameLinkService gameLinkService, DiscordService discordService) {
        this.imperaService = imperaService;
        this.userService = userService;
        this.gameService = gameService;
        this.gameLinkService = gameLinkService;
        this.discordService = discordService;
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(checkVerifyRequests(), 1, 5, TimeUnit.MINUTES);
        // Update the token a minute before it expires.
        executorService.scheduleAtFixedRate(imperaService.updateAccessToken(), (ImperaService.bearerToken.expires_in - 60), (ImperaService.bearerToken.expires_in), TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(checkTurns(), 1, 1, TimeUnit.MINUTES);

    }

    public Runnable checkVerifyRequests() {
        return () -> {
            try {
                logger.info("Checking verify requests");
                List<ImperaMessageDTO> linkMessages = imperaService.getLinkMessages();
                for (ImperaMessageDTO linkMessage : linkMessages) {
                    try {
                        userService.verifyUser(linkMessage.text.trim(), UUID.fromString(linkMessage.from.id));
                        imperaService.deleteMessage(linkMessage.id);
                    } catch (UserNotFoundException e) {
                        logger.warn("User matching code " + linkMessage.text + " Not found, skipping...");
                    } catch (UserAlreadyVerfiedException e) {
                        logger.warn("User already verified");
                        imperaService.deleteMessage(linkMessage.id);
                    }
                }
            } catch (Exception e) {
                logger.error("Handled Error: " + e.getMessage());
            }
        };
    }

    //TODO: Notify users when someone has lost or won
    public Runnable checkTurns() {
        return () -> {
            try {
                logger.info("Checking turns!");
                List<Game> trackedGames = gameService.findAllGames();
                for (Game game : trackedGames) {
                    ImperaGameViewDTO imperaGame = imperaService.getGame(game.getId());

                    boolean gameEnded = imperaGame.state.equals("Ended");
                    boolean gameHasYetToStart = imperaGame.state.equals("Open");
                    boolean turnHasChanged = game.currentTurn != imperaGame.turnCounter;
                    boolean halfOfTurnTimeHasPassed = imperaGame.timeoutSecondsLeft <= (imperaGame.options.timeoutInSeconds / 2);
                    // If the turn hasn't changed, and it has not been half-time OR the notice has already been sent, and the game hasn't ended, or the game is open
                    if ((!turnHasChanged && (!halfOfTurnTimeHasPassed || game.halfTimeNotice) && !gameEnded) || gameHasYetToStart) {
                        // Skip this game
                        continue;
                    }
                    // Get all players that have been defeated since the last turn change
                    List<ImperaGamePlayerDTO> defeatedPlayers = IntStream.range(game.currentTurn, imperaGame.turnCounter + 1)
                            .mapToObj(turn -> imperaService.playersThatWereDefeated(game.getId(), turn))
                            .flatMap(Collection::stream)
                            .map(imperaGame::findPlayerByGameId)
                            .toList();
                    // Get all players that surrendered in the last turn(s)
                    List<ImperaGamePlayerDTO> surrenderedPlayers = IntStream.range(game.currentTurn, imperaGame.turnCounter + 1)
                            .mapToObj(turn -> imperaService.playersThatSurrendered(game.getId(), turn))
                            .flatMap(Collection::stream)
                            .map(imperaGame::findPlayerByGameId)
                            .toList();

                    // Get all channels that want to receive updates for this game.
                    List<Channel> channels = new ArrayList<>(gameLinkService.findLinksByGame(game.getId())
                            .stream()
                            .filter(gameChannelLink ->
                                    gameLinkService.deepGetNotificationSetting(gameChannelLink.getGameLinkId()) != GuildNotificationSettings.NO_NOTIFICATIONS)
                            .map(GameChannelLink::getChannelId)
                            .map(discordService::getChannelById)
                            .toList());

                    defeatedPlayers.forEach(player -> {
                        logger.info("Sending defeated notice!");
                        discordService.sendDefeatedMessage(channels, player, imperaGame);
                    });
                    surrenderedPlayers.forEach(gamePlayer -> {
                        logger.info("Sending surrendered notice!");
                        discordService.sendSurrenderMessage(channels, gamePlayer, imperaGame);
                    });
                    // There is probably a better way to do this, but I'm not sure what it is.
                    if (gameEnded) {
                        logger.info("Game " + game.getId() + " has ended!");
                        logger.info("Sending victory notice!");
                        // Send a message to all channels that are tracking this game, who won
                        // TODO: Allow an entire team to win
                        // TODO: Fix user being declared winner when they surrendered on their turn
                        discordService.sendVictorMessage(channels, imperaGame.currentPlayer, imperaGame);
                        gameService.deleteGame(game.getId());
                    } else if (turnHasChanged) {
                        //Send notice
                        logger.info("Sending turn notice!");
                        discordService.sendNewTurnMessage(channels, imperaGame.currentPlayer, imperaGame);
                        gameService.turnChanged(game.getId(), imperaGame.turnCounter);
                    } else {
                        //Send half-time notice
                        logger.info("Sending half time notice!");
                        discordService.sendHalfTimeMessage(channels, imperaGame.currentPlayer, imperaGame);
                        gameService.setHalfTimeNoticeForGame(game.getId());
                    }
                }
            } catch (Exception e) {
                logger.error("Handled Error: " + e.getMessage(), e);
            }
        };
    }
}
