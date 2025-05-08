package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

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

    @Schema(description = "Rôle de lea participant·e", defaultValue = "MEMBER")
    public Role getRole() {
        return Objects.requireNonNullElse(this.role, Role.MEMBER);
    }

    @Schema(description = "Statut de lea participant·e", defaultValue = "ACTIVE")
    public Status getStatus() {
        return Objects.requireNonNullElse(this.status, Status.ACTIVE);
    }
}
