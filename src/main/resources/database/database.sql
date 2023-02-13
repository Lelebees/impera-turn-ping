CREATE TABLE
    bot_game (
                 game_id INT NOT NULL,
                 last_turn INT NOT NULL DEFAULT 0,
                 half_time_notice BOOLEAN NOT NULL DEFAULT false,
                 PRIMARY KEY (game_id)
);

CREATE TABLE
    bot_game_channel (
                         game_id INT NOT NULL,
                         channel_id BIGINT NOT NULL,
                         notification_setting INT NULL,
                         PRIMARY KEY (game_id, channel_id)
);

CREATE TABLE
    bot_guild_settings (
                           guild_id BIGINT NOT NULL,
                           default_channel_id BIGINT NULL,
                           notification_setting INT NOT NULL DEFAULT 0,
                           PRIMARY KEY (guild_id)
);

CREATE TABLE
    bot_log (
                id BIGINT NOT NULL,
                bot_user_id BIGINT NULL,
                timestamp TIMESTAMP NOT NULL,
                description VARCHAR NOT NULL,
                PRIMARY KEY (id)
);

CREATE TABLE
    bot_user (
                 discord_user_id BIGINT NOT NULL,
                 impera_player_id UUID NULL,
                 notification_setting INT NOT NULL DEFAULT 3,
                 super_secret_code VARCHAR NOT NULL,
                 PRIMARY KEY (discord_user_id)
);

ALTER TABLE bot_log ADD CONSTRAINT FK_bot_user_TO_bot_log FOREIGN KEY (bot_user_id) REFERENCES bot_user (discord_user_id);