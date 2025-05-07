package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.Objects;

public class ParticipantDto {
    public enum Role {
        OWNER,
        MEMBER
    }

    public enum Status {
        ACTIVE,
        INACTIVE,
        BLOCKED
    }

    private Long userId;
    private String username;
    private Role role;
    private Status status;

    public ParticipantDto(Long userId, String username, Role role, Status status) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.status = status;
    }

    public Long getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public String getRole() {
        return Objects.requireNonNullElse(this.role, Role.MEMBER).toString();
    }

    public String getStatus() {
        return Objects.requireNonNullElse(this.status, Status.ACTIVE).toString();
    }
}
