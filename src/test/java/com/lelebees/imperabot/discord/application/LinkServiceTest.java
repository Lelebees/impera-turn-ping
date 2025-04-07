package com.lelebees.imperabot.discord.application;

import com.lelebees.imperabot.bot.application.UserService;
import com.lelebees.imperabot.bot.domain.user.BotUser;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import discord4j.common.util.Snowflake;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinkServiceTest {
    @Test
    @DisplayName("Verification code is returned if user is not linked")
    public void verificationCodeReturns() throws UserAlreadyVerfiedException {
        UserService userService = Mockito.mock(UserService.class);
        LinkService linkService = new LinkService(userService);
        Mockito.when(userService.findOrCreateUser(1L)).thenReturn(new BotUser(1L, null, null, "code"));
        assertEquals("code", linkService.getVerificationCode(Snowflake.of(1L)));
    }

    @Test
    @DisplayName("Verification code is not returned if user is linked")
    public void verificationCodeNotReturned() {
        UserService userService = Mockito.mock(UserService.class);
        LinkService linkService = new LinkService(userService);
        Mockito.when(userService.findOrCreateUser(1L)).thenReturn(new BotUser(1L, UUID.randomUUID(), null, "code"));
        assertThrows(UserAlreadyVerfiedException.class, () -> linkService.getVerificationCode(Snowflake.of(1L)));
    }
}