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
        logger.info("Found " + linkMessages.size() + " link requests.");
        int skippedRequests = 0;
        for (ImperaMessageDTO linkMessage : linkMessages) {
            UUID imperaUserId = linkMessage.from.id;
            if (userService.isImperaUserAlreadyVerified(imperaUserId)) {
                skippedRequests++;
                logger.warn("User with Impera account " + linkMessage.from.name + " (" + linkMessage.from.id + ") already exists. Skipping and destroying message...");
                imperaService.deleteMessage(linkMessage.id);
                continue;
            }
            try {
                BotUser user = userService.verifyUser(linkMessage.text.trim(), imperaUserId);
                imperaService.deleteMessage(linkMessage.id);
                logger.info("User " + linkMessage.from.name + " (" + linkMessage.from.id + ") aka (snowflake) " + user.getUserId() + " has been verified!");
                discordService.sendVerificationDM(user.getUserId());
            } catch (UserNotFoundException e) {
                //TODO: Notify user of failed verification
                logger.warn("User matching code " + linkMessage.text + " Not found, skipping...");
                skippedRequests++;
            } catch (UserAlreadyVerfiedException e) {
                try {
                    BotUser user = userService.findImperaUserOrThrow(linkMessage.from.id);
                    logger.warn("User " + linkMessage.from.name + " (" + linkMessage.from.id.toString() + ") aka (snowflake) " + user.getUserId() + " already verified!");
                } catch (UserNotFoundException er) {
                    logger.error("User " + linkMessage.from.name + " (" + linkMessage.from.id.toString() + ") was already verified, yet user could not be found in database.", er, e);
                    logger.warn("To prevent future errors, the offending link request (" + linkMessage.id + ") will be deleted. A copy of the message data follows now.\n" + linkMessage);
                }
                imperaService.deleteMessage(linkMessage.id);
            } catch (IncorrecVerificationCodeException e) {
                try {
                    BotUser user = userService.findImperaUserOrThrow(linkMessage.from.id);
                    logger.warn("User " + linkMessage.from.name + " (" + linkMessage.from.id + ") could not be verified as (snowflake) " + user.getUserId() + " because the supplied verification code was incorrect.");
                } catch (UserNotFoundException er) {
                    logger.error("User " + linkMessage.from.name + " (" + linkMessage.from.id.toString() + ") supplied incorrect verification code, yet user could not be found in database.", er, e);
                    logger.warn("To prevent future errors, the offending link request (" + linkMessage.id + ") will be deleted. A copy of the message data follows now.\n" + linkMessage);
                }
                imperaService.deleteMessage(linkMessage.id);
            }
        }
        logger.info("Skipped " + skippedRequests + " requests.");
    }
}
