package com.lelebees.imperabot.core.application.dto;

import com.lelebees.imperabot.core.domain.Channel;
import discord4j.common.util.Snowflake;

import java.util.Set;
import java.util.stream.Collectors;

public record ChannelDTO(long id) {
    public static Set<ChannelDTO> From(Set<Channel> trackingChannels) {
        return trackingChannels.stream().map(ChannelDTO::From).collect(Collectors.toSet());
    }

    public static ChannelDTO From(Channel channel) {
        return new ChannelDTO(channel.getId());
    }

    public Snowflake idAsSnowflake() {
        return Snowflake.of(id());
    }
}
