package com.lelebees.imperabot.bot.data;

import com.lelebees.imperabot.bot.domain.game.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
