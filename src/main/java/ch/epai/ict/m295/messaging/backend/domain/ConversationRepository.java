package ch.epai.ict.m295.messaging.backend.domain;

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
    void addParticipants(long conversationId, List<Participant> participants);
    void updateParticipant(long conversationId, Participant participant);
    void removeParticipant(long conversationId, long userId);
    
    List<Message> getMessages(long conversationId, long userId, int pageNumber, int pageSize);
    long getNumberOfMessagesForConversation(long conversationId);
    Message getMessageById(long messageId);
    void createMessage(Message message);
    void markMessageAsReadForUser(long messageId, long userId);
    void markMessageAsDeletedForUser(long messageId, long userId); 
}