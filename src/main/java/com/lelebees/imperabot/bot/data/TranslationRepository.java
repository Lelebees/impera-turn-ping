package com.lelebees.imperabot.bot.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lelebees.imperabot.bot.domain.translation.TranslationObject;
import discord4j.common.JacksonResources;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TranslationRepository {
    private final JacksonResources translationMapper = JacksonResources.create();
    private final PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
    private final Map<String, Map<String, TranslationObject>> translations = new HashMap<>();

    public Map<String, TranslationObject> getTranslationsGroup(String group) throws IOException {
        return this.translations.get(group);
    }

    @PostConstruct
    private void init() throws IOException {
        Arrays.stream(matcher.getResources("*.json")).forEach(resource -> {
            try {
                String fileName = resource.getFilename();
                String groupId = fileName.substring(0, fileName.lastIndexOf('.'));
                translations.put(groupId, getTranslations(resource.getFile()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private Map<String, TranslationObject> getTranslations(File translationFile) throws IOException {
        List<TranslationObject> translations = translationMapper.getObjectMapper().readValue(translationFile, new TypeReference<>() {
        });
        return translations.stream().collect(HashMap::new, (map, translation) -> map.put(translation.getId(), translation), Map::putAll);
    }

}
