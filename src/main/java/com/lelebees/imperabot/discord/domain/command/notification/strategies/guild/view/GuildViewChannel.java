package com.lelebees.imperabot.discord.domain.command.notification.strategies.guild.view;

import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

// View the settings/games for the given channel
//TODO: requires new system for channels to be in place
public class GuildViewChannel implements NotificationCommandStrategy {
    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return null;
    }
}
