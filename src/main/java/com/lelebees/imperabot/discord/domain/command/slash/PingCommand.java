package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionCallbackSpecDeferReplyMono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class PingCommand implements SlashCommand {

    private final static Logger logger = LoggerFactory.getLogger(PingCommand.class);

    public PingCommand() {
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        InteractionCallbackSpecDeferReplyMono spec = event.deferReply();
        Instant requestFired = event.getCommandId().getTimestamp();
        long difference = Instant.now().toEpochMilli() - requestFired.toEpochMilli();
        float differenceInMilis = (float) difference / 10000;
        User user = event.getInteraction().getUser();
        logger.info(user.getUsername() + " (" + user.getId().asLong() + ") used /ping!");
        return spec.event().reply()
                .withContent("Pong in " + differenceInMilis + "ms");
    }
}
