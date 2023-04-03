package com.lelebees.imperabot.domain.impera.game.view;

import com.lelebees.imperabot.domain.impera.game.ImperaGameOptionsDTO;
import com.lelebees.imperabot.domain.impera.game.ImperaGameTeamDTO;

import java.util.List;

public class ImperaGameViewDTO {
    public int id;
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
    public String toString()
    {
        return "{ " + this.id + "; \""+this.name+"\" }";
    }
}
