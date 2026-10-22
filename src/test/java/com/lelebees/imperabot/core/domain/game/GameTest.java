package com.lelebees.imperabot.core.domain.game;

import com.lelebees.imperabot.core.domain.CourseOfAction;
import com.lelebees.imperabot.core.domain.Game;
import com.lelebees.imperabot.core.domain.exception.TurnAlreadyPassedException;
import com.lelebees.imperabot.impera.domain.game.ImperaGameOptionsDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    @DisplayName("Setting the current turn sets half time notice to false")
    void settingCurrentTurnsSetsHalfTimeNotice() {
        Game game = new Game(1L, 0, true, new HashSet<>());
        game.updateGameStatus(2);
        assertFalse(game.sentHalfTimeNotice());
    }

    @Test
    @DisplayName("Current turn cannot decrease")
    void currentTurnCantDecrease() {
        Game game = new Game(1L, 5, false, new HashSet<>());
        assertThrows(TurnAlreadyPassedException.class, () -> game.updateGameStatus(1));
    }


    @Test
    @DisplayName("Games waiting for players will be skipped")
    public void courseOfActionForWaitingGameIsSkip() {
        ImperaGameViewDTO gameViewDTO = new ImperaGameViewDTO(0L, "Fun", "Test", false, "", null, "Open", "", null, null, null, "", 100, 1, 1, 1, 1);
        Game game = new Game(0L, 0, false, null);
        assertEquals(CourseOfAction.SKIP_CHECK, game.getCourseOfAction(gameViewDTO));
    }

    @Test
    @DisplayName("Finished games will have a victor declared")
    public void courseOfActionForWonGameIsDeclareVictor() {
        ImperaGameViewDTO gameViewDTO = new ImperaGameViewDTO(0L, "Fun", "Test", false, "", null, "Ended", "", null, null, null, "", 100, 1, 1, 1, 1);
        Game game = new Game(0L, 0, false, null);
        assertEquals(CourseOfAction.DECLARE_VICTOR, game.getCourseOfAction(gameViewDTO));
    }

    @Test
    @DisplayName("Game with turn difference will notify next player")
    public void courseOfActionForNextTurnIsNotifyNextPlayer() {
        ImperaGameViewDTO gameViewDTO = new ImperaGameViewDTO(0L, "Fun", "Test", false, "", null, "Playing", "", null, null, null, "", 100, 1, 1, 1, 1);
        Game game = new Game(0L, 0, false, null);
        assertEquals(CourseOfAction.NOTIFY_NEXT_PLAYER, game.getCourseOfAction(gameViewDTO));
    }

    @Test
    @DisplayName("Game with no turn difference will be skipped")
    public void courseOfActionForNothingIsSkip() {
        ImperaGameViewDTO gameViewDTO = new ImperaGameViewDTO(0L, "Fun", "Test", false, "", null, "Playing", "", null, null, new ImperaGameOptionsDTO(0,0,0,0,0,0,0,"", 100, 1, null, null), "", 100, 1, 1, 1, 1);
        Game game = new Game(0L, 1, false, null);
        assertEquals(CourseOfAction.SKIP_CHECK, game.getCourseOfAction(gameViewDTO));
    }

    @Test
    @DisplayName("Game with half time remaining will notify half time")
    public void courseOfActionForHalfTimeIsHalfTime() {
        ImperaGameViewDTO gameViewDTO = new ImperaGameViewDTO(0L, "Fun", "Test", false, "", null, "Playing", "", null, null, new ImperaGameOptionsDTO(0,0,0,0,0,0,0,"", 100, 1, null, null), "", 50, 1, 1, 1, 1);
        Game game = new Game(0L, 1, false, null);
        assertEquals(CourseOfAction.NOTIFY_HALF_TIME_PASSED, game.getCourseOfAction(gameViewDTO));
    }
    @Test
    @DisplayName("Game with half time remaining and already notified will do nothing")
    public void courseOfActionForHalfTimeNoticedIsSkip() {
        ImperaGameViewDTO gameViewDTO = new ImperaGameViewDTO(0L, "Fun", "Test", false, "", null, "Playing", "", null, null, new ImperaGameOptionsDTO(0,0,0,0,0,0,0,"", 100, 1, null, null), "", 50, 1, 1, 1, 1);
        Game game = new Game(0L, 1, true, null);
        assertEquals(CourseOfAction.SKIP_CHECK, game.getCourseOfAction(gameViewDTO));
    }
}