package com.lelebees.imperabot.discord.application;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.Channel;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DiscordService {
    private final GatewayDiscordClient gatewayClient;

    public DiscordService(GatewayDiscordClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }


    public void sendMessage(long channelId, boolean halfTimeNotice, String username) {
        Channel channel = gatewayClient.getChannelById(Snowflake.of(channelId)).block();
        if (halfTimeNotice) {
            channel.getRestChannel().createMessage(username + ", you have half time remaining!").block();
        } else {
            channel.getRestChannel().createMessage(username + ", it is your turn!").block();
        }
    }

    public Channel getChannelById(long channelId) {
        return gatewayClient.getChannelById(Snowflake.of(channelId)).block();
    }

    public boolean isMe(long userId) {
        return gatewayClient.getSelfId().equals(Snowflake.of(userId));
    }

    public boolean channelIsDM(long channelId) {
        return getChannelById(channelId).getType() == Channel.Type.DM;
    }

    public boolean channelIsGuildChannel(long channelId) {
        Set<Channel.Type> guildChannelTypes = Set.of(Channel.Type.GUILD_TEXT, Channel.Type.GUILD_NEWS);
        return guildChannelTypes.contains(getChannelById(channelId).getType());
    }
}
