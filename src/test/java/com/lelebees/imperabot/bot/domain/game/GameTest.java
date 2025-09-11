package com.lelebees.imperabot.bot.domain.game;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class GameTest {

    @Test
    @DisplayName("Setting the current turn sets half time notice to false")
    void settingCurrentTurnsSetsHalfTimeNotice() {
        Game game = new Game(1L, 0, true);
        game.setCurrentTurn(2);
        assertFalse(game.sentHalfTimeNotice());
    }

}