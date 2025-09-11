package com.lelebees.imperabot.impera.domain.game;

import java.util.List;

public record ImperaGameOptionsDTO(int numberOfPlayersPerTeam, int numberOfTeams, int minUnitsPerCountry,
                                   int newUnitsPerTurn, int attacksPerTurn, int movesPerTurn, int initialCountryUnits,
                                   String mapDistribution, int timeoutInSeconds, int maximumTimeoutsPerPlayer,
                                   List<String> victoryConditions, List<String> visibilityModifier) {
}
