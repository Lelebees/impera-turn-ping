package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.GameChannelLinkRepository;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameLinkId;
import com.lelebees.imperabot.bot.domain.gamechannellink.exception.GameChannelLinkNotFoundException;
import com.lelebees.imperabot.discord.application.DiscordService;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class GameLinkService {
    private final GameChannelLinkRepository repository;
    private final DiscordService discordService;
    private final GuildSettingsService guildSettingsService;
    private final UserService userService;

    public GameLinkService(GameChannelLinkRepository repository, DiscordService discordService, GuildSettingsService guildSettingsService, UserService userService) {
        this.repository = repository;
        this.discordService = discordService;
        this.guildSettingsService = guildSettingsService;
        this.userService = userService;
    }

    public GameChannelLink saveLink(GameChannelLink gameChannelLink) {
        return repository.save(gameChannelLink);
    }

    public GameChannelLink findLink(GameLinkId id) {
        return getFromOptional(repository.findById(id));
    }

    public GameChannelLink createLink(long gameId, long channelId, Integer notificationSetting) {
        return repository.save(new GameChannelLink(gameId, channelId, notificationSetting));
    }

    public GameChannelLink findOrCreateLink(long gameId, long channelId, Integer notificationSetting) {
        Optional<GameChannelLink> gameChannelLinkOptional = repository.findById(new GameLinkId(gameId, channelId));
        if (gameChannelLinkOptional.isEmpty()) {
            return createLink(gameId, channelId, notificationSetting);
        }
        GameChannelLink link = gameChannelLinkOptional.get();
        link.notificationSetting = notificationSetting;
        return link;
    }

    private GameChannelLink getFromOptional(Optional<GameChannelLink> optional) {
        return optional.orElseThrow(() -> new GameChannelLinkNotFoundException("Could not find the link"));
    }

    public List<GameChannelLink> findLinksByChannel(long channelId) {
        return repository.findGameChannelLinkByChannelId(channelId);
    }

    public List<GameChannelLink> findLinksByGame(long gameId) {
        return repository.findGameChannelLinkByGameId(gameId);
    }

    public int deepGetNotificationSetting(GameLinkId id) {
        GameChannelLink gameChannelLink = findLink(id);
        if (gameChannelLink.notificationSetting != null) {
            return gameChannelLink.notificationSetting;
        }

        long channelId = gameChannelLink.getChannelId();
        Channel channel = discordService.getChannelById(channelId);
        if (discordService.channelIsDM(channelId)) {
            PrivateChannel dmChannel = (PrivateChannel) channel;
            Set<Snowflake> people = dmChannel.getRecipientIds();
            Snowflake user = Snowflake.of(0);
            for (Snowflake userId : people) {
                if (!discordService.isMe(userId.asLong())) {
                    user = userId;
                }
            }
            return userService.findUser(user.asLong()).getNotificationSetting();
        } else if (discordService.channelIsGuildChannel(channelId)) {
            GuildMessageChannel guildChannel = (GuildMessageChannel) channel;
            long guildId = guildChannel.getGuildId().asLong();
            return guildSettingsService.getGuildSettingsById(guildId).notificationSetting;
        } else {
            throw new IllegalStateException("Incorrect channel type!");
        }
    }
}
