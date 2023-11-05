package com.lelebees.imperabot.bot.domain.translation;

import java.util.Map;

public class TranslationObject {

    private String id;
    private Map<String, String> translations;

    public TranslationObject(String id, Map<String, String> translations) {
        this.id = id;
        this.translations = translations;
    }

    protected TranslationObject() {
    }

    public String getId() {
        return id;
    }

    public String getTranslationByLocale(String locale) {
        if (!translations.containsKey(locale)) {
            return translations.get("en-US");
        }
        return translations.get(locale);
    }

    @Override
    public String toString() {
        return "TranslationObject{" +
               ", id='" + id + '\'' +
               ", translations=" + translations +
               '}';
    }
}
