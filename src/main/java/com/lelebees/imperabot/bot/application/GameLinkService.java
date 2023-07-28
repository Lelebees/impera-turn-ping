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

    public GameChannelLink createLink(long gameId, long channelId, int notificationSetting) {
        return repository.save(new GameChannelLink(gameId, channelId, notificationSetting));
    }

    private GameChannelLink getFromOptional(Optional<GameChannelLink> optional) {
        return optional.orElseThrow(() -> new GameChannelLinkNotFoundException("Could not find the link"));
    }

}
