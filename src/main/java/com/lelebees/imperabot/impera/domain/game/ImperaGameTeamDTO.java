package com.lelebees.imperabot.impera.domain.game;

import com.lelebees.imperabot.impera.domain.game.view.ImperaGamePlayerDTO;

import java.util.List;

public record ImperaGameTeamDTO(String id, int playOrder, List<ImperaGamePlayerDTO> players) {

    public boolean hasWon() {
        return players.stream().anyMatch(ImperaGamePlayerDTO::hasWon);
    }

    public List<ImperaGamePlayerDTO> getWinningPlayers() {
        return players.stream().filter(ImperaGamePlayerDTO::hasWon).toList();
    }
}
