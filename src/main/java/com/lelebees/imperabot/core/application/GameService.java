package com.lelebees.imperabot.core.application;

import com.lelebees.imperabot.core.application.dto.GameDTO;
import com.lelebees.imperabot.core.application.exception.ChannelNotFoundException;
import com.lelebees.imperabot.core.application.exception.GameNotFoundException;
import com.lelebees.imperabot.core.data.GameRepository;
import com.lelebees.imperabot.core.domain.Channel;
import com.lelebees.imperabot.core.domain.Game;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.application.exception.ImperaGameNotFoundException;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGamePlayerDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.history.HistoryActionName;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class GameService {
    private final Logger logger = LoggerFactory.getLogger(GameService.class);
    private final GameRepository repository;
    private final ChannelService channelService;
    private final DiscordService discordService;
    private final ImperaService imperaService;

    public GameService(GameRepository repository, ChannelService channelService, DiscordService discordService, ImperaService imperaService) {
        this.repository = repository;
        this.channelService = channelService;
        this.discordService = discordService;
        this.imperaService = imperaService;
    }

    private Game findGame(long ID) throws GameNotFoundException {
        Optional<Game> gameOptional = repository.findById(ID);
        return gameOptional.orElseThrow(() -> new GameNotFoundException("Could not find game: " + ID));
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
        repository.save(game);
        return alreadyTracked;
    }

    @Transactional
    public boolean untrackGame(long gameId, long channelId) throws GameNotFoundException, ChannelNotFoundException {
        Game game = findGame(gameId);
        boolean wasBeingTracked = game.untrackInChannel(channelService.stopTracking(game, channelId));
        if (!game.isBeingTracked()) {
            repository.delete(game);
        } else {
            repository.save(game);
        }
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
            repository.save(game);
        }
    }

    public void checkTurns() {
        List<Game> games = repository.findAll();
        logger.info("Checking turns for {} games.", games.size());
        int skippedGames = 0;
        int handledGames = 0;
        for (Game game : games) {
            if (!notifyPlayersFor(game)) {
                skippedGames++;
                continue;
            }
            handledGames++;
        }
        logger.info("Handled {} games, skipped {} games.", handledGames, skippedGames);
    }


    private boolean notifyPlayersFor(Game game) {
        ImperaGameViewDTO imperaGame;
        try {
            imperaGame = imperaService.getGame(game.getId());
        } catch (ImperaGameNotFoundException e) {
            logger.error("Game [{}] could not be found on the Impera server. Skipping and deleting game.", game.getId(), e);
            repository.delete(game);
            return false;
        }
        if (!imperaGame.hasStarted()) {
            return false;
        }
        boolean turnHasChanged = game.getCurrentTurn() != imperaGame.turnCounter();
        boolean shouldSendHalfTimeNotice = imperaGame.hasHalfOfTurnPassed() && !game.sentHalfTimeNotice();
        // If the turn hasn't changed, and we don't need to send a half-time notice, and the game hasn't ended,
        if (!turnHasChanged && !shouldSendHalfTimeNotice && !imperaGame.hasEnded()) {
            return false;
        }

        List<discord4j.core.object.entity.channel.Channel> channels = getChannelsToNotify(game);
        logger.debug("Found {} channels to notify.", channels.size());

        HashMap<String, HistoryActionName> playersThatAreNoLongerPlaying = imperaService.getPlayersThatAreNoLongerPlaying(game.getId(), game.getCurrentTurn(), imperaGame.turnCounter() + 1);
        logger.debug("Found {} players that are no longer playing.", playersThatAreNoLongerPlaying.size());
        playersThatAreNoLongerPlaying.forEach((player, outcome) -> discordService.sendLoserMessage(channels, imperaGame.findPlayerById(player), imperaGame, outcome));

        // There is probably a better way to do this, but I'm not sure what it is.
        if (imperaGame.hasEnded()) {
            logger.debug("Game {} has ended!", game.getId());
            sendVictoryNotice(game, imperaGame, channels);
        } else if (turnHasChanged) {
            notifyNextUser(game, imperaGame, channels);
        } else {
            sendHalfTimeNotice(game, imperaGame, channels);
        }
        return true;
    }

    private void sendVictoryNotice(Game game, ImperaGameViewDTO imperaGame, List<discord4j.core.object.entity.channel.Channel> channels) {
        logger.info("Sending victory notice for {} ({})!", imperaGame.name(), imperaGame.id());
        List<ImperaGamePlayerDTO> winningPlayers = imperaGame.getWinningPlayers();
        // Send a message to all channels that are tracking this game, who won
        discordService.sendVictorsMessage(channels, winningPlayers, imperaGame);
        winningPlayers.forEach(winner -> discordService.giveWinnerRole(GameDTO.from(game), winner));
        repository.delete(game);
    }

    private void notifyNextUser(Game game, ImperaGameViewDTO imperaGame, List<discord4j.core.object.entity.channel.Channel> channels) {
        logger.info("Sending turn notice for {} ({})!", imperaGame.name(), imperaGame.id());
        discordService.sendNewTurnMessage(channels, imperaGame);
        game.setCurrentTurn(imperaGame.turnCounter());
        repository.save(game);
    }

    private void sendHalfTimeNotice(Game game, ImperaGameViewDTO imperaGame, List<discord4j.core.object.entity.channel.Channel> channels) {
        logger.info("Sending half time notice for {} ({})!", imperaGame.name(), imperaGame.id());
        discordService.sendHalfTimeMessage(channels, imperaGame);
        game.setHalfTimeNoticeTrue();
        repository.save(game);
    }

    private List<discord4j.core.object.entity.channel.Channel> getChannelsToNotify(Game game) {
        return game.getTrackingChannels()
                .stream()
                .map(Channel::getId)
                .map(discordService::getChannelById)
                .toList();
    }
}
