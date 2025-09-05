package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.application.runnable.CheckTurns;
import com.lelebees.imperabot.bot.application.runnable.CheckVerifyRequests;
import com.lelebees.imperabot.bot.application.runnable.UpdateImperaToken;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.data.ImperaAPIRepository;
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
    private final ImperaAPIRepository imperaAPIRepository;
    private final UserService userService;
    private final GameService gameService;
    private final GameLinkService gameLinkService;
    private final DiscordService discordService;
    private final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    public SchedulerService(ImperaService imperaService, ImperaAPIRepository imperaAPIRepository, UserService userService, GameService gameService, GameLinkService gameLinkService, DiscordService discordService) {
        this.imperaService = imperaService;
        this.imperaAPIRepository = imperaAPIRepository;
        this.userService = userService;
        this.gameService = gameService;
        this.gameLinkService = gameLinkService;
        this.discordService = discordService;
    }

    @PostConstruct
    public void schedule() {
        logger.info("Scheduling tasks...");
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new CheckVerifyRequests(imperaService, userService, discordService), 1, 5, TimeUnit.MINUTES);
        // Update the token a minute before it expires.
        executorService.scheduleAtFixedRate(new UpdateImperaToken(imperaAPIRepository), (imperaAPIRepository.getTokenExpiryTime() - 60), (imperaAPIRepository.getTokenExpiryTime()), TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(new CheckTurns(imperaService, gameService, gameLinkService, discordService), 1, 1, TimeUnit.MINUTES);
    }

}
