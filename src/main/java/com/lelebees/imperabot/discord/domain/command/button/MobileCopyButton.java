package com.lelebees.imperabot.discord.domain.command.button;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
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
        Snowflake id = event.getInteraction().getUser().getId();
        BotUser user = userService.findOrCreateUser(id.asLong());
        return event.reply().withEphemeral(true).withContent(user.getVerificationCode());
    }
}
