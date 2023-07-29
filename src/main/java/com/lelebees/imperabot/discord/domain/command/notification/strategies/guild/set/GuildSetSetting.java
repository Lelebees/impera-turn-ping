package com.lelebees.imperabot.discord.domain.command.notification.strategies.guild.set;

import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

// Sets default setting to given setting
public class GuildSetSetting implements NotificationCommandStrategy {
    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return null;
    }
}
