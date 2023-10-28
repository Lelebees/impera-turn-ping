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
        String turnMessage = "your turn in [%s](https://imperaonline.de/game/play/%s)!".formatted(game.name, game.id);
        String directTurnMessage = "It's %s".formatted(turnMessage);
        if (user.isPresent()) {
            BotUser player = user.get();
            userString = getUserStringWithSettings(player, directTurnMessage, gamePlayer, channels);
        }
        String generalTurnMessage = "%s, it is %s".formatted(userString, turnMessage);
        for (Channel channel : channels) {
            sendMessage(channel.getId().asLong(), generalTurnMessage);
        }
    }

    public void sendHalfTimeMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        Optional<BotUser> user = userService.findImperaUser(UUID.fromString(gamePlayer.userId));
        String userString = gamePlayer.name;
        String halfTimeMessage = "have half time remaining in [%s](https://imperaonline.de/game/play/%s)!".formatted(game.name, game.id);
        String directHalfTimeMessage = "You %s ".formatted(halfTimeMessage);
        if (user.isPresent()) {
            BotUser player = user.get();
            userString = getUserStringWithSettings(player, directHalfTimeMessage, gamePlayer, channels);
        }
        String generalHalfTimeMessage = "%s %s".formatted(userString, halfTimeMessage);
        for (Channel channel : channels) {
            sendMessage(channel.getId().asLong(), generalHalfTimeMessage);
        }
    }

    public void sendVictorMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        Optional<BotUser> user = userService.findImperaUser(UUID.fromString(gamePlayer.userId));
        String userString = gamePlayer.name;
        String victoryMessage = "Game [%s](https://imperaonline.de/game/play/%s) has ended!".formatted(game.name, game.id);
        String directVictoryMessage = "%s You have won!".formatted(victoryMessage);
        if (user.isPresent()) {
            BotUser player = user.get();
            userString = getUserStringWithSettings(player, directVictoryMessage, gamePlayer, channels);
        }
        String generalVictoryMessage = "%s %s has won!".formatted(victoryMessage, userString);
        for (Channel channel : channels) {
            sendMessage(channel.getId().asLong(), generalVictoryMessage);
        }
    }

    public void sendVictorsMessage(List<Channel> channels, List<ImperaGamePlayerDTO> winningPlayers, ImperaGameViewDTO game) {
        List<String> userStrings = new ArrayList<>();
        String victoryMessage = "Game [%s](https://imperaonline.de/game/play/%s) has ended!".formatted(game.name, game.id);
        for (ImperaGamePlayerDTO gamePlayer : winningPlayers) {
            Optional<BotUser> user = userService.findImperaUser(UUID.fromString(gamePlayer.userId));
            String userString = gamePlayer.name;
            String directVictoryMessage = "%s You have won!".formatted(victoryMessage);
            if (user.isPresent()) {
                BotUser player = user.get();
                userString = getUserStringWithSettings(player, directVictoryMessage, gamePlayer, channels);
            }
            userStrings.add(userString);
        }
        String singledUser = userStrings.get(0);
        userStrings.remove(0);
        // TODO: test this
        String generalVictoryMessage = "%s %s %s won!".formatted(victoryMessage, String.join(", ", userStrings), !userStrings.isEmpty() ? "and %s have".formatted(singledUser) : "%s has".formatted(singledUser));
        for (Channel channel : channels) {
            sendMessage(channel.getId().asLong(), generalVictoryMessage);
        }
    }

    public void sendSurrenderMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        String generalSurrenderMessage = "%s has surrendered in [%s](https://imperaonline.de/game/play/%s)!".formatted(gamePlayer.name, game.name, game.id);
        for (Channel channel : channels) {
            sendMessage(channel.getId().asLong(), generalSurrenderMessage);
        }
    }

    public void sendDefeatedMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        Optional<BotUser> user = userService.findImperaUser(UUID.fromString(gamePlayer.userId));
        String userString = gamePlayer.name;
        String defeatedMessage = "been defeated in [%s](https://imperaonline.de/game/play/%s)!".formatted(game.name, game.id);
        String directDefeatedMessage = "You have %s".formatted(defeatedMessage);
        if (user.isPresent()) {
            BotUser player = user.get();
            userString = getUserStringWithSettings(player, directDefeatedMessage, gamePlayer, channels);
        }
        String generalDefeatedMessage = "%s has %s".formatted(userString, defeatedMessage);
        for (Channel channel : channels) {
            sendMessage(channel.getId().asLong(), generalDefeatedMessage);
        }
    }

    public void sendTimedOutMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO imperaGame) {
        Optional<BotUser> user = userService.findImperaUser(UUID.fromString(gamePlayer.userId));
        String userString = gamePlayer.name;
        String timedOutMessage = "timed out in [%s](https://imperaonline.de/game/play/%s)!".formatted(imperaGame.name, imperaGame.id);
        String directTimeOutMessage = "You have %s".formatted(timedOutMessage);
        if (user.isPresent()) {
            BotUser player = user.get();
            userString = getUserStringWithSettings(player, directTimeOutMessage, gamePlayer, channels);
        }
        String generalTimeOutMessage = "%s has %s".formatted(userString, timedOutMessage);
        for (Channel channel : channels) {
            sendMessage(channel.getId().asLong(), generalTimeOutMessage);
        }
    }

    private String getUserStringWithSettings(BotUser player, String directMessage, ImperaGamePlayerDTO gamePlayer, List<Channel> channels) {
        String userString = player.getMention();
        switch (player.getNotificationSetting()) {
            case NO_NOTIFICATIONS -> userString = gamePlayer.name;
            case GUILD_ONLY -> {
            }
            case DMS_ONLY -> {
                sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), directMessage);
                userString = gamePlayer.name;
            }
            case PREFER_GUILD_OVER_DMS -> {
                if (channels.isEmpty()) {
                    sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), directMessage);
                }
            }
            case DMS_AND_GUILD -> sendMessage(getDMChannelByOwner(player.getUserId()).getId().asLong(), directMessage);
        }
        return userString;
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
