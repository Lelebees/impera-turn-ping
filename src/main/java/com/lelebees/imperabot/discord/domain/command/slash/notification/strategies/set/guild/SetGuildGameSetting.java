package com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.set.guild;

import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.guild.GuildNotificationSettings;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotInGameException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotVerifiedException;
import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.discord.application.NotificationService;
import com.lelebees.imperabot.discord.domain.command.slash.notification.exception.IncorrectContextException;
import com.lelebees.imperabot.discord.domain.command.slash.notification.strategies.NotificationCommandStrategy;
import com.lelebees.imperabot.discord.domain.exception.NoDefaultChannelException;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

import java.util.Optional;

//Starts tracking a game on the default channel with given setting
public class SetGuildGameSetting implements NotificationCommandStrategy {

    private final NotificationService notificationService;
    private final DiscordService discordService;

    public SetGuildGameSetting(NotificationService notificationService, DiscordService discordService) {
        this.notificationService = notificationService;
        this.discordService = discordService;
    }

    @Override
    public Mono<Void> execute(ChatInputInteractionEvent event) {
        Optional<Snowflake> guildIdOptional = event.getInteraction().getGuildId();
        if (guildIdOptional.isEmpty()) {
            throw new IncorrectContextException("You cannot run a guild command outside of a guild!");
        }

        Optional<Member> callingUser = event.getInteraction().getMember();
        if (callingUser.isEmpty()) {
            throw new IllegalStateException("No one sent this command???");
        }

        long gameid = discordService.getGameIdOption(event);
        GuildNotificationSettings setting = discordService.getGuildSettingOption(event);

        try {
            GameChannelLink gameChannelLink = notificationService.setGuildGame(guildIdOptional.get().asLong(), gameid, null, setting, callingUser.get());
            if (gameChannelLink == null) {
                return event.reply().withContent("Stopped logging notifications for [" + notificationService.getGameName(gameid) + "](https://imperaonline.de/game/play/" + gameid + ")");
            }
            return event.reply().withContent("Logging notifications for [" + notificationService.getGameName(gameChannelLink.getGameId()) + "](https://imperaonline.de/game/play/" + gameChannelLink.getGameId() + ") in default channel (<#" + gameChannelLink.getChannelId() + ">)");
        } catch (UserNotVerifiedException e) {
            return event.reply().withContent("You are not linked to an Impera account!").withEphemeral(true);
        } catch (UserNotInGameException e) {
            return event.reply().withContent("You are not allowed to access this game!").withEphemeral(true);
        } catch (NoDefaultChannelException e) {
            return event.reply().withContent("No default channel has been set for this guild. Please set one with `/notifications set guild <channel>`").withEphemeral(true);
        }
    }
}
