package com.lelebees.imperabot.discord.application.listeners;

import com.lelebees.imperabot.discord.domain.command.ButtonCommand;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class ButtonCommandListener {

    private final static Logger logger = LoggerFactory.getLogger(ButtonCommandListener.class);
    private final Collection<ButtonCommand> buttons;

    public ButtonCommandListener(GatewayDiscordClient client, Collection<ButtonCommand> buttons) {
        this.buttons = buttons;
        client.on(ButtonInteractionEvent.class, this::handle).subscribe();

    }

    public Mono<Void> handle(ButtonInteractionEvent event) {
        return Flux.fromIterable(buttons)
                .filter(button -> button.getCustomId().equals(event.getCustomId()))
                .next()
                .flatMap(button -> {
                    User user = event.getInteraction().getUser();
                    logger.info("User " + user.getId().asLong() + " (" + user.getUsername() + ") clicked " + button.getCustomId() + " button.");
                    return button.handle(event);
                });
    }
}
