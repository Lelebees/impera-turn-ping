package com.lelebees.imperabot.discord.domain.command.slash;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GuildSettingsService;
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
 * Stop tracking one or more games. <br>
 * <br>
 * - If nothing is specified, stop tracking ALL games in the channel the command is used in, or default channel if present. <br>
 * - If a game is specified, stop tracking that game in the channel the command is used in, or default channel if present. <br>
 * - If a channel is specified, stop tracking ALL games in that channel. (Guild only) <br>
 * - If a game and channel is specified, stop tracking that game in that channel. (Guild only) <br>
 */
@Component
public class UntrackCommand implements SlashCommand {
    private final static Logger logger = LoggerFactory.getLogger(UntrackCommand.class);
    private final GuildSettingsService guildSettingsService;
    private final GameLinkService gameLinkService;
    private final ImperaService imperaService;
    private final String imperaUrl;

    public UntrackCommand(GuildSettingsService guildSettingsService, GameLinkService gameLinkService, ImperaService imperaService, @Value("${impera.web.url}") String imperaUrl) {
        this.guildSettingsService = guildSettingsService;
        this.gameLinkService = gameLinkService;
        this.imperaService = imperaService;
        this.imperaUrl = imperaUrl + "/game/play";
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

        logger.info("User " + callingUser.getId().asLong() + " (" + callingUser.getUsername() + ") used /untrack with gameid: " + gameId + " in channel: " + channel.getId().asLong() + (guildIdOptional.isPresent() ? "(" + channel.getData().name().get() + ")" : "") + ". Context: " + (guildIdOptional.map(snowflake -> "Guild (" + snowflake.asLong() + ")").orElse("DM")));

        if (channelOptional.isPresent()) {
            channel = channelOptional.get().getValue().orElseThrow(() -> new NullPointerException("Somehow, no channel was entered")).asChannel().block();
        }

        if (guildIdOptional.isPresent()) {
            // We're in a guild, so untrack for guild
            Snowflake guildId = guildIdOptional.get();
            GuildSettings guildSettings = guildSettingsService.getOrCreateGuildSettings(guildId.asLong());
            if (guildSettings.defaultChannelId != null && channelOptional.isEmpty()) {
                channel = event.getInteraction().getGuild().block().getChannelById(Snowflake.of(guildSettings.defaultChannelId)).block();
                logger.info("No channel was specified, but a default channel was set, and the command was used in a guild, so untracking in channel: " + channel.getId().asLong() + " (" + channel.getData().name().get() + ").");
            }
            Member callingMember = callingUser.asMember(guildId).block();
            if (!callingMember.getBasePermissions().block().contains(Permission.MANAGE_CHANNELS)) {
                logger.info("User " + callingUser.getId() + " (" + callingUser.getUsername() + ") was denied access to /untrack because they do not have the correct permissions.");
                return event.reply().withContent("You are not allowed to stop tracking games in this guild.").withEphemeral(true);
            }
        }

        long channelId = channel.getId().asLong();
        if (gameId == null) {
            // Stop tracking all games in channel
            gameLinkService.deleteLinksForChannel(channelId);
            return event.reply().withContent("Stopped tracking notifications for all games in <#%s>".formatted(channelId));
        }
        // else, stop tracking specific game in channel
        gameLinkService.deleteLink(channelId, gameId);
        try {
            ImperaGameViewDTO gameView = imperaService.getGame(gameId);
            return event.reply().withContent("Stopped logging notifications for [%s](%s/%s) in <#%s>".formatted(gameView.name, imperaUrl, gameId, channelId));
        } catch (Exception e) {
            return event.reply().withContent("Stopped logging notifications for [%s]".formatted(gameId));
        }
    }
}
