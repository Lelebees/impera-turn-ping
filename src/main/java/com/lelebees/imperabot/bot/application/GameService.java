package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.GameRepository;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.game.exception.GameNotFoundException;
import com.lelebees.imperabot.bot.presentation.game.GameDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
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

    private Game findGameByID(long ID) throws GameNotFoundException {
        Optional<Game> gameOptional = gameRepository.findById(ID);
        return gameOptional.orElseThrow(() -> new GameNotFoundException("Could not find game: " + ID));
    }

    public GameDTO createGame(long ID, int turn) {
        return GameDTO.from(gameRepository.save(new Game(ID, turn - 1)));
    }

    public GameDTO createGame(ImperaGameViewDTO game) {
        return createGame(game.id(), game.turnCounter());
    }

    public List<GameDTO> getAllGames() {
        return GameDTO.from(gameRepository.findAll());
    }

    public GameDTO setHalfTimeNoticeForGame(long gameId) throws GameNotFoundException {
        Game game = findGameByID(gameId);
        game.setHalfTimeNoticeTrue();
        return GameDTO.from(gameRepository.save(game));
    }

    public GameDTO changeTurn(long gameId, int newTurn) throws GameNotFoundException {
        Game game = findGameByID(gameId);
        game.setCurrentTurn(newTurn);
        return GameDTO.from(gameRepository.save(game));
    }

    public boolean gameExists(long gameId) {
        return gameRepository.existsById(gameId);
    }

    public void deleteGame(long id) {
        gameLinkService.deleteLinksForGame(id);
        gameRepository.deleteById(id);
    }
}
