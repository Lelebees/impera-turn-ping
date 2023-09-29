package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
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

    public void sendHalfTimeMessage(Long channelId, String username, long gameId, String name) {
        getChannelById(channelId).getRestChannel()
                .createMessage(username + ", you have half time remaining in [" + name + "](https://imperaonline.de/game/play/" + gameId + ")!")
                .block();
    }

    public void sendVictorMessage(Long channel, String playerName, long gameId, String gameName) {
        getChannelById(channel).getRestChannel()
                .createMessage("Game [" + gameName + "](https://imperaonline.de/game/play/" + gameId + ") has ended! " + playerName + " has won!")
                .block();
    }

    public void sendDefeatedMessage(Long channel, String playerName, long gameId, String gameName) {
        getChannelById(channel).getRestChannel()
                .createMessage(playerName + " has been defeated in [" + gameName + "](https://imperaonline.de/game/play/" + gameId + ")!")
                .block();
    }

    public void sendSurrenderMessage(Long channel, String playerName, long gameId, String gameName) {
        getChannelById(channel).getRestChannel()
                .createMessage(playerName + " has surrendered in [" + gameName + "](https://imperaonline.de/game/play/" + gameId + ")!")
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

    public long getGameIdOption(ChatInputInteractionEvent event) {
        return event.getOptions()
                .get(0)
                .getOptions()
                .get(0)
                .getOption("gameid")
                .orElseThrow(() -> new NullPointerException("This is impossible, How could gameid not exist?!"))
                .getValue()
                .orElseThrow(() -> new NullPointerException("No gameid?!?!"))
                .asLong();
    }

    public Channel getChannelOption(ChatInputInteractionEvent event) {
        return event.getOptions()
                .get(0)
                .getOptions()
                .get(0)
                .getOption("channel")
                .orElseThrow(() -> new NullPointerException("This is impossible, How could channel not exist?!"))
                .getValue()
                .orElseThrow(() -> new NullPointerException("No channel?!?!"))
                .asChannel()
                .block();
    }

    public GuildNotificationSettings getGuildSettingOption(ChatInputInteractionEvent event) {
        return GuildNotificationSettings.get(getSettingOption(event));
    }

    public UserNotificationSetting getUserSettingOption(ChatInputInteractionEvent event) {
        return UserNotificationSetting.get(getSettingOption(event));
    }

    private int getSettingOption(ChatInputInteractionEvent event) {
        return Math.toIntExact(event.getOptions()
                .get(0)
                .getOptions()
                .get(0)
                .getOption("setting")
                .orElseThrow(() -> new NullPointerException("This is impossible, How could setting not exist?!"))
                .getValue()
                .orElseThrow(() -> new NullPointerException("No setting?!?!"))
                .asLong());
    }

    private boolean isMe(long userId) {
        return gatewayClient.getSelfId().equals(Snowflake.of(userId));
    }

    private Channel getChannelById(long channelId) {
        return gatewayClient.getChannelById(Snowflake.of(channelId)).block();
    }
}
