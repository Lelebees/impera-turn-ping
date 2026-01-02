package com.lelebees.imperabot.discord.domain.command.select;

import com.lelebees.imperabot.core.application.dto.GuildSettingsDTO;
import com.lelebees.imperabot.core.application.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.core.application.protectedservices.GuildSettingsService;
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
public class WinnerRoleSettingSelect implements SelectMenuInteraction {
    private final Logger logger = LoggerFactory.getLogger(WinnerRoleSettingSelect.class);
    private final GuildSettingsService guildSettingsService;

    public WinnerRoleSettingSelect(GuildSettingsService guildSettingsService) {
        this.guildSettingsService = guildSettingsService;
    }

    @Override
    public String getCustomId() {
        return "guild-settings-winner-role-select";
    }

    @Override
    public Mono<Void> handle(SelectMenuInteractionEvent event) {
        User callingUser = event.getInteraction().getUser();
        Optional<Guild> guildOptional = event.getInteraction().getGuild().blockOptional();
        if (guildOptional.isEmpty()) {
            logger.error("User {} ({}) attempted to update winner role for guild but interaction was issued outside one. How did we get here?", callingUser.getUsername(), callingUser.getId().asLong());
            return event.reply("Could not update winner role. Not in a server. How did you get here?").withEphemeral(true);
        }
        Guild guild = guildOptional.get();
        List<String> values = event.getValues();
        Long roleId = null;
        if (!values.isEmpty()) {
            roleId = Long.parseLong(values.get(0));
        }
        try {
            GuildSettingsDTO oldSettings = guildSettingsService.getGuildSettingsById(guild.getId().asLong());
            if (!oldSettings.hasVanityRoleManagePermissions(callingUser)) {
                logger.info("User {} ({}) attempted to update the winner role for guild {} ({}) but was denied because they have no permission", callingUser.getUsername(), callingUser.getId().asLong(), guild.getName(), guild.getId().asLong());
                return event.reply("Interaction Failed: No Permission").withEphemeral(true);
            }
            GuildSettingsDTO guildSettings = guildSettingsService.updateWinnerRole(guild.getId().asLong(), roleId);
            logger.info("User {} ({}) updated the winner role for guild {} ({}).", callingUser.getUsername(), callingUser.getId().asLong(), guild.getName(), guild.getId().asLong());
            return event.edit(InteractionApplicationCommandCallbackSpec.builder().addAllComponents(List.of(SettingsMenu.buildForGuild(guildSettings, guild, callingUser))).build());
        } catch (GuildSettingsNotFoundException e) {
            logger.warn("User {} ({}) attempted to update winner role for guild {} ({}) but it's settings were not in the database.", callingUser.getUsername(), callingUser.getId().asLong(), guild.getName(), guild.getId().asLong());
            return event.reply("Could not update server settings, server is not in the database. Please file a bug report or cease using this outdated embed.").withEphemeral(true);
        }
    }
}
