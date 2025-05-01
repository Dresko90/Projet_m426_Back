package ch.epai.ict.m295.messaging.backend.domain;

import java.util.List;

public class Conversation {
    private long id;
    private List<Participant> participants;
    private boolean isGroup;

    public Conversation(long id, boolean isGroup, List<Participant> participants) {
        this.id = id;
        this.isGroup = isGroup;
        this.participants = participants;
    }

    public long getId() {
        return id;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public boolean isParticipant(long userId) {
        for (Participant participant : participants) {
            if (participant.getUserId() == userId) {
                return true;
            }
        }
        return false;
    }

    public Participant getParticipant(long userId) {
        for (Participant participant : participants) {
            if (participant.getUserId() == userId) {
                return participant;
            }
        }
        return null;
    }

    public List<Participant> getParticipants() {
        return participants;
    }
}