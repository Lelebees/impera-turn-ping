package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.guild.GuildSettingsService;
import com.lelebees.imperabot.bot.application.guild.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.bot.presentation.guildsettings.GuildSettingsDTO;
import com.lelebees.imperabot.discord.domain.SettingsMenu;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.rest.util.AllowedMentions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class GuildSettingsCommand implements SlashCommand {
    private final Logger logger = LoggerFactory.getLogger(GuildSettingsCommand.class);
    private final GuildSettingsService guildSettingsService;

    public GuildSettingsCommand(GuildSettingsService guildSettingsService) {
        this.guildSettingsService = guildSettingsService;
    }

    @Override
    public String getName() {
        return "guildsettings";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Optional<Guild> guildOptional = event.getInteraction().getGuild().blockOptional();
        if (guildOptional.isEmpty()) {
            logger.error("A guild command was called, but not ran inside a guild.");
            return event.reply().withContent("Could not run command because it must be run inside a server.").withEphemeral(true);
        }
        Guild guild = guildOptional.get();
        User callingUser = event.getUser();

        logger.info("User {} ({}) used /guildsettings", callingUser.getId().asLong(), callingUser.getUsername());

        GuildSettingsDTO settings;
        try {
            settings = guildSettingsService.getGuildSettingsById(guild.getId().asLong());
        } catch (GuildSettingsNotFoundException e) {
            settings = guildSettingsService.createNewGuildSettings(guild.getId().asLong());
        }
        return event.reply().withComponents(SettingsMenu.buildForGuild(settings, guild, callingUser)).withAllowedMentions(AllowedMentions.suppressAll());
    }
}
