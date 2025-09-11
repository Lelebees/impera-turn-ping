package com.lelebees.imperabot.impera.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lelebees.imperabot.impera.domain.ExceptionModel;
import com.lelebees.imperabot.impera.domain.ImperaLoginDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.history.ImperaGameHistoryDTO;
import com.lelebees.imperabot.impera.domain.history.exception.TurnNotFoundException;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import com.lelebees.imperabot.impera.domain.message.exception.ImperaMessageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.*;

@Component
public class ImperaAPIRepository implements ImperaRepository {
    private final Logger logger = LoggerFactory.getLogger(ImperaAPIRepository.class);
    private HttpEntity<String> entity;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String imperaURL;
    private final String imperaUsername;
    private final String imperaPassword;
    private ImperaLoginDTO bearerToken;

    public ImperaAPIRepository(@Value("${impera.api.url}") String imperaURL, @Value("${impera.username}") String imperaUsername, @Value("${impera.password}") String imperaPassword) {
        this.imperaURL = imperaURL;
        this.imperaUsername = imperaUsername;
        this.imperaPassword = imperaPassword;
        refreshBearerToken();
        logger.info("Ready to connect to the Impera API");
    }

    public void refreshBearerToken() {
        this.bearerToken = getBearerToken();
        updateEntity(bearerToken);
    }

    public int getTokenExpiryTime() {
        return bearerToken.expires_in();
    }

    private void updateEntity(ImperaLoginDTO bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken.access_token());
        entity = new HttpEntity<>(headers);
    }


    private ImperaLoginDTO getBearerToken() {
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

    @Override
    public Optional<ImperaGameViewDTO> findGameByGameId(long gameId) {
        String url = imperaURL + "/games/" + gameId;
        try {
            ResponseEntity<ImperaGameViewDTO> response = restTemplate.exchange(url, GET, entity, ImperaGameViewDTO.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.BadRequest e) {
            try {
                ExceptionModel exceptionModel = new ObjectMapper().readValue(e.getResponseBodyAsString(), ExceptionModel.class);
                if (!exceptionModel.error().equals("CannotFindGame")) {
                    throw new RuntimeException("Impera API returned unknown error.");
                }
                logger.warn("Impera API could not find game (" + gameId + ")");
                return Optional.empty();
            } catch (JsonProcessingException jsonProcessingException) {
                logger.error("Error parsing JSON: " + jsonProcessingException.getMessage(), jsonProcessingException);
            }
            logger.error("Unknown error attempting to access game (" + gameId + ")");
            return Optional.empty();
        }
    }

    @Override
    public List<ImperaMessageDTO> getMessages() {
        String url = imperaURL + "/messages/folder/Inbox";
        ResponseEntity<List<ImperaMessageDTO>> response = restTemplate.exchange(url, GET, entity, new ParameterizedTypeReference<>() {
        });
        return response.getBody();
    }

    @Override
    public List<ImperaMessageDTO> getMessagesBySubject(String subject) {
        return getMessages().stream()
                .filter(message -> message.subject().trim().equalsIgnoreCase(subject))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMessage(String id) {
        try {
            String url = imperaURL + "/messages/" + id;
            ResponseEntity<?> response = restTemplate.exchange(url, DELETE, entity, Map.class);
            response.getBody();
        } catch (HttpClientErrorException.BadRequest e) {
            try {
                ExceptionModel exceptionModel = new ObjectMapper().readValue(e.getResponseBodyAsString(), ExceptionModel.class);
                if (exceptionModel.error().equals("CannotFindMessage")) {
                    logger.info("Message " + id + " not found. It most likely does not exist.");
                    throw new ImperaMessageNotFoundException(e.getMessage());
                }
            } catch (JsonProcessingException jsonProcessingException) {
                logger.error("Error parsing JSON: " + jsonProcessingException.getMessage(), jsonProcessingException);
            }
            logger.info("Message " + id + " not found or inaccessible.");
        }
    }

    @Override
    public List<ImperaGameHistoryDTO> getTurnHistoryInRange(long gameId, int startTurnId, int endTurnId) throws TurnNotFoundException {
        List<ImperaGameHistoryDTO> turnStates = new ArrayList<>();
        for (int turnId = startTurnId; turnId < endTurnId; turnId++) {
            Optional<ImperaGameHistoryDTO> turnState = getTurnState(gameId, turnId);
            if (turnState.isEmpty()) {
                throw new TurnNotFoundException("Could not find turn " + turnId + " in game " + gameId + ".");
            }
            turnStates.add(turnState.get());
        }
        return turnStates;
    }

    private Optional<ImperaGameHistoryDTO> getTurnState(long gameId, int turnId) {
        try {
            String url = imperaURL + "/games/" + gameId + "/history/" + turnId;
            ResponseEntity<ImperaGameHistoryDTO> response = restTemplate.exchange(url, GET, entity, ImperaGameHistoryDTO.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.BadRequest e) {
            try {
                ExceptionModel exceptionModel = new ObjectMapper().readValue(e.getResponseBodyAsString(), ExceptionModel.class);
                if (exceptionModel.error().equals("CannotFindGame")) {
                    logger.warn("Game " + gameId + " not found. It most likely does not exist.");
                    return Optional.empty();
                }
                if (exceptionModel.error().equals("CannotFindTurn")) {
                    logger.warn("Turn " + turnId + " not found for game " + gameId + ". It most likely does not exist.");
                    return Optional.empty();
                }
            } catch (JsonProcessingException jsonProcessingException) {
                logger.error("Error parsing JSON: " + jsonProcessingException.getMessage(), jsonProcessingException);
            }
            logger.warn("Couldn't get turn state for game " + gameId + " and turn " + turnId + ".");
            return Optional.empty();
        }
    }
}
