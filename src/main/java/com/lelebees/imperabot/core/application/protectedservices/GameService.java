package com.lelebees.imperabot.core.application.protectedservices;

import com.lelebees.imperabot.core.application.dto.GameDTO;
import com.lelebees.imperabot.core.application.exception.ChannelNotFoundException;
import com.lelebees.imperabot.core.application.exception.GameNotFoundException;
import com.lelebees.imperabot.core.data.GameRepository;
import com.lelebees.imperabot.core.domain.Channel;
import com.lelebees.imperabot.core.domain.Game;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final ChannelService channelService;

    public GameService(GameRepository gameRepository, ChannelService channelService) {
        this.gameRepository = gameRepository;
        this.channelService = channelService;
    }

    private Game findGame(long ID) throws GameNotFoundException {
        Optional<Game> gameOptional = gameRepository.findById(ID);
        return gameOptional.orElseThrow(() -> new GameNotFoundException("Could not find game: " + ID));
    }

    public List<GameDTO> getAllGames() {
        return GameDTO.from(gameRepository.findAll());
    }

    public GameDTO setHalfTimeNoticeForGame(long gameId) throws GameNotFoundException {
        Game game = findGame(gameId);
        game.setHalfTimeNoticeTrue();
        return GameDTO.from(gameRepository.save(game));
    }

    public GameDTO changeTurn(long gameId, int newTurn) throws GameNotFoundException {
        Game game = findGame(gameId);
        game.setCurrentTurn(newTurn);
        return GameDTO.from(gameRepository.save(game));
    }

    public void deleteGame(long id) {
        gameRepository.deleteById(id);
    }

    @Transactional
    public boolean trackGame(ImperaGameViewDTO imperaGame, long channelId) {
        Game game;
        try {
            game = findGame(imperaGame.id());
        } catch (GameNotFoundException e) {
            game = Game.From(imperaGame.id(), imperaGame.turnCounter());
        }
        boolean alreadyTracked = !game.trackInChannel(channelService.trackGame(game, channelId));
        gameRepository.save(game);
        return alreadyTracked;
    }

    @Transactional
    public boolean untrackGame(long gameId, long channelId) throws GameNotFoundException, ChannelNotFoundException {
        Game game = findGame(gameId);
        boolean wasBeingTracked = game.untrackInChannel(channelService.stopTracking(game, channelId));
        gameRepository.save(game);
        return wasBeingTracked;
    }

    @Transactional
    public void deleteLinksForChannel(long channelId) {
        Channel channel;
        try {
            channel = channelService.findChannel(channelId);
        } catch (ChannelNotFoundException e) {
            return;
        }
        for (Game game : channel.getTrackedGames()) {
            game.untrackInChannel(channel);
            gameRepository.save(game);
        }
        channelService.deleteChannel(channelId);
    }
}
