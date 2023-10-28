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

    //TODO: Determine if this needs to be saved.
    @Column(name = "notification_setting")
    @Convert(converter = GuildNotificationSettingsConverter.class)
    public GuildNotificationSettings notificationSetting;

    @Column(name = "permission_role_id")
    @Nullable
    public Long permissionRoleId;

    public GuildSettings(long id, @Nullable Long defaultChannelId, GuildNotificationSettings notificationSetting, @Nullable Long permissionRoleId) {
        this.id = id;
        this.defaultChannelId = defaultChannelId;
        this.notificationSetting = notificationSetting;
    }

    public GuildSettings(long id) {
        this(id, null, NOTIFICATIONS_ON, null);
    }

    protected GuildSettings() {
    }

    public long getId() {
        return id;
    }
}
