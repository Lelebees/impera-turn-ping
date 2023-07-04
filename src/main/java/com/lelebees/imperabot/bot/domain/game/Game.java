package com.lelebees.imperabot.bot.domain.game;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bot_game")
public class Game {
    @Column(name = "current_turn")
    public int currentTurn;
    @Column(name = "half_time_notice")
    public boolean halfTimeNotice;
    @Id
    @Column(name = "game_id")
    private long id;

}
