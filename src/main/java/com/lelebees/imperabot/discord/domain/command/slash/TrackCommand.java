package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.game.GameService;
import com.lelebees.imperabot.bot.application.guild.GuildSettingsService;
import com.lelebees.imperabot.bot.application.guild.exception.GuildSettingsNotFoundException;
import com.lelebees.imperabot.bot.presentation.guildsettings.GuildSettingsDTO;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.game.exception.ImperaGameNotFoundException;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
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
    private final GuildSettingsService guildSettingsService;
    private final String imperaUrl;

    public TrackCommand(GameService gameService, ImperaService imperaService, GuildSettingsService guildSettingsService, @Value("${impera.web.url}") String imperaUrl) {
        this.gameService = gameService;
        this.imperaService = imperaService;
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
        long channelId = channel.getId().asLong();
        logger.info("User {} ({}) used /track with gameid: {} in channel: {}{}. Context: {}", callingUser.getId().asLong(), callingUser.getUsername(), gameId, channelId, guildIdOptional.isPresent() ? "(" + channel.getData().name().get() + ")" : "", guildIdOptional.map(snowflake -> "Guild (" + snowflake.asLong() + ")").orElse("DM"));

        if (channelOptional.isPresent()) {
            channel = channelOptional.get()
                    .getValue()
                    .orElseThrow(() -> new NullPointerException("Somehow, no channel was entered"))
                    .asChannel()
                    .block();
            logger.info("Channel was specified, so tracking in channel: {} ({}).", channelId, channel.getData().name().get());
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
            if (!guildSettings.hasTrackPermissions(callingUser) && !DiscordService.userIsLelebees(callingUser)) {
                logger.info("User {} ({}) was denied access to /track because they do not have the correct permissions.", callingUser.getId().asLong(), callingUser.getUsername());
                return event.reply().withContent("You are not allowed to track games in this guild.").withEphemeral(true);
            }

            if (guildSettings.defaultChannelId() != null && channelOptional.isEmpty()) {
                channel = event.getInteraction().getGuild().block().getChannelById(Snowflake.of(guildSettings.defaultChannelId())).block();
                logger.info("No channel was specified, but a default channel was set, and the command was used in a guild, so tracking in channel: {} ({}).", channelId, channel.getData().name().get());
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

        boolean alreadyTracked = gameService.trackGame(gameView, channelId);
        if (alreadyTracked) {
            return event.reply().withContent("[%s](%s/%s)  is already being tracked in <#%s>".formatted(gameView.name(), imperaUrl, gameId, channelId)).withEphemeral(true);
        }

        logger.debug("Created new GameChannelLink with gameId: {} and channelId: {}", gameId, channelId);
        return event.reply().withContent("Started tracking [%s](%s/%s) in <#%s>".formatted(gameView.name(), imperaUrl, gameId, channelId));
    }
}
