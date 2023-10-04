package com.lelebees.imperabot.discord.domain.command.slash.notification;

import com.lelebees.imperabot.bot.application.GameLinkService;
import com.lelebees.imperabot.bot.application.GuildSettingsService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.gamechannellink.exception.GameChannelLinkNotFoundException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotFoundException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotInGameException;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.discord.application.NotificationService;
import com.lelebees.imperabot.discord.domain.command.slash.SlashCommand;
import com.lelebees.imperabot.discord.domain.command.slash.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.command.slash.notification.exception.IncorrectPermissionException;
import com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.NotificationCommandStrategy;
import com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.TrackGame;
import com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.set.guild.*;
import com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.set.user.SetUserSetting;
import com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.view.guild.ViewGuild;
import com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.view.user.ViewUser;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NotificationCommand implements SlashCommand {
    private final Map<Set<String>, NotificationCommandStrategy> strategyMap;
    private static final Logger logger = LoggerFactory.getLogger(NotificationCommand.class);

    public NotificationCommand(GuildSettingsService guildSettingsService, UserService userService, NotificationService notificationService, DiscordService discordService, GameLinkService gameLinkService) {
        strategyMap = new HashMap<>();
        // Populate the strategy map with option combinations and corresponding strategies

        // Set
        // Guild
        strategyMap.put(Set.of("set", "guild", "channel"), new SetGuildChannel(guildSettingsService, notificationService, discordService));
        strategyMap.put(Set.of("set", "guild", "channel", "gameid"), new SetGuildChannelGame(notificationService, discordService));
        strategyMap.put(Set.of("set", "guild", "channel", "gameid", "setting"), new SetGuildChannelGameSetting(notificationService, discordService));
        strategyMap.put(Set.of("set", "guild", "channel", "setting"), new SetGuildChannelSetting(discordService, gameLinkService, notificationService));
        strategyMap.put(Set.of("set", "guild", "gameid"), new SetGuildGame(notificationService, discordService));
        strategyMap.put(Set.of("set", "guild", "gameid", "setting"), new SetGuildGameSetting(notificationService, discordService));
        strategyMap.put(Set.of("set", "guild", "setting"), new SetGuildSetting(guildSettingsService, notificationService, discordService));
        // User
        strategyMap.put(Set.of("set", "user", "setting"), new SetUserSetting(userService, discordService));

        // View
        // Guild
        strategyMap.put(Set.of("view", "guild"), new ViewGuild(guildSettingsService));
        // User
        strategyMap.put(Set.of("view", "user"), new ViewUser(notificationService));

        // Track
        strategyMap.put(Set.of("track", "gameid"), new TrackGame(notificationService));
    }

    @Override
    public String getName() {
        return "notifications";
    }

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        Set<String> options = new HashSet<>();
        ApplicationCommandInteractionOption setOrViewOrTrack = event.getOptions().get(0);
        ApplicationCommandInteractionOption guildOrUser = setOrViewOrTrack.getOptions().get(0);
        options.add(setOrViewOrTrack.getName());
        options.add(guildOrUser.getName());
        options.addAll(guildOrUser.getOptions().stream()
                .map(ApplicationCommandInteractionOption::getName)
                .collect(Collectors.toSet()));

        logger.info(options.toString());

        NotificationCommandStrategy strategy = strategyMap.get(options);

        // Handle the case when no valid option combination is provided
        if (strategy == null) {
            return event.reply().withEphemeral(true).withContent("Please choose at least one valid option.");
        }

        try {
            return strategy.execute(event);
        } catch (IncorrectContextException e) {
            return event.reply().withEphemeral(true).withContent("Cannot use `/notifications guild` in private messages.");
        } catch (IncorrectPermissionException e) {
            return event.reply().withEphemeral(true).withContent("Cannot use `/notifications guild` without the Manage Channels permission.");
        } catch (UserNotFoundException e) {
            return event.reply().withEphemeral(true).withContent("Cannot use this command if you are not registered with the service. Please use /link first.");
        } catch (UserNotInGameException e) {
            return event.reply().withEphemeral(true).withContent("You are not allowed to keep track of this game.");
        } catch (GameChannelLinkNotFoundException e) {
            return event.reply().withEphemeral(true).withContent("Cannot find the combination of game and channel. Perhaps you need to use `/notifications set` first?");
        } catch (HttpClientErrorException.BadRequest e) {
            return event.reply().withEphemeral(true).withContent("Something went wrong, but due to API limitations, we cannot assert what. Please check if you have entered the correct information");
        }
    }
}