package ch.epai.ict.m295.messaging.backend.api.dto;

public class ParticipantDto {
    public enum Role {
        OWNER,
        MEMBER
    }

    public enum Status {
        ACTIVE,
        INACTIVE,
        BLOCKED,
        INVITED
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
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role == null ? Role.MEMBER.toString() : role.toString();
    }

    public String getStatus() {
        return status == null ? Status.INVITED.toString() : status.toString();
    }
}
