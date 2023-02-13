package com.lelebees.imperabot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.util.UUID;
@Entity
public class User {
    @Id
    private long discord_user_id;
    private UUID impera_player_id;
    //Probably want to turn this into an enum instead!
    private int notification_setting;
    private String super_secret_code;
}
