package com.lelebees.imperabot.core.application.runnable;

import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageCommunicatorDTO;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import com.lelebees.imperabot.user.application.UserService;
import com.lelebees.imperabot.user.application.dto.BotUserDTO;
import com.lelebees.imperabot.user.application.exception.UserNotFoundException;
import com.lelebees.imperabot.user.domain.exception.IncorrectVerificationCodeException;
import com.lelebees.imperabot.user.domain.exception.UserAlreadyVerfiedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CheckVerifyRequests implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(CheckVerifyRequests.class);
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
            ImperaMessageCommunicatorDTO sender = linkMessage.from();
            if (userService.isImperaUserVerified(sender.id())) {
                skippedRequests++;
                logger.warn("User with Impera account " + sender.name() + " (" + sender.id() + ") already exists. Skipping and destroying message...");
                imperaService.deleteMessage(linkMessage);
                continue;
            }
            try {
                BotUserDTO user = userService.verifyUser(linkMessage.text().trim(), sender.id(), sender.name());
                imperaService.deleteMessage(linkMessage);
                logger.info("User " + sender.name() + " (" + sender.id() + ") aka (snowflake) " + user.discordId() + " has been verified!");
                discordService.sendVerificationDM(user.discordId());
            } catch (UserNotFoundException e) {
                //TODO: Notify user of failed verification
                logger.warn("User matching code " + linkMessage.text() + " Not found, skipping...");
                skippedRequests++;
            } catch (UserAlreadyVerfiedException e) {
                try {
                    BotUserDTO user = userService.findImperaUserOrThrow(sender.id());
                    logger.warn("User " + sender.name() + " (" + sender.id().toString() + ") aka (snowflake) " + user.discordId() + " already verified!");
                } catch (UserNotFoundException er) {
                    logger.error("User " + sender.name() + " (" + sender.id().toString() + ") was already verified, yet user could not be found in database.", er, e);
                    logger.warn("To prevent future errors, the offending link request (" + linkMessage.id() + ") will be deleted. A copy of the message data follows now.\n" + linkMessage);
                }
                imperaService.deleteMessage(linkMessage);
            } catch (IncorrectVerificationCodeException e) {
                try {
                    BotUserDTO user = userService.findImperaUserOrThrow(sender.id());
                    logger.warn("User " + sender.name() + " (" + sender.id() + ") could not be verified as (snowflake) " + user.discordId() + " because the supplied verification code was incorrect.");
                } catch (UserNotFoundException er) {
                    logger.error("User " + sender.name() + " (" + sender.id().toString() + ") supplied incorrect verification code, yet user could not be found in database.", er, e);
                    logger.warn("To prevent future errors, the offending link request (" + linkMessage.id() + ") will be deleted. A copy of the message data follows now.\n" + linkMessage);
                }
                imperaService.deleteMessage(linkMessage);
            }
        }
        logger.info("Skipped " + skippedRequests + " requests.");
    }
}
