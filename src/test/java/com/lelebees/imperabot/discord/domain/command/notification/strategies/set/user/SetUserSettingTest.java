package com.lelebees.imperabot.discord.domain.command.notification.strategies.set.user;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

class SetUserSettingTest {

    @Test
    @Disabled
        //TODO: Fix this test
    void setSettingSetsSetting() {
        int botUserId = 123;
        ChatInputInteractionEvent event = Mockito.mock(ChatInputInteractionEvent.class);
        ApplicationCommandInteractionOption setting = Mockito.mock(ApplicationCommandInteractionOption.class);
        Optional<ApplicationCommandInteractionOptionValue> settingValue = Optional.of(Mockito.mock(ApplicationCommandInteractionOptionValue.class));
        Mockito.when(event.getOptions().get(anyInt()).getOptions().get(anyInt()).getOption("setting")).thenReturn(Optional.of(setting));
        Mockito.when(setting.getValue()).thenReturn(settingValue);
        Mockito.when(settingValue.get().asLong()).thenReturn(1L);
        BotUser user = new BotUser(botUserId);
        User discordUser = Mockito.mock(User.class);
        Mockito.when(event.getInteraction().getUser()).thenReturn(discordUser);
        Mockito.when(discordUser.getId()).thenReturn(Snowflake.of(botUserId));
        UserService userService = Mockito.mock(UserService.class);
        Mockito.when(userService.updateDefaultSetting(botUserId, any())).thenReturn(user);
        Mockito.when(user.getNotificationSetting()).thenReturn(UserNotificationSetting.DMS_ONLY);

        assertDoesNotThrow(() -> new SetUserSetting(userService).execute(event).block());
    }

}