package com.lelebees.imperabot.bot.presentation.guildsettings;

public class GuildSettingsModificationDTO {
    public Long channelId;
    public int notificationSetting;

    public GuildSettingsModificationDTO(Long channelId, int notificationSetting) {
        this.channelId = channelId;
        this.notificationSetting = notificationSetting;
    }
}
