package com.lelebees.imperabot.bot.domain.guild;

import com.lelebees.imperabot.bot.data.converter.GuildNotificationSettingsConverter;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;

import static com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings.NOTIFICATIONS_ON;

@Entity
@Table(name = "bot_guild_settings")
public class GuildSettings {
    @Id
    @Column(name = "guild_id")
    private long id;
    @Column(name = "default_channel_id")
    @Nullable
    public Long defaultChannelId;
    @Column(name = "notification_setting")
    @Convert(converter = GuildNotificationSettingsConverter.class)
    public GuildNotificationSettings notificationSetting;

    public GuildSettings(long id, @Nullable Long defaultChannelId, GuildNotificationSettings notificationSetting) {
        this.id = id;
        this.defaultChannelId = defaultChannelId;
        this.notificationSetting = notificationSetting;
    }

    public GuildSettings(long id) {
        this(id, null, NOTIFICATIONS_ON);
    }

    protected GuildSettings() {
    }

    public long getId() {
        return id;
    }
}
