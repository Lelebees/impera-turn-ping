package com.lelebees.imperabot.impera.domain.history;

import com.lelebees.imperabot.impera.domain.ImperaGameActionDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;

import java.util.List;

public record ImperaGameHistoryDTO(int gameId, int turnId, List<ImperaGameActionDTO> actions, ImperaGameViewDTO game) {

    public List<ImperaGameActionDTO> findSurrenderAction() {
        return filterAction("PlayerSurrendered");
    }

    public List<ImperaGameActionDTO> findLossAction() {
        return filterAction("PlayerLost");
    }

    public List<ImperaGameActionDTO> findTimeOutAction() {
        return filterAction("PlayerTimeout");
    }

    public List<ImperaGameActionDTO> filterAction(String action){
        return this.actions.stream()
                .filter(actionDTO -> actionDTO.action().equals(action))
                .toList();
    }
}
