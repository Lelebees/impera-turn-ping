package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.GameChannelLinkRepository;
import com.lelebees.imperabot.bot.domain.NotificationSettings;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameLinkId;
import com.lelebees.imperabot.bot.domain.gamechannellink.exception.GameChannelLinkNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GameLinkService {
    private final GameChannelLinkRepository repository;

    public GameLinkService(GameChannelLinkRepository repository) {
        this.repository = repository;
    }

    public GameChannelLink saveLink(GameChannelLink gameChannelLink) {
        return repository.save(gameChannelLink);
    }

    public GameChannelLink findLink(GameLinkId id) throws GameChannelLinkNotFoundException {
        try {
            return getFromOptional(repository.findById(id));
        } catch (GameChannelLinkNotFoundException e){
            throw new GameChannelLinkNotFoundException("Could not find game channel link with id: " + id);
        }
    }

    public GameChannelLink createLink(long gameId, long channelId, NotificationSettings notificationSetting) {
        return repository.save(new GameChannelLink(gameId, channelId, notificationSetting));
    }

    public GameChannelLink findOrCreateLink(long gameId, long channelId, NotificationSettings notificationSetting) {
        Optional<GameChannelLink> gameChannelLinkOptional = repository.findById(new GameLinkId(gameId, channelId));
        if (gameChannelLinkOptional.isEmpty()) {
            return createLink(gameId, channelId, notificationSetting);
        }
        GameChannelLink link = gameChannelLinkOptional.get();
        link.notificationSetting = (notificationSetting == null ? null : notificationSetting.ordinal());
        return link;
    }

    private GameChannelLink getFromOptional(Optional<GameChannelLink> optional) throws GameChannelLinkNotFoundException {
        return optional.orElseThrow(() -> new GameChannelLinkNotFoundException("Could not find the link"));
    }

    public List<GameChannelLink> findLinksByChannel(long channelId) {
        return repository.findGameChannelLinkByChannelId(channelId);
    }

    public List<GameChannelLink> findLinksByGame(long gameId) {
        return repository.findGameChannelLinkByGameId(gameId);
    }

    public boolean linkExists(long gameId, long channelId) {
        return repository.existsById(new GameLinkId(gameId, channelId));
    }

    public void deleteLink(long gameId, long channelId) throws GameChannelLinkNotFoundException {
        GameChannelLink gameChannelLink = findLink(new GameLinkId(gameId, channelId));
        repository.delete(gameChannelLink);
    }

    public void deleteLinksForGame(long gameId) {
        repository.deleteGameChannelLinksByGameId(gameId);
    }

    public void deleteLinksForChannel(long channelId) {
        repository.deleteGameChannelLinksByChannelId(channelId);
    }
}
