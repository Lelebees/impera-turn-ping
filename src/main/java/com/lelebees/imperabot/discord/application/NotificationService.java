package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GameService;
import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotInGameException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotVerifiedException;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectPermissionException;
import com.lelebees.imperabot.discord.domain.exception.NoDefaultChannelException;
import com.lelebees.imperabot.impera.application.ImperaService;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {

    private final GuildSettingsService guildSettingsService;
    private final GameLinkService gameLinkService;
    private final UserService userService;
    private final ImperaService imperaService;
    private final GameService gameService;

    public NotificationService(GuildSettingsService guildSettingsService, GameLinkService gameLinkService, UserService userService, ImperaService imperaService, GameService gameService) {
        this.guildSettingsService = guildSettingsService;
        this.gameLinkService = gameLinkService;
        this.userService = userService;
        this.imperaService = imperaService;
        this.gameService = gameService;
    }

    public GameChannelLink setGuildGame(long guildId, long gameId, Long channelId, GuildNotificationSettings notificationSettings, Member callingUser) {
        if (!callingUser.getBasePermissions().block().contains(Permission.MANAGE_CHANNELS)) {
            throw new IncorrectPermissionException("You do not have the correct permissions!");
        }

        BotUser user = userService.findUser(callingUser.getId().asLong());
        UUID imperaId = user.getImperaId();
        if (imperaId == null) {
            throw new UserNotVerifiedException("You are not linked to an Impera account!");
        }

        if (!imperaService.isPlayerInGame(user.getImperaId().toString(), gameId)) {
            throw new UserNotInGameException("You are not allowed to access this game!");
        }

        // If the game is not currently being tracked, track it.
        if (!gameService.gameExists(gameId)) {
            trackGame(gameId, callingUser.getId().asLong());
        }

        if (channelId != null) {
            return setGame(gameId, channelId, notificationSettings);
        }
        GuildSettings settings = guildSettingsService.getOrCreateGuildSettings(guildId);
        Long defaultChannelId = settings.defaultChannelId;
        if (defaultChannelId == null) {
            throw new NoDefaultChannelException("No default channel set for this guild!");
        }
        return setGame(gameId, defaultChannelId, notificationSettings);
    }

    private GameChannelLink setGame(long gameId, long channelId, GuildNotificationSettings setting) {
        if (setting == GuildNotificationSettings.NO_NOTIFICATIONS && gameLinkService.linkExists(gameId, channelId)) {
            gameLinkService.deleteLink(gameId, channelId);
            return null;
        }
        return gameLinkService.findOrCreateLink(gameId, channelId, setting);
    }

    public Game trackGame(long gameId, long userId) {
        BotUser user = userService.findOrCreateUser(userId);

        if (!user.isLinked()) {
            throw new UserNotVerifiedException("You are not linked to an Impera account!");
        }

        if (!imperaService.isPlayerInGame(user.getImperaId().toString(), gameId)) {
            throw new UserNotInGameException("You are not in this game!");
        }
        return gameService.findOrCreateGame(gameId);
    }

    public GuildSettings setGuildSetting(long guildId, GuildNotificationSettings setting, Long channelId, Member callingUser) {
        if (!callingUser.getBasePermissions().block().contains(Permission.MANAGE_CHANNELS)) {
            throw new IncorrectPermissionException("You're not allowed to change this!");
        }
        // ANOTHER findorcreate implementation
        // I'm doing something wrong!
        if (!guildSettingsService.guildSettingsExist(guildId)) {
            guildSettingsService.createNewGuildSettings(guildId);
        }
        return guildSettingsService.updateGuildSettings(guildId, channelId, setting);
    }

    public String getGameName(long gameId) {
        return imperaService.getGame(gameId).name;
    }

    public EmbedCreateSpec getUserSettingsEmbed(User user) {
        BotUser botUser = userService.findOrCreateUser(user.getId().asLong());
        return EmbedCreateSpec.builder()
                .title("Settings for " + user.getUsername())
                .addField("Default notification setting: ", "`" + botUser.getNotificationSetting().toString() + "`", false)
                .footer("You are " + (botUser.isLinked() ? "linked to: " + botUser.getImperaId() : "not linked to an Impera account"), null)
                .color(Color.of(230, 200, 90))
                .build();
    }
}
