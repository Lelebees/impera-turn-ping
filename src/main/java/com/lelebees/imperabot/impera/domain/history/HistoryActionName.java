package com.lelebees.imperabot.impera.domain.history;

public enum HistoryActionName {
    SURRENDERED("PlayerSurrendered"),
    LOST("PlayerLost"),
    TIMED_OUT("PlayerTimeout");

    private final String imperaName;

    HistoryActionName(String imperaName) {
        this.imperaName = imperaName;
    }

    @Override
    public String toString() {
        return imperaName;
    }
}
