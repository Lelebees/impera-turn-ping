package com.lelebees.imperabot.discord.domain.exception;

public class NoDefaultChannelException extends RuntimeException {
    public NoDefaultChannelException(String message) {
        super(message);
    }
}
