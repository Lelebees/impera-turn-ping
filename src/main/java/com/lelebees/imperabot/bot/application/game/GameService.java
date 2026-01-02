package com.lelebees.imperabot.bot.application.game;

import com.lelebees.imperabot.bot.application.game.exception.ChannelNotFoundException;
import com.lelebees.imperabot.bot.application.game.exception.GameNotFoundException;
import com.lelebees.imperabot.bot.data.ChannelRepository;
import com.lelebees.imperabot.bot.data.GameRepository;
import com.lelebees.imperabot.bot.domain.game.Channel;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.presentation.game.GameDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    private final GameRepository gameRepository;
    private final ChannelRepository channelRepository;

    public GameService(GameRepository gameRepository, ChannelRepository channelRepository) {
        this.gameRepository = gameRepository;
        this.channelRepository = channelRepository;
    }

    private Game findGame(long ID) throws GameNotFoundException {
        Optional<Game> gameOptional = gameRepository.findById(ID);
        return gameOptional.orElseThrow(() -> new GameNotFoundException("Could not find game: " + ID));
    }

    private Channel findChannel(long id) throws ChannelNotFoundException {
        Optional<Channel> channelOptional = channelRepository.findById(id);
        return channelOptional.orElseThrow(() -> new ChannelNotFoundException("Could not find channel: " + id));
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
        Channel channel;
        try {
            channel = findChannel(channelId);
        } catch (ChannelNotFoundException e) {
            channel = Channel.From(channelId);
        }

        boolean alreadyTracked = !game.trackInChannel(channel);
        boolean alreadyTracking = !channel.trackGame(game);
        gameRepository.save(game);
        channelRepository.save(channel);
        return alreadyTracked && alreadyTracking;
    }

    @Transactional
    public boolean untrackGame(long gameId, long channelId) throws GameNotFoundException, ChannelNotFoundException {
        Game game = findGame(gameId);
        Channel channel = findChannel(channelId);
        boolean wasBeingTracked = game.untrackInChannel(channel);
        boolean wasTracking = channel.stopTracking(game);
        gameRepository.save(game);
        channelRepository.save(channel);
        return wasBeingTracked && wasTracking;
    }

    @Transactional
    public void deleteLinksForChannel(long channelId) {
        Channel channel;
        try {
            channel = findChannel(channelId);
        } catch (ChannelNotFoundException e) {
            return;
        }
        for (Game game : channel.getTrackedGames()) {
            game.untrackInChannel(channel);
            gameRepository.save(game);
        }
        channelRepository.deleteById(channelId);
    }
}
