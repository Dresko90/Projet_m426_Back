package ch.epai.ict.m295.messaging.backend.domain;

import java.util.List;

public interface MessageRepository {
    void createMessage(Message message);
    List<Message> getMessages(long conversationId);
    void updateMessageStatusForUser(long messageId, long userId);
    void deleteMessageForUser(long messageId, long userId); 
}