package com.lelebees.imperabot.discord.domain.command;

import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import reactor.core.publisher.Mono;

public interface SelectMenuInteraction {
    String getCustomId();

    Mono<Void> handle(SelectMenuInteractionEvent event);
}
