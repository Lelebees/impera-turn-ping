package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.application.game.GameService;
import com.lelebees.imperabot.bot.application.runnable.CheckTurns;
import com.lelebees.imperabot.bot.application.runnable.CheckVerifyRequests;
import com.lelebees.imperabot.bot.application.user.UserService;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.impera.application.ImperaService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerService {
    private final ImperaService imperaService;
    private final UserService userService;
    private final GameService gameService;
    private final DiscordService discordService;
    private final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    public SchedulerService(ImperaService imperaService, UserService userService, GameService gameService, DiscordService discordService) {
        this.imperaService = imperaService;
        this.userService = userService;
        this.gameService = gameService;
        this.discordService = discordService;
    }

    @PostConstruct
    public void schedule() {
        logger.info("Scheduling tasks...");
        try (ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1)) {
            executorService.scheduleAtFixedRate(new CheckVerifyRequests(imperaService, userService, discordService), 1, 5, TimeUnit.MINUTES);
            executorService.scheduleAtFixedRate(new CheckTurns(imperaService, gameService, discordService), 1, 1, TimeUnit.MINUTES);
        } catch (RuntimeException e) {
            logger.error("Caught Runtime Exception while attempting to schedule tasks.", e);
            throw new RuntimeException(e);
        }
    }

}
