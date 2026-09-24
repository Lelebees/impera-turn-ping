package com.lelebees.imperabot.core.domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;

@Entity
@Table(name = "bot_guild_settings")
public class GuildSettings {
    @JoinColumn(name = "default_channel_id")
    @Nullable
    @OneToOne
    public Channel defaultChannel;
    @Column(name = "permission_role_id")
    @Nullable
    public Long permissionRoleId;
    @Column(name = "winner_role_id")
    @Nullable
    public Long winnerRoleId;
    @Id
    @Column(name = "guild_id")
    private long id;

    public GuildSettings(long id, @Nullable Channel defaultChannel, @Nullable Long permissionRoleId, @Nullable Long winnerRoleId) {
        this.id = id;
        this.defaultChannel = defaultChannel;
        this.permissionRoleId = permissionRoleId;
        this.winnerRoleId = winnerRoleId;
    }

    protected GuildSettings() {
    }

    public long getId() {
        return id;
    }

    public static GuildSettings From(long id) {
        return new GuildSettings(id, null, null, null);
    }
}
