package com.lelebees.imperabot.impera.domain.message;

import java.time.LocalDateTime;
import java.util.UUID;

public record ImperaMessageDTO(ImperaMessageCommunicatorDTO to, String subject, String text, UUID id,
                               ImperaMessageCommunicatorDTO from, String folder, LocalDateTime sentAt, boolean isRead) {
}
