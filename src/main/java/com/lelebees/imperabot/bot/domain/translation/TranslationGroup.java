package com.lelebees.imperabot.bot.domain.translation;

import java.util.Map;

public class TranslationGroup {
    private Map<String, TranslationObject> translations;

    private TranslationGroup(Map<String, TranslationObject> translations) {
        this.translations = translations;
    }

    protected TranslationGroup() {
    }

    public static TranslationGroup of(Map<String, TranslationObject> translations) {
        return new TranslationGroup(translations);
    }

    public String getTranslation(String id, String locale) {
        if (!translations.containsKey(id)) {
            throw new TranslationNotFoundException("Translation not found");
        }
        return translations.get(id).getTranslationByLocale(locale);
    }
}
