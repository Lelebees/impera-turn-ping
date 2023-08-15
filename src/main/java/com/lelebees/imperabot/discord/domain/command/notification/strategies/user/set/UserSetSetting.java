package com.lelebees.imperabot.discord.domain.command.notification.strategies.user.set;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class UserSetSetting implements NotificationCommandStrategy {
    private final UserService userService;

    public UserSetSetting(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        User callingUser = event.getInteraction().getUser();

        Optional<ApplicationCommandInteractionOptionValue> setting = event.getOptions().get(0).getOptions().get(0).getOption("setting").orElseThrow(() -> new NullPointerException("This is impossible, How could setting not exist?!")).getValue();
        if (setting.isEmpty()) {
            throw new NullPointerException("setting is present but not entered?");
        }
        int settingInt = Math.toIntExact(setting.get().asLong());

        BotUser user = userService.updateDefaultSetting(callingUser.getId().asLong(), UserNotificationSetting.values()[settingInt]);
        return event.reply().withEphemeral(true).withContent("Updated notification setting to `" + user.getNotificationSetting().toString() + "`");
    }
}
