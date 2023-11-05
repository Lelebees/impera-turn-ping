package com.lelebees.imperabot.bot.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lelebees.imperabot.bot.domain.translation.TranslationObject;
import discord4j.common.JacksonResources;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TranslationRepository {
    private final JacksonResources translationMapper = JacksonResources.create();
    private final PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();

    public Map<String, TranslationObject> getTranslations(String group) throws IOException {
        File translationsFile = matcher.getResource(group + ".json").getFile();
        List<TranslationObject> translations = translationMapper.getObjectMapper().readValue(translationsFile, new TypeReference<>() {
        });
        return translations.stream()
                .collect(HashMap::new, (map, translation) -> map.put(translation.getId(), translation), Map::putAll);
    }
}
