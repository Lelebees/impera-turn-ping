package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.GameRepository;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.game.GameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GameService {
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game findGameByID(long ID) {
        Optional<Game> gameOptional = gameRepository.findById(ID);
        if (gameOptional.isPresent()) {
            return gameOptional.get();
        }
        throw new GameNotFoundException("Could not find game: " + ID);
    }
}
