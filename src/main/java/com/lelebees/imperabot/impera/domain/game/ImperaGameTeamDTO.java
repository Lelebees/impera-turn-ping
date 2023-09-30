package com.lelebees.imperabot.impera.domain.game;

import com.lelebees.imperabot.impera.domain.game.view.ImperaGamePlayerDTO;

import java.util.List;

public class ImperaGameTeamDTO {
    public String id;
    public int playOrder;
    public List<ImperaGamePlayerDTO> players;

    public ImperaGamePlayerDTO getPlayer(String playerId) {
        return this.players.stream()
                .filter(p -> p.userId.equals(playerId))
                .findFirst()
                .orElse(null);
    }
}
