package com.lelebees.imperabot.impera.domain.game.view;

import com.lelebees.imperabot.impera.domain.game.ImperaGameOptionsDTO;
import com.lelebees.imperabot.impera.domain.game.ImperaGameTeamDTO;

import java.util.Collection;
import java.util.List;

public record ImperaGameViewDTO(long id, String type, String name, boolean hasPassword, String mapTemplate,
                                List<ImperaGameTeamDTO> teams, String state, String playState,
                                ImperaGamePlayerDTO currentPlayer, ImperaGameMapDTO map, ImperaGameOptionsDTO options,
                                String lastModifiedAt, int timeoutSecondsLeft, int turnCounter, int unitsToPlace,
                                int attacksInCurrentTurn, int movesInCurrentTurn) {

    @Override
    public String toString() {
        return "{ " + this.id + "; \"" + this.name + "\" }";
    }

    public ImperaGamePlayerDTO findPlayerById(String playerId) {
        // Note that "Id" here means Game-specific id. This is not their account id!
        return this.teams.stream()
                .flatMap(team -> team.players().stream())
                .filter(player -> player.id().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public boolean hasEnded() {
        return state.equals("Ended");
    }

    public boolean hasYetToStart() {
        return state.equals("Open");
    }

    public List<ImperaGameTeamDTO> getWinningTeams() {
        return teams().stream().filter(ImperaGameTeamDTO::hasWon).toList();
    }

    public List<ImperaGamePlayerDTO> getWinningPlayers() {
        return teams().stream()
                .map(ImperaGameTeamDTO::getWinningPlayers)
                .flatMap(Collection::stream)
                .toList();
    }
}
