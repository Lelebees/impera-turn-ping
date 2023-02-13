``` mermaid
---
title: Impera Bot Database ERD
---
erDiagram

bot_user ||--|| discord_user : ""
bot_user ||--|o impera_player : ""
bot_user |o--o{ bot_log : ""
bot_game_channel ||--|| discord_channel : ""
bot_game ||--|| impera_game : ""

impera_player ||--o{ impera_message : ""
impera_player ||--o{ impera_player_game : ""
impera_game ||--|{ impera_player_game : ""
impera_game ||--o{ bot_game_channel : ""
impera_game ||--|| impera_player : ""
impera_message }o--|| impera_player : ""

discord_user ||--o{ discord_user_channel : ""
discord_user ||--o{ discord_guild_user : ""
discord_guild ||--|{ discord_guild_user : ""
discord_guild ||--|| bot_guild_settings : ""
discord_guild |o--|{ discord_channel : ""
discord_channel |o--|| bot_guild_settings : ""
discord_channel ||--o{ discord_user_channel : ""

bot_guild_settings {
    guild_id SNOWFLAKE PK
    notification_setting INT
    default_channel_id SNOWFLAKE FK
}
bot_game {
    game_id INT FK
    last_turn INT
    half_time_notice BOOL
}
bot_user {
    discord_user_id SNOWFLAKE PK
    impera_player_id UUID FK
    notification_setting INT
    super_secret_code VARCHAR
}
impera_player {
    player_id UUID PK
    player_name VARCHAR
}
discord_user {
    user_id SNOWFLAKE PK
}
impera_player_game {
    player_id UUID FK
    game_id UUID FK
}
impera_game {
    game_id INT PK
    current_player_id UUID FK
    game_name VARCHAR
    current_turn INT
    current_time INT
    turn_time INT
}
bot_game_channel {
    game_id INT PK
    channel_id SNOWFLAKE PK
    notification_setting INT
}
discord_guild_user {
    user_id SNOWFLAKE FK
    guild_id SNOWFLAKE FK
}
discord_guild {
    guild_id SNOWFLAKE PK
}
discord_channel {
    channel_id SNOWFLAKE PK
    channel_type INT
    guild_id SNOWFLAKE FK
}
discord_user_channel {
    channel_id SNOWFLAKE FK
    user_id SNOFLAKE FK
}

bot_log {
    id BIGINT PK
    bot_user_id SNOWFLAKE FK
    timestamp DATETIME
    description VARCHAR
}
impera_message {
    id UUID PK
    sender_id UUID FK
    receiver_id UUID FK
    subject VARCHAR
    body VARCHAR
}

```