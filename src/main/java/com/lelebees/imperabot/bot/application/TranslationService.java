package com.lelebees.imperabot.bot.application;

import com.lelebees.imperabot.bot.data.TranslationRepository;
import com.lelebees.imperabot.bot.domain.translation.TranslationGroup;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {
    private final TranslationRepository translationRepository;

    public TranslationService(TranslationRepository translationRepository) {
        this.translationRepository = translationRepository;
    }

    public TranslationGroup getTranslationsByGroup(String groupId) {
        return TranslationGroup.of(translationRepository.getTranslationsGroup(groupId));
    }

}
