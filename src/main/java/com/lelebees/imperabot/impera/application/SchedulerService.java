package com.lelebees.imperabot.impera.application;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GameService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
    private final ScheduledFuture<?> linkHandle;
    private final ScheduledFuture<?> relogHandle;

    public SchedulerService(ImperaService imperaService, UserService userService, GameService gameService, GameLinkService gameLinkService) {
        this.imperaService = imperaService;
        this.userService = userService;
        this.gameService = gameService;
        this.gameLinkService = gameLinkService;
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        linkHandle = executorService.scheduleAtFixedRate(checkVerifyRequests(), 0, 1, TimeUnit.MINUTES);
        // Update the token a minute before it expires, and then 5 seconds before it expires thereafter.
        relogHandle = executorService.scheduleAtFixedRate(imperaService.updateAccessToken(), (imperaService.bearerToken.expires_in - 60), (imperaService.bearerToken.expires_in - 5), TimeUnit.SECONDS);

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
                List<Game> trackedGames = gameService.findAllGames();
                for (Game game : trackedGames) {
                    ImperaGameViewDTO imperaGame = imperaService.getGame(game.getId());
                    boolean turnChanged = game.currentTurn < imperaGame.turnCounter;
                    boolean halfTimeNotNoticed = !game.halfTimeNotice && imperaGame.timeoutSecondsLeft <= imperaGame.options.timeoutInSeconds;

                    if (turnChanged || halfTimeNotNoticed) {

                        for (GameChannelLink gameChannelLink : gameLinkService.findLinksByGame(game.getId())) {

                        }

                        Optional<BotUser> user = userService.findImperaUser(UUID.fromString(imperaGame.currentPlayer.userId));
                        String userString = user.map(botUser -> "<@" + botUser.getUserId() + ">").orElseGet(() -> imperaGame.currentPlayer.name);
                        if (turnChanged) {
                            //Send notice
                        } else {
                            //Send half time notice
                            gameService.setHalfTimeNoticeForGame(game.getId());
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };
    }
}
