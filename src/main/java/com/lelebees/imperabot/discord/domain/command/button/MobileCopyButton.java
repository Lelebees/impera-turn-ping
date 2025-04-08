package com.lelebees.imperabot.discord.domain.command.button;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.discord.domain.command.ButtonCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MobileCopyButton implements ButtonCommand {

    private final UserService userService;

    public MobileCopyButton(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getCustomId() {
        return "mobileCode";
    }

    @Override
    public Mono<Void> handle(ButtonInteractionEvent event) {
        BotUser user = userService.findOrCreateUser(event.getInteraction().getUser().getId().asLong());
        return event.reply().withEphemeral(true).withContent(user.getVerificationCode());
    }
}
