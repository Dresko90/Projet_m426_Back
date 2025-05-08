package ch.epai.ict.m295.messaging.backend.api.dto;

import java.util.ArrayList;
import java.util.List;

public class ParticipantsAddDto {
    private List<ParticipantDto> participants;

    public ParticipantsAddDto() {
        this.participants = new ArrayList<>();
    }

    public List<ParticipantDto> getParticipants() {
        return participants;
    }
}
