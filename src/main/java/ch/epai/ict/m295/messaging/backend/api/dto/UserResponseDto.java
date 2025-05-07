package ch.epai.ict.m295.messaging.backend.api.dto;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "users")
public class UserResponseDto extends RepresentationModel<UserResponseDto> {
    private long id;
    private String username;
    private String displayName;
    
    public UserResponseDto(long id, String username, String displayName) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
