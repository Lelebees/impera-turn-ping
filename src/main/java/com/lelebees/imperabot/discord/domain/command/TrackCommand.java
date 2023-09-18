package com.lelebees.imperabot.discord.domain.command;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GameService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.game.exception.GameNotFoundException;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.impera.application.ImperaService;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class TrackCommand implements SlashCommand {

    final ImperaService imperaService;
    final UserService userService;
    final GameService gameService;
    final GameLinkService gameLinkService;

    public TrackCommand(ImperaService imperaService, UserService userService, GameService gameService, GameLinkService gameLinkService) {
        this.imperaService = imperaService;
        this.userService = userService;
        this.gameService = gameService;
        this.gameLinkService = gameLinkService;
    }

    @Override
    public String getName() {
        return "track";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        User discordUser = event.getInteraction().getUser();
        Snowflake userId = discordUser.getId();

        Optional<ApplicationCommandInteractionOption> gameOption = event.getOption("game");
        long gameId = gameOption.flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asLong).orElseThrow(() -> new GameNotFoundException("No game was entered!"));

        Optional<ApplicationCommandInteractionOption> passwordOption = event.getOption("password");
        String password = null;

        BotUser user;
        try {
            user = userService.findUser(userId.asLong());
        } catch (UserNotFoundException e) {
            return event.reply().withEphemeral(true).withContent("Game [" + gameId + "] cannot be tracked because you are not linked to an impera account");
        }

        if (!user.isLinked()) {
            return event.reply().withEphemeral(true).withContent("Game [" + gameId + "] cannot be tracked because you are not linked to an impera account");
        }

        ImperaGameViewDTO gameDTO;
        //TODO: Handle just the 404
        try {
            gameDTO = imperaService.getGame(gameId);
        } catch (Exception e) {
            return event.reply().withContent("Game [" + gameId + "] cannot be tracked because it does not exist");
        }

        if (gameDTO.hasPassword) {
            try {
                password = passwordOption.orElseThrow(NullPointerException::new).getValue().orElseThrow(NullPointerException::new).asString();
            } catch (NullPointerException e) {
                return event.reply().withEphemeral(true).withContent("Cannot join game [" + gameId + "] because it has a password, and none was given!");
            }
        }

        // This gives a warning. This should be impossible, because we've already done an isLinked() check.
        boolean playerInGame = imperaService.isPlayerInGame(user.getImperaId().toString(), gameId);

        if (!playerInGame) {
            return event.reply().withEphemeral(true).withContent("Cannot track game [" + gameId + "] because you are not in it");
        }

        boolean botInGame = imperaService.isBotInGame(gameId);
        if (!botInGame) {
            boolean success = imperaService.joinGame(gameId, password);
            if (!success) {
                return event.reply().withEphemeral(true).withContent("Cannot track game [" + gameId + "] because something went wrong while joining");
            }
        }
        // We must get all users from the game, and if we have them, add their channels to the GameChannelLink-list
        // NOTE: What if the bot has already joined a game?
        // Solution: Don't do this here. Instead, in the same routine where we check if we can surrender, we also add all users that have joined the game.
        // This should not create problems for users who have already manually set their notification settings for that game.
        Game game = gameService.findOrCreateGame(gameId);
//        imperaService.surrenderGame(gameId);
        return event.reply().withEphemeral(true).withContent("Now tracking game [" + gameId + "]!");
    }
}
