package com.lelebees.imperabot.discord.domain.command.notification.strategies.set.guild;

import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.discord.application.NotificationService;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

import java.util.Optional;

// Sets the default channel for a guild
public class SetGuildChannel implements NotificationCommandStrategy {

    private final GuildSettingsService guildSettingsService;
    private final NotificationService notificationService;

    public SetGuildChannel(GuildSettingsService guildSettingsService, NotificationService notificationService) {
        this.guildSettingsService = guildSettingsService;
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

        // Dip into guild, then set, then find channel.
        Optional<ApplicationCommandInteractionOptionValue> channelInput = event.getOptions().get(0).getOptions().get(0).getOption("channel").orElseThrow(() -> new NullPointerException("No channel present! (How?!?!)")).getValue();
        if (channelInput.isEmpty()) {
            throw new NullPointerException("There is no channel entered! (HOOOWWW?!?!!?!?!)");
        }
        Long channelId = channelInput.get().asChannel().block().getId().asLong();
        long guildId = guildIdOptional.get().asLong();

        // This is a findOrCreate implementation
        // TODO: Find a smarter and reusable way to do this, preferably through NotificationService.
        if (guildSettingsService.guildSettingsExist(guildId)) {
            GuildSettings oldGuildSettings = guildSettingsService.getGuildSettingsById(guildId);
            Long oldChannelId = oldGuildSettings.defaultChannelId;
            if (oldChannelId != null) {
                notificationService.setGuildSetting(guildId, null, channelId, callingUser.get());
                return event.reply().withContent("Changed default channel from <#" + oldChannelId + "> to <#" + channelId + ">");
            }
        } else {
            guildSettingsService.createNewGuildSettings(guildId);
        }
        notificationService.setGuildSetting(guildId, null, channelId, callingUser.get());
        return event.reply().withContent("Set default channel to <#" + channelId + ">");


    }
}