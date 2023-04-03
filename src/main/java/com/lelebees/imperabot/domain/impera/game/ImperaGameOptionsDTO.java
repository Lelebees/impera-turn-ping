package com.lelebees.imperabot.domain.impera.game;

import java.util.List;

public class ImperaGameOptionsDTO {
    public int numberOfPlayersPerTeam;
    public int numberOfTeams;
    public int minUnitsPerCountry;
    public int newUnitsPerTurn;
    public int attacksPerTurn;
    public int movesPerTurn;
    public int initialCountryUnits;
    public String mapDistribution;
    public int timeoutInSeconds;
    public int maximumTimeoutsPerPlayer;
    public List<String> victoryConditions;
    public List<String> visibilityModifier;
}
