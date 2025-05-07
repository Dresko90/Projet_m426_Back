package ch.epai.ict.m295.messaging.backend.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.epai.ict.m295.messaging.backend.domain.Participant;

public record ParticipantUpdateDto(ParticipantDto.Role role, ParticipantDto.Status status) {
    @JsonIgnore
    public boolean isActive() {
        return status == ParticipantDto.Status.ACTIVE;
    }
    
    @JsonIgnore
    public boolean isOwner() {
        return role == ParticipantDto.Role.OWNER;
    }

    @JsonIgnore
    public boolean isMember() {
        return role == ParticipantDto.Role.MEMBER;
    }

    @JsonIgnore
    public boolean isBlocked() {
        return status == ParticipantDto.Status.BLOCKED;
    }

    @JsonIgnore
    public boolean hasSameStatus(Participant participant) {
        String status1 = status.toString();
        String status2 = participant.getStatus().toString();
        return status1.equals(status2);
    }

    @JsonIgnore
    public boolean hasSameRole(Participant participant) {
        return role.toString().equals(participant.getRole().toString());
    }
}
