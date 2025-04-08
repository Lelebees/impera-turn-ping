package com.lelebees.imperabot.impera.domain.game.view;

import java.util.Objects;

public record ImperaGamePlayerDTO(String id, String userId, String name, String state, String outcome, String teamId,
                                  int playOrder, int timeouts) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImperaGamePlayerDTO that = (ImperaGamePlayerDTO) o;
        return Objects.equals(this.userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    public boolean hasWon() {
        return state.equals("Won");
    }
}
