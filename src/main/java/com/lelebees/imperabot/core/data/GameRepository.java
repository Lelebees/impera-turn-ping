package com.lelebees.imperabot.core.data;

import com.lelebees.imperabot.core.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
