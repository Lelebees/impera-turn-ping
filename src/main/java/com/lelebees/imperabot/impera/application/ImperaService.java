package com.lelebees.imperabot.impera.application;

import com.lelebees.imperabot.impera.domain.ImperaLoginDTO;
import com.lelebees.imperabot.impera.domain.game.ImperaGameDTO;
import com.lelebees.imperabot.impera.domain.game.view.ImperaGameViewDTO;
import com.lelebees.imperabot.impera.domain.message.ImperaMessageDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.lelebees.imperabot.ImperaBotApplication.env;

@Service
public class ImperaService {
    private final String imperaURL = env.get("IMPERA_API_URL");
    private final HttpEntity<String> entity;
    private final RestTemplate restTemplate = new RestTemplate();

    public ImperaService() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getBearerToken().access_token);
        this.entity = new HttpEntity<>(headers);
        //TODO: Replace with declaration of who we logged in as
//        System.out.println("[I] ImperaService ready to make connections!");
//        System.out.println(getGames());
//        System.out.println(getMessages());
    }

    public ImperaLoginDTO getBearerToken() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", env.get("IMPERA_USER_NAME"));
        map.add("password", env.get("IMPERA_USER_PASSWORD"));
        map.add("scope", "openid offline_access roles");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        String url = imperaURL + "/Account/token";

        ResponseEntity<ImperaLoginDTO> response = restTemplate.exchange(url, HttpMethod.POST, request, ImperaLoginDTO.class);

        return response.getBody();
    }

    public List<ImperaGameDTO> getGames() {
        String url = imperaURL + "/games/my";
        ResponseEntity<List<ImperaGameDTO>> response = restTemplate.exchange(url, HttpMethod.GET, this.entity, new ParameterizedTypeReference<>() {
        });
        return response.getBody();
    }

    //TODO: Evaluate if this is necessary
    public ImperaGameViewDTO getGame(int gameID) {
        String url = imperaURL + "/games/" + gameID;
        ResponseEntity<ImperaGameViewDTO> response = restTemplate.exchange(url, HttpMethod.GET, this.entity, ImperaGameViewDTO.class);
        return response.getBody();
    }

    public List<ImperaMessageDTO> getMessages() {
        String url = imperaURL + "/messages/folder/Inbox";
        ResponseEntity<List<ImperaMessageDTO>> response = restTemplate.exchange(url, HttpMethod.GET, this.entity, new ParameterizedTypeReference<>() {
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
}
