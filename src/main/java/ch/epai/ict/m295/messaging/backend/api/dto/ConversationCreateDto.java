package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.epai.ict.m295.messaging.backend.domain.User;

public class ConversationCreateDto {
    private Long userId;
    private String username;
    private List<ParticipantDto> participants;

    public ConversationCreateDto(Long userId, String username, List<ParticipantDto> participants) {
        this.userId = userId;
        this.username = username;
        this.participants = participants;
    }

    @JsonIgnore
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
        return this.participants;
    }

    @JsonIgnore
    public List<ParticipantDto> getAllParticipant(User principal) {
        List<ParticipantDto> fullList = new ArrayList<>();
        if (this.isGroup()) {
            fullList.addAll(this.participants);
            fullList.add(new ParticipantDto(
                    principal.getId(),
                    principal.getUsername(),
                    ParticipantDto.Role.OWNER,
                    ParticipantDto.Status.ACTIVE));
        } else {
            fullList = List.of(
                new ParticipantDto(
                        this.userId,
                        this.username,
                        ParticipantDto.Role.MEMBER,
                        ParticipantDto.Status.ACTIVE),
                new ParticipantDto(
                        principal.getId(),
                        principal.getUsername(),
                        ParticipantDto.Role.MEMBER,
                        ParticipantDto.Status.ACTIVE));
        }
        return Collections.unmodifiableList(fullList);
     }
}
