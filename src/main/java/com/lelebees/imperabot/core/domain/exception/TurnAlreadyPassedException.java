package com.lelebees.imperabot.core.domain.exception;

public class TurnAlreadyPassedException extends RuntimeException {
    public TurnAlreadyPassedException(String message) {
        super(message);
    }
}
