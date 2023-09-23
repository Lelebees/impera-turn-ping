package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.GameRepository;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.game.exception.GameNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GameService {
    private final GameRepository gameRepository;
    private final GameLinkService gameLinkService;

    public GameService(GameRepository gameRepository, GameLinkService gameLinkService) {
        this.gameRepository = gameRepository;
        this.gameLinkService = gameLinkService;
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

    public Game turnChanged(long gameId, int newTurn) {
        Game game = findGameByID(gameId);
        game.currentTurn = newTurn;
        game.halfTimeNotice = false;
        return gameRepository.save(game);
    }

    public boolean gameExists(long gameId) {
        return gameRepository.existsById(gameId);
    }

    public void deleteGame(long id) {
        gameLinkService.deleteLinksForGame(id);
        gameRepository.deleteById(id);
    }
}
