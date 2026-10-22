package com.lelebees.imperabot.core.application.dto;

import com.lelebees.imperabot.core.domain.Channel;
import discord4j.common.util.Snowflake;

import java.util.Set;
import java.util.stream.Collectors;

public record ChannelDTO(Snowflake id) {
    public static Set<ChannelDTO> From(Set<Channel> channels) {
        return channels.stream().map(ChannelDTO::From).collect(Collectors.toSet());
    }

    public static ChannelDTO From(Channel channel) {
        return new ChannelDTO(Snowflake.of(channel.getId()));
    }

    public long idAsLong() {
        return id.asLong();
    }
}
