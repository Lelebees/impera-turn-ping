package com.lelebees.imperabot.bot.domain.game.exception;

public class TurnAlreadyPassedException extends RuntimeException {
    public TurnAlreadyPassedException(String message) {
        super(message);
    }
}
