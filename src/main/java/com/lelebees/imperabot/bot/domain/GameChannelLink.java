package com.lelebees.imperabot.bot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "bot_game_channel")
@IdClass(GameLinkId.class)
public class GameChannelLink {
    @Id
    private long gameId;
    @Id
    private long channelId;
    private int notificationSetting;
}
