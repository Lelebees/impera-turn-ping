package com.lelebees.imperabot.discord.domain.command.notification.strategies;

import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotInGameException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotVerifiedException;
import com.lelebees.imperabot.discord.application.NotificationService;
import com.lelebees.imperabot.impera.domain.game.exception.ImperaGameNotFoundException;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class TrackGame implements NotificationCommandStrategy {
    private final NotificationService notificationService;

    public TrackGame(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        // Dip into track, then get the gameID
        Optional<ApplicationCommandInteractionOption> gameOption = event.getOptions().get(0).getOption("gameid");
        long gameId = gameOption.flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asLong).orElseThrow(() -> new NullPointerException("No game was entered!"));

        User discordUser = event.getInteraction().getUser();
        Snowflake userId = discordUser.getId();

        try {
            Game game = notificationService.trackGame(gameId, userId.asLong());
            return event.reply().withEphemeral(true).withContent("Now tracking [" + game.getId() + "]");
        } catch (UserNotVerifiedException e) {
            return event.reply().withEphemeral(true).withContent("Game [" + gameId + "] cannot be tracked because you are not linked to an impera account");
        } catch (UserNotInGameException e) {
            return event.reply().withEphemeral(true).withContent("Cannot track game [" + gameId + "] because you are not in it");
        } catch (ImperaGameNotFoundException e) {
            return event.reply().withEphemeral(true).withContent("Game [" + gameId + "] could not be found");
        }
    }
}
