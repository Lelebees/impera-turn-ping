package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings.NO_NOTIFICATIONS;

@Service
public class SchedulerService {
    private final ImperaService imperaService;
    private final UserService userService;
    private final GameService gameService;
    private final GameLinkService gameLinkService;
    private final DiscordService discordService;

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

    //TODO: Notify users when someone has lost or won
    public Runnable checkTurns() {
        return () -> {
            try {
                System.out.println("Checking turns!");
                List<Game> trackedGames = gameService.findAllGames();
                for (Game game : trackedGames) {
                    ImperaGameViewDTO imperaGame = imperaService.getGame(game.getId());

                    boolean gameEnded = imperaGame.state.equals("Ended");
                    boolean gameOpen = imperaGame.state.equals("Open");
                    boolean turnChanged = game.currentTurn != imperaGame.turnCounter;
                    boolean halfTimeNotNoticed = !game.halfTimeNotice && (imperaGame.timeoutSecondsLeft <= (imperaGame.options.timeoutInSeconds / 2));
                    // If nothing has changed (we don't need to ping anyone)
                    // If the turn hasnt changed, and the half time notice has been sent, and the game hasn't ended, or the game is open
                    if ((!turnChanged && !halfTimeNotNoticed && !gameEnded) || gameOpen) {
                        continue;
                    }

                    // Get all channels that want to receive updates for this game.
                    Set<Long> channels = gameLinkService.findLinksByGame(game.getId())
                            .stream()
                            .filter(gameChannelLink ->
                                    gameLinkService.deepGetNotificationSetting(gameChannelLink.getGameLinkId()) != NO_NOTIFICATIONS)
                            .map(GameChannelLink::getChannelId)
                            .collect(Collectors.toSet());

                    Optional<BotUser> currentPlayer = userService.findImperaUser(UUID.fromString(imperaGame.currentPlayer.userId));
                    String userString;
                    if (currentPlayer.isEmpty()) {
                        // The user is not registered with the service, so we can't send a DM
                        userString = imperaGame.currentPlayer.name;
                    } else {
                        BotUser player = currentPlayer.get();
                        userString = "<@" + player.getUserId() + ">";
                        switch (player.getNotificationSetting()) {
                            case NO_NOTIFICATIONS -> userString = imperaGame.currentPlayer.name;
                            case GUILD_ONLY -> {
                            }
                            case DMS_ONLY -> {
                                userString = imperaGame.currentPlayer.name;
                                channels.add(discordService.getDMChannelByOwner(player.getUserId()).getId().asLong());
                            }
                            case PREFER_GUILD_OVER_DMS -> {
                                if (channels.isEmpty()) {
                                    userString = imperaGame.currentPlayer.name;
                                    channels.add(discordService.getDMChannelByOwner(player.getUserId()).getId().asLong());
                                }
                            }
                            case DMS_AND_GUILD ->
                                    channels.add(discordService.getDMChannelByOwner(player.getUserId()).getId().asLong());
                        }
                    }
                    String finalUserString = userString;

                    // There is probably a better way to do this, but I'm not sure what it is.
                    if (gameEnded) {
                        System.out.println("Game " + game.getId() + " has ended!");
                        // Send a message to all channels that are tracking this game, who won
                        channels.forEach((channel) -> discordService.sendVictorMessage(channel, game.getId(), imperaGame.name, finalUserString));
                        gameService.deleteGame(game.getId());
                    } else if (turnChanged) {
                        //Send notice
                        System.out.println("Sending turn notice!");
                        channels.forEach((channel) -> discordService.sendMessage(channel, false, finalUserString, game.getId(), imperaGame.name));
                        gameService.turnChanged(game.getId(), imperaGame.turnCounter);
                    } else {
                        //Send half-time notice
                        System.out.println("Sending half time notice!");
                        channels.forEach((channel) -> discordService.sendMessage(channel, true, finalUserString, game.getId(), imperaGame.name));
                        gameService.setHalfTimeNoticeForGame(game.getId());
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };
    }
}
