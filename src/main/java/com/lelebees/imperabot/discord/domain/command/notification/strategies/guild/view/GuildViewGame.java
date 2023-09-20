package com.lelebees.imperabot.discord.domain.command.notification.strategies.guild.view;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameLinkId;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.discord.domain.EmbedField;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.*;

// View which channels are connected to the game, within this guild.
public class GuildViewGame implements NotificationCommandStrategy {

    private final GameLinkService gameLinkService;
    private final GuildSettingsService guildSettingsService;

    public GuildViewGame(GameLinkService gameLinkService, GuildSettingsService guildSettingsService) {
        this.gameLinkService = gameLinkService;
        this.guildSettingsService = guildSettingsService;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        Guild guild = event.getInteraction().getGuild().block();
        if (guild == null) {
            throw new IncorrectContextException("Not in a guild!");
        }

        Optional<ApplicationCommandInteractionOptionValue> gameIdOpt = event.getOptions().get(0).getOptions().get(0).getOption("gameid").orElseThrow(() -> new NullPointerException("gameid not present")).getValue();
        if (gameIdOpt.isEmpty()) {
            throw new NullPointerException("gameid present but not entered");
        }
        long gameid = gameIdOpt.get().asLong();

        List<GuildChannel> channels = guild.getChannels().collectList().block();
        List<GameChannelLink> gameChannelLinks = gameLinkService.findLinksByGame(gameid);

        Set<Long> channelIds = new HashSet<>();
        for (GameChannelLink link : gameChannelLinks) {
            channelIds.add(link.getChannelId());
        }

        List<GuildChannel> resultList = new ArrayList<>();
        for (GuildChannel channel : channels) {
            if (channelIds.contains(channel.getId().asLong())) {
                resultList.add(channel);
            }
        }

        List<EmbedCreateFields.Field> fields = new ArrayList<>();
        for (GuildChannel channel : resultList) {
            GameChannelLink link = gameLinkService.findLink(new GameLinkId(gameid, channel.getId().asLong()));
            GuildNotificationSettings notificationSetting;
            if (link.notificationSetting == null) {
                GuildSettings guildSettings = guildSettingsService.getGuildSettingsById(guild.getId().asLong());
                notificationSetting = guildSettings.notificationSetting;
            } else {
                notificationSetting = GuildNotificationSettings.values()[link.notificationSetting];
            }

            fields.add(new EmbedField("<#" + channel.getId() + ">", "`" + notificationSetting.toString() + "`", false));
        }

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("Channels connected to [" + gameid + "]")
                .addAllFields(fields)
                .color(Color.of(230, 200, 90))
                .build();

        return event.reply().withEmbeds(embed);
    }
}
