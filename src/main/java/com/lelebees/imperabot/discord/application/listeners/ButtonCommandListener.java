package com.lelebees.imperabot.discord.application.listeners;

import com.lelebees.imperabot.discord.domain.command.button.ButtonCommand;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class ButtonCommandListener {

    private final Collection<ButtonCommand> buttons;

    public ButtonCommandListener(GatewayDiscordClient client, Collection<ButtonCommand> buttons) {
        this.buttons = buttons;
        client.on(ButtonInteractionEvent.class, this::handle).subscribe();

    }

    public Mono<Void> handle(ButtonInteractionEvent event) {
        return Flux.fromIterable(buttons)
                .filter(button -> button.getCustomId().equals(event.getCustomId()))
                .next()
                .flatMap(button -> button.handle(event));
    }
}
