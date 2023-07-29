package com.lelebees.imperabot.discord.domain.command.notification.strategies;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface NotificationCommandStrategy {
    public Mono<Void> execute(ChatInputInteractionEvent event);
}
