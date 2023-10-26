package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGamePlayerDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DiscordService {
    private final GatewayDiscordClient gatewayClient;
    private final UserService userService;
    private final static Logger logger = LoggerFactory.getLogger(DiscordService.class);

    public DiscordService(GatewayDiscordClient gatewayClient, UserService userService) {
        this.gatewayClient = gatewayClient;
        this.userService = userService;
    }

    public void sendNewTurnMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        Optional<BotUser> user = userService.findImperaUser(UUID.fromString(gamePlayer.userId));
        String userString = gamePlayer.name;
        if (user.isPresent()) {
            BotUser player = user.get();
            userString = player.getMention();
            switch (player.getNotificationSetting()) {
                case NO_NOTIFICATIONS -> userString = gamePlayer.name;
                case GUILD_ONLY -> {
                }
                case DMS_ONLY -> {
                    sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), "It's your turn in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
                    return;
                }
                case PREFER_GUILD_OVER_DMS -> {
                    if (channels.isEmpty()) {
                        sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), "It's your turn in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
                    }
                }
                case DMS_AND_GUILD ->
                        sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), "It's your turn in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
            }
        }
        for (Channel channel : channels) {
            sendMessage(channel.getId().asLong(), userString + ", it is your turn in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
        }
    }

    public void sendHalfTimeMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        Optional<BotUser> user = userService.findImperaUser(UUID.fromString(gamePlayer.userId));
        String userString = gamePlayer.name;
        if (user.isPresent()) {
            BotUser player = user.get();
            userString = player.getMention();
            switch (player.getNotificationSetting()) {
                case NO_NOTIFICATIONS -> userString = gamePlayer.name;
                case GUILD_ONLY -> {
                }
                case DMS_ONLY -> {
                    sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), "You have half time remaining in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
                    return;
                }
                case PREFER_GUILD_OVER_DMS -> {
                    if (channels.isEmpty()) {
                        sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), "You have half time remaining in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
                    }
                }
                case DMS_AND_GUILD ->
                        sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), "You have half time remaining in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
            }
        }
        for (Channel channel : channels) {
            sendMessage(channel.getId().asLong(), userString + ", you have half time remaining in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
        }
    }

    public void sendVictorMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        Optional<BotUser> user = userService.findImperaUser(UUID.fromString(gamePlayer.userId));
        String userString = gamePlayer.name;
        if (user.isPresent()) {
            BotUser player = user.get();
            userString = player.getMention();
            switch (player.getNotificationSetting()) {
                case NO_NOTIFICATIONS -> userString = gamePlayer.name;
                case GUILD_ONLY -> {
                }
                case DMS_ONLY -> {
                    sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), "Game [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ") has ended! You have won!");
                    return;
                }
                case PREFER_GUILD_OVER_DMS -> {
                    if (channels.isEmpty()) {
                        sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), "Game [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ") has ended! You have won!");
                    }
                }
                case DMS_AND_GUILD ->
                        sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), "Game [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ") has ended! You have won!");
            }
        }
        for (Channel channel : channels) {
            sendMessage(channel.getId().asLong(), "Game [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ") has ended! " + userString + " has won!");
        }
    }

    public void sendSurrenderMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        for (Channel channel : channels) {
            sendMessage(channel.getId().asLong(), gamePlayer.name + " has surrendered in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
        }
    }

    public void sendDefeatedMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        Optional<BotUser> user = userService.findImperaUser(UUID.fromString(gamePlayer.userId));
        String userString = gamePlayer.name;
        if (user.isPresent()) {
            BotUser player = user.get();
            userString = player.getMention();
            switch (player.getNotificationSetting()) {
                case NO_NOTIFICATIONS -> userString = gamePlayer.name;
                case GUILD_ONLY -> {
                }
                case DMS_ONLY -> {
                    sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), "You have been defeated in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
                    return;
                }
                case PREFER_GUILD_OVER_DMS -> {
                    if (channels.isEmpty()) {
                        sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), "You have been defeated in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
                    }
                }
                case DMS_AND_GUILD ->
                        sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), "You have been defeated in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
            }
        }
        for (Channel channel : channels) {
            sendMessage(channel.getId().asLong(), userString + " has been defeated in [" + game.name + "](https://imperaonline.de/game/play/" + game.id + ")!");
        }
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

    public PrivateChannel getDMChannelByOwner(long userId) {
        User user = gatewayClient.getUserById(Snowflake.of(userId)).block();
        return user.getPrivateChannel().block();
    }

    public Channel getChannelById(long channelId) {
        return gatewayClient.getChannelById(Snowflake.of(channelId)).block();
    }

    public Map<String, Long> getApplicationCommands() {
        Map<String, Long> commands = new HashMap<>();
        gatewayClient.getRestClient().getApplicationService().getGlobalApplicationCommands(gatewayClient.getSelfId().asLong())
                .collectList()
                .block()
                .forEach(command -> commands.put(command.name(), command.id().asLong()));
        logger.info("All application (slash) commands: " + commands);
        return commands;
    }

    private void sendMessage(long channelId, String message) {
        Channel channel = getChannelById(channelId);

        channel.getRestChannel()
                .createMessage(message)
                .block();
    }

    private boolean isMe(long userId) {
        return gatewayClient.getSelfId().equals(Snowflake.of(userId));
    }
}
