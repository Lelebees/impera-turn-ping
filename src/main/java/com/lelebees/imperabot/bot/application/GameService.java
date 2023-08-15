package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.GameRepository;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.game.exception.GameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public Game findGameByID(long ID) {
        Optional<Game> gameOptional = gameRepository.findById(ID);
        return gameOptional.orElseThrow(() -> new GameNotFoundException("Could not find game: " + ID));
    }

    public Game createGame(long ID) {
        return gameRepository.save(new Game(ID));
    }

    public Game findOrCreateGame(long ID) {
        Game game;
        try {
            game = findGameByID(ID);
        } catch (GameNotFoundException e) {
            game = createGame(ID);
        }
        return game;
    }

    public List<Game> findAllGames() {
        return gameRepository.findAll();
    }

    public Game setHalfTimeNoticeForGame(long gameId) {
        Game game = findGameByID(gameId);
        game.halfTimeNotice = true;
        return gameRepository.save(game);
    }
}
