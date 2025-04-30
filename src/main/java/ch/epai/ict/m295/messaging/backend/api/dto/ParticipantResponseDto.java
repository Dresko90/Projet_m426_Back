package ch.epai.ict.m295.messaging.backend.api.dto;

import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "participants")
public record ParticipantResponseDto(long userId, String username, String role, String status) {
}
 