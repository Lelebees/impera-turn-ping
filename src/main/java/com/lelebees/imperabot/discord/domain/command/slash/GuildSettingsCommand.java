package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.domain.guild.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.bot.presentation.guildsettings.GuildSettingsDTO;
import com.lelebees.imperabot.discord.domain.SettingsMenu;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
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

        if (event.getOption("set").isPresent()) {
            GuildSettingsDTO guildSettings = guildSettingsService.getOrCreateGuildSettings(guild.getId().asLong());
            if (!SettingsMenu.userHasPermission(callingUser, guildSettings)) {
                logger.info("User %s (%d) was denied acces to /guildsettings set because they did not have the correct permissions.".formatted(callingUser.getUsername(), callingUser.getId().asLong()));
                return event.reply().withEphemeral(true).withContent("You do not have the required permissions to use this command!");
            }
            ApplicationCommandInteractionOption subOption = event.getOption("set").get().getOptions().get(0);
            try {
                switch (subOption.getName()) {
                    case "defaultchannel" -> {
                        Optional<ApplicationCommandInteractionOption> channelOption = subOption.getOption("channel");
                        Channel channel = event.getInteraction().getChannel().block();

                        if (channelOption.isPresent()) {
                            channel = channelOption.get().getValue().get().asChannel().block();
                        }
                        Snowflake channelId = channel.getId();
                        logger.info("User %s (%d) used /guildsettings set defaultchannel with channel: %s (%d)".formatted(callingUser.getUsername(), callingUser.getId().asLong(), channel.getData().name().get(), channelId.asLong()));

                        guildSettingsService.updateDefaultChannel(guild.getId().asLong(), channelId.asLong());
                        return event.reply().withContent("Updated default channel to <#%s>".formatted(channelId.asString()));
                    }
                    case "permissionrole" -> {
                        Optional<ApplicationCommandInteractionOption> roleOption = subOption.getOption("role");
                        Long roleId = null;
                        if (roleOption.isPresent()) {
                            roleId = roleOption.get().getValue().get().asRole().block().getId().asLong();
                        }
                        logger.info("User " + callingUser.getId().asLong() + " (" + callingUser.getUsername() + ") used /guildsettings set permissionrole with role: " + roleId);

                        guildSettingsService.updatePermissionRole(guild.getId().asLong(), roleId);
                        return event.reply().withContent("Updated permission role to <@&%s>".formatted(roleId)).withAllowedMentions(AllowedMentions.suppressAll());
                    }
                    case "winnerrole" -> {
                        Optional<ApplicationCommandInteractionOption> roleOption = subOption.getOption("role");
                        Long roleId = null;
                        if (roleOption.isPresent()) {
                            roleId = roleOption.get().getValue().get().asRole().block().getId().asLong();
                        }
                        logger.info("User " + callingUser.getId().asLong() + " (" + callingUser.getUsername() + ") used /guildsettings set winnerrole with role: " + roleId);

                        guildSettingsService.updateWinnerRole(guild.getId().asLong(), roleId);
                        return event.reply().withContent("Updated winner role to <@&%s>".formatted(roleId)).withAllowedMentions(AllowedMentions.suppressAll());
                    }
                }
            } catch (GuildSettingsNotFoundException e) {
                return event.reply().withContent("Could not find settings for this guild!").withEphemeral(true);
            }
            logger.info("User " + callingUser.getId().asLong() + " (" + callingUser.getUsername() + ") used /guildsettings set");
            return event.reply().withEphemeral(true).withContent("This is not a valid option. Please select one!");
        }

        logger.info("User " + callingUser.getId().asLong() + " (" + callingUser.getUsername() + ") used /guildsettings view");

        try {
            GuildSettingsDTO settings = guildSettingsService.getGuildSettingsById(guild.getId().asLong());
            return event.reply().withComponents(SettingsMenu.getForGuild(settings, guild)).withAllowedMentions(AllowedMentions.suppressAll());
        } catch (GuildSettingsNotFoundException e) {
            return event.reply().withEphemeral(true).withContent("Could not find guild settings! use /guildsettings set to create a settings list for this guild.");
        }
    }
}
