package ch.epai.ict.m295.messaging.backend.domain;

import java.time.LocalDateTime;
import java.util.List;

public interface ConversationRepository {
    void createConversation(Conversation conversation);
    Conversation getConversationById(long conversationId);
    List<Conversation> getConversationsByUser(User user, int pageNumber, int pageSize);
    public long getNumberOfConvesationForUser(User user);
    List<Conversation> getConversationsByParticipants(Participant participant1, Participant participant2);
    void updateConversation(Conversation conversation);
    void deleteConversation(long conversationId);

    void addParticipant(long conversationId, Participant participant);
    void updateParticipant(long conversationId, Participant participant);
    void removeParticipant(long conversationId, long userId);
    
    List<Message> getMessages(long conversationId, int pageNumber, int pageSize);
    long getNumberOfMessagesForConversation(long conversationId);
    void createMessage(Message message);
    void updateMessageReadAtForUser(long messageId, long userId, LocalDateTime readAt);
    void deleteMessageForUser(long messageId, long userId); 
}