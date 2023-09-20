package com.lelebees.imperabot.discord.domain.command.notification.strategies.guild.set;

import com.lelebees.imperabot.bot.application.GuildSettingsService;
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
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

//Starts tracking a game on the default channel with given setting
public class GuildSetGameSetting implements NotificationCommandStrategy {

    private final GuildSettingsService guildSettingsService;
    private final UserService userService;
    private final ImperaService imperaService;

    private final NotificationService notificationService;

    public GuildSetGameSetting(GuildSettingsService guildSettingsService, UserService userService, ImperaService imperaService, NotificationService notificationService) {
        this.guildSettingsService = guildSettingsService;
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

        Optional<Member> callingUser = event.getInteraction().getMember();
        if (callingUser.isEmpty()) {
            throw new IllegalStateException("No one sent this command???");
        }

        //TODO: Fix these errors
        if (!callingUser.get().getBasePermissions().block().contains(Permission.MANAGE_CHANNELS)) {
            throw new IncorrectPermissionException("You do not have the correct permissions!");
        }


        Optional<ApplicationCommandInteractionOptionValue> gameInput = event.getOptions().get(0).getOptions().get(0).getOption("gameid").orElseThrow(() -> new NullPointerException("This is impossible, How could gameid not exist?!")).getValue();
        long gameid = gameInput.orElseThrow(() -> new NullPointerException("HOW IS THIS POSSIBLE?! NO GAME?!")).asLong();

        Optional<ApplicationCommandInteractionOptionValue> settingInput = event.getOptions().get(0).getOptions().get(0).getOption("setting").orElseThrow(() -> new NullPointerException("setting argument not present")).getValue();
        long settingLong = settingInput.orElseThrow(() -> new NullPointerException("No setting entered")).asLong();
        int setting = Math.toIntExact(settingLong);

        BotUser user = userService.findUser(callingUser.get().getId().asLong());
        UUID imperaId = user.getImperaId();
        if (imperaId == null) {
            return event.reply().withEphemeral(true).withContent("Cannot log notifications for game because you do not have an Impera account linked");
        }

        if (!imperaService.isPlayerInGame(user.getImperaId().toString(), gameid)) {
            throw new UserNotInGameException("You are not allowed to access this game!");
        }

        return notificationService.guildSetGame(event, gameid, null, GuildNotificationSettings.values()[setting]);
    }
}
