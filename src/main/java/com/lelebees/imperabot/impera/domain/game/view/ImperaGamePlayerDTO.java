package com.lelebees.imperabot.impera.domain.game.view;

import java.util.Objects;

public class ImperaGamePlayerDTO {
    public String id;
    public String userId;
    public String name;
    public String state;
    public String outcome;
    public String teamId;
    public int playOrder;
    public int timeouts;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImperaGamePlayerDTO that = (ImperaGamePlayerDTO) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
