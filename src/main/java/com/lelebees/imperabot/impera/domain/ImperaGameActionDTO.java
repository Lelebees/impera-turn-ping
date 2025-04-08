package com.lelebees.imperabot.impera.domain;

public record ImperaGameActionDTO(String actorId, String action, int id, int turnNo, String dateTime,
                                  String otherPlayerId, String originIdentifier, String destinationIdentifier,
                                  int units, int unitsLost, int unitsLostOther, boolean result) {
}
