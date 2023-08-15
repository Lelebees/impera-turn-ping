package com.lelebees.imperabot.impera.application;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerService {
    private final ImperaService imperaService;
    private final UserService userService;
    private final ScheduledFuture<?> linkHandle;
    private final ScheduledFuture<?> relogHandle;

    public SchedulerService(ImperaService imperaService, UserService userService) {
        this.imperaService = imperaService;
        this.userService = userService;
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        linkHandle = executorService.scheduleAtFixedRate(checkVerifyRequests(), 0, 1, TimeUnit.MINUTES);
        // Update the token a minute before it expires, and then 5 seconds before it expires thereafter.
        relogHandle = executorService.scheduleAtFixedRate(imperaService.updateAccessToken(), (imperaService.bearerToken.expires_in - 60), (imperaService.bearerToken.expires_in - 5), TimeUnit.SECONDS);

    }

    public Runnable checkVerifyRequests() {
        return () -> {
            try {
                System.out.println("Test");
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
}
