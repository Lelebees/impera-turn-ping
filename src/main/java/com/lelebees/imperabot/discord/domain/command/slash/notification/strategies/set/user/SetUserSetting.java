package com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.set.user;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.NotificationCommandStrategy;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

public class SetUserSetting implements NotificationCommandStrategy {

    private final UserService userService;
    private final DiscordService discordService;


    public SetUserSetting(UserService userService, DiscordService discordService) {
        this.userService = userService;
        this.discordService = discordService;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        User callingUser = event.getInteraction().getUser();

        UserNotificationSetting setting = discordService.getUserSettingOption(event);

        BotUser user = userService.updateDefaultSetting(callingUser.getId().asLong(), setting);
        return event.reply().withEphemeral(true).withContent("Updated notification setting to `" + user.getNotificationSetting().toString() + "`");
    }
}
