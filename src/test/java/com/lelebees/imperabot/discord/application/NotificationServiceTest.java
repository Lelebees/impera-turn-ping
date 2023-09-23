package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.application.GameService;
import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.game.Game;
import com.lelebees.imperabot.bot.domain.gamechannellink.GameChannelLink;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotInGameException;
import com.lelebees.imperabot.bot.domain.user.exception.UserNotVerifiedException;
import com.lelebees.imperabot.impera.application.ImperaService;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

class NotificationServiceTest {

    NotificationService notificationService;

    @Test
    @DisplayName("Can track a game")
    void canTrackGame() {
        int gameId = 123;
        int botUserId = 456;
        UUID imperaId = UUID.randomUUID();
        ImperaService imperaService = Mockito.mock(ImperaService.class);
        UserService userService = Mockito.mock(UserService.class);
        GameService gameService = Mockito.mock(GameService.class);
        BotUser botUser = Mockito.mock(BotUser.class);
        notificationService = new NotificationService(null, null, userService, imperaService, gameService);

        Mockito.when(gameService.findOrCreateGame(gameId)).thenReturn(new Game(gameId));
        Mockito.when(userService.findOrCreateUser(anyLong())).thenReturn(botUser);
        Mockito.when(botUser.isLinked()).thenReturn(true);
        Mockito.when(botUser.getImperaId()).thenReturn(imperaId);
        Mockito.when(imperaService.isPlayerInGame(imperaId.toString(), gameId)).thenReturn(true);

        Game game = notificationService.trackGame(gameId, botUserId);
        assertEquals(gameId, game.getId());
    }

    @Test
    @DisplayName("Cannot track game if user is not linked to impera")
    void cantTrackNoImperaId() {
        int gameId = 123;
        int botUserId = 456;
        BotUser botUser = Mockito.mock(BotUser.class);
        UserService userService = Mockito.mock(UserService.class);
        Mockito.when(userService.findOrCreateUser(botUserId)).thenReturn(botUser);
        Mockito.when(botUser.isLinked()).thenReturn(false);

        notificationService = new NotificationService(null, null, userService, null, null);
        assertThrows(UserNotVerifiedException.class, () -> notificationService.trackGame(gameId, botUserId));
    }

    @Test
    @DisplayName("Cannot track game if user is not in the game")
    void cantTrackNotInGame() {
        int gameId = 123;
        int botUserId = 456;
        UUID uuid = UUID.randomUUID();
        BotUser botUser = Mockito.mock(BotUser.class);
        UserService userService = Mockito.mock(UserService.class);
        ImperaService imperaService = Mockito.mock(ImperaService.class);
        Mockito.when(userService.findOrCreateUser(botUserId)).thenReturn(botUser);
        Mockito.when(botUser.isLinked()).thenReturn(true);
        Mockito.when(botUser.getImperaId()).thenReturn(uuid);
        Mockito.when(imperaService.isPlayerInGame(uuid.toString(), gameId)).thenReturn(false);

        notificationService = new NotificationService(null, null, userService, imperaService, null);
        assertThrows(UserNotInGameException.class, () -> notificationService.trackGame(gameId, botUserId));
    }

    @Test
    @DisplayName("Adding a game with no setting creates a link")
    @Disabled
        // I'll write tests i said, it'll increase the bot stability i said.
        // But nooooo, YoU cAnT mOcK tHe FiNaL cLaSsEs
    void addGameNoSettingMakesLink() {
        int gameId = 123;
        long channelId = 456;
        int guildId = 789;
        long userId = 101112;
        UUID uuid = UUID.randomUUID();
        Member member = Mockito.mock(Member.class);
        BotUser botUser = Mockito.mock(BotUser.class);
        UserService userService = Mockito.mock(UserService.class);
        ImperaService imperaService = Mockito.mock(ImperaService.class);
        Mockito.when(userService.findUser(anyLong())).thenReturn(botUser);
        Mockito.when(botUser.isLinked()).thenReturn(true);
        Mockito.when(botUser.getImperaId()).thenReturn(uuid);
        Mockito.when(imperaService.isPlayerInGame(uuid.toString(), gameId)).thenReturn(true);
        Mockito.when(member.getBasePermissions().block().contains(any())).thenReturn(true);
        Mockito.when(member.getId()).thenReturn(Snowflake.of(userId));
        Mockito.when(userService.findUser(userId)).thenReturn(botUser);

        notificationService = new NotificationService(null, null, userService, imperaService, null);
        GameChannelLink link = notificationService.setGuildGame(guildId, gameId, channelId, null, member);
        assertEquals(gameId, link.getGameId());
        assertEquals(channelId, link.getChannelId());
        assertNull(link.notificationSetting);
    }
}