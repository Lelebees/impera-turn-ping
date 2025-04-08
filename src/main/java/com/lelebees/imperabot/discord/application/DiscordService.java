package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.guild.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGamePlayerDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.AllowedMentions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

import static discord4j.rest.util.Permission.VIEW_CHANNEL;

@Service
public class DiscordService {
    private final static Logger logger = LoggerFactory.getLogger(DiscordService.class);
    private final GatewayDiscordClient gatewayClient;
    private final UserService userService;
    private final GuildSettingsService guildSettingsService;
    private final GameLinkService gameLinkService;

    public DiscordService(GatewayDiscordClient gatewayClient, UserService userService, GuildSettingsService guildSettingsService, GameLinkService gameLinkService) {
        this.gatewayClient = gatewayClient;
        this.userService = userService;
        this.guildSettingsService = guildSettingsService;
        this.gameLinkService = gameLinkService;
    }

    public static Instant convertSnowflakeToInstant(Snowflake snowflake) {
        long snowflakeLong = snowflake.asLong();
        StringBuilder binarySnowflake = new StringBuilder(Long.toBinaryString(snowflakeLong));
        /* The binary representation has missing bits, a snowflake always has 64.
        The last 42 bits of the snowflake is the timestamp, which has leading zeroes so there's room to grow.
        We pad the number with the missing zeroes here so the calculation doesn't mess up. */
        int numOfMissingBits = 63 - binarySnowflake.length();
        for (int i = 0; i < numOfMissingBits; i++) {
            binarySnowflake.insert(0, "0");
        }
        String binaryTimestamp = binarySnowflake.substring(0, 41);
        long discordTimestamp = Long.parseUnsignedLong(binaryTimestamp, 2);
        return Instant.ofEpochMilli(discordTimestamp + Snowflake.DISCORD_EPOCH);
    }

    public void sendVerificationDM(long userId) {
        sendDM(userId, "Successfully linked your Impera account with Discord");
    }

    public void sendDM(long recipientId, String message) {
        getDMChannelByOwner(recipientId).createMessage(message).block();
    }

    public void sendNewTurnMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        String turnMessage = "your turn in [%s](https://imperaonline.de/game/play/%s)!".formatted(game.name(), game.id());
        String directTurnMessage = "It's " + turnMessage;
        String generalTurnMessage = "%s, it is " + turnMessage;
        ordinaryNotify(channels, gamePlayer, generalTurnMessage, directTurnMessage);
    }

    public void sendHalfTimeMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        String halfTimeMessage = "have half time remaining in [%s](https://imperaonline.de/game/play/%s)!".formatted(game.name(), game.id());
        String directHalfTimeMessage = "You " + halfTimeMessage;
        String generalHalfTimeMessage = "%s, you " + halfTimeMessage;
        ordinaryNotify(channels, gamePlayer, generalHalfTimeMessage, directHalfTimeMessage);
    }

    public void sendDefeatedMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        String defeatedMessage = "been defeated in [%s](https://imperaonline.de/game/play/%s)!".formatted(game.name(), game.id());
        String directDefeatedMessage = "You have " + defeatedMessage;
        String generalDefeatedMessage = "%s has " + defeatedMessage;
        ordinaryNotify(channels, gamePlayer, generalDefeatedMessage, directDefeatedMessage);
    }

    public void sendTimedOutMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO imperaGame) {
        String timedOutMessage = "timed out in [%s](https://imperaonline.de/game/play/%s)!".formatted(imperaGame.name(), imperaGame.id());
        String directTimeOutMessage = "You have " + timedOutMessage;
        String generalTimeOutMessage = "%s has " + timedOutMessage;
        ordinaryNotify(channels, gamePlayer, generalTimeOutMessage, directTimeOutMessage);
    }

    public void sendSurrenderMessage(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        String generalSurrenderMessage = "%s has surrendered in [%s](https://imperaonline.de/game/play/%s)!".formatted(gamePlayer.name(), game.name(), game.id());
        for (Channel channel : channels) {
            ((MessageChannel) channel).createMessage(generalSurrenderMessage).block();
        }
    }

    public void sendVictorsMessage(List<Channel> channels, List<ImperaGamePlayerDTO> winningPlayers, ImperaGameViewDTO game) {
        List<String> userStrings = new ArrayList<>();
        String victoryMessage = "Game [%s](https://imperaonline.de/game/play/%s) has ended!".formatted(game.name(), game.id());
        for (ImperaGamePlayerDTO gamePlayer : winningPlayers) {
            Optional<BotUser> user = userService.findImperaUser(UUID.fromString(gamePlayer.userId()));
            String userString = gamePlayer.name();
            String directVictoryMessage = victoryMessage + " You have won!";
            if (user.isPresent()) {
                BotUser player = user.get();
                userString = getUserStringWithSettings(player, directVictoryMessage, gamePlayer, channels);
            }
            userStrings.add(userString);
        }
        String singledUser = userStrings.get(0);
        userStrings.remove(0);
        String generalVictoryMessage = victoryMessage + " %s and %s have won!".formatted(String.join(", ", userStrings), singledUser);
        if (userStrings.isEmpty()) {
            generalVictoryMessage = victoryMessage + " %s has won!".formatted(singledUser);
        }
        for (Channel channel : channels) {
            ((MessageChannel) channel).createMessage(generalVictoryMessage).block();
        }
    }

    private AllowedMentions getAllowedMentions(BotUser player) {
        return switch (player.getNotificationSetting()) {
            case NO_NOTIFICATIONS, DMS_ONLY -> AllowedMentions.suppressAll();
            case DMS_AND_GUILD, PREFER_GUILD_OVER_DMS, GUILD_ONLY ->
                    AllowedMentions.builder().allowUser(Snowflake.of(player.getUserId())).build();
        };
    }

    private void sendDMAccordingToSettings(BotUser player, String directMessage, boolean noGuildChannels) {
        switch (player.getNotificationSetting()) {
            case NO_NOTIFICATIONS, GUILD_ONLY -> {
            }
            case DMS_ONLY, DMS_AND_GUILD -> sendDM(player.getUserId(), directMessage);
            case PREFER_GUILD_OVER_DMS -> {
                if (noGuildChannels) {
                    sendDM(player.getUserId(), directMessage);
                }
            }
        }
    }

    private void ordinaryNotify(List<Channel> channels, ImperaGamePlayerDTO gamePlayer, String generalMessage, String directMessage) {
        List<MessageChannel> guildChannels = channels.stream().filter(channel -> channel.getType() != Channel.Type.DM).map(channel -> (MessageChannel) channel).toList();
        List<MessageChannel> dmChannels = channels.stream().filter(channel -> channel.getType() == Channel.Type.DM).map(channel -> (MessageChannel) channel).toList();
        AllowedMentions allowedMentions = AllowedMentions.suppressAll();
        String userString = gamePlayer.name();
        Optional<BotUser> user = userService.findImperaUser(UUID.fromString(gamePlayer.userId()));
        if (user.isPresent()) {
            BotUser player = user.get();
            userString = player.getMention();

            boolean userCanSeeMessageInGuild = false;
            for (Channel channel : guildChannels) {
                Snowflake guildId = Snowflake.of(channel.getData().guildId().get());
                Guild guild = gatewayClient.getGuildById(guildId).block();
                try {
                    Member guildMember = guild.getMemberById(Snowflake.of(player.getUserId())).block();
                    // Check if the guildmember has access to the specific channel
                    userCanSeeMessageInGuild = !((GuildMessageChannel) channel).getEffectivePermissions(guildMember.getId()).block().contains(VIEW_CHANNEL);
                } catch (ClientException e) {
                    logger.debug("User " + player.getUserId() + " does not have access to guild " + guildId.asLong() + " (" + guild.getName() + ")");
                }
            }

            PrivateChannel usersChannel = getDMChannelByOwner(player.getUserId());
            if (dmChannels.contains(usersChannel)) {
                sendDMAccordingToSettings(player, directMessage.formatted(userString), userCanSeeMessageInGuild);
            }
            allowedMentions = getAllowedMentions(player);
        }

        AllowedMentions finalAllowedMentions = allowedMentions;
        String finalUserString = userString;
        // TODO: Force swap userstring to playername if player is not in a guild
        guildChannels.forEach(channel -> channel.createMessage(generalMessage.formatted(finalUserString)).withAllowedMentions(finalAllowedMentions).block());
    }
    //TODO: This method produces side effects, replace with better method :)
    private String getUserStringWithSettings(BotUser player, String directMessage, ImperaGamePlayerDTO gamePlayer, List<Channel> channels) {
        String userString = player.getMention();
        switch (player.getNotificationSetting()) {
            case NO_NOTIFICATIONS -> userString = gamePlayer.name();
            case GUILD_ONLY -> {
            }
            case DMS_ONLY -> {
                getDMChannelByOwner(player.getUserId()).createMessage(directMessage);
                userString = gamePlayer.name();
            }
            case PREFER_GUILD_OVER_DMS -> {
                if (channels.isEmpty()) {
                    getDMChannelByOwner(player.getUserId()).createMessage(directMessage);
                }
            }
            case DMS_AND_GUILD -> getDMChannelByOwner(player.getUserId()).createMessage(directMessage);
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
        if (user == null){
//            Doing some dangerous shit here :3
            return null;
        }
        return user.getPrivateChannel().block();
    }

    public Channel getChannelById(long channelId) {
        return gatewayClient.getChannelById(Snowflake.of(channelId)).block();
    }

    public Map<String, Long> getApplicationCommands() {
        Map<String, Long> commands = new HashMap<>();
        gatewayClient.getRestClient().getApplicationService().getGlobalApplicationCommands(gatewayClient.getSelfId().asLong()).collectList().block().forEach(command -> commands.put(command.name(), command.id().asLong()));
//        logger.info("All application (slash) commands: " + commands);
        return commands;
    }

    private boolean isMe(long userId) {
        return gatewayClient.getSelfId().equals(Snowflake.of(userId));
    }

    public void giveWinnerRole(Game game, ImperaGamePlayerDTO winner) {
        Optional<BotUser> user = userService.findImperaUser(UUID.fromString(winner.userId()));
        if (user.isEmpty()) {
            return;
        }
        BotUser winnerUser = user.get();

        List<GameChannelLink> links = gameLinkService.findLinksByGame(game.getId());
        logger.debug("Found " + links.size() + " links for game " + game.getId() + " when awarding winner role.");
        if (links.isEmpty()) {
            return;
        }

        // Find which channels belong to which guilds, and filter out the ones that are DMs
        List<Guild> guilds = links.stream().map(GameChannelLink::getChannelId).filter(this::channelIsGuildChannel).map(this::getGuildChannelGuild).map(guildId -> gatewayClient.getGuildById(Snowflake.of(guildId)).block()).toList();

        int numberOfGuilds = guilds.size();
        logger.info("Found " + numberOfGuilds + " guilds to award winner role in.");
        int numberOfSkippedGuilds = 0;
        // In each guild, find the winner role, and give it to the winner
        for (Guild guild : guilds) {
            long guildId = guild.getId().asLong();
            String guildDebugId = guildId + " (" + guild.getName() + ")";
            try {
                Long winnerRoleId = guildSettingsService.getGuildSettingsById(guild.getId().asLong()).winnerRoleId;
                if (winnerRoleId == null) {
                    numberOfSkippedGuilds++;
                    logger.debug("Skipping guild " + guildDebugId + " because it has no winner role.");
                    continue;
                }
                Member winningMember = guild.getMemberById(Snowflake.of(winnerUser.getUserId())).block();
                if (winningMember == null) {
                    numberOfSkippedGuilds++;
                    logger.debug("Skipping guild " + guildDebugId + " because the winner is not a member of the guild.");
                    continue;
                }
                String winningMemberDebugId = winningMember.getId().asLong() + " (" + winningMember.getUsername() + ")";
                try {
                    winningMember.addRole(Snowflake.of(winnerRoleId)).block();
                } catch (ClientException e) {
                    logger.error("Bot cannot give roles to " + winningMemberDebugId + " in guild " + guildDebugId + "!\n Most likely, the bot does not have the \"Manage Roles\" permission.");
                    numberOfSkippedGuilds++;
                    continue;
                }
                logger.debug("Successfully awarded winner role to " + winningMemberDebugId + " in guild " + guildDebugId);
            } catch (GuildSettingsNotFoundException e) {
                // With the way the system is set up, this shouldn't ever trigger, but if it does, we'll know :)
                numberOfSkippedGuilds++;
                logger.error("Settings for guild: " + guildDebugId + " could not be found. Skipping...");
            }
        }
        logger.info("Skipped " + numberOfSkippedGuilds + " guilds.");
    }
}
