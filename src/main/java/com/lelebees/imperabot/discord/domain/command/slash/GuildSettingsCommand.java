package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.bot.domain.guild.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class GuildSettingsCommand implements SlashCommand {
    private final static Logger logger = LoggerFactory.getLogger(GuildSettingsCommand.class);
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
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        if (guildIdOptional.isEmpty()) {
            logger.error("A guild command was called, but no guild Id was supplied with the interaction.");
            return event.reply().withContent("Could not run command because a critical error has occurred, please file a bug report and include the timestamp of the interaction").withEphemeral(true);
        }
        Member guildMember = event.getInteraction().getMember().get();

        if (event.getOption("set").isPresent()) {
            GuildSettings guildSettings = guildSettingsService.getOrCreateGuildSettings(guildIdOptional.get().asLong());
            boolean userHasManageChannelsPermission = guildMember.getBasePermissions().block().contains(Permission.MANAGE_CHANNELS);
            boolean userHasPermissionRole = guildSettings.permissionRoleId != null && guildMember.getRoleIds().contains(Snowflake.of(guildSettings.permissionRoleId));
            if (!userHasManageChannelsPermission && !userHasPermissionRole) {
                logger.info("User " + guildMember.getId().asLong() + " (" + guildMember.getUsername() + ") was denied acces to /guildsettings set because they did not have the correct permissions.");
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
                        logger.info("User " + guildMember.getId().asLong() + " (" + guildMember.getUsername() + ") used /guildsettings set defaultchannel with channel: " + channelId.asLong() + " (" + channel.getData().name().get() + ")");

                        guildSettingsService.updateDefaultChannel(guildIdOptional.get().asLong(), channelId.asLong());
                        return event.reply().withContent("Updated default channel to <#%s>".formatted(channelId.asString()));
                    }
                    case "permissionrole" -> {
                        Optional<ApplicationCommandInteractionOption> roleOption = subOption.getOption("role");
                        Long roleId = null;
                        if (roleOption.isPresent()) {
                            roleId = roleOption.get().getValue().get().asRole().block().getId().asLong();
                        }
                        logger.info("User " + guildMember.getId().asLong() + " (" + guildMember.getUsername() + ") used /guildsettings set permissionrole with role: " + roleId);

                        guildSettingsService.updatePermissionRole(guildIdOptional.get().asLong(), roleId);
                        return event.reply().withContent("Updated permission role to <@&%s>".formatted(roleId)).withAllowedMentions(AllowedMentions.suppressAll());
                    }
                    case "winnerrole" -> {
                        Optional<ApplicationCommandInteractionOption> roleOption = subOption.getOption("role");
                        Long roleId = null;
                        if (roleOption.isPresent()) {
                            roleId = roleOption.get().getValue().get().asRole().block().getId().asLong();
                        }
                        logger.info("User " + guildMember.getId().asLong() + " (" + guildMember.getUsername() + ") used /guildsettings set winnerrole with role: " + roleId);

                        guildSettingsService.updateWinnerRole(guildIdOptional.get().asLong(), roleId);
                        return event.reply().withContent("Updated winner role to <@&%s>".formatted(roleId)).withAllowedMentions(AllowedMentions.suppressAll());
                    }
                }
            } catch (GuildSettingsNotFoundException e) {
                return event.reply().withContent("Could not find settings for this guild!").withEphemeral(true);
            }
            logger.info("User " + guildMember.getId().asLong() + " (" + guildMember.getUsername() + ") used /guildsettings set");
            return event.reply().withEphemeral(true).withContent("This is not a valid option. Please select one!");
        }

        logger.info("User " + guildMember.getId().asLong() + " (" + guildMember.getUsername() + ") used /guildsettings view");

        long guildId = guildIdOptional.get().asLong();
        String guildName = event.getInteraction().getGuild().block().getName();
        try {
            GuildSettings settings = guildSettingsService.getGuildSettingsById(guildId);
            EmbedCreateSpec embed = EmbedCreateSpec.builder()
                    .title("Settings for %s".formatted(guildName))
                    .addField("Default channel:", (settings.defaultChannelId == null ? "`None`" : "<#%s>".formatted(settings.defaultChannelId)), false)
                    .addField("Permission role:", (settings.permissionRoleId == null ? "`None`" : "<@&%s>".formatted(settings.permissionRoleId)), false)
                    .addField("Winner role:", (settings.winnerRoleId == null ? "`None`" : "<@&%s>".formatted(settings.winnerRoleId)), false)
                    .color(Color.of(230, 200, 90))
                    .build();
            return event.reply().withEmbeds(embed).withAllowedMentions(AllowedMentions.suppressAll());
        } catch (GuildSettingsNotFoundException e) {
            return event.reply().withEphemeral(true).withContent("Could not find guild settings! use /guildsettings set to create a settings list for this guild.");
        }
    }
}
