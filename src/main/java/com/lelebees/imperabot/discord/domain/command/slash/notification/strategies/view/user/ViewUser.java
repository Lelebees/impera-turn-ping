package com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.view.user;

import com.lelebees.imperabot.discord.application.NotificationService;
import com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.NotificationCommandStrategy;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public class ViewUser implements NotificationCommandStrategy {

    private final NotificationService notificationService;

    public ViewUser(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return event.reply().withEphemeral(true).withEmbeds(notificationService.getUserSettingsEmbed(event.getInteraction().getUser()));
    }
}
