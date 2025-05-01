package ch.epai.ict.m295.messaging.backend.domain;

import java.util.ArrayList;
import java.util.List;

public class ConversationBuilder {
    private Long id;
    private Boolean isGroup;
    private List<Participant> participants;

    public static ConversationBuilder create() {
        return new ConversationBuilder();
    }

    private ConversationBuilder() {
        this.participants = new ArrayList<>();
    }

    public ConversationBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public ConversationBuilder setIsGroup(boolean isGroup) {
        this.isGroup = isGroup;
        return this;
    }

    public ConversationBuilder setParticipants(List<Participant> participants) {
        this.participants = participants;
        return this;
    }

    public ConversationBuilder addParticipant(Participant participantId) {
        this.participants.add(participantId);
        return this;
    }

    public Conversation build() {
        if (id == null) {
            id = IdGeneratorManager.get(Conversation.class).getNextId();
        }
        if (isGroup == null) {
            isGroup = false;
        }
        return new Conversation(id, isGroup, participants);
    }
}