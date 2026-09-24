package com.lelebees.imperabot.discord.application.messages;

import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import org.springframework.beans.factory.annotation.Value;

public class NewTurnMessage {
    private final String format = "%s your turn in %s!";
    private AddressType addressType;
    private final ImperaGameViewDTO game;
    private final String imperaURL;

    public NewTurnMessage(AddressType addressType, ImperaGameViewDTO game, @Value("${impera.web.url}") String imperaURL) {
        this.addressType = addressType;
        this.game = game;
        this.imperaURL = imperaURL;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    @Override
    public String toString() {
        return format.formatted(getPronoun(), getGameURI());
    }

    private String getPronoun() {
        return addressType == AddressType.DIRECT ? "It's" : "%s, it is";
    }

    private String getGameURI() {
        return "[%s](%s/game/play/%d)".formatted(game.name(), imperaURL, game.id());
    }
}
