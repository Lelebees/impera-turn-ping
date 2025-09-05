package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionCallbackSpecDeferReplyMono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class PingCommand implements SlashCommand {

    private final Logger logger = LoggerFactory.getLogger(PingCommand.class);


    public PingCommand() {
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Instant now = Instant.now();
        InteractionCallbackSpecDeferReplyMono spec = event.deferReply();

        Instant eventInstant = event.getInteraction().getId().getTimestamp();
        long difference = now.toEpochMilli() - eventInstant.toEpochMilli();

        return spec.event().reply().withContent("Pong in " + difference + "ms");
    }


}
