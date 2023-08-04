package com.lelebees.imperabot.discord.domain.command.notification.strategies.guild.view;

import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

// Overview of guild settings
public class GuildView implements NotificationCommandStrategy {

    private final GuildSettingsService guildSettingsService;

    public GuildView(GuildSettingsService guildSettingsService) {
        this.guildSettingsService = guildSettingsService;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        if (guildIdOptional.isEmpty()) {
            throw new IncorrectContextException("No guildId on guild command");
        }
        long guildId = guildIdOptional.get().asLong();
        String guildName = event.getInteraction().getGuild().block().getName();

        GuildSettings settings = guildSettingsService.getGuildSettingsById(guildId);
        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("Settings for " + guildName)
                .addField("Default channel:", "<#" + settings.defaultChannelId + ">", false)
                .addField("Default notification setting:", "`" + GuildNotificationSettings.values()[settings.notificationSetting].toString() + "`", false)
                .color(Color.of(230, 200, 90))
                .build();
        return event.reply().withEmbeds(List.of(embed));
    }
}
