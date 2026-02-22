package com.lelebees.imperabot.core.application;

import com.lelebees.imperabot.user.application.UserService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerService {
    private final UserService userService;
    private final GameService gameService;
    private final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    public SchedulerService(UserService userService, GameService gameService) {
        this.userService = userService;
        this.gameService = gameService;
    }

    @PostConstruct
    public void schedule() {
        logger.info("Scheduling tasks...");
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(userService::checkVerificationMessages, 1, 5, TimeUnit.MINUTES);
        executorService.scheduleAtFixedRate(gameService::checkTurns, 1, 1, TimeUnit.MINUTES);
    }

}
