package com.lelebees.imperabot.impera.domain.message;

import java.time.LocalDateTime;

public class ImperaMessageDTO {
    public ImperaMessageCommunicatorDTO to;
    public String subject;
    public String text;
    public String id;
    public ImperaMessageCommunicatorDTO from;
    public String folder;
    public LocalDateTime sentAt;
    public boolean isRead;
}
