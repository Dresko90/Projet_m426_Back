package ch.epai.ict.m295.messaging.backend.api.dto;

public record CreateMessageDto(Long senderId, String body) {
}