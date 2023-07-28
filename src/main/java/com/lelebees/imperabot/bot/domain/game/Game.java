package com.lelebees.imperabot.bot.domain.game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bot_game")
public class Game {
    @Id
    @Column(name = "game_id")
    private long id;
    @Column(name = "current_turn")
    public int currentTurn;
    @Column(name = "half_time_notice")
    public boolean halfTimeNotice;

    public Game(long id) {
        this(id, 0, false);
    }

    public Game(long id, int currentTurn, boolean halfTimeNotice) {
        this.id = id;
        this.currentTurn = currentTurn;
        this.halfTimeNotice = halfTimeNotice;
    }

    protected Game() {

    }

    public long getId() {
        return id;
    }
}
