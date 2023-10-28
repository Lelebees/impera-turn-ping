package com.lelebees.imperabot.bot.application.runnable;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class CheckVerifyRequests implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(CheckVerifyRequests.class);
    private final ImperaService imperaService;
    private final UserService userService;

    public CheckVerifyRequests(ImperaService imperaService, UserService userService) {
        this.imperaService = imperaService;
        this.userService = userService;
    }

    @Override
    public void run() {
        try {
            List<ImperaMessageDTO> linkMessages = imperaService.getLinkMessages();
            logger.info("[CLR] Found " + linkMessages.size() + " link requests.");
            int skippedRequests = 0;
            for (ImperaMessageDTO linkMessage : linkMessages) {
                try {
                    BotUser user = userService.verifyUser(linkMessage.text.trim(), UUID.fromString(linkMessage.from.id));
                    imperaService.deleteMessage(linkMessage.id);
                    logger.info("User " + linkMessage.from.name + " (" + linkMessage.from.id + ") aka (snowflake)" + user.getUserId() + " has been verified!");
                } catch (UserNotFoundException e) {
                    logger.warn("User matching code " + linkMessage.text + " Not found, skipping...");
                    skippedRequests++;
                } catch (UserAlreadyVerfiedException e) {
                    BotUser user = userService.findImperaUser(UUID.fromString(linkMessage.from.id)).get();
                    logger.warn("User " + linkMessage.from.name + " (" + linkMessage.from.id + ") aka (snowflake) " + user.getUserId() + " already verified!");
                    imperaService.deleteMessage(linkMessage.id);
                }
            }
            logger.info("Skipped " + skippedRequests + " requests.");
        } catch (Exception e) {
            logger.error("Handled Error: " + e.getMessage(), e);
        }
    }
}
