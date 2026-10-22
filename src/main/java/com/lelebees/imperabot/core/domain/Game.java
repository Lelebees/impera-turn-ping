package com.lelebees.imperabot.core.domain;

import com.lelebees.imperabot.core.domain.exception.TurnAlreadyPassedException;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import jakarta.persistence.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.lelebees.imperabot.core.domain.CourseOfAction.*;

@Entity
@Table(name = "bot_game")
public class Game {
    @Id
    @Column(name = "game_id")
    private long id;
    @Column(name = "current_turn")
    private int currentTurn;
    @Column(name = "half_time_notice")
    private boolean halfTimeNotice;
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "bot_game_channel",
            joinColumns = {@JoinColumn(name = "game_id")},
            inverseJoinColumns = {@JoinColumn(name = "channel_id")}
    )
    private Set<Channel> trackingChannels;

    public Game(long id, int currentTurn, boolean halfTimeNotice, Set<Channel> trackingChannels) {
        this.id = id;
        this.currentTurn = currentTurn;
        this.halfTimeNotice = halfTimeNotice;
        this.trackingChannels = trackingChannels;
    }

    protected Game() {

    }

    public static Game From(long id, int currentTurn) {
        return new Game(id, currentTurn, false, new HashSet<>());
    }

    public long getId() {
        return id;
    }

    public boolean sentHalfTimeNotice() {
        return halfTimeNotice;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    /**
     * Sets the {@link Game}'s current turn indicator to the given turn, and resets the half-time tracker to false.
     *
     * @param newTurn The turn the {@link Game} will now be on.
     */
    public void updateGameStatus(int newTurn) {
        if (newTurn <= currentTurn) {
            throw new TurnAlreadyPassedException("New turn (#" + newTurn + ") cannot be before or the same as the current turn (#" + currentTurn + ").");
        }
        this.currentTurn = newTurn;
        this.halfTimeNotice = false;
    }

    public void setHalfTimeNoticeTrue() {
        this.halfTimeNotice = true;
    }

    public boolean trackInChannel(Channel channel) {
        return trackingChannels.add(channel);
    }

    public Set<Channel> getTrackingChannels() {
        return Collections.unmodifiableSet(trackingChannels);
    }

    public boolean isBeingTracked() {
        return !trackingChannels.isEmpty();
    }

    public boolean untrackInChannel(Channel channel) {
        return trackingChannels.remove(channel);
    }

    public CourseOfAction getCourseOfAction(ImperaGameViewDTO imperaGame) {
        // REMINDER: The order of if statements matters!
        if (!imperaGame.hasStarted()) return SKIP_CHECK;
        if (imperaGame.hasEnded()) return DECLARE_VICTOR;
        if (currentTurn != imperaGame.turnCounter()) return NOTIFY_NEXT_PLAYER;
        if (imperaGame.hasHalfOfTurnPassed() && !halfTimeNotice) return NOTIFY_HALF_TIME_PASSED;
        return SKIP_CHECK;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return id == game.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
