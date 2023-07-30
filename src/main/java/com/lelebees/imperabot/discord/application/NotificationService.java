package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.impera.application.ImperaService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    public Mono<Void> setGame(ChatInputInteractionEvent event, long gameid, long channelId, Integer setting) {
        GameChannelLink link = gameLinkService.findOrCreateLink(gameid, channelId, setting);
        return event.reply().withContent("Started logging notifications for game [" + link.getGameId() + "] in <#" + link.getChannelId() + "> with `" + GuildNotificationSettings.values()[link.notificationSetting].toString() + "`");
    }
}
