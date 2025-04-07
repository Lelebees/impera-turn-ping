package com.lelebees.imperabot.bot.application.runnable;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.IncorrecVerificationCodeException;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CheckVerifyRequests implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(CheckVerifyRequests.class);
    private final ImperaService imperaService;
    private final UserService userService;
    private final DiscordService discordService;

    public CheckVerifyRequests(ImperaService imperaService, UserService userService, DiscordService discordService) {
        this.imperaService = imperaService;
        this.userService = userService;
        this.discordService = discordService;
    }

    @Override
    public void run() {
        // We MUST catch-all errors here, because if we don't, the thread will SILENTLY CRASH and NEVER RESTART.
        try {
            checkRequests();
        } catch (Exception e) {
            logger.error("Handled Error: " + e.getMessage(), e);
        }
    }

    private void checkRequests() {
        List<ImperaMessageDTO> linkMessages = imperaService.getLinkMessages();
        logger.info("[CLR] Found " + linkMessages.size() + " link requests.");
        int skippedRequests = 0;
        for (ImperaMessageDTO linkMessage : linkMessages) {
            UUID imperaUserId = UUID.fromString(linkMessage.from.id);
            Optional<BotUser> userOptional = userService.findImperaUser(imperaUserId);
            if (userOptional.isPresent()) {
                skippedRequests++;
                logger.warn("User " + linkMessage.from.name + " (" + linkMessage.from.id + ") aka (snowflake) " + userOptional.get().getUserId() + " already exists! Skipping and destroying message...");
                imperaService.deleteMessage(linkMessage.id);
                continue;
            }
            try {
                BotUser user = userService.verifyUser(linkMessage.text.trim(), imperaUserId);
                imperaService.deleteMessage(linkMessage.id);
                logger.info("User " + linkMessage.from.name + " (" + linkMessage.from.id + ") aka (snowflake) " + user.getUserId() + " has been verified!");
                discordService.sendVerificationDM(user.getUserId());
            } catch (UserNotFoundException e) {
                skippedRequests++;
                logger.warn("User matching code " + linkMessage.text + " Not found, skipping...");
            } catch (UserAlreadyVerfiedException e) {
                BotUser user = userService.findImperaUser(UUID.fromString(linkMessage.from.id)).get();
                logger.warn("User " + linkMessage.from.name + " (" + linkMessage.from.id + ") aka (snowflake) " + user.getUserId() + " already verified!");
                imperaService.deleteMessage(linkMessage.id);
            } catch (IncorrecVerificationCodeException e) {
                BotUser user = userService.findImperaUser(UUID.fromString(linkMessage.from.id)).get();
                logger.warn("User " + linkMessage.from.name + " (" + linkMessage.from.id + ") could not be verified as " + user.getUserId() + " because the supplied verification code was incorrect.");
                imperaService.deleteMessage(linkMessage.id);
            }
        }
        logger.info("Skipped " + skippedRequests + " requests.");
    }
}
