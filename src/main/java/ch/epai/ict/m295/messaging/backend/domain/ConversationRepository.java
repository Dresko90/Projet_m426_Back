package ch.epai.ict.m295.messaging.backend.domain;

import java.util.List;

public interface ConversationRepository {
    void createConversation(Conversation conversation);
    Conversation getConversation(long conversationId);
    List<Conversation> findConversationsByUser(User user);
    void updateConversation(Conversation conversation);
    void addParticipant(long conversationId, Participant participant);
    void removeParticipant(long conversationId, long userId);
    void deleteConversation(long conversationId);
}