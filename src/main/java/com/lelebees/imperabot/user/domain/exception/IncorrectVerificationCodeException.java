package com.lelebees.imperabot.user.domain.exception;

public class IncorrectVerificationCodeException extends Exception {
    public IncorrectVerificationCodeException(String message) {
        super(message);
    }
}
