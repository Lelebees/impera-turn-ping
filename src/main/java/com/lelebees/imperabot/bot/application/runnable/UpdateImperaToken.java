package com.lelebees.imperabot.bot.application.runnable;

import com.lelebees.imperabot.impera.data.ImperaAPIRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateImperaToken implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(UpdateImperaToken.class);
    private final ImperaAPIRepository imperaRepository;

    public UpdateImperaToken(ImperaAPIRepository imperaService) {
        this.imperaRepository = imperaService;
    }

    @Override
    public void run() {
        try {
            logger.info("Updating bearer token!");
            imperaRepository.refreshBearerToken();
        } catch (Exception e) {
            logger.error("Handled Error: " + e.getMessage(), e);
        }
    }
}
