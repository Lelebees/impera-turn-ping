package com.lelebees.imperabot.impera.domain.game;

import com.lelebees.imperabot.impera.domain.game.view.ImperaGamePlayerDTO;

import java.time.LocalDateTime;
import java.util.List;

public record ImperaGameDTO(long id, String type, String name, boolean hasPassword, ImperaGameOptionsDTO options,
                            String createdByUserId, String createdByName, LocalDateTime startedAt,
                            LocalDateTime lastActionAt, int timeoutSecondsLeft, String mapTemplate, String state,
                            ImperaGamePlayerDTO currentPlayer, List<ImperaGameTeamDTO> teams, int turnCounter) {

    @Override
    public String toString() {
        return "ImperaGameDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
