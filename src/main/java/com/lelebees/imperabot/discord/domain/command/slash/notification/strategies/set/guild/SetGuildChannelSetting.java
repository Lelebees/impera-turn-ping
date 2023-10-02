package com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.set.guild;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.discord.application.NotificationService;
import com.lelebees.imperabot.discord.domain.command.slash.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.NotificationCommandStrategy;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

// Sets the settings for ALL games in a single channel to given settings
public class SetGuildChannelSetting implements NotificationCommandStrategy {
    private static final Logger logger = LoggerFactory.getLogger(SetGuildChannelSetting.class);
    private final DiscordService discordService;
    private final GameLinkService gameLinkService;
    private final NotificationService notificationService;

    public SetGuildChannelSetting(DiscordService discordService, GameLinkService gameLinkService, NotificationService notificationService) {
        this.discordService = discordService;
        this.gameLinkService = gameLinkService;
        this.notificationService = notificationService;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        if (guildIdOptional.isEmpty()) {
            throw new IncorrectContextException("You cannot run a guild command outside of a guild!");
        }

        Optional<Member> callingUser = event.getInteraction().getMember();
        if (callingUser.isEmpty()) {
            throw new IllegalStateException("No one sent this command???");
        }

        Channel channel = discordService.getChannelOption(event);
        long channelId = channel.getId().asLong();
        GuildNotificationSettings setting = discordService.getGuildSettingOption(event);

        List<GameChannelLink> gameChannelLinks = gameLinkService.findLinksByChannel(channelId);
        try {
            if (gameChannelLinks.isEmpty()) {
                return event.reply().withContent("No games are being tracked in this channel");
            }
            gameChannelLinks.forEach(gameChannelLink -> {
                notificationService.setGuildGame(guildIdOptional.get().asLong(), gameChannelLink.getGameId(), channelId, setting, callingUser.get());
            });
        } catch (Exception e) {
            logger.warn("Something went wrong while setting the setting for all games in a channel", e);
        }

        return event.reply().withContent("Set setting for all games (found: " + gameChannelLinks.size() + ") in this channel to `" + setting.toString() + "`");
    }
}
