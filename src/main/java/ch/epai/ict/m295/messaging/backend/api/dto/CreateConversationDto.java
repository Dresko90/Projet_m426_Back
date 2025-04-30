package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.List;

public class CreateConversationDto {
    private List<Long> participants;

    public CreateConversationDto() {
        // Constructeur par défaut requis pour la désérialisation JSON
    }

    public CreateConversationDto(List<Long> participants) {
        this.participants = participants;
    }

    public List<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Long> participants) {
        this.participants = participants;
    }
}