package com.lelebees.imperabot.discord.domain.command.select;

import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.domain.guild.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.bot.presentation.guildsettings.GuildSettingsDTO;
import com.lelebees.imperabot.discord.domain.SettingsMenu;
import com.lelebees.imperabot.discord.domain.command.SelectMenuInteraction;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
public class PermissionRoleSettingSelect implements SelectMenuInteraction {
    private final Logger logger = LoggerFactory.getLogger(PermissionRoleSettingSelect.class);
    private final GuildSettingsService guildSettingsService;

    public PermissionRoleSettingSelect(GuildSettingsService guildSettingsService) {
        this.guildSettingsService = guildSettingsService;
    }

    @Override
    public String getCustomId() {
        return "guild-settings-permission-role-select";
    }

    @Override
    public Mono<Void> handle(SelectMenuInteractionEvent event) {
        User callingUser = event.getInteraction().getUser();
        Optional<Guild> guildOptional = event.getInteraction().getGuild().blockOptional();
        if (guildOptional.isEmpty()) {
            logger.error("User %s (%d) attempted to update permission role for guild but interaction was issued outside one. How did we get here?".formatted(callingUser.getUsername(), callingUser.getId().asLong()));
            return event.reply("Could not update permission role. Not in a server. How did you get here?").withEphemeral(true);
        }
        Guild guild = guildOptional.get();
        List<String> values = event.getValues();
        Long roleId = null;
        if (!values.isEmpty()) {
            roleId = Long.parseLong(values.get(0));
        }
        try {
            GuildSettingsDTO oldSettings = guildSettingsService.getGuildSettingsById(guild.getId().asLong());
            if (!oldSettings.userHasEditPermission(callingUser)) {
                logger.info("User %s (%d) attempted to update the permission role for guild %s (%d) but was denied because they have no permission".formatted(callingUser.getUsername(), callingUser.getId().asLong(), guild.getName(), guild.getId().asLong()));
                return event.reply("Interaction Failed: No Permission").withEphemeral(true);
            }
            GuildSettingsDTO guildSettings = guildSettingsService.updatePermissionRole(guild.getId().asLong(), roleId);
            logger.info("User %s (%d) updated the permission role for guild %s (%d).".formatted(callingUser.getUsername(), callingUser.getId().asLong(), guild.getName(), guild.getId().asLong()));
            return event.edit(InteractionApplicationCommandCallbackSpec.builder().addAllComponents(List.of(SettingsMenu.buildForGuild(guildSettings, guild))).build());
        } catch (GuildSettingsNotFoundException e) {
            logger.warn("User %s (%d) attempted to update permission role for guild %s (%d) but it's settings were not in the database.".formatted(callingUser.getUsername(), callingUser.getId().asLong(), guild.getName(), guild.getId().asLong()));
            return event.reply("Could not update server settings, server is not in the database. Please file a bug report or cease using this outdated embed.").withEphemeral(true);
        }
    }
}
