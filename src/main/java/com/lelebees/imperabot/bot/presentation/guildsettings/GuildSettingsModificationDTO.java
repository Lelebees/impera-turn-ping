package com.lelebees.imperabot.bot.presentation.guildsettings;

import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;

public class GuildSettingsModificationDTO {
    public Long channelId;
    public GuildNotificationSettings notificationSetting;

    public GuildSettingsModificationDTO(Long channelId, GuildNotificationSettings notificationSetting) {
        this.channelId = channelId;
        this.notificationSetting = notificationSetting;
    }
}
