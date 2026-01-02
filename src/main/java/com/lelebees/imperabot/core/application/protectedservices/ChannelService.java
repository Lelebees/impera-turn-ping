package com.lelebees.imperabot.core.application.protectedservices;

import com.lelebees.imperabot.core.application.exception.ChannelNotFoundException;
import com.lelebees.imperabot.core.data.ChannelRepository;
import com.lelebees.imperabot.core.domain.Channel;
import com.lelebees.imperabot.core.domain.Game;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
// Service is package/private because it returns Entities, which may not leave the application layer.
class ChannelService {
    private final ChannelRepository repository;

    public ChannelService(ChannelRepository repository) {
        this.repository = repository;
    }

    private Channel fromOptional(Optional<Channel> channelOptional) throws ChannelNotFoundException {
        return channelOptional.orElseThrow(() -> new ChannelNotFoundException("Could not find channel"));
    }

    Channel findChannel(long id) throws ChannelNotFoundException {
        return fromOptional(repository.findById(id));
    }

    Channel createChannel(long channelId) {
        return repository.save(Channel.From(channelId));
    }

    Channel trackGame(Game game, long channelId) {
        Channel channel;
        try {
            channel = findChannel(channelId);
        } catch (ChannelNotFoundException e) {
            channel = createChannel(channelId);
        }
        channel.trackGame(game);
        return repository.save(channel);
    }

    Channel stopTracking(Game game, long channelId) throws ChannelNotFoundException {
        Channel channel = findChannel(channelId);
        channel.stopTracking(game);
        return repository.save(channel);
    }

    void deleteChannel(long channelId) {
        repository.deleteById(channelId);
    }
}
