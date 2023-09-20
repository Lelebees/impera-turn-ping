package com.lelebees.imperabot.discord.domain.command.notification.strategies.user.view;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.NotificationSettings;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameLinkId;
import com.lelebees.imperabot.bot.domain.user.UserNotificationSetting;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

public class UserViewGame implements NotificationCommandStrategy {
    private final GameLinkService gameLinkService;
    private final UserService userService;

    public UserViewGame(GameLinkService gameLinkService, UserService userService) {
        this.gameLinkService = gameLinkService;
        this.userService = userService;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        User callingUser = event.getInteraction().getUser();
        PrivateChannel channel = callingUser.getPrivateChannel().block();

        Optional<ApplicationCommandInteractionOptionValue> gameInput = event.getOptions().get(0).getOptions().get(0).getOption("gameid").orElseThrow(() -> new NullPointerException("This is impossible, How could gameid not exist?!")).getValue();
        if (gameInput.isEmpty()) {
            throw new NullPointerException("HOW IS THIS POSSIBLE?! NO GAME?!");
        }
        long gameid = gameInput.get().asLong();


        GameChannelLink link = gameLinkService.findLink(new GameLinkId(gameid, channel.getId().asLong()));
        NotificationSettings notificationSetting = Objects.requireNonNullElseGet(UserNotificationSetting.values()[link.notificationSetting], () -> userService.findUser(callingUser.getId().asLong()).notificationSetting);

        EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .title("Settings for game [" + gameid + "]")
                .addField("Setting: ", "`" + notificationSetting + "`", false)
                .addField("Channel: ", "<#" + channel.getId() + ">", false)
                .build();

        return event.reply().withEmbeds(embed).withEphemeral(true);
    }
}
