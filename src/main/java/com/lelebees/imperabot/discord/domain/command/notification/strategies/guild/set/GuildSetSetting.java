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

// Sets default setting to given setting
public class GuildSetSetting implements NotificationCommandStrategy {
    private final GuildSettingsService guildSettingsService;

    public GuildSetSetting(GuildSettingsService guildSettingsService) {
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
        Optional<ApplicationCommandInteractionOptionValue> settingInput = event.getOptions().get(0).getOptions().get(0).getOption("setting").orElseThrow(() -> new NullPointerException("No channel present! (How?!?!)")).getValue();
        if (settingInput.isEmpty()) {
            throw new NullPointerException("There is no channel entered! (HOOOWWW?!?!!?!?!)");
        }
        long settingLong = settingInput.get().asLong();
        int setting = Math.toIntExact(settingLong);

        // GetOrCreate:
        GuildSettings guildSettings;
        try {
            guildSettings = guildSettingsService.getGuildSettingsById(guildIdOptional.get().asLong());
        } catch (GuildSettingsNotFoundException e) {
            guildSettings = guildSettingsService.createNewGuildSettings(guildIdOptional.get().asLong());
        }
        int oldSetting = guildSettings.notificationSetting;
        GuildSettingsModificationDTO newSettings = new GuildSettingsModificationDTO(guildSettings.defaultChannelId, setting);
        guildSettingsService.updateGuildSettings(guildIdOptional.get().asLong(), newSettings);

        return event.reply().withContent("Changed default setting from " + switch (oldSetting) {
            case 0 -> "No Notifications";
            case 1 -> "Notifications On";
            default -> throw new IllegalStateException("Unexpected value: " + oldSetting);
        } + " to " + switch (setting) {
            case 0 -> "No Notifications";
            case 1 -> "Notifications On";
            default -> throw new IllegalStateException("Unexpected value: " + setting);
        });
    }
}
