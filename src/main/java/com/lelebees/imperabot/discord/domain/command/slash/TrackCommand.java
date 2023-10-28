package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GameService;
import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import com.lelebees.imperabot.impera.application.ImperaService;
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
    private static final Logger logger = LoggerFactory.getLogger(TrackCommand.class);
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
        if (gameOptional.isEmpty()) {
            return event.reply().withContent("You must specify a game to track!").withEphemeral(true);
        }
        long gameId = gameOptional.get().getValue().orElseThrow(() -> new NullPointerException("Somehow, no gameId was entered")).asLong();

        Optional<ApplicationCommandInteractionOption> channelOptional = event.getOption("channel");
        Channel channel = event.getInteraction().getChannel().block();

        logger.info("User " + callingUser.getId().asLong() + " (" + callingUser.getUsername() + ") used /track with gameid: " + gameId + " in channel: " + channel.getId().asLong() + (guildIdOptional.isPresent() ? "(" + channel.getData().name().get() + ")" : "") + ". Context: " + (guildIdOptional.map(snowflake -> "Guild (" + snowflake.asLong() + ")").orElse("DM")));

        if (channelOptional.isPresent()) {
            channel = channelOptional.get().getValue().orElseThrow(() -> new NullPointerException("Somehow, no channel was entered")).asChannel().block();
            logger.info("Channel was specified, so tracking in channel: " + channel.getId().asLong() + " (" + channel.getData().name().get() + ").");
        }


        if (guildIdOptional.isPresent()) {
            // We're in a guild, so track for guild
            Snowflake guildId = guildIdOptional.get();
            GuildSettings guildSettings = guildSettingsService.getOrCreateGuildSettings(guildId.asLong());
            Member callingMember = callingUser.asMember(guildIdOptional.get()).block();
            boolean userHasManageChannelsPermission = callingMember.getBasePermissions().block().contains(Permission.MANAGE_CHANNELS);
            boolean userHasPermissionRole = guildSettings.permissionRoleId != null && callingMember.getRoleIds().contains(Snowflake.of(guildSettings.permissionRoleId));
            if (!userHasManageChannelsPermission && !userHasPermissionRole) {
                logger.info("User " + callingUser.getId().asLong() + " (" + callingUser.getUsername() + ") was denied access to /track because they do not have the correct permissions.");
                return event.reply().withContent("You are not allowed to track games in this guild.").withEphemeral(true);
            }

            if (guildSettings.defaultChannelId != null && channelOptional.isEmpty()) {
                channel = event.getInteraction().getGuild().block().getChannelById(Snowflake.of(guildSettings.defaultChannelId)).block();
                logger.info("No channel was specified, but a default channel was set, and the command was used in a guild, so tracking in channel: " + channel.getId().asLong() + " (" + channel.getData().name().get() + ").");
            }
        }
        ImperaGameViewDTO gameView;
        try {
            gameView = imperaService.getGame(gameId);
        } catch (Exception e) {
            logger.info("User " + callingUser.getId() + " (" + callingUser.getUsername() + ") was denied access to /track because Impera (or the fetch request) returned an error.");
            return event.reply().withContent("An error occurred while trying to get game information from Impera. Please try again later.").withEphemeral(true);
        }

        if (!gameService.gameExists(gameId)) {
            // Add game to database
            gameService.createGame(gameId);
        }
        long channelId = channel.getId().asLong();
        // Add line to tracking table with gameid and channelid
        if (gameLinkService.linkExists(gameId, channelId)) {
            return event.reply().withEphemeral(true).withContent("[%s](%s/%s)  is already being tracked in <#%s>".formatted(gameView.name, imperaUrl, gameId, channelId));
        }
        GameChannelLink gameLink = gameLinkService.createLink(gameId, channelId, null);
        logger.debug("Created new GameChannelLink with gameId: " + gameLink.getGameId() + " and channelId: " + gameLink.getChannelId());
        return event.reply().withContent("Started tracking [%s](%s/%s) in <#%s>".formatted(gameView.name, imperaUrl, gameId, channelId));
    }
}
