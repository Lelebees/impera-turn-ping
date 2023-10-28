package com.lelebees.imperabot.bot.presentation.guildsettings;

import com.lelebees.imperabot.bot.domain.guild.GuildSettings;

public class GuildSettingsModificationDTO {
    public Long channelId;
    public Long permissionRoleId;
    public Long winnerRoleId;

    public GuildSettingsModificationDTO(Long channelId, Long permissionRoleId, Long winnerRoleId) {
        this.channelId = channelId;
        this.permissionRoleId = permissionRoleId;
        this.winnerRoleId = winnerRoleId;
    }

    public GuildSettingsModificationDTO(GuildSettings guildSettings) {
        this(guildSettings.defaultChannelId, guildSettings.permissionRoleId, guildSettings.winnerRoleId);
    }
}
