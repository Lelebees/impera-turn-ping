package com.lelebees.imperabot.discord.application.listeners;

import com.lelebees.imperabot.discord.domain.command.SelectMenuInteraction;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class SelectMenuInteractionListener {
    private final Logger logger = LoggerFactory.getLogger(SelectMenuInteractionListener.class);
    private final Collection<SelectMenuInteraction> selectMenuInteractions;

    public SelectMenuInteractionListener(GatewayDiscordClient client, Collection<SelectMenuInteraction> selectMenuInteractions) {
        this.selectMenuInteractions = selectMenuInteractions;
        client.on(SelectMenuInteractionEvent.class, this::handle).subscribe();
    }

    public Mono<Void> handle(SelectMenuInteractionEvent event) {
        return Flux.fromIterable(selectMenuInteractions)
                .filter(selectMenu -> selectMenu.getCustomId().equals(event.getCustomId()))
                .next()
                .flatMap(selectMenu -> {
                    User user = event.getInteraction().getUser();
                    logger.debug("User " + user.getUsername() + " (" + user.getId().asLong() + ") entered a value into " + selectMenu.getCustomId() + " select.");
                    return selectMenu.handle(event);
                });
    }
}
