package com.lelebees.imperabot.bot.application.runnable;

import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.ImperaLoginDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public class UpdateImperaToken implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(UpdateImperaToken.class);
    private final ImperaService imperaService;

    public UpdateImperaToken(ImperaService imperaService) {
        this.imperaService = imperaService;
    }

    @Override
    public void run() {
        try {
            logger.info("Updating bearer token!");
            ImperaLoginDTO bearerToken = imperaService.getBearerToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(bearerToken.access_token);
            ImperaService.entity = new HttpEntity<>(headers);
        } catch (Exception e) {
            logger.error("Handled Error: " + e.getMessage(), e);
        }
    }
}
