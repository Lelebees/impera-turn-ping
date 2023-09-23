package com.lelebees.imperabot.impera.application;

import com.lelebees.imperabot.bot.domain.user.exception.UserNotVerifiedException;
import com.lelebees.imperabot.impera.domain.ImperaLoginDTO;
import com.lelebees.imperabot.impera.domain.ImperaMeDTO;
import com.lelebees.imperabot.impera.domain.game.ImperaGameDTO;
import com.lelebees.imperabot.impera.domain.game.exception.ImperaGameNotFoundException;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGamePlayerDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.springframework.http.HttpMethod.*;

@Service
public class ImperaService {
    private final String imperaURL;
    private final String imperaUsername;
    private final String imperaPassword;
    private HttpEntity<String> entity;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String botId;
    public static ImperaLoginDTO bearerToken;

    public ImperaService(@Value("${impera.api.url}") String imperaURL, @Value("${impera.username}") String imperaUsername, @Value("${impera.password}") String imperaPassword) {
        this.imperaURL = imperaURL;
        this.imperaUsername = imperaUsername;
        this.imperaPassword = imperaPassword;
        bearerToken = getBearerToken(null);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken.access_token);
        this.entity = new HttpEntity<>(headers);
        this.botId = getMyId().userId;
        System.out.println("[I] ImperaService ready to make connections!");
    }

    public ImperaLoginDTO getBearerToken(String refreshToken) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", imperaUsername);
        map.add("password", imperaPassword);
        map.add("scope", "openid offline_access roles");
        map.add("refresh_token", (refreshToken == null ? "" : refreshToken));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        String url = imperaURL + "/Account/token";

        ResponseEntity<ImperaLoginDTO> response = restTemplate.exchange(url, POST, request, ImperaLoginDTO.class);
        return response.getBody();
    }

    public ImperaMeDTO getMyId() {
        String url = imperaURL + "/Account/UserInfo";
        ResponseEntity<ImperaMeDTO> response = restTemplate.exchange(url, GET, this.entity, ImperaMeDTO.class);
        return response.getBody();
    }

    public List<ImperaGameDTO> getGames() {
        String url = imperaURL + "/games/my";
        ResponseEntity<List<ImperaGameDTO>> response = restTemplate.exchange(url, GET, this.entity, new ParameterizedTypeReference<>() {
        });
        return response.getBody();
    }

    public ImperaGameViewDTO getGame(long gameID) {
        try {
            String url = imperaURL + "/games/" + gameID;
            ResponseEntity<ImperaGameViewDTO> response = restTemplate.exchange(url, GET, this.entity, ImperaGameViewDTO.class);
            return response.getBody();
        } catch (Exception e) {
            throw new ImperaGameNotFoundException(e.getMessage());
        }
    }

    public boolean joinGame(long gameID, String password) {
        String url = imperaURL + "/games/" + gameID + "/join?password=" + password;
        ResponseEntity<?> response = restTemplate.exchange(url, POST, this.entity, Map.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    public ImperaGameViewDTO surrenderGame(long gameID) {
        String url = imperaURL + "/games/" + gameID + "/surrender";
        ResponseEntity<ImperaGameViewDTO> response = restTemplate.exchange(url, POST, this.entity, ImperaGameViewDTO.class);
        return response.getBody();
    }

    public List<ImperaMessageDTO> getMessages() {
        String url = imperaURL + "/messages/folder/Inbox";
        ResponseEntity<List<ImperaMessageDTO>> response = restTemplate.exchange(url, GET, this.entity, new ParameterizedTypeReference<>() {
        });
        return response.getBody();
    }

    public List<ImperaMessageDTO> getLinkMessages() {
        List<ImperaMessageDTO> allMessages = getMessages();
        List<ImperaMessageDTO> linkMessages = new ArrayList<>();
        for (ImperaMessageDTO message : allMessages) {
            if (message.subject.equalsIgnoreCase("link")) {
                linkMessages.add(message);
            }
        }
        return linkMessages;
    }

    public boolean isPlayerInGame(String playerId, long gameId) {
        ImperaGameViewDTO game = getGame(gameId);
        if (playerId == null) {
            throw new UserNotVerifiedException("No user was entered!");
        }
        ImperaGamePlayerDTO playerDTO = new ImperaGamePlayerDTO();
        playerDTO.userId = playerId;

        AtomicBoolean playerInGame = new AtomicBoolean(false);
        game.teams.forEach(team -> {
            if (team.players.contains(playerDTO)) {
                playerInGame.set(true);
            }
        });
        return playerInGame.get();
    }

    public boolean isBotInGame(long gameID) {
        return isPlayerInGame(botId, gameID);
    }

    public Runnable updateAccessToken() {
        return () -> {
            try {
                System.out.println("Updating bearer token!");
//                System.out.println(bearerToken.access_token);
                bearerToken = getBearerToken(bearerToken.refresh_token);
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(bearerToken.access_token);
                this.entity = new HttpEntity<>(headers);
//                System.out.println(bearerToken.access_token);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        };
    }

    public Object deleteMessage(String id) {
        String url = imperaURL + "/messages/" + id;
        ResponseEntity<?> response = restTemplate.exchange(url, DELETE, this.entity, Map.class);
        return response.getBody();
    }
}
