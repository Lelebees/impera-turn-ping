package com.lelebees.imperabot.discord.domain.command.notification.strategies.guild.set;

import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

// Sets the setting for a given channel to a given setting
public class GuildSetChannelSetting implements NotificationCommandStrategy {
    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return null;
    }
}
