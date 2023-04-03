package com.lelebees.imperabot.domain.impera.game;

import com.lelebees.imperabot.domain.impera.game.view.ImperaGamePlayerDTO;

import java.time.LocalDateTime;
import java.util.List;

public class ImperaGameDTO {

    public int id;
    public String type;
    public String name;
    public boolean hasPassword;
    public ImperaGameOptionsDTO options;
    public String createdByUserId;
    public String createdByName;
    public LocalDateTime startedAt;
    public LocalDateTime lastActionAt;
    public int timeoutSecondsLeft;
    public String mapTemplate;
    public String state;
    public ImperaGamePlayerDTO currentPlayer;
    public List<ImperaGameTeamDTO> teams;
    public int turnCounter;

    @Override
    public String toString() {
        return "ImperaGameDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
