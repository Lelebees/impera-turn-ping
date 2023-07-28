package com.lelebees.imperabot.bot.domain.guild;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bot_guild_settings")
public class GuildSettings {
    @Id
    @Column(name = "guild_id")
    private long id;
    @Column(name = "default_channel_id")
    @Nullable
    private Long defaultChannelId;
    @Column(name = "notification_setting")
    private int notificationSetting;

    public GuildSettings(long id, @Nullable Long defaultChannelId, int notificationSetting) {
        this.id = id;
        this.defaultChannelId = defaultChannelId;
        this.notificationSetting = notificationSetting;
    }

    protected GuildSettings() {
    }

    public long getId() {
        return id;
    }

    @Nullable
    public Long getDefaultChannelId() {
        return defaultChannelId;
    }

    public int getNotificationSetting() {
        return notificationSetting;
    }
}
