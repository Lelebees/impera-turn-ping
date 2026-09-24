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
import com.lelebees.imperabot.user.application.UserService;
import com.lelebees.imperabot.user.application.dto.BotUserDTO;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.lelebees.imperabot.core.application.CourseOfAction.*;

@Service
public class GameService {
    private final Logger logger = LoggerFactory.getLogger(GameService.class);
    private final GameRepository repository;
    private final ChannelService channelService;
    private final DiscordService discordService;
    private final ImperaService imperaService;
    private final NotificationService notificationService;
    private final UserService userService;

    public GameService(GameRepository repository, ChannelService channelService, DiscordService discordService, ImperaService imperaService, NotificationService notificationService, UserService userService) {
        this.repository = repository;
        this.channelService = channelService;
        this.discordService = discordService;
        this.imperaService = imperaService;
        this.notificationService = notificationService;
        this.userService = userService;
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
            repository.save(game);
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
        int handledGames = (int) games.stream()
                .map(this::notifyPlayersFor)
                .filter(action -> action != SKIP_CHECK)
                .count();
        logger.info("Handled {} games, skipped {} games.", handledGames, games.size() - handledGames);
    }


    public CourseOfAction notifyPlayersFor(Game game) {
        ImperaGameViewDTO imperaGame;
        try {
            imperaGame = imperaService.getGame(game.getId());
        } catch (ImperaGameNotFoundException e) {
            logger.error("Game [{}] could not be found on the Impera server. Skipping and deleting game.", game.getId(), e);
            repository.delete(game);
            return SKIP_CHECK;
        }
        CourseOfAction courseOfAction = decideAction(game, imperaGame);
        if (courseOfAction == SKIP_CHECK) return SKIP_CHECK;
        List<PrivateChannel> dmChannels = new ArrayList<>();
        List<GuildMessageChannel> guildChannels = new ArrayList<>();
        getChannelsToNotify(game, dmChannels, guildChannels);
        logger.debug("Found {} channels to notify.", dmChannels.size() + guildChannels.size());

        HashMap<String, HistoryActionName> playersThatAreNoLongerPlaying = imperaService.getPlayersThatAreNoLongerPlaying(game.getId(), game.getCurrentTurn(), imperaGame.turnCounter() + 1);
        logger.debug("Found {} players that are no longer playing.", playersThatAreNoLongerPlaying.size());
        playersThatAreNoLongerPlaying.forEach((player, outcome) -> notificationService.sendLoserMessage(guildChannels, dmChannels, imperaGame.findPlayerById(player), imperaGame, outcome));

        switch (courseOfAction) {
            case DECLARE_VICTOR -> sendVictoryNotice(game, imperaGame, guildChannels, dmChannels);
            case NOTIFY_NEXT_PLAYER -> notifyNextUser(game, imperaGame, guildChannels, dmChannels);
            case NOTIFY_HALF_TIME_PASSED -> sendHalfTimeNotice(game, imperaGame, guildChannels, dmChannels);
        }
        return courseOfAction;
    }

    public static CourseOfAction decideAction(Game game, ImperaGameViewDTO imperaGame) {
        // REMINDER: The order of if statements matters!
        if (!imperaGame.hasStarted()) return SKIP_CHECK;
        if (imperaGame.hasEnded()) return DECLARE_VICTOR;
        if (game.getCurrentTurn() != imperaGame.turnCounter()) return NOTIFY_NEXT_PLAYER;
        if (imperaGame.hasHalfOfTurnPassed() && !game.sentHalfTimeNotice()) return NOTIFY_HALF_TIME_PASSED;
        return SKIP_CHECK;
    }

    private void sendVictoryNotice(Game game, ImperaGameViewDTO imperaGame, List<GuildMessageChannel> guildChannels, List<PrivateChannel> dmChannels) {
        logger.debug("Game {} has ended!", game.getId());
        logger.info("Sending victory notice for {} ({})!", imperaGame.name(), imperaGame.id());
        List<ImperaGamePlayerDTO> winningPlayers = imperaGame.getWinningPlayers();
        notificationService.sendVictorsMessage(guildChannels, dmChannels, winningPlayers, imperaGame);
        for (ImperaGamePlayerDTO winner : winningPlayers) {
            Optional<BotUserDTO> userOptional = userService.findImperaUser(UUID.fromString(winner.userId()));
            if (userOptional.isEmpty()) continue;
            discordService.giveWinnerRole(GameDTO.from(game), userOptional.get());
        }
        repository.delete(game);
    }

    private void notifyNextUser(Game game, ImperaGameViewDTO imperaGame, List<GuildMessageChannel> guildChannels, List<PrivateChannel> dmChannels) {
        logger.info("Sending turn notice for {} ({})!", imperaGame.name(), imperaGame.id());
        notificationService.sendNewTurnMessage(guildChannels, dmChannels, imperaGame);
        game.updateGameStatus(imperaGame.turnCounter());
        repository.save(game);
    }

    private void sendHalfTimeNotice(Game game, ImperaGameViewDTO imperaGame, List<GuildMessageChannel> guildChannels, List<PrivateChannel> dmChannels) {
        logger.info("Sending half time notice for {} ({})!", imperaGame.name(), imperaGame.id());
        notificationService.sendHalfTimeMessage(guildChannels, dmChannels, imperaGame);
        game.setHalfTimeNoticeTrue();
        repository.save(game);
    }

    private void getChannelsToNotify(Game game, List<PrivateChannel> dmChannels, List<GuildMessageChannel> otherChannels) {
        List<discord4j.core.object.entity.channel.Channel> allChannels = game.getTrackingChannels()
                .stream()
                .map(Channel::getDiscordId)
                .map(discordService::getChannelById)
                .toList();
        dmChannels.addAll(
                allChannels.stream()
                        .filter(channel -> channel.getType() == discord4j.core.object.entity.channel.Channel.Type.DM)
                        .map(channel -> (PrivateChannel) channel)
                        .toList()
        );
        otherChannels.addAll(
                allChannels.stream()
                        .filter(channel -> channel.getType() != discord4j.core.object.entity.channel.Channel.Type.DM)
                        .map(channel -> (GuildMessageChannel) channel)
                        .toList()
        );
    }
}
