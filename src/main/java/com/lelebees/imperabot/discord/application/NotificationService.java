package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GameService;
import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.NotificationSettings;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotInGameException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotVerifiedException;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.exception.NoDefaultChannelException;
import com.lelebees.imperabot.impera.application.ImperaService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public GameChannelLink setGuildGame(ChatInputInteractionEvent event, long gameId, Long channelId, NotificationSettings notificationSettings) {
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        if (guildIdOptional.isEmpty()) {
            throw new IncorrectContextException("Cannot run guild command outside of guild");
        }
        GuildSettings settings = guildSettingsService.getOrCreateGuildSettings(guildIdOptional.get().asLong());
        if (channelId != null) {
            return setGame(gameId, channelId, notificationSettings);
        }
        Long defaultChannelId = settings.defaultChannelId;
        if (defaultChannelId == null) {
            throw new NoDefaultChannelException("No default channel set for this guild!");
        }
        return setGame(gameId, defaultChannelId, notificationSettings);


    }

    private GameChannelLink setGame(long gameId, long channelId, NotificationSettings setting) {
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
}
