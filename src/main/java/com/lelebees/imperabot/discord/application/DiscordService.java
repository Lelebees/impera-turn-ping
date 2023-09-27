package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.domain.user.BotUser;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class DiscordService {
    private final GatewayDiscordClient gatewayClient;

    public DiscordService(GatewayDiscordClient gatewayClient) {
        this.gatewayClient = gatewayClient;
    }

    public void sendNewTurnMessage(long channelId, String username, long gameid, String name) {
        getChannelById(channelId).getRestChannel()
                .createMessage(username + ", it is your turn in [" + name + "](https://imperaonline.de/game/play/" + gameid + ")!")
                .block();
    }

    public void sendHalfTimeMessage(long channelId, String username, long gameid, String name) {
        getChannelById(channelId).getRestChannel()
                .createMessage(username + ", you have half time remaining in [" + name + "](https://imperaonline.de/game/play/" + gameid + ")!")
                .block();
    }

    public void sendVictorMessage(Long channel, long id, String gameName, String playerName) {
        getChannelById(channel).getRestChannel()
                .createMessage("Game [" + gameName + "](https://imperaonline.de/game/play/" + id + ") has ended! " + playerName + " has won!")
                .block();
    }

    public boolean channelIsDM(long channelId) {
        return getChannelById(channelId).getType() == Channel.Type.DM;
    }

    public boolean channelIsGuildChannel(long channelId) {
        Set<Channel.Type> guildChannelTypes = Set.of(Channel.Type.GUILD_TEXT, Channel.Type.GUILD_NEWS, Channel.Type.GUILD_PUBLIC_THREAD, Channel.Type.GUILD_PRIVATE_THREAD);
        return guildChannelTypes.contains(getChannelById(channelId).getType());
    }

    public long getChannelOwner(long channelId) {
        Channel channel = getChannelById(channelId);
        if (channel.getClass() != PrivateChannel.class || channel.getType() != Channel.Type.DM) {
            throw new NullPointerException("Non-Private channel has no owner!");
        }
        PrivateChannel dmChannel = (PrivateChannel) channel;
        Set<Snowflake> recipients = dmChannel.getRecipientIds();
        for (Snowflake recipient : recipients) {
            if (!isMe(recipient.asLong())) {
                return recipient.asLong();
            }
        }
        throw new IllegalStateException("There is no one but me!");
    }

    public long getGuildChannelGuild(long channelId) {
        Channel channel = getChannelById(channelId);
        if (!(channel instanceof GuildMessageChannel guildChannel)) {
            throw new NullPointerException("Non-Guild channel has no guild!");
        }
        return guildChannel.getGuildId().asLong();
    }

    public boolean botUserCanAccessChannel(long channelId, BotUser user) {
        Channel channel = getChannelById(channelId);
        return true;
    }

    public PrivateChannel getDMChannelByOwner(long userId) {
        User user = gatewayClient.getUserById(Snowflake.of(userId)).block();
        return user.getPrivateChannel().block();
    }

    private boolean isMe(long userId) {
        return gatewayClient.getSelfId().equals(Snowflake.of(userId));
    }

    private Channel getChannelById(long channelId) {
        return gatewayClient.getChannelById(Snowflake.of(channelId)).block();
    }

}
