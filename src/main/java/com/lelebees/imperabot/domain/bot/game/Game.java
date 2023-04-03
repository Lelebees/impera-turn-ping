package com.lelebees.imperabot.domain.bot.game;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Game {
    @Id
    private int game_id;
    public int current_turn;
    public boolean half_time_notice;

}
