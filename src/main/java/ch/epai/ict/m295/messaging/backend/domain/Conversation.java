package ch.epai.ict.m295.messaging.backend.domain;

import java.util.List;

public class Conversation {
    private long id;
    private List<Participant> participants;

    public Conversation(long id, List<Participant> participants) {
        this.id = id;
        this.participants = participants;
    }

    public long getId() {
        return id;
    }

    public List<Participant> getParticipants() {
        return participants;
    }
}