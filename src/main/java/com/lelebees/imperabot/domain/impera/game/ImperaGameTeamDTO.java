package com.lelebees.imperabot.domain.impera.game;

import com.lelebees.imperabot.domain.impera.game.view.ImperaGamePlayerDTO;

import java.util.List;

public class ImperaGameTeamDTO {
    public String id;
    public int playOrder;
    public List<ImperaGamePlayerDTO> players;
}
