package com.lelebees.imperabot.core.domain.game;

import com.lelebees.imperabot.core.domain.Game;
import com.lelebees.imperabot.core.domain.exception.TurnAlreadyPassedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameTest {

    @Test
    @DisplayName("Setting the current turn sets half time notice to false")
    void settingCurrentTurnsSetsHalfTimeNotice() {
        Game game = new Game(1L, 0, true, new HashSet<>());
        game.setCurrentTurn(2);
        assertFalse(game.sentHalfTimeNotice());
    }

    @Test
    @DisplayName("Current turn cannot decrease")
    void currentTurnCantDecrease() {
        Game game = new Game(1L, 5, false, new HashSet<>());
        assertThrows(TurnAlreadyPassedException.class, () -> game.setCurrentTurn(1));
    }

}