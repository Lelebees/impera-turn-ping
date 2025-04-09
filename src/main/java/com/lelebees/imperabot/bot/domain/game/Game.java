package com.lelebees.imperabot.bot.domain.game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bot_game")
public class Game {
    @Column(name = "current_turn")
    private int currentTurn;
    @Column(name = "half_time_notice")
    private boolean halfTimeNotice;
    @Id
    @Column(name = "game_id")
    private long id;

    public Game(long id) {
        this(id, 0, false);
    }

    public Game(long id, int currentTurn, boolean halfTimeNotice) {
        this.id = id;
        this.currentTurn = currentTurn;
        this.halfTimeNotice = halfTimeNotice;
    }

    public Game(long id, int currentTurn) {
        this(id, currentTurn, false);
    }

    protected Game() {

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
    public void setCurrentTurn(int newTurn) {
        this.currentTurn = newTurn;
        this.halfTimeNotice = false;
    }


    /**
     * Sets the half-time tracker to true, indicating that the current player has been notified that they have half-time remaining.
     *
     * @see #setCurrentTurn(int newTurn)
     */
    public void setHalfTimeNoticeTrue() {
        this.halfTimeNotice = true;
    }
}
