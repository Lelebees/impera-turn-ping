package com.lelebees.imperabot.impera.application;

import com.lelebees.imperabot.impera.domain.ImperaGameHistoryDTO;
import com.lelebees.imperabot.impera.domain.ImperaLoginDTO;
import com.lelebees.imperabot.impera.domain.game.exception.ImperaGameNotFoundException;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.*;

@Service
public class ImperaService {
    public static HttpEntity<String> entity;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String imperaURL;
    private final String imperaUsername;
    private final String imperaPassword;
    public ImperaLoginDTO bearerToken;
    private static final Logger logger = LoggerFactory.getLogger(ImperaService.class);

    public ImperaService(@Value("${impera.api.url}") String imperaURL, @Value("${impera.username}") String imperaUsername, @Value("${impera.password}") String imperaPassword) {
        this.imperaURL = imperaURL;
        this.imperaUsername = imperaUsername;
        this.imperaPassword = imperaPassword;
        bearerToken = getBearerToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken.access_token);
        entity = new HttpEntity<>(headers);
        logger.info("ImperaService ready to make connections!");
    }

    public ImperaLoginDTO getBearerToken() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", imperaUsername);
        map.add("password", imperaPassword);
        map.add("scope", "openid offline_access roles");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        String url = imperaURL + "/Account/token";

        ResponseEntity<ImperaLoginDTO> response = restTemplate.exchange(url, POST, request, ImperaLoginDTO.class);
        return response.getBody();
    }

    public ImperaGameViewDTO getGame(long gameID) {
        try {
            String url = imperaURL + "/games/" + gameID;
            ResponseEntity<ImperaGameViewDTO> response = restTemplate.exchange(url, GET, entity, ImperaGameViewDTO.class);
            return response.getBody();
        } catch (Exception e) {
            throw new ImperaGameNotFoundException(e.getMessage());
        }
    }

    public List<ImperaMessageDTO> getMessages() {
        String url = imperaURL + "/messages/folder/Inbox";
        ResponseEntity<List<ImperaMessageDTO>> response = restTemplate.exchange(url, GET, entity, new ParameterizedTypeReference<>() {
        });
        return response.getBody();
    }

    public List<ImperaMessageDTO> getLinkMessages() {
        return getMessages().stream()
                .filter(message -> message.subject.trim().equalsIgnoreCase("link"))
                .collect(Collectors.toList());
    }

    public Object deleteMessage(String id) {
        String url = imperaURL + "/messages/" + id;
        ResponseEntity<?> response = restTemplate.exchange(url, DELETE, entity, Map.class);
        return response.getBody();
    }

    public List<String> playersThatSurrendered(long gameId, int turnId) {
        ImperaGameHistoryDTO turnState = getTurnState(gameId, turnId);
        if (turnState == null) {
            return null;
        }
        return turnState.actions.stream().filter(action -> action.action.equals("PlayerSurrendered")).map(action -> action.actorId).toList();
    }

    public List<String> playersThatWereDefeated(long gameId, int turnId) {
        ImperaGameHistoryDTO turnState = getTurnState(gameId, turnId);
        if (turnState == null) {
            return null;
        }
        return turnState.actions.stream().filter(action -> action.action.equals("PlayerLost")).map(action -> action.actorId).toList();
    }

    public List<String> playersThatTimedOut(long gameId, int turnId) {
        ImperaGameHistoryDTO turnState = getTurnState(gameId, turnId);
        if (turnState == null) {
            return null;
        }
        return turnState.actions.stream().filter(action -> action.action.equals("PlayerTimeout")).map(action -> action.actorId).toList();
    }

    private ImperaGameHistoryDTO getTurnState(long gameId, int turnId) {
        String url = imperaURL + "/games/" + gameId + "/history/" + turnId;
        ResponseEntity<ImperaGameHistoryDTO> response = restTemplate.exchange(url, GET, entity, ImperaGameHistoryDTO.class);
        return response.getBody();
    }
}
