package com.lelebees.imperabot.discord.domain.command.notification.strategies.user.view;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public class UserViewGame implements NotificationCommandStrategy {
    private final GameLinkService gameLinkService;
    private final UserService userService;

    public UserViewGame(GameLinkService gameLinkService, UserService userService) {
        this.gameLinkService = gameLinkService;
        this.userService = userService;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return event.reply().withContent("Command is deprecated, sorry!").withEphemeral(true);
    }
}
