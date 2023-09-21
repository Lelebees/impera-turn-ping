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

import static com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings.NO_NOTIFICATIONS;

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
        linkHandle = executorService.scheduleAtFixedRate(checkVerifyRequests(), 0, 5, TimeUnit.MINUTES);
        // Update the token a minute before it expires, and then 5 seconds before it expires thereafter.
        relogHandle = executorService.scheduleAtFixedRate(imperaService.updateAccessToken(), (ImperaService.bearerToken.expires_in - 60), (ImperaService.bearerToken.expires_in), TimeUnit.SECONDS);
        gameUpdateHandle = executorService.scheduleAtFixedRate(checkTurns(), 0, 1, TimeUnit.MINUTES);

    }

    public Runnable checkVerifyRequests() {
        return () -> {
            try {
                System.out.println("Checking verify requests");
                List<ImperaMessageDTO> linkMessages = imperaService.getLinkMessages();
                for (ImperaMessageDTO linkMessage : linkMessages) {
                    try {
                        userService.verifyUser(linkMessage.text.trim(), UUID.fromString(linkMessage.from.id));
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
                    boolean halfTimeNotNoticed = !game.halfTimeNotice && (imperaGame.timeoutSecondsLeft <= (imperaGame.options.timeoutInSeconds / 2));
                    // If nothing has changed (we don't need to ping anyone)
                    if (!turnChanged && !halfTimeNotNoticed) {
                        continue;
                    }

                    Map<GameChannelLink, GuildNotificationSettings> GuildChannels = new HashMap<>();
                    Map<GameChannelLink, UserNotificationSetting> DMChannels = new HashMap<>();
                    for (GameChannelLink gameChannelLink : gameLinkService.findLinksByGame(game.getId())) {
                        NotificationSettings notificationSettings = gameLinkService.deepGetNotificationSetting(gameChannelLink.getGameLinkId());
                        // If it's a DM channel
                        if (notificationSettings instanceof UserNotificationSetting) {
                            DMChannels.put(gameChannelLink, (UserNotificationSetting) notificationSettings);
                        }
                        // If it's a guild channel
                        if (notificationSettings instanceof GuildNotificationSettings) {
                            GuildChannels.put(gameChannelLink, (GuildNotificationSettings) notificationSettings);
                        }
                    }

                    Optional<BotUser> currentPlayer = userService.findImperaUser(UUID.fromString(imperaGame.currentPlayer.userId));
                    String userString;
                    if (currentPlayer.isEmpty()) {
                        // The user is not registered with the service, so we can't send a DM
                        userString = imperaGame.currentPlayer.name;
                        DMChannels.clear();
                    } else {
                        BotUser player = currentPlayer.get();
                        userString = "<@" + player.getUserId() + ">";
                        switch (player.getNotificationSetting()) {
                            case NO_NOTIFICATIONS -> {
                                DMChannels.clear();
                                userString = imperaGame.currentPlayer.name;
                            }
                            case GUILD_ONLY -> DMChannels.clear();
                            case DMS_ONLY -> userString = imperaGame.currentPlayer.name;
                            case PREFER_GUILD_OVER_DMS -> {
                                if (!GuildChannels.isEmpty()) {
                                    DMChannels.clear();
                                } else {
                                    userString = imperaGame.currentPlayer.name;
                                }
                            }
                        }
                    }

                    GuildChannels.forEach((gameChannelLink, notificationSettings) -> {
                        if (notificationSettings == NO_NOTIFICATIONS) {
                            GuildChannels.remove(gameChannelLink);
                        }
                    });

                    String finalUserString = userString;

                    if (turnChanged) {
                        //Send notice
                        System.out.println("Sending turn notice!");
                        GuildChannels.forEach((gameChannelLink, notificationSettings) -> {
                            discordService.sendMessage(gameChannelLink.getChannelId(), false, finalUserString, gameChannelLink.getGameId());
                        });
                        DMChannels.forEach(((gameChannelLink, userNotificationSetting) -> discordService.sendMessage(gameChannelLink.getChannelId(), false, finalUserString, gameChannelLink.getGameId())));
                        gameService.turnChanged(game.getId(), imperaGame.turnCounter);
                    } else {
                        //Send half time notice
                        System.out.println("Sending half time notice!");
                        GuildChannels.forEach((gameChannelLink, notificationSettings) -> {
                            discordService.sendMessage(gameChannelLink.getChannelId(), true, finalUserString, gameChannelLink.getGameId());
                        });
                        DMChannels.forEach(((gameChannelLink, userNotificationSetting) -> discordService.sendMessage(gameChannelLink.getChannelId(), true, finalUserString, gameChannelLink.getGameId())));
                        gameService.setHalfTimeNoticeForGame(game.getId());
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };
    }
}
