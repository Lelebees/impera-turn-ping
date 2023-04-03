package com.lelebees.imperabot.domain.bot;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class GuildSettings {
    @Id
    private long guild_id;
    private Long default_channel_id;
    private int notification_setting;
}
