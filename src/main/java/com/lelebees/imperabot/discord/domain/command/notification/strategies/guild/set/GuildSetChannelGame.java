package com.lelebees.imperabot.discord.domain.command.notification.strategies.guild.set;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.guild.GuildSettings;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotInGameException;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectPermissionException;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import com.lelebees.imperabot.impera.application.ImperaService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

// Starts tracking a given game in a given channel, with default setting
public class GuildSetChannelGame implements NotificationCommandStrategy {
    private final GuildSettingsService guildSettingsService;
    private final GameLinkService gameLinkService;
    private final UserService userService;
    private final ImperaService imperaService;

    public GuildSetChannelGame(GuildSettingsService guildSettingsService, GameLinkService gameLinkService, UserService userService, ImperaService imperaService) {
        this.guildSettingsService = guildSettingsService;
        this.gameLinkService = gameLinkService;
        this.userService = userService;
        this.imperaService = imperaService;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        if (guildIdOptional.isEmpty()) {
            throw new IncorrectContextException("You cannot run a guild command outside of a guild!");
        }

        Optional<Member> callingUser = event.getInteraction().getMember();
        if (callingUser.isEmpty()) {
            throw new IllegalStateException("No one sent this command???");
        }

        //TODO: Fix these errors
        if (!callingUser.get().getBasePermissions().block().contains(Permission.MANAGE_CHANNELS)) {
            throw new IncorrectPermissionException("You do not have the correct permissions!");
        }

        BotUser user = userService.findUser(callingUser.get().getId().asLong());

        Optional<ApplicationCommandInteractionOptionValue> channelInput = event.getOptions().get(0).getOptions().get(0).getOption("channel").orElseThrow(() -> new NullPointerException("This is impossible, How could channel not exist?!")).getValue();
        Optional<ApplicationCommandInteractionOptionValue> gameInput = event.getOptions().get(0).getOptions().get(0).getOption("gameid").orElseThrow(() -> new NullPointerException("This is impossible, How could gameid not exist?!")).getValue();
        if (gameInput.isEmpty()) {
            throw new NullPointerException("HOW IS THIS POSSIBLE?! NO GAME?!");
        }
        long gameid = gameInput.get().asLong();

        if (channelInput.isEmpty()) {
            throw new NullPointerException("No channel?");
        }

        Channel channel = channelInput.get().asChannel().block();
        long channelId = channel.getId().asLong();

        UUID imperaId = user.getImperaId();
        if (imperaId == null) {
            return event.reply().withEphemeral(true).withContent("Cannot log notifications for game because you do not have an Impera account linked");
        }

        if (!imperaService.isPlayerInGame(user.getImperaId().toString(), gameid)) {
            throw new UserNotInGameException("You are not allowed to access this game!");
        }

        GuildSettings settings = guildSettingsService.getOrCreateGuildSettings(guildIdOptional.get().asLong());
        int defaultNotificationSetting = settings.notificationSetting;

        GameChannelLink link = gameLinkService.findOrCreateLink(gameid, channelId, null);
        return event.reply().withContent("Started logging notifications for game [" + link.getGameId() + "] in <#" + link.getChannelId() + "> with default notification setting (Currently: `" + GuildNotificationSettings.values()[defaultNotificationSetting].toString() + "`)");

    }
}
