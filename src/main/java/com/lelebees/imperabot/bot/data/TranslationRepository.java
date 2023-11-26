package com.lelebees.imperabot.bot.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lelebees.imperabot.bot.domain.translation.TranslationObject;
import discord4j.common.JacksonResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final static Logger logger = LoggerFactory.getLogger(TranslationRepository.class);
    private final Map<String, Map<String, TranslationObject>> translations;

    public TranslationRepository() throws IOException {
        logger.info("Loading translations");
        translations = new HashMap<>();
        PathMatchingResourcePatternResolver matcher = new PathMatchingResourcePatternResolver();
        Arrays.stream(matcher.getResources("translations/*.json")).forEach(resource -> {
            try {
                String fileName = resource.getFilename();
                String groupId = fileName.substring(0, fileName.lastIndexOf('.'));
                translations.put(groupId, readTranslations(resource.getFile()));
                logger.info("Loaded translations for file: " + fileName);
            } catch (IOException e) {
                logger.error("Failed to load translations for file: " + resource.getFilename(), e);
            }
        });

    }

    public Map<String, TranslationObject> getTranslationsGroup(String group) {
        return translations.get(group);
    }

    private Map<String, TranslationObject> readTranslations(File translationFile) throws IOException {
        List<TranslationObject> translations = translationMapper.getObjectMapper()
                .readValue(translationFile, new TypeReference<>() {
                });
        return translations.stream()
                .collect(HashMap::new, (map, translation) -> map.put(translation.getId(), translation), Map::putAll);
    }

}
