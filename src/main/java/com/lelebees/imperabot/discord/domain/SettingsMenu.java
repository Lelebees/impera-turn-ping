package com.lelebees.imperabot.discord.domain;

import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.bot.presentation.user.BotUserDTO;
import discord4j.core.object.component.*;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Color;

import java.util.Map;

import static com.lelebees.imperabot.bot.domain.user.UserNotificationSetting.*;

public class SettingsMenu {


    public static Container getForUser(BotUserDTO botUser, User discordUser) {
        return Container.of(Color.of(230, 200, 90),
                TextDisplay.of("# Settings for %s".formatted(discordUser.getUserData().globalName().get())),
                TextDisplay.of("## :bell: Notifications"),
                ActionRow.of(
                        SelectMenu.of("notification-setting", getOptions(botUser))
                ),
                TextDisplay.of("## :link: Impera Account"),
                getImperaLinkComponent(botUser)

        );
    }

    public static Container getForGuild() {
        return null;
    }

    private static Section getImperaLinkComponent(BotUserDTO botUser) {
        if (!botUser.isLinked()) {
            return Section.of(
                    Button.primary("link", "Link your account"),
                    TextDisplay.of("You're currently not linked to an Impera account."));
        }
        return Section.of(
                Button.danger("unlink", "Unlink your account"),
                TextDisplay.of("You're currently linked to: %s".formatted(botUser.username())),
                TextDisplay.of("-# (%s)".formatted(botUser.imperaId())));
    }

    private static SelectMenu.Option[] getOptions(BotUserDTO botUser) {
        Map<UserNotificationSetting, String> optionLabels = Map.of(
                NO_NOTIFICATIONS, "No notifications",
                DMS_ONLY, "Only in DMs",
                GUILD_ONLY, "Only in Servers",
                PREFER_GUILD_OVER_DMS, "Servers if available, otherwise, DM",
                DMS_AND_GUILD, "Servers and DMs"
        );
        SelectMenu.Option[] options = new SelectMenu.Option[values().length];
        for (int i = 0; i < values().length; i++) {
            UserNotificationSetting setting = get(i);
            if (setting.equals(botUser.notificationSetting())) {
                options[i] = SelectMenu.Option.ofDefault(optionLabels.get(setting), setting.toString());
                continue;
            }
            options[i] = SelectMenu.Option.of(optionLabels.get(setting), setting.toString());
        }
        return options;
    }

}
