package com.lelebees.imperabot.discord.domain.command.notification.strategies.set.guild;

import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.discord.application.NotificationService;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

import java.util.Optional;

// Sets default setting to given setting
public class SetGuildSetting implements NotificationCommandStrategy {
    private final GuildSettingsService guildSettingsService;
    private final NotificationService notificationService;
    private final DiscordService discordService;

    public SetGuildSetting(GuildSettingsService guildSettingsService, NotificationService notificationService, DiscordService discordService) {
        this.guildSettingsService = guildSettingsService;
        this.notificationService = notificationService;
        this.discordService = discordService;
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

        GuildNotificationSettings setting = discordService.getGuildSettingOption(event);
        // TODO: Find a smarter and reuseable way to do this
        long guildId = guildIdOptional.get().asLong();

        if (guildSettingsService.guildSettingsExist(guildId)) {
            GuildSettings oldGuildSettings = guildSettingsService.getGuildSettingsById(guildId);
            GuildNotificationSettings oldSetting = oldGuildSettings.notificationSetting;

            GuildSettings newSettings = notificationService.setGuildSetting(guildId, setting, oldGuildSettings.defaultChannelId, callingUser.get());
            return event.reply().withContent("Changed default setting from `" + oldSetting.toString() + "` to `" + newSettings.notificationSetting.toString() + "`");
        }
        GuildSettings newSettings = notificationService.setGuildSetting(guildId, setting, null, callingUser.get());
        return event.reply().withContent("Set default setting to `" + newSettings.notificationSetting.toString() + "`");
    }
}
