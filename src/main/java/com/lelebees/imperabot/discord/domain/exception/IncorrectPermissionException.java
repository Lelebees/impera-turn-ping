package com.lelebees.imperabot.discord.domain.exception;

public class IncorrectPermissionException extends RuntimeException {
    public IncorrectPermissionException(String message) {
        super(message);
    }
}
