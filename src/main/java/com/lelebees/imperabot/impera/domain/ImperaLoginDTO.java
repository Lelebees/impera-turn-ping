package com.lelebees.imperabot.impera.domain;

public record ImperaLoginDTO(String access_token, String refresh_token, String id_token, String token_type,
                             int expires_in) {

    @Override
    public String toString() {
        return access_token;
    }
}
