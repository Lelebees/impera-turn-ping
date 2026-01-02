package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.game.GameService;
import com.lelebees.imperabot.bot.application.game.exception.ChannelNotFoundException;
import com.lelebees.imperabot.bot.application.game.exception.GameNotFoundException;
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
 * Stop tracking one or more games. <br>
 * <br>
 * - If nothing is specified, stop tracking ALL games in the channel the command is used in, or default channel if present. <br>
 * - If a game is specified, stop tracking that game in the channel the command is used in, or default channel if present. <br>
 * - If a channel is specified, stop tracking ALL games in that channel. (Guild only) <br>
 * - If a game and channel is specified, stop tracking that game in that channel. (Guild only) <br>
 */
@Component
public class UntrackCommand implements SlashCommand {
    private final Logger logger = LoggerFactory.getLogger(UntrackCommand.class);
    private final GuildSettingsService guildSettingsService;
    private final ImperaService imperaService;
    private final String imperaUrl;
    private final GameService gameService;

    public UntrackCommand(GuildSettingsService guildSettingsService, ImperaService imperaService, @Value("${impera.web.url}") String imperaUrl, GameService gameService) {
        this.guildSettingsService = guildSettingsService;
        this.imperaService = imperaService;
        this.imperaUrl = imperaUrl + "/game/play";
        this.gameService = gameService;
    }

    @Override
    public String getName() {
        return "untrack";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        User callingUser = event.getInteraction().getUser();

        Optional<ApplicationCommandInteractionOption> gameOptional = event.getOption("gameid");
        Long gameId = null;
        if (gameOptional.isPresent()) {
            gameId = gameOptional.get().getValue().orElseThrow(() -> new NullPointerException("Somehow, no gameId was entered")).asLong();
        }

        Optional<ApplicationCommandInteractionOption> channelOptional = event.getOption("channel");
        Channel channel = event.getInteraction().getChannel().block();

        logger.info("User {} ({}) used /untrack with gameid: {} in channel: {}{}. Context: {}", callingUser.getId().asLong(), callingUser.getUsername(), gameId, channel.getId().asLong(), guildIdOptional.isPresent() ? " (" + channel.getData().name().get() + ")" : "", guildIdOptional.map(snowflake -> "Guild (" + snowflake.asLong() + ")").orElse("DM"));

        if (channelOptional.isPresent()) {
            channel = channelOptional.get().getValue().orElseThrow(() -> new NullPointerException("Somehow, no channel was entered")).asChannel().block();
        }

        if (guildIdOptional.isPresent()) {
            // We're in a guild, so untrack for guild
            Snowflake guildId = guildIdOptional.get();
            GuildSettingsDTO guildSettings;
            try {
                guildSettings = guildSettingsService.getGuildSettingsById(guildId.asLong());
            } catch (GuildSettingsNotFoundException e) {
                return event.reply().withContent("Could not find guild settings! use /guildsettings to create a settings list for this guild.").withEphemeral(true);
            }
            if (!guildSettings.hasTrackPermissions(callingUser) && !DiscordService.userIsLelebees(callingUser)) {
                logger.info("User {} ({}) was denied access to /untrack because they do not have the correct permissions.", callingUser.getId(), callingUser.getUsername());
                return event.reply().withContent("You are not allowed to stop tracking games in this guild.").withEphemeral(true);
            }

            if (guildSettings.defaultChannelId() != null && channelOptional.isEmpty()) {
                channel = event.getInteraction().getGuild().block().getChannelById(Snowflake.of(guildSettings.defaultChannelId())).block();
                logger.info("No channel was specified, but a default channel was set, and the command was used in a guild, so untracking in channel: {} ({}).", channel.getId().asLong(), channel.getData().name().get());
            }
        }

        Long channelId = channel.getId().asLong();
        if (gameId == null) {
            // Stop tracking all games in channel
            gameService.deleteLinksForChannel(channelId);
            return event.reply().withContent("Stopped tracking notifications for all games in <#%s>".formatted(channelId));
        }
        // else, stop tracking specific game in channel
        try {
            boolean gameWasBeingTracked = gameService.untrackGame(gameId, channelId);
            if (!gameWasBeingTracked) {
                return event.reply().withContent("Game [%s] is not being tracked in <#%s>.".formatted(gameId, channelId));
            }
        } catch (GameNotFoundException e) {
            logger.error("User {} ({}) attempted to stop tracking a game ({}) in channel <#{}> but the corresponding Game could not be found.", callingUser.getId().asLong(), callingUser.getUsername(), gameId, channelId);
            return event.reply().withContent("Game [%s] could not be found.".formatted(gameId)).withEphemeral(true);
        } catch (ChannelNotFoundException e) {
            logger.error("????");
        }
        try {
            ImperaGameViewDTO gameView = imperaService.getGame(gameId);
            return event.reply().withContent("Stopped logging notifications for [%s](%s/%s) in <#%s>".formatted(gameView.name(), imperaUrl, gameId, channelId));
        } catch (ImperaGameNotFoundException e) {
            return event.reply().withContent("Stopped logging notifications for [%s] in <#%s>".formatted(gameId, channelId));
        } catch (RuntimeException e) {
            logger.error("Unknown error occurred while attempting to fetch game from Impera. It has probably been deleted", e);
            return event.reply("An error occurred, but we still managed to stop logging notifications for [%s] in <#%s>. Please do file a bug report.".formatted(gameId, channelId));
        }
    }
}
