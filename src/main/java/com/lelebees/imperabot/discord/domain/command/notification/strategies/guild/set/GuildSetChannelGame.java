package com.lelebees.imperabot.discord.domain.command.notification.strategies.guild.set;

import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

// Starts tracking a given game in a given channel, with default setting
public class GuildSetChannelGame implements NotificationCommandStrategy {
    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return null;
    }
}
