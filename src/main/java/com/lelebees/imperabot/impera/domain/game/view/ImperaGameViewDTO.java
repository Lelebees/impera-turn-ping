package com.lelebees.imperabot.impera.domain.game.view;

import com.lelebees.imperabot.impera.domain.game.ImperaGameOptionsDTO;
import com.lelebees.imperabot.impera.domain.game.ImperaGameTeamDTO;

import java.util.List;

public class ImperaGameViewDTO {
    public long id;
    public String type;
    public String name;
    public boolean hasPassword;
    public String mapTemplate;
    public List<ImperaGameTeamDTO> teams;
    public String state;
    public String playState;
    public ImperaGamePlayerDTO currentPlayer;
    public ImperaGameMapDTO map;
    public ImperaGameOptionsDTO options;
    public String lastModifiedAt;
    public int timeoutSecondsLeft;
    public int turnCounter;
    public int unitsToPlace;
    public int attacksInCurrentTurn;
    public int movesInCurrentTurn;

    @Override
    public String toString() {
        return "{ " + this.id + "; \"" + this.name + "\" }";
    }

    public ImperaGamePlayerDTO findPlayerByGameId(String playerId) {
        return this.teams.stream()
                .flatMap(team -> team.players.stream())
                .filter(player -> player.id.equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public boolean hasEnded() {
        return state.equals("Ended");
    }

    public boolean hasYetToStart() {
        return state.equals("Open");
    }
}
