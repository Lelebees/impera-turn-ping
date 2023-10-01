package com.lelebees.imperabot.discord.domain.command.slash.notification.exception;

public class IncorrectPermissionException extends RuntimeException {
    public IncorrectPermissionException(String message) {
        super(message);
    }
}
