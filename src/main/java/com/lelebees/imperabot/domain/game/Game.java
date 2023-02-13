package com.lelebees.imperabot.domain.game;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Game {
    @Id
    private int game_id;
    public int last_turn;
    public boolean half_time_notice;

}
