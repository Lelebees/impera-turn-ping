package com.lelebees.imperabot.impera.domain;

import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;

import java.util.List;

public record ImperaGameHistoryDTO(int gameId, int turnId, List<ImperaGameActionDTO> actions, ImperaGameViewDTO game) {

    public List<ImperaGameActionDTO> findSurrenderAction() {
        return this.actions.stream()
                .filter(action -> action.action().equals("PlayerSurrendered"))
                .toList();
    }
}
