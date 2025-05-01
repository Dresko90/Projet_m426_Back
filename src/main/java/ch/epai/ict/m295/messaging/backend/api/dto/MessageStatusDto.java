package ch.epai.ict.m295.messaging.backend.api.dto;

import java.time.LocalDateTime;

public record MessageStatusDto(long userId, LocalDateTime readAt, boolean deleted) {
}
