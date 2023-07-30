package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.GameChannelLinkRepository;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameLinkId;
import com.lelebees.imperabot.bot.domain.gamechannellink.exception.GameChannelLinkNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GameLinkService {
    private final GameChannelLinkRepository repository;

    public GameLinkService(GameChannelLinkRepository repository) {
        this.repository = repository;
    }

    public GameChannelLink saveLink(GameChannelLink gameChannelLink) {
        return repository.save(gameChannelLink);
    }

    public GameChannelLink findLink(GameLinkId id) {
        return getFromOptional(repository.findById(id));
    }

    public GameChannelLink createLink(long gameId, long channelId, Integer notificationSetting) {
        return repository.save(new GameChannelLink(gameId, channelId, notificationSetting));
    }

    public GameChannelLink findOrCreateLink(long gameId, long channelId, Integer notificationSetting) {
        Optional<GameChannelLink> gameChannelLinkOptional = repository.findById(new GameLinkId(gameId, channelId));
        if (gameChannelLinkOptional.isEmpty()) {
            return createLink(gameId, channelId, notificationSetting);
        }
        GameChannelLink link = gameChannelLinkOptional.get();
        link.notificationSetting = notificationSetting;
        return link;
    }

    private GameChannelLink getFromOptional(Optional<GameChannelLink> optional) {
        return optional.orElseThrow(() -> new GameChannelLinkNotFoundException("Could not find the link"));
    }

}
