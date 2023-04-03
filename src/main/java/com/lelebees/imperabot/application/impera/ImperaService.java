package com.lelebees.imperabot.application.impera;

import com.lelebees.imperabot.domain.impera.ImperaLoginDTO;
import com.lelebees.imperabot.domain.impera.game.ImperaGameDTO;
import com.lelebees.imperabot.domain.impera.game.view.ImperaGameViewDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static com.lelebees.imperabot.ImperaBotApplication.dotenv;

@Service
public class ImperaService {
    private final String imperaURL = dotenv.get("IMPERA_API_URL");
    private final ImperaLoginDTO bearerToken;
    private final HttpHeaders headers;

    public ImperaService() {
        this.bearerToken = getBearerToken();
        this.headers = new HttpHeaders();
        headers.setBearerAuth(bearerToken.access_token);
        System.out.println(getGames());
        System.out.println(getGame(367410));
    }

    public ImperaLoginDTO getBearerToken() {
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("username", dotenv.get("IMPERA_USER_NAME"));
        map.add("password", dotenv.get("IMPERA_USER_PASSWORD"));
        map.add("scope", "openid offline_access roles");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        String url = imperaURL + "/Account/token";

        ResponseEntity<ImperaLoginDTO> response = restTemplate.postForEntity(url, request, ImperaLoginDTO.class);

        return response.getBody();
    }

    public List<ImperaGameDTO> getGames() {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = imperaURL + "/games/my";
        ParameterizedTypeReference<List<ImperaGameDTO>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<List<ImperaGameDTO>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<ImperaGameDTO>>() {
        });
        return response.getBody();
    }

    public ImperaGameViewDTO getGame(int gameID)
    {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = imperaURL + "/games/"+gameID;
        ResponseEntity<ImperaGameViewDTO> response = restTemplate.exchange(url, HttpMethod.GET, entity, ImperaGameViewDTO.class);
        return response.getBody();
    }
}
