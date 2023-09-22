package com.lelebees.imperabot.discord.domain.command.notification.strategies.user.set;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.discord.application.NotificationService;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import com.lelebees.imperabot.impera.application.ImperaService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public class UserSetGame implements NotificationCommandStrategy {

    private final UserService userService;
    private final ImperaService imperaService;
    private final NotificationService notificationService;

    public UserSetGame(UserService userService, ImperaService imperaService, NotificationService notificationService) {
        this.userService = userService;
        this.imperaService = imperaService;
        this.notificationService = notificationService;
    }


    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        return event.reply().withContent("Command is deprecated, sorry!").withEphemeral(true);
//        User callingUser = event.getInteraction().getUser();
//
//        Optional<ApplicationCommandInteractionOptionValue> gameInput = event.getOptions().get(0).getOptions().get(0).getOption("gameid").orElseThrow(() -> new NullPointerException("This is impossible, How could gameid not exist?!")).getValue();
//        if (gameInput.isEmpty()) {
//            throw new NullPointerException("HOW IS THIS POSSIBLE?! NO GAME?!");
//        }
//        long gameid = gameInput.get().asLong();
//
//        BotUser user = userService.findUser(callingUser.getId().asLong());
//        UUID imperaId = user.getImperaId();
//        if (imperaId == null) {
//            return event.reply().withEphemeral(true).withContent("Cannot log notifications for game because you do not have an Impera account linked");
//        }
//
//        if (!imperaService.isPlayerInGame(user.getImperaId().toString(), gameid)) {
//            throw new UserNotInGameException("You are not allowed to access this game!");
//        }
//        return notificationService.userSetGame(event, gameid, callingUser, null);
    }
}
