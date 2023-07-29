package com.lelebees.imperabot.discord.domain.command.notification.exception;

public class IncorrectContextException extends RuntimeException {
    public IncorrectContextException(String message) {
        super(message);
    }
}
