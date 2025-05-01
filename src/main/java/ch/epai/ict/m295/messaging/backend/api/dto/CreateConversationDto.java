package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.List;

public class CreateConversationDto {
    private Long userId;
    private String username;
    private List<ParticipantDto> participants;

    public CreateConversationDto(Long userId, String username, List<ParticipantDto> participants) {
        this.userId = userId;
        this.username = username;
        this.participants = participants;
    }

    public Boolean isGroup() {
        return participants != null && participants.size() > 0;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public List<ParticipantDto> getParticipants() {
        return participants;
    }
}
