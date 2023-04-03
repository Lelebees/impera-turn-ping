package com.lelebees.imperabot.data;

import com.lelebees.imperabot.domain.bot.game.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Integer> {
}
