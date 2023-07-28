package com.lelebees.imperabot.bot.domain.gamechannellink;

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

    public GameChannelLink(long gameId, long channelId, int notificationSetting) {
        this.gameId = gameId;
        this.channelId = channelId;
        this.notificationSetting = notificationSetting;
    }

    protected GameChannelLink() {

    }

    public long getGameId() {
        return gameId;
    }

    public long getChannelId() {
        return channelId;
    }

    public int getNotificationSetting() {
        return notificationSetting;
    }
}
