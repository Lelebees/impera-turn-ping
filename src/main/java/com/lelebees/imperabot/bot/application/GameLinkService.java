package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.GameChannelLinkRepository;
import com.lelebees.imperabot.bot.domain.NotificationSettings;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameLinkId;
import com.lelebees.imperabot.bot.domain.gamechannellink.exception.GameChannelLinkNotFoundException;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.discord.application.DiscordService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
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

    public GameChannelLink createLink(long gameId, long channelId, NotificationSettings notificationSetting) {
        return repository.save(new GameChannelLink(gameId, channelId, notificationSetting));
    }

    public GameChannelLink findOrCreateLink(long gameId, long channelId, NotificationSettings notificationSetting) {
        Optional<GameChannelLink> gameChannelLinkOptional = repository.findById(new GameLinkId(gameId, channelId));
        if (gameChannelLinkOptional.isEmpty()) {
            return createLink(gameId, channelId, notificationSetting);
        }
        GameChannelLink link = gameChannelLinkOptional.get();
        link.notificationSetting = (notificationSetting == null ? null : notificationSetting.ordinal());
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

    public NotificationSettings deepGetNotificationSetting(GameLinkId id) {
        GameChannelLink gameChannelLink = findLink(id);
        long channelId = gameChannelLink.getChannelId();
        if (discordService.channelIsDM(channelId)) {
            if (gameChannelLink.notificationSetting != null) {
                return UserNotificationSetting.values()[gameChannelLink.notificationSetting];
            }
            long user = discordService.getChannelOwner(channelId);
            return userService.findUser(user).getNotificationSetting();
        }
        if (discordService.channelIsGuildChannel(channelId)) {
            if (gameChannelLink.notificationSetting != null) {
                return GuildNotificationSettings.values()[gameChannelLink.notificationSetting];
            }
            long guildId = discordService.getGuildChannelGuild(channelId);
            return guildSettingsService.getGuildSettingsById(guildId).notificationSetting;
        }
        throw new IllegalStateException("Incorrect channel type!");
    }

    public boolean linkExists(long gameId, long channelId) {
        return repository.existsById(new GameLinkId(gameId, channelId));
    }

    public void deleteLink(long gameId, long channelId) {
        GameChannelLink gameChannelLink = findLink(new GameLinkId(gameId, channelId));
        repository.delete(gameChannelLink);
    }

    public void deleteLinksForGame(long gameId) {
        repository.deleteGameChannelLinksByGameId(gameId);
    }

    public void deleteLinksForChannel(long channelId) {
        repository.deleteGameChannelLinksByChannelId(channelId);
    }
}
