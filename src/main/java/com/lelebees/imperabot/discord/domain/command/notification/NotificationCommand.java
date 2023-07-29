package com.lelebees.imperabot.discord.domain.command.notification;

import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.discord.domain.command.SlashCommand;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.command.notification.exception.IncorrectPermissionException;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.NotificationCommandStrategy;
import com.lelebees.imperabot.discord.domain.command.notification.strategies.guild.set.*;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NotificationCommand implements SlashCommand {
    private final Map<Set<String>, NotificationCommandStrategy> strategyMap;
    private final GuildSettingsService guildSettingsService;

    public NotificationCommand(GuildSettingsService guildSettingsService) {
        this.guildSettingsService = guildSettingsService;
        strategyMap = new HashMap<>();
        // Populate the strategy map with option combinations and corresponding strategies

        //Guild
        //Set
        strategyMap.put(Set.of("guild", "set", "channel"), new GuildSetChannel(guildSettingsService));
        strategyMap.put(Set.of("guild", "set", "channel", "gameid"), new GuildSetChannelGame());
        strategyMap.put(Set.of("guild", "set", "channel", "gameid", "setting"), new GuildSetChannelGameSetting());
        strategyMap.put(Set.of("guild", "set", "channel", "setting"), new GuildSetChannelSetting());
        strategyMap.put(Set.of("guild", "set", "gameid"), new GuildSetGame());
        strategyMap.put(Set.of("guild", "set", "gameid", "setting"), new GuildSetGameSetting());
        strategyMap.put(Set.of("guild", "set", "setting"), new GuildSetSetting(guildSettingsService));
        //View

        //User
        //Set

        //View
    }

    @Override
    public String getName() {
        return "notifications";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Set<String> options = new HashSet<>();
        ApplicationCommandInteractionOption guildOrUser = event.getOptions().get(0);
        ApplicationCommandInteractionOption setOrView = guildOrUser.getOptions().get(0);
        options.add(guildOrUser.getName());
        options.add(setOrView.getName());
        options.addAll(setOrView.getOptions().stream()
                .map(ApplicationCommandInteractionOption::getName)
                .collect(Collectors.toSet()));

        System.out.println(options);

        NotificationCommandStrategy strategy = strategyMap.get(options);

        if (strategy != null) {
            try {
                return strategy.execute(event);
            } catch (IncorrectContextException e) {
                return event.reply().withEphemeral(true).withContent("Cannot use `/notifications guild` in private messages!");
            } catch (IncorrectPermissionException e) {
                return event.reply().withEphemeral(true).withContent("Cannot use `/notifications guild` without the Manage Channels permission!");
            }
        }
        // Handle the case when no valid option combination is provided
        return event.reply().withEphemeral(true).withContent("Please choose at least one valid option");
    }
}