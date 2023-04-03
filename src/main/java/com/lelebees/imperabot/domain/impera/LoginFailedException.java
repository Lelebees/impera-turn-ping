package com.lelebees.imperabot.domain.impera;

public class LoginFailedException extends RuntimeException{
    public LoginFailedException(String message){
        super(message);
    }
}
