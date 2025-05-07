package ch.epai.ict.m295.messaging.backend.api.dto;


import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "participants")
public class ParticipantResponseDto extends RepresentationModel<ParticipantResponseDto> {
    private long userId;
    private String username;
    private String role;
    private String status;

    public ParticipantResponseDto(long userId, String username, String role, String status) {
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
 