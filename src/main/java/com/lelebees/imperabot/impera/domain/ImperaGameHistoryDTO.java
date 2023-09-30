package com.lelebees.imperabot.impera.domain;

import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;

import java.util.List;

public class ImperaGameHistoryDTO {

    public int gameId;
    public int turnId;
    public List<ImperaGameActionDTO> actions;
    public ImperaGameViewDTO game;

    public List<ImperaGameActionDTO> findSurrenderAction() {
        return this.actions.stream()
                .filter(action -> action.action.equals("PlayerSurrendered"))
                .toList();
    }
}
