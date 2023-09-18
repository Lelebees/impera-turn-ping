package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.domain.NotificationSettings;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerService {
    private final ImperaService imperaService;
    private final UserService userService;
    private final GameService gameService;
    private final GameLinkService gameLinkService;
    private final DiscordService discordService;
    private final ScheduledFuture<?> linkHandle;
    private final ScheduledFuture<?> relogHandle;

    private final ScheduledFuture<?> gameUpdateHandle;

    public SchedulerService(ImperaService imperaService, UserService userService, GameService gameService, GameLinkService gameLinkService, DiscordService discordService) {
        this.imperaService = imperaService;
        this.userService = userService;
        this.gameService = gameService;
        this.gameLinkService = gameLinkService;
        this.discordService = discordService;
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        linkHandle = executorService.scheduleAtFixedRate(checkVerifyRequests(), 0, 1, TimeUnit.MINUTES);
        // Update the token a minute before it expires, and then 5 seconds before it expires thereafter.
        relogHandle = executorService.scheduleAtFixedRate(imperaService.updateAccessToken(), (ImperaService.bearerToken.expires_in - 60), (ImperaService.bearerToken.expires_in - 5), TimeUnit.SECONDS);
        gameUpdateHandle = executorService.scheduleAtFixedRate(checkTurns(), 0, 1, TimeUnit.MINUTES);

    }

    public Runnable checkVerifyRequests() {
        return () -> {
            try {
                System.out.println("Checking verify requests");
                List<ImperaMessageDTO> linkMessages = imperaService.getLinkMessages();
                for (ImperaMessageDTO linkMessage : linkMessages) {
                    try {
                        userService.verifyUser(linkMessage.text, UUID.fromString(linkMessage.from.id));
                        imperaService.deleteMessage(linkMessage.id);
                    } catch (UserNotFoundException e) {
                        System.out.println("User matching code " + linkMessage.text + " Not found, skipping...");
                    } catch (UserAlreadyVerfiedException e) {
                        System.out.println("User already verified");
                        imperaService.deleteMessage(linkMessage.id);
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };
    }

    public Runnable checkTurns() {
        return () -> {
            try {
                System.out.println("Checking turns!");
                List<Game> trackedGames = gameService.findAllGames();
                for (Game game : trackedGames) {
                    ImperaGameViewDTO imperaGame = imperaService.getGame(game.getId());

                    boolean turnChanged = game.currentTurn != imperaGame.turnCounter;
                    boolean halfTimeNotNoticed = !game.halfTimeNotice && imperaGame.timeoutSecondsLeft <= imperaGame.options.timeoutInSeconds;
                    // If nothing has changed (we don't need to ping anyone)
                    if (!turnChanged && !halfTimeNotNoticed) {
                        continue;
                    }

                    Optional<BotUser> currentPlayer = userService.findImperaUser(UUID.fromString(imperaGame.currentPlayer.userId));
                    String userString = currentPlayer.map(botUser -> "<@" + botUser.getUserId() + ">").orElseGet(() -> imperaGame.currentPlayer.name);


                    Map<GameChannelLink, GuildNotificationSettings> GuildChannels = new HashMap<>();
                    Map<GameChannelLink, UserNotificationSetting> DMChannels = new HashMap<>();
                    for (GameChannelLink gameChannelLink : gameLinkService.findLinksByGame(game.getId())) {
                        NotificationSettings notificationSettings = gameLinkService.deepGetNotificationSetting(gameChannelLink.getGameLinkId());
                        // If it's No notifications...
                        // TODO: Hardcoded reference to enum position
                        if (notificationSettings.ordinal() == 0) {
                            continue;
                        }
                        // If it's a DM channel
                        if (notificationSettings.getClass() == UserNotificationSetting.class) {
                            if (currentPlayer.isEmpty()) {
                                System.out.println("currentPlayer is Empty!");
                                continue;
                            }
                            if (currentPlayer.get().getUserId() != discordService.getChannelOwner(gameChannelLink.getChannelId())) {
                                continue;
                            }
                            UserNotificationSetting userNotificationSetting = (UserNotificationSetting) notificationSettings;
                            if (userNotificationSetting == UserNotificationSetting.GUILD_ONLY) {
                                continue;
                            }
                            DMChannels.put(gameChannelLink, userNotificationSetting);

                        }
                        // If it's a guild channel
                        if (notificationSettings.getClass() == GuildNotificationSettings.class) {
                            GuildNotificationSettings guildNotificationSettings = (GuildNotificationSettings) notificationSettings;
                            if (guildNotificationSettings == GuildNotificationSettings.NO_NOTIFICATIONS) {
                                continue;
                            }
                            GuildChannels.put(gameChannelLink, guildNotificationSettings);
                        }
                    }

                    if (turnChanged) {
                        //Send notice
                        GuildChannels.forEach((gameChannelLink, notificationSettings) -> {
                            discordService.sendMessage(gameChannelLink.getChannelId(), false, userString, gameChannelLink.getGameId());
                        });
                        DMChannels.forEach(((gameChannelLink, userNotificationSetting) -> discordService.sendMessage(gameChannelLink.getChannelId(), false, userString, gameChannelLink.getGameId())));
                        gameService.turnChanged(game.getId(), imperaGame.turnCounter);
                    } else {
                        //Send half time notice
                        GuildChannels.forEach((gameChannelLink, notificationSettings) -> {
                            discordService.sendMessage(gameChannelLink.getChannelId(), true, userString, gameChannelLink.getGameId());
                        });
                        DMChannels.forEach(((gameChannelLink, userNotificationSetting) -> discordService.sendMessage(gameChannelLink.getChannelId(), true, userString, gameChannelLink.getGameId())));
                        gameService.setHalfTimeNoticeForGame(game.getId());
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };
    }
}
