package com.lelebees.imperabot.discord.domain.command;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import reactor.core.publisher.Mono;

public interface ButtonCommand {
    String getCustomId();

    Mono<Void> handle(ButtonInteractionEvent event);
}
