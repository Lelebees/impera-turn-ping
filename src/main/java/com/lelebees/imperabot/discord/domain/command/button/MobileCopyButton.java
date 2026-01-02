package com.lelebees.imperabot.discord.domain.command.button;

import com.lelebees.imperabot.bot.application.user.UserService;
import com.lelebees.imperabot.bot.application.user.exception.UserNotFoundException;
import com.lelebees.imperabot.discord.domain.command.ButtonCommand;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MobileCopyButton implements ButtonCommand {

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(MobileCopyButton.class);

    public MobileCopyButton(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getCustomId() {
        return "mobileCode";
    }

    @Override
    public Mono<Void> handle(ButtonInteractionEvent event) {
        User user = event.getInteraction().getUser();
        try {
            return event.reply().withEphemeral(true).withContent(userService.getVerificationCode(user.getId().asLong()));
        } catch (UserNotFoundException e) {
            logger.error("Could not return verification code for %s (%d), as this user does not exist in the database.".formatted(user.getUsername(), user.getId().asLong()));
            return event.reply().withEphemeral(true).withContent("Exception occurred while trying to fetch your verification code: user %s (%d) does not exist.".formatted(user.getUsername(), user.getId().asLong()));
        }
    }
}
