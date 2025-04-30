package ch.epai.ict.m295.messaging.backend.api.dto;

import java.time.LocalDateTime;

public record MessageDto(Long id, String content, LocalDateTime timestamp) {
}
