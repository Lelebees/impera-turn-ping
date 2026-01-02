package com.lelebees.imperabot.core.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "bot_channel")
public class Channel {
    @Id
    private long id;

    @ManyToMany(mappedBy = "trackingChannels")
    private Set<Game> trackedGames;

    public Channel(long id, Set<Game> trackedGames) {
        this.id = id;
        this.trackedGames = trackedGames;
    }

    protected Channel() {
    }

    public static Channel From(long channelId) {
        return new Channel(channelId, new HashSet<>());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Channel channel = (Channel) o;
        return id == channel.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public boolean trackGame(Game game) {
        return trackedGames.add(game);
    }

    public boolean stopTracking(Game game) {
        return trackedGames.remove(game);
    }

    public long getId() {
        return id;
    }

    public Set<Game> getTrackedGames() {
        return Collections.unmodifiableSet(trackedGames);
    }
}
