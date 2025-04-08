package com.lelebees.imperabot.bot.domain.user;

import com.lelebees.imperabot.bot.domain.user.exception.IncorrectVerificationCodeException;
import com.lelebees.imperabot.bot.domain.user.exception.UserAlreadyVerfiedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BotUserTest {

    @Test
    @DisplayName("Verifying user that is already verified throws exception")
    void verifyUserThrowsIfVerified() {
        UUID imperaId = UUID.randomUUID();
        String verificationCode = UUID.randomUUID().toString();
        BotUser user = new BotUser(1L, imperaId, UserNotificationSetting.NO_NOTIFICATIONS, verificationCode);
        assertThrows(UserAlreadyVerfiedException.class, () -> user.verifyUser(imperaId, verificationCode));
    }

    @Test
    @DisplayName("Verifying user with incorrect code throws exception")
    void verifyUserThrowsIfCodeIncorrect() {
        UUID imperaId = UUID.randomUUID();
        String verificationCode = UUID.randomUUID().toString();
        BotUser user = new BotUser(1L, imperaId, UserNotificationSetting.NO_NOTIFICATIONS, verificationCode);
        assertThrows(IncorrectVerificationCodeException.class, () -> user.verifyUser(imperaId, ""));
    }

    @Test
    @DisplayName("Can verify User")
    void verifyUserWorks() {
        UUID imperaId = UUID.randomUUID();
        String verificationCode = UUID.randomUUID().toString();
        BotUser user = new BotUser(1L, null, UserNotificationSetting.NO_NOTIFICATIONS, verificationCode);
        assertDoesNotThrow(() -> user.verifyUser(imperaId, verificationCode));
    }

    @Test
    @DisplayName("Cannot start verification if user is already verified")
    void noVerifyWhenVerified() {
        UUID imperaId = UUID.randomUUID();
        String verificationCode = UUID.randomUUID().toString();
        BotUser user = new BotUser(1L, imperaId, UserNotificationSetting.NO_NOTIFICATIONS, verificationCode);
        assertThrows(UserAlreadyVerfiedException.class, user::startVerification);
    }

    @Test
    @DisplayName("Restarting verification process regenerates verification code")
    void restartVerifyRegeneratesCode() throws UserAlreadyVerfiedException {
        BotUser user = new BotUser(1L);
        String verificationCode = user.startVerification();
        assertNotEquals(verificationCode, user.startVerification());
    }
}