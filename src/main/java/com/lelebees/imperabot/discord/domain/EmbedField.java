package com.lelebees.imperabot.discord.domain;

import discord4j.core.spec.EmbedCreateFields;

public class EmbedField implements EmbedCreateFields.Field {
    private String name;
    private String value;
    private boolean inline;

    public EmbedField(String name, String value, boolean inline) {
        this.name = name;
        this.value = value;
        this.inline = inline;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public boolean inline() {
        return inline;
    }
}
