package com.lelebees.imperabot.bot.presentation.game;

import com.lelebees.imperabot.bot.domain.game.Channel;

import java.util.Set;
import java.util.stream.Collectors;

public record ChannelDTO(long id) {
    public static Set<ChannelDTO> From(Set<Channel> trackingChannels) {
        return trackingChannels.stream().map(ChannelDTO::From).collect(Collectors.toSet());
    }

    public static ChannelDTO From(Channel channel) {
        return new ChannelDTO(channel.getId());
    }
}
