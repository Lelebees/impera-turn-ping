package com.lelebees.imperabot.bot.presentation.guildsettings;

import com.lelebees.imperabot.bot.domain.guild.GuildSettings;

public record GuildSettingsDTO(long guildId, Long defaultChannelId, Long permissionRoleId, Long winnerRoleId) {

    public static GuildSettingsDTO from(GuildSettings settings) {
        return new GuildSettingsDTO(settings.getId(), settings.defaultChannelId, settings.permissionRoleId, settings.winnerRoleId);
    }
}
