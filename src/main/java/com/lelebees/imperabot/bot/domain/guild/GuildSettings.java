package com.lelebees.imperabot.bot.domain.guild;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bot_guild_settings")
public class GuildSettings {
    @Column(name = "default_channel_id")
    @Nullable
    public Long defaultChannelId;
    @Column(name = "permission_role_id")
    @Nullable
    public Long permissionRoleId;
    @Column(name = "winner_role_id")
    @Nullable
    public Long winnerRoleId;
    @Id
    @Column(name = "guild_id")
    private long id;

    public GuildSettings(long id, @Nullable Long defaultChannelId, @Nullable Long permissionRoleId, @Nullable Long winnerRoleId) {
        this.id = id;
        this.defaultChannelId = defaultChannelId;
        this.permissionRoleId = permissionRoleId;
        this.winnerRoleId = winnerRoleId;
    }

    public GuildSettings(long id) {
        this(id, null, null, null);
    }

    protected GuildSettings() {
    }

    public long getId() {
        return id;
    }
}
