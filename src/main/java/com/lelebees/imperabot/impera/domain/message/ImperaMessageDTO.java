package com.lelebees.imperabot.impera.domain.message;

import java.time.LocalDateTime;

public record ImperaMessageDTO(ImperaMessageCommunicatorDTO to, String subject, String text, String id,
                               ImperaMessageCommunicatorDTO from, String folder, LocalDateTime sentAt, boolean isRead) {

    @Override
    public String toString() {
        return "ImperaMessageDTO{" +
                "to=" + to +
                ", subject='" + subject + '\'' +
                ", text='" + text + '\'' +
                ", id='" + id + '\'' +
                ", from=" + from +
                ", folder='" + folder + '\'' +
                ", sentAt=" + sentAt +
                ", isRead=" + isRead +
                '}';
    }
}
