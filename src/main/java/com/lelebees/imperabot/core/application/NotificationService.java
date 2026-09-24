package com.lelebees.imperabot.core.application;

import com.lelebees.imperabot.discord.application.DiscordService;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGamePlayerDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.history.HistoryActionName;
import com.lelebees.imperabot.user.application.UserService;
import com.lelebees.imperabot.user.application.dto.BotUserDTO;
import com.lelebees.imperabot.user.domain.UserNotificationSetting;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildMessageChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.AllowedMentions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.lelebees.imperabot.user.domain.UserNotificationSetting.*;
import static discord4j.rest.util.Permission.VIEW_CHANNEL;

@Service
public class NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final String imperaURL;
    private final UserService userService;
    private final DiscordService discordService;

    public NotificationService(UserService userService, DiscordService discordService, @Value("${impera.web.url}") String imperaURL) {
        this.userService = userService;
        this.imperaURL = imperaURL;
        this.discordService = discordService;
    }

    public void sendNewTurnMessage(List<GuildMessageChannel> guildChannels, List<PrivateChannel> dmChannels, ImperaGameViewDTO game) {
        String turnMessage = "your turn in %s!".formatted(getGameURI(game));
        String directTurnMessage = "It's " + turnMessage;
        String generalTurnMessage = "%s, it is " + turnMessage;
        ordinaryNotify(guildChannels, dmChannels, game.currentPlayer(), generalTurnMessage, directTurnMessage);
    }

    public void sendHalfTimeMessage(List<GuildMessageChannel> guildChannels, List<PrivateChannel> dmChannels, ImperaGameViewDTO game) {
        String halfTimeMessage = "have half time remaining in %s!".formatted(getGameURI(game));
        String directHalfTimeMessage = "You " + halfTimeMessage;
        String generalHalfTimeMessage = "%s, you " + halfTimeMessage;
        ordinaryNotify(guildChannels, dmChannels, game.currentPlayer(), generalHalfTimeMessage, directHalfTimeMessage);
    }

    public void sendLoserMessage(List<GuildMessageChannel> guildChannels, List<PrivateChannel> dmChannels, ImperaGamePlayerDTO player, ImperaGameViewDTO game, HistoryActionName outcome) {
        switch (outcome) {
            case LOST -> sendDefeatedMessage(guildChannels, dmChannels, player, game);
            case SURRENDERED -> sendSurrenderMessage(guildChannels, dmChannels, player, game);
            case TIMED_OUT -> sendTimedOutMessage(guildChannels, dmChannels, player, game);
        }
    }

    public void sendDefeatedMessage(List<GuildMessageChannel> guildChannels, List<PrivateChannel> dmChannels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        logger.info("Sending defeated notice for {} ({})!", game.name(), game.id());
        String defeatedMessage = "been defeated in %s!".formatted(getGameURI(game));
        String directDefeatedMessage = "You have " + defeatedMessage;
        String generalDefeatedMessage = "%s has " + defeatedMessage;
        ordinaryNotify(guildChannels, dmChannels, gamePlayer, generalDefeatedMessage, directDefeatedMessage);
    }

    public void sendTimedOutMessage(List<GuildMessageChannel> guildChannels, List<PrivateChannel> dmChannels, ImperaGamePlayerDTO player, ImperaGameViewDTO game) {
        logger.info("Sending timed out notice for {} ({})!", game.name(), game.id());
        String timedOutMessage = "timed out in %s!".formatted(getGameURI(game));
        String directTimeOutMessage = "You have " + timedOutMessage;
        String generalTimeOutMessage = "%s has " + timedOutMessage;
        ordinaryNotify(guildChannels, dmChannels, player, generalTimeOutMessage, directTimeOutMessage);
    }

    public void sendSurrenderMessage(List<GuildMessageChannel> guildChannels, List<PrivateChannel> dmChannels, ImperaGamePlayerDTO gamePlayer, ImperaGameViewDTO game) {
        logger.info("Sending surrendered notice for {} ({})!", game.name(), game.id());
        String generalSurrenderMessage = "%s has surrendered in %s!".formatted(gamePlayer.name(), getGameURI(game));
        for (MessageChannel channel : guildChannels) {
            channel.createMessage(generalSurrenderMessage).block();
        }
        for (MessageChannel channel : dmChannels) {
            channel.createMessage(generalSurrenderMessage).block();
        }
    }

    public String getGameURI(ImperaGameViewDTO game) {
        return "[%s](%s/game/play/%s)".formatted(game.name(), imperaURL, game.id());
    }

    public void sendVictorsMessage(List<GuildMessageChannel> guildChannels, List<PrivateChannel> dmChannels, List<ImperaGamePlayerDTO> winningPlayers, ImperaGameViewDTO game) {
        List<String> userStrings = new ArrayList<>();
        String victoryMessage = "Game %s has ended! %s";
        for (ImperaGamePlayerDTO gamePlayer : winningPlayers) {
            Optional<BotUserDTO> user = userService.findImperaUser(UUID.fromString(gamePlayer.userId()));
            String userString = gamePlayer.name();
            if (user.isPresent()) {
                BotUserDTO player = user.get();
                userString = getUserStringDependingOnSettings(player);
                Snowflake targetUser = Snowflake.of(player.discordId());
                sendDMAccordingToSettings(player, victoryMessage.formatted(getGameURI(game), "You have won!"), guildChannels.isEmpty());
                dmChannels.removeIf(channel -> channel.getRecipientIds().contains(targetUser));
            }
            userStrings.add(userString);
        }
        String singledUser = userStrings.remove(0);
        String generalVictoryMessage = victoryMessage.formatted(getGameURI(game), "%s and %s have won!".formatted(String.join(", ", userStrings), singledUser));
        if (userStrings.isEmpty()) {
            generalVictoryMessage = victoryMessage.formatted(getGameURI(game), "%s has won!".formatted(singledUser));
        }
        for (MessageChannel channel : guildChannels) {
            channel.createMessage(generalVictoryMessage).block();
        }
        for (MessageChannel channel : dmChannels) {
            channel.createMessage(generalVictoryMessage).block();
        }
    }

    private AllowedMentions getAllowedMentions(BotUserDTO player) {
        return switch (player.notificationSetting()) {
            case NO_NOTIFICATIONS, DMS_ONLY -> AllowedMentions.suppressAll();
            case DMS_AND_GUILD, PREFER_GUILD_OVER_DMS, GUILD_ONLY ->
                    AllowedMentions.builder().allowUser(Snowflake.of(player.discordId())).build();
        };
    }

    private void sendDMAccordingToSettings(BotUserDTO user, String directMessage, boolean noGuildChannels) {
        UserNotificationSetting setting = user.notificationSetting();
        if (setting == DMS_ONLY || setting == DMS_AND_GUILD || (setting == PREFER_GUILD_OVER_DMS && noGuildChannels)) {
            discordService.sendDM(Snowflake.of(user.discordId()), directMessage);
        }
    }

    private void ordinaryNotify(List<GuildMessageChannel> guildChannels, List<PrivateChannel> dmChannels, ImperaGamePlayerDTO gamePlayer, String generalMessage, String directMessage) {
        AllowedMentions allowedMentions = AllowedMentions.suppressAll();
        String userString = gamePlayer.name();
        Optional<BotUserDTO> userOptional = userService.findImperaUser(UUID.fromString(gamePlayer.userId()));
        if (userOptional.isPresent()) {
            BotUserDTO user = userOptional.get();
            userString = user.getMention();
            Snowflake userId = Snowflake.of(user.discordId());

            PrivateChannel usersChannel = discordService.getDMChannelByOwner(userId);
            if (dmChannels.contains(usersChannel)) {
                sendDMAccordingToSettings(user, directMessage.formatted(userString), !canUserSeeMessageInAGuild(guildChannels, user));
                dmChannels.remove(usersChannel);
            }
            allowedMentions = getAllowedMentions(user);
        }

        AllowedMentions finalAllowedMentions = allowedMentions;
        String finalUserString = userString;
        // TODO: Force swap userstring to playername if player is not in a guild
        guildChannels.forEach(channel -> channel.createMessage(generalMessage.formatted(finalUserString)).withAllowedMentions(finalAllowedMentions).block());
    }

    private boolean canUserSeeMessageInAGuild(List<GuildMessageChannel> guildChannels, BotUserDTO player) {
        Snowflake userId = Snowflake.of(player.discordId());
        for (GuildMessageChannel channel : guildChannels) {
            Snowflake guildId = Snowflake.of(channel.getData().guildId().get());
            Guild guild = discordService.getGuildById(guildId);
            try {
                Member guildMember = guild.getMemberById(userId).block();
                if (channel.getEffectivePermissions(guildMember.getId()).block().contains(VIEW_CHANNEL)) return true;
            } catch (ClientException e) {
                logger.debug("User {} does not have access to guild {} ({})", player.discordId(), guildId.asLong(), guild.getName());
            }
        }
        return false;
    }

    private static String getUserStringDependingOnSettings(BotUserDTO player) {
        return switch (player.notificationSetting()) {
            case NO_NOTIFICATIONS, DMS_ONLY -> player.username();
            default -> player.getMention();
        };
    }
}
