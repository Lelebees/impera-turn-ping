package com.lelebees.imperabot.bot.domain.gamechannellink;

import com.lelebees.imperabot.bot.domain.NotificationSettings;
import jakarta.persistence.*;

@Entity
@Table(name = "bot_game_channel")
@IdClass(GameLinkId.class)
public class GameChannelLink {
    @Id
    private long gameId;
    @Id
    private long channelId;
    @Column(name = "notification_setting")
    public Integer notificationSetting;

    public GameChannelLink(long gameId, long channelId, NotificationSettings notificationSetting) {
        this.gameId = gameId;
        this.channelId = channelId;
        this.notificationSetting = (notificationSetting == null ? null : notificationSetting.ordinal());
    }

    protected GameChannelLink() {

    }

    public long getGameId() {
        return gameId;
    }

    public long getChannelId() {
        return channelId;
    }

    public GameLinkId getGameLinkId() {
        return new GameLinkId(this.gameId, this.channelId);
    }
}
