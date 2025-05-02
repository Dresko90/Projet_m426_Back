package ch.epai.ict.m295.messaging.backend.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface ConversationRepository {
    void createConversation(Conversation conversation);
    Conversation getConversation(long conversationId);
    List<Conversation> findConversationsByUser(User user);
    List<Conversation> findConversationsByParticipants(Participant participant1, Participant participant2);
    void updateConversation(Conversation conversation);
    void deleteConversation(long conversationId);

    void addParticipant(long conversationId, Participant participant);
    void updateParticipant(long conversationId, Participant participant);
    void removeParticipant(long conversationId, long userId);
    
    List<Message> getMessages(long conversationId);
    void createMessage(Message message);
    void updateMessageReadAtForUser(long messageId, long userId, LocalDateTime readAt);
    void deleteMessageForUser(long messageId, long userId); 
}