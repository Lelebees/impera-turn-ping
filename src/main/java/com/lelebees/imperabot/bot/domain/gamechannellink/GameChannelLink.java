package com.lelebees.imperabot.bot.domain.gamechannellink;

import jakarta.persistence.*;

@Entity
@Table(name = "bot_game_channel")
@IdClass(GameLinkId.class)
public class GameChannelLink {
    @Id
    private long gameId;
    @Id
    private long channelId;
    @Column(nullable = true, name = "notification_setting")
    public Integer notificationSetting;

    public GameChannelLink(long gameId, long channelId, Integer notificationSetting) {
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
}
