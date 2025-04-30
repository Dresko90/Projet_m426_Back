package ch.epai.ict.m295.messaging.backend.api.dto;

import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "users")
public record UserResponseDto(long id, String username, String displayName) {
}
