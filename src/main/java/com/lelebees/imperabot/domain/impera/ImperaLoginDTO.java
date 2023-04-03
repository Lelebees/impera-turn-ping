package com.lelebees.imperabot.domain.impera;

public class ImperaLoginDTO {
    public String access_token;
    public String refresh_token;
    public String id_token;
    public String token_type;
    public int expires_in;

    public ImperaLoginDTO(String access_token, String refresh_token, String id_token, String token_type, int expires_in) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.id_token = id_token;
        this.token_type = token_type;
        this.expires_in = expires_in;
    }

    public ImperaLoginDTO() {

    }

    @Override
    public String toString() {
        return access_token;
    }
}
