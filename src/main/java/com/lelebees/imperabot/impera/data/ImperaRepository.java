package com.lelebees.imperabot.impera.data;

import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;

import java.util.List;
import java.util.Optional;

public interface ImperaRepository {

    Optional<ImperaGameViewDTO> findGameByGameId(long gameId);

    List<ImperaMessageDTO> getMessages();

    List<ImperaMessageDTO> getMessagesBySubject(String subject);

    void deleteMessage(String id);

    List<String> getActorIdByActionAndTurn(long gameId, int turnId, String actionString);
}
