package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GameService;
import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.guild.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.bot.presentation.game.GameDTO;
import com.lelebees.imperabot.bot.presentation.guildsettings.GuildSettingsDTO;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.game.exception.ImperaGameNotFoundException;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.util.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Track a game so users can get notifications. <br>
 * <br>
 * - If no channel is specified, and there is no default channel, the command will track in the channel it is used in (Guilds only).<br>
 * - If no channel is specified, but there is a default channel, the command will track in the default channel (Guilds only).<br>
 * - If a channel is specified, the command will track in that channel (Guilds only).<br>
 * - If a channel is specified, but the command isn't ran in a guild, Politely tell the user that we can't do that, and offer to track in DMs instead. <br>
 * <br>
 * A game must always be specified.
 */
@Component
public class TrackCommand implements SlashCommand {
    private final Logger logger = LoggerFactory.getLogger(TrackCommand.class);
    private final GameService gameService;
    private final ImperaService imperaService;
    private final GameLinkService gameLinkService;
    private final GuildSettingsService guildSettingsService;
    private final String imperaUrl;

    public TrackCommand(GameService gameService, ImperaService imperaService, GameLinkService gameLinkService, GuildSettingsService guildSettingsService, @Value("${impera.web.url}") String imperaUrl) {
        this.gameService = gameService;
        this.imperaService = imperaService;
        this.gameLinkService = gameLinkService;
        this.guildSettingsService = guildSettingsService;
        this.imperaUrl = imperaUrl + "/game/play";
    }

    @Override
    public String getName() {
        return "track";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        User callingUser = event.getInteraction().getUser();

        Optional<ApplicationCommandInteractionOption> gameOptional = event.getOption("gameid");
        long gameId = gameOptional
                .orElseThrow(() -> new NullPointerException("You must specify a gameid."))
                .getValue()
                .orElseThrow(() -> new NullPointerException("Somehow, no gameId was entered"))
                .asLong();

        Optional<ApplicationCommandInteractionOption> channelOptional = event.getOption("channel");
        Channel channel = event.getInteraction().getChannel().block();

        logger.info("User {} ({}) used /track with gameid: {} in channel: {}{}. Context: {}", callingUser.getId().asLong(), callingUser.getUsername(), gameId, channel.getId().asLong(), guildIdOptional.isPresent() ? "(" + channel.getData().name().get() + ")" : "", guildIdOptional.map(snowflake -> "Guild (" + snowflake.asLong() + ")").orElse("DM"));

        if (channelOptional.isPresent()) {
            channel = channelOptional.get()
                    .getValue()
                    .orElseThrow(() -> new NullPointerException("Somehow, no channel was entered"))
                    .asChannel()
                    .block();
            logger.info("Channel was specified, so tracking in channel: {} ({}).", channel.getId().asLong(), channel.getData().name().get());
        }


        if (guildIdOptional.isPresent()) {
            // We're in a guild, so track for guild
            Snowflake guildId = guildIdOptional.get();
            GuildSettingsDTO guildSettings;
            try {
                guildSettings = guildSettingsService.getGuildSettingsById(guildId.asLong());
            } catch (GuildSettingsNotFoundException e) {
                return event.reply().withContent("Could not find guild settings! use /guildsettings to create a settings list for this guild.").withEphemeral(true);
            }
            Member callingMember = callingUser.asMember(guildIdOptional.get()).block();
            boolean userIsLelebees = callingMember.getId().asLong() == 373532675522166787L;
            if (!hasPermissions(guildSettings, callingMember) && !userIsLelebees) {
                logger.info("User {} ({}) was denied access to /track because they do not have the correct permissions.", callingUser.getId().asLong(), callingUser.getUsername());
                return event.reply().withContent("You are not allowed to track games in this guild.").withEphemeral(true);
            }

            if (guildSettings.defaultChannelId() != null && channelOptional.isEmpty()) {
                channel = event.getInteraction().getGuild().block().getChannelById(Snowflake.of(guildSettings.defaultChannelId())).block();
                logger.info("No channel was specified, but a default channel was set, and the command was used in a guild, so tracking in channel: {} ({}).", channel.getId().asLong(), channel.getData().name().get());
            }
        }
        ImperaGameViewDTO gameView;
        try {
            gameView = imperaService.getGame(gameId);
        } catch (RuntimeException e) {
            logger.error("Impera server returned an unknown error while trying to track game ({}) for {} ({}).", gameId, callingUser.getId().asLong(), callingUser.getUsername());
            return event.reply().withContent("An error occurred while trying to get game information from Impera. Please try again later.").withEphemeral(true);
        } catch (ImperaGameNotFoundException e) {
            logger.warn("Impera game could not be found {}", gameId);
            return event.reply().withContent("Impera could not find the game (" + gameId + ") you are looking for.").withEphemeral(true);
        }

        if (!gameService.gameExists(gameView.id())) {
            /* We pass the current turn as a parameter, because this would otherwise allow someone to start a DDoS attack on the Impera service.
            By playing a game for a while, then repeatedly tracking and untracking the game,
            the bot would make a number of requests equal to the number of turns the game has been going on for, at once.*/
            GameDTO gameDTO = gameService.createGame(gameView);
        }
        long channelId = channel.getId().asLong();
        // Add line to tracking table with gameid and channelid
        if (gameLinkService.linkExists(gameView.id(), channelId)) {
            return event.reply().withEphemeral(true).withContent("[%s](%s/%s)  is already being tracked in <#%s>".formatted(gameView.name(), imperaUrl, gameId, channelId));
        }
        GameChannelLink gameLink = gameLinkService.createLink(gameId, channelId, null);
        logger.debug("Created new GameChannelLink with gameId: {} and channelId: {}", gameLink.getGameId(), gameLink.getChannelId());
        return event.reply().withContent("Started tracking [%s](%s/%s) in <#%s>".formatted(gameView.name(), imperaUrl, gameId, channelId));
    }

    public boolean hasPermissions(GuildSettingsDTO guildSettings, Member guildMember) {
        boolean userHasManageChannelsPermission = guildMember.getBasePermissions().block().contains(Permission.MANAGE_CHANNELS);
        boolean userHasPermissionRole = guildSettings.permissionRoleId() != null && guildMember.getRoleIds().contains(Snowflake.of(guildSettings.permissionRoleId()));
        return userHasManageChannelsPermission || userHasPermissionRole;
    }
}
