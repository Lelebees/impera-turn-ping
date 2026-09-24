package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.core.application.GuildSettingsService;
import com.lelebees.imperabot.core.application.dto.ChannelDTO;
import com.lelebees.imperabot.core.application.dto.GameDTO;
import com.lelebees.imperabot.core.application.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.user.application.dto.BotUserDTO;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.http.client.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class DiscordService {
    private final Logger logger = LoggerFactory.getLogger(DiscordService.class);
    private final GatewayDiscordClient gatewayClient;
    private final GuildSettingsService guildSettingsService;


    public DiscordService(GatewayDiscordClient gatewayClient, GuildSettingsService guildSettingsService) {
        this.gatewayClient = gatewayClient;
        this.guildSettingsService = guildSettingsService;
    }

    public void sendDM(Snowflake recipientId, String message) {
        getDMChannelByOwner(recipientId).createMessage(message).block();
    }

    public boolean channelIsGuildChannel(Snowflake channelId) {
        Set<Channel.Type> guildChannelTypes = Set.of(Channel.Type.GUILD_TEXT, Channel.Type.GUILD_NEWS, Channel.Type.GUILD_PUBLIC_THREAD, Channel.Type.GUILD_PRIVATE_THREAD);
        return guildChannelTypes.contains(getChannelById(channelId).getType());
    }

    public Snowflake getGuildChannelGuild(Snowflake channelId) {
        Channel channel = getChannelById(channelId);
        if (!(channel instanceof GuildMessageChannel guildChannel)) {
            throw new NullPointerException("Non-Guild channel has no guild!");
        }
        return guildChannel.getGuildId();
    }

    public PrivateChannel getDMChannelByOwner(Snowflake userId) {
        User user = gatewayClient.getUserById(userId).block();
        if (user == null) {
//            Doing some dangerous shit here :3
            return null;
        }
        return user.getPrivateChannel().block();
    }

    public Channel getChannelById(Snowflake channelId) {
        return gatewayClient.getChannelById(channelId).block();
    }

    public Map<String, Long> getApplicationCommands() {
        Map<String, Long> commands = new HashMap<>();
        gatewayClient.getRestClient().getApplicationService().getGlobalApplicationCommands(gatewayClient.getSelfId().asLong()).collectList().block().forEach(command -> commands.put(command.name(), command.id().asLong()));
        return commands;
    }

    public void giveWinnerRole(GameDTO game, BotUserDTO winner) {
        logger.debug("Found {} links for game {} when awarding winner role.", game.trackingChannels().size(), game.id());
        if (game.trackingChannels().isEmpty()) {
            return;
        }

        // Find which channels belong to which guilds, and filter out the ones that are DMs
        List<Guild> guilds = game.trackingChannels().stream().map(ChannelDTO::id).map(Snowflake::of).filter(this::channelIsGuildChannel).map(this::getGuildChannelGuild).map(guildId -> gatewayClient.getGuildById(guildId).block()).toList();

        int numberOfGuilds = guilds.size();
        logger.info("Found {} guilds to award winner role in.", numberOfGuilds);
        int numberOfSkippedGuilds = 0;
        // In each guild, find the winner role, and give it to the winner
        for (Guild guild : guilds) {
            long guildId = guild.getId().asLong();
            String guildDebugId = guildId + " (" + guild.getName() + ")";
            try {
                Long winnerRoleId = guildSettingsService.getGuildSettingsById(guild.getId().asLong()).winnerRoleId();
                if (winnerRoleId == null) {
                    numberOfSkippedGuilds++;
                    logger.debug("Skipping guild {} because it has no winner role.", guildDebugId);
                    continue;
                }
                Member winningMember = guild.getMemberById(Snowflake.of(winner.discordId())).block();
                if (winningMember == null) {
                    numberOfSkippedGuilds++;
                    logger.debug("Skipping guild {} because the winner is not a member of the guild.", guildDebugId);
                    continue;
                }
                String winningMemberDebugId = winningMember.getId().asLong() + " (" + winningMember.getUsername() + ")";
                try {
                    winningMember.addRole(Snowflake.of(winnerRoleId)).block();
                } catch (ClientException e) {
                    logger.error("Bot cannot give roles to {} in guild {}!\n Most likely, the bot does not have the \"Manage Roles\" permission.", winningMemberDebugId, guildDebugId);
                    numberOfSkippedGuilds++;
                    continue;
                }
                logger.debug("Successfully awarded winner role to {} in guild {}", winningMemberDebugId, guildDebugId);
            } catch (GuildSettingsNotFoundException e) {
                // With the way the system is set up, this shouldn't ever trigger, but if it does, we'll know :)
                numberOfSkippedGuilds++;
                logger.error("Settings for guild: {} could not be found. Skipping...", guildDebugId);
            }
        }
        logger.info("Skipped {} guilds.", numberOfSkippedGuilds);
    }

    public static boolean userIsLelebees(User user) {
        return user.getId().asLong() == 373532675522166787L;
    }

    public Guild getGuildById(Snowflake guildId) {
        return gatewayClient.getGuildById(guildId).block();
    }
}
