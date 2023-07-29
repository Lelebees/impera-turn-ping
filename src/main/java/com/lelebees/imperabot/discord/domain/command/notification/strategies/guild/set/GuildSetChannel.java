package com.lelebees.imperabot.discord.domain.command.notification.strategies.guild.set;

import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.bot.domain.guild.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.bot.presentation.guildsettings.GuildSettingsModificationDTO;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectPermissionException;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

import java.util.Optional;

// Sets the default channel for a guild
public class GuildSetChannel implements NotificationCommandStrategy {

    private final GuildSettingsService guildSettingsService;

    public GuildSetChannel(GuildSettingsService guildSettingsService) {
        this.guildSettingsService = guildSettingsService;
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

        //TODO: Fix these errors
        if (!callingUser.get().getBasePermissions().block().contains(Permission.MANAGE_CHANNELS)) {
            throw new IncorrectPermissionException("You do not have the correct permissions!");
        }

        // Dip into guild, then set, then find channel.
        Optional<ApplicationCommandInteractionOptionValue> channelInput = event.getOptions().get(0).getOptions().get(0).getOption("channel").orElseThrow(() -> new NullPointerException("No channel present! (How?!?!)")).getValue();
        if (channelInput.isEmpty()) {
            throw new NullPointerException("There is no channel entered! (HOOOWWW?!?!!?!?!)");
        }
        Long channelId = channelInput.get().asChannel().block().getId().asLong();

        // GetOrCreate:
        GuildSettings guildSettings;
        try {
            guildSettings = guildSettingsService.getGuildSettingsById(guildIdOptional.get().asLong());
        } catch (GuildSettingsNotFoundException e) {
            guildSettings = guildSettingsService.createNewGuildSettings(guildIdOptional.get().asLong());
        }
        Optional<Long> oldChannelId = Optional.ofNullable(guildSettings.defaultChannelId);
        GuildSettingsModificationDTO newSettings = new GuildSettingsModificationDTO(channelId, guildSettings.notificationSetting);
        guildSettingsService.updateGuildSettings(guildIdOptional.get().asLong(), newSettings);

        return oldChannelId.map(id -> event.reply().withContent("Changed default channel from <#" + id + "> to <#" + newSettings.channelId + ">")).orElseGet(() -> event.reply().withContent("Set default channel to <#" + newSettings.channelId + ">"));
    }
}
