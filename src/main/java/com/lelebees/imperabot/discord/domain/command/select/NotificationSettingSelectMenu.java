package com.lelebees.imperabot.discord.domain.command.select;

import com.lelebees.imperabot.bot.application.user.UserService;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.bot.presentation.user.BotUserDTO;
import com.lelebees.imperabot.discord.domain.SettingsMenu;
import com.lelebees.imperabot.discord.domain.command.SelectMenuInteraction;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class NotificationSettingSelectMenu implements SelectMenuInteraction {
    private final Logger logger = LoggerFactory.getLogger(NotificationSettingSelectMenu.class);
    private final UserService userService;


    public NotificationSettingSelectMenu(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getCustomId() {
        return "notification-setting";
    }

    @Override
    public Mono<Void> handle(SelectMenuInteractionEvent event) {
        UserNotificationSetting setting = UserNotificationSetting.parse(event.getValues().get(0));
        BotUserDTO user = userService.updateDefaultSetting(event.getInteraction().getUser().getId().asLong(), setting);
        logger.info("Updated " + event.getUser().getUsername() + "'s (" + event.getUser().getId().asLong() + ") notification setting to " + user.notificationSetting());
        return event.edit(InteractionApplicationCommandCallbackSpec.builder().addAllComponents(List.of(SettingsMenu.buildForUser(user, event.getUser()))).build());
    }
}
