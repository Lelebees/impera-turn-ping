package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.impera.application.ImperaService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class NotificationService {

    private final GuildSettingsService guildSettingsService;
    private final GameLinkService gameLinkService;
    private final UserService userService;
    private final ImperaService imperaService;

    public NotificationService(GuildSettingsService guildSettingsService, GameLinkService gameLinkService, UserService userService, ImperaService imperaService) {
        this.guildSettingsService = guildSettingsService;
        this.gameLinkService = gameLinkService;
        this.userService = userService;
        this.imperaService = imperaService;
    }

    public Mono<Void> guildSetGame(ChatInputInteractionEvent event, long gameId, Long channelId, Integer setting) {
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        if (guildIdOptional.isEmpty()) {
            throw new IncorrectContextException("Cannot run guild command outside of guild");
        }
        GuildSettings settings = guildSettingsService.getOrCreateGuildSettings(guildIdOptional.get().asLong());
        if (channelId == null) {
            Long defaultChannelId = settings.defaultChannelId;
            if (defaultChannelId == null) {
                return event.reply().withEphemeral(true).withContent("No default channel has been set for this guild. use `/notifications guild set channel` to set the default channel, or use `/notifications guild set channel gameid setting` to use a channel without setting a default one.");
            }
        }

        GameChannelLink link = setGame(gameId, (channelId == null ? settings.defaultChannelId : channelId), setting);
        return event.reply().withContent("Started logging notifications for game [" + link.getGameId() + "] in <#" + link.getChannelId() + "> with " + (setting == null ? "default setting" : "`" + GuildNotificationSettings.values()[link.notificationSetting].toString() + "`"));
    }

    private GameChannelLink setGame(long gameId, long channelId, Integer setting) {
        return gameLinkService.findOrCreateLink(gameId, channelId, setting);
    }
}
