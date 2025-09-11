package com.lelebees.imperabot.bot.presentation.game;

import com.lelebees.imperabot.bot.domain.game.Game;

import java.util.ArrayList;
import java.util.List;

public record GameDTO(long id, int currentTurn, boolean halfTimeNotice) {
    public static GameDTO from(Game game) {
        return new GameDTO(game.getId(), game.getCurrentTurn(), game.sentHalfTimeNotice());
    }

    public static List<GameDTO> from(List<Game> games) {
        List<GameDTO> dtos = new ArrayList<>();
        games.forEach(game -> dtos.add(GameDTO.from(game)));
        return dtos;
    }
}
