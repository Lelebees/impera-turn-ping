package com.lelebees.imperabot.bot.domain.gamechannellink;

import java.io.Serializable;
import java.util.Objects;

public class GameLinkId implements Serializable {
    private final long gameId;
    private final long channelId;

    public GameLinkId(long gameId, long channelId) {
        this.gameId = gameId;
        this.channelId = channelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameLinkId that = (GameLinkId) o;
        return gameId == that.gameId && channelId == that.channelId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId, channelId);
    }
}
