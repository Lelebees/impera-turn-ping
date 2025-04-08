package com.lelebees.imperabot.impera.domain;

import java.util.List;

public record ImperaMeDTO(String userId, String userName, boolean hasRegistered, String loginProvider, String language,
                          List<String> roles, String allianceId, boolean allianceAdmin) {
}
