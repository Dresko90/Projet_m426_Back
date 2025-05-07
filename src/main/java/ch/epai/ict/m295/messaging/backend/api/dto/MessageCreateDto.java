package ch.epai.ict.m295.messaging.backend.api.dto;

public record MessageCreateDto(Long senderId, String body) {
}