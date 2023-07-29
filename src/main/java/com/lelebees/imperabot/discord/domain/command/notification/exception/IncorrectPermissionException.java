package com.lelebees.imperabot.discord.domain.command.notification.exception;

public class IncorrectPermissionException extends RuntimeException {
    public IncorrectPermissionException(String message) {
        super(message);
    }
}
