package com.lelebees.imperabot.core.application.dto;

import com.lelebees.imperabot.core.domain.Game;

import java.util.List;
import java.util.Set;

public record GameDTO(long id, int currentTurn, boolean halfTimeNotice, Set<ChannelDTO> trackingChannels) {
    public static GameDTO from(Game game) {
        return new GameDTO(game.getId(), game.getCurrentTurn(), game.sentHalfTimeNotice(), ChannelDTO.From(game.getTrackingChannels()));
    }

    public static List<GameDTO> from(List<Game> games) {
        return games.stream().map(GameDTO::from).toList();
    }
}
