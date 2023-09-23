package com.lelebees.imperabot.discord.domain.command.notification.strategies.set.guild;

import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotInGameException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotVerifiedException;
import com.lelebees.imperabot.discord.application.NotificationService;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import com.lelebees.imperabot.discord.domain.exception.NoDefaultChannelException;
import com.lelebees.imperabot.impera.application.ImperaService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;
import reactor.core.publisher.Mono;

import java.util.Optional;

// Starts tracking a given game in a given channel, with default setting
public class SetGuildChannelGame implements NotificationCommandStrategy {
    private final GuildSettingsService guildSettingsService;
    private final UserService userService;
    private final ImperaService imperaService;
    private final NotificationService notificationService;

    public SetGuildChannelGame(GuildSettingsService guildSettingsService, UserService userService, ImperaService imperaService, NotificationService notificationService) {
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

        try {
            GameChannelLink gameChannelLink = notificationService.setGuildGame(guildIdOptional.get().asLong(), gameid, channelId, null, callingUser.get());
            if (gameChannelLink == null) {
                return event.reply().withContent("Stopped logging notifications for [" + gameid + "]");
            }
            return event.reply().withContent("Logging notifications for [" + gameChannelLink.getGameId() + "] in default channel (<#" + gameChannelLink.getChannelId() + ">)");
        } catch (UserNotVerifiedException e) {
            return event.reply().withContent("You are not linked to an Impera account!").withEphemeral(true);
        } catch (UserNotInGameException e) {
            return event.reply().withContent("You are not allowed to access this game!").withEphemeral(true);
        } catch (NoDefaultChannelException e) {
            return event.reply().withContent("No default channel has been set for this guild. Please set one with `/notifications set guild <channel>`").withEphemeral(true);
        }
    }
}
