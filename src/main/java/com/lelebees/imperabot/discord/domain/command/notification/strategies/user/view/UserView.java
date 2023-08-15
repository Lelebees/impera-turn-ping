package com.lelebees.imperabot.discord.domain.command.notification.strategies.user.view;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

public class UserView implements NotificationCommandStrategy {

    private final UserService userService;

    public UserView(UserService userService) {
        this.userService = userService;
    }


    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        User user = event.getInteraction().getUser();
        long userId = user.getId().asLong();
        BotUser botUser = userService.findOrCreateUser(userId);
        PrivateChannel channel = user.getPrivateChannel().block();


        EmbedCreateSpec embed = EmbedCreateSpec.builder().title("Settings for " + user.getUsername()).addField("Channel: ", "<#" + channel.getId().asLong() + ">", false).addField("Default notification setting: ", "`" + botUser.getNotificationSetting().toString() + "`", false).color(Color.of(230, 200, 90)).build();

        return event.reply().withEphemeral(true).withEmbeds(embed);
    }
}
