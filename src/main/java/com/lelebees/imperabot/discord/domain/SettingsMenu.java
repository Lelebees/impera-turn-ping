package com.lelebees.imperabot.discord.domain;

import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.bot.presentation.guildsettings.GuildSettingsDTO;
import com.lelebees.imperabot.bot.presentation.user.BotUserDTO;
import discord4j.common.util.Snowflake;
import discord4j.core.object.component.*;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.util.Color;

import java.util.List;
import java.util.Map;

import static com.lelebees.imperabot.bot.domain.user.UserNotificationSetting.*;

public class SettingsMenu {


    public static Container buildForUser(BotUserDTO botUser, User discordUser) {
        return Container.of(Color.of(230, 200, 90),
                TextDisplay.of("# Settings for %s".formatted(discordUser.getUserData().globalName().get())),
                TextDisplay.of("## :bell: Notifications"),
                ActionRow.of(
                        SelectMenu.of("notification-setting", getNotificationOptions(botUser))
                ),
                TextDisplay.of("## :link: Impera Account"),
                getImperaLinkComponent(botUser)

        );
    }

    public static Container buildForGuild(GuildSettingsDTO guildSettings, Guild guild) {
        return Container.of(Color.of(230, 200, 90),
                TextDisplay.of("# Settings for %s".formatted(guild.getName())),
                TextDisplay.of("## :bell: Notifications"),
                TextDisplay.of("### Default Channel"),
                ActionRow.of(
                        SelectMenu.ofChannel("guild-settings-channel-select", getDefaultChannelOption(guildSettings), getAllowedChannelTypes()).withMinValues(0)
                ),
                TextDisplay.of("## :trophy: Winners"),
                TextDisplay.of("### Role to award winners"),
                ActionRow.of(
                        SelectMenu.ofRole("guild-settings-winner-role-select", getWinnerRoleOption(guildSettings)).withMinValues(0)
                ),
/*                TextDisplay.of("### Automatically remove winner role when a different server member wins?"),
                ActionRow.of(
                        SelectMenu.of("guild-settings-auto-remove-winner-select", getAutoRemovePreviousWinOptions(guildSettings))
                ), */
                TextDisplay.of("## :identification_card: Permissions"),
                TextDisplay.of("### Allow users with this role to edit these settings"),
                ActionRow.of(
                        SelectMenu.ofRole("guild-settings-permission-role-select", getPermissionRoleOption(guildSettings)).withMinValues(0)
                )
        );
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

    private static SelectMenu.Option[] getNotificationOptions(BotUserDTO botUser) {
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

    private static SelectMenu.Option[] getAutoRemovePreviousWinOptions(GuildSettingsDTO guildSettingsDTO) {
        Map<Boolean, String> optionLabels = Map.of(
                true, "Yes",
                false, "No"
        );
        SelectMenu.Option[] options = new SelectMenu.Option[2];
        options[0] = SelectMenu.Option.of(optionLabels.get(true), "true");
        options[1] = SelectMenu.Option.of(optionLabels.get(false), "false");
        //TODO: Implement Defaults
        return options;
    }

    private static List<SelectMenu.DefaultValue> getDefaultChannelOption(GuildSettingsDTO guildSettingsDTO) {
        if (guildSettingsDTO.defaultChannelId() == null) {
            return List.of();
        }
        return List.of(
                SelectMenu.DefaultValue.of(Snowflake.of(guildSettingsDTO.defaultChannelId()), SelectMenu.DefaultValue.Type.CHANNEL)
        );
    }

    private static List<SelectMenu.DefaultValue> getPermissionRoleOption(GuildSettingsDTO guildSettingsDTO) {
        if (guildSettingsDTO.permissionRoleId() == null) {
            return List.of();
        }
        return List.of(
                SelectMenu.DefaultValue.of(Snowflake.of(guildSettingsDTO.permissionRoleId()), SelectMenu.DefaultValue.Type.ROLE)
        );
    }

    private static List<SelectMenu.DefaultValue> getWinnerRoleOption(GuildSettingsDTO guildSettingsDTO) {
        if (guildSettingsDTO.winnerRoleId() == null) {
            return List.of();
        }
        return List.of(
                SelectMenu.DefaultValue.of(Snowflake.of(guildSettingsDTO.winnerRoleId()), SelectMenu.DefaultValue.Type.ROLE)
        );
    }

    private static List<Channel.Type> getAllowedChannelTypes() {
        return List.of(
                Channel.Type.GUILD_TEXT,
                Channel.Type.GUILD_NEWS,
                Channel.Type.GUILD_PRIVATE_THREAD,
                Channel.Type.GUILD_PUBLIC_THREAD
        );
    }
}
