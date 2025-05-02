package ch.epai.ict.m295.messaging.backend.api.dto;

public record UpdateParticipantDto(ParticipantDto.Role role, ParticipantDto.Status status) {
    public boolean isActive() {
        return status == ParticipantDto.Status.ACTIVE;
    }
    public boolean isOwner() {
        return role == ParticipantDto.Role.OWNER;
    }
    public boolean isMember() {
        return role == ParticipantDto.Role.MEMBER;
    }
    public boolean isBlocked() {
        return status == ParticipantDto.Status.BLOCKED;
    }
}
