package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import ch.epai.ict.m295.messaging.backend.domain.Participant;

@Relation(collectionRelation = "participants")
public class ParticipantResponseDto extends RepresentationModel<ParticipantResponseDto> {
    private long userId;
    private String username;
    private Participant.Role role;
    private Participant.Status status;

    public ParticipantResponseDto(long userId, String username, Participant.Role role, Participant.Status status, Link... links) {
        super(List.of(links));
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role.toString();
    }

    public String getStatus() {
        return status.toString();
    }
}
 