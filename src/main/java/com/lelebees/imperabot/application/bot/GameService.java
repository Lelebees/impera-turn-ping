package com.lelebees.imperabot.application.bot;

import com.lelebees.imperabot.data.GameRepository;
import com.lelebees.imperabot.domain.bot.game.Game;
import com.lelebees.imperabot.domain.bot.game.GameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GameService {
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game findGameByID(int ID) {
        Optional<Game> gameOptional = gameRepository.findById(ID);
        if (gameOptional.isPresent()) {
            return gameOptional.get();
        }
        throw new GameNotFoundException("Could not find game: " + ID);
    }
}
