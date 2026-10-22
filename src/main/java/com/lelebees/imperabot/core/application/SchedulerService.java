package com.lelebees.imperabot.core.application;

import com.lelebees.imperabot.impera.data.SimpleImperaAPIRepository;
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
    private final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    private final UserService userService;
    private final GameService gameService;
    private final SimpleImperaAPIRepository imperaRepository;

    public SchedulerService(UserService userService, GameService gameService, SimpleImperaAPIRepository imperaRepository) {
        this.userService = userService;
        this.gameService = gameService;
        this.imperaRepository = imperaRepository;
    }

    @PostConstruct
    public void schedule() {
        logger.info("Scheduling tasks...");
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(userService::checkVerificationRequests, 1, 5, TimeUnit.MINUTES);
        executorService.scheduleAtFixedRate(gameService::checkTurns, 1, 1, TimeUnit.MINUTES);
        executorService.scheduleAtFixedRate(imperaRepository::updateBearerToken, 0, 1, TimeUnit.HOURS);
    }

}
