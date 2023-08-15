package com.lelebees.imperabot.discord.domain.command.notification.strategies.guild.set;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotInGameException;
import com.lelebees.imperabot.discord.application.NotificationService;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectPermissionException;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import com.lelebees.imperabot.impera.application.ImperaService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

// Sets the setting for a given channel/game combination to the given setting
public class GuildSetChannelGameSetting implements NotificationCommandStrategy {
    private final UserService userService;
    private final ImperaService imperaService;
    private final NotificationService notificationService;

    public GuildSetChannelGameSetting(UserService userService, ImperaService imperaService, NotificationService notificationService) {
        this.userService = userService;
        this.imperaService = imperaService;
        this.notificationService = notificationService;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        if (guildIdOptional.isEmpty()) {
            throw new IncorrectContextException("You cannot run a guild command outside of a guild!");
        }

        User callingUser = event.getInteraction().getUser();
        //TODO: Fix these errors
        Member callingMember = callingUser.asMember(guildIdOptional.get()).block();
        if (!callingMember.getBasePermissions().block().contains(Permission.MANAGE_CHANNELS)) {
            throw new IncorrectPermissionException("You do not have the correct permissions!");
        }


        Optional<ApplicationCommandInteractionOptionValue> gameInput = event.getOptions().get(0).getOptions().get(0).getOption("gameid").orElseThrow(() -> new NullPointerException("This is impossible, How could gameid not exist?!")).getValue();
        Optional<ApplicationCommandInteractionOptionValue> channelInput = event.getOptions().get(0).getOptions().get(0).getOption("channel").orElseThrow(() -> new NullPointerException("This is impossible, How could channel not exist?!")).getValue();
        Optional<ApplicationCommandInteractionOptionValue> settingInput = event.getOptions().get(0).getOptions().get(0).getOption("setting").orElseThrow(() -> new NullPointerException("This is impossible, How could setting not exist?!")).getValue();
        if (gameInput.isEmpty()) {
            throw new NullPointerException("HOW IS THIS POSSIBLE?! NO GAME?!");
        }
        long gameid = gameInput.get().asLong();

        if (channelInput.isEmpty()) {
            throw new NullPointerException("No channel?");
        }

        Channel channel = channelInput.get().asChannel().block();
        long channelId = channel.getId().asLong();

        if (settingInput.isEmpty()) {
            throw new NullPointerException("No Setting?");
        }

        long settingLong = settingInput.get().asLong();
        int setting = Math.toIntExact(settingLong);

        BotUser user = userService.findUser(callingUser.getId().asLong());
        UUID imperaId = user.getImperaId();
        if (imperaId == null) {
            return event.reply().withEphemeral(true).withContent("Cannot log notifications for game because you do not have an Impera account linked");
        }

        if (!imperaService.isPlayerInGame(user.getImperaId().toString(), gameid)) {
            throw new UserNotInGameException("You are not allowed to access this game!");
        }
        return notificationService.guildSetGame(event, gameid, channelId, GuildNotificationSettings.values()[setting]);
    }
}
