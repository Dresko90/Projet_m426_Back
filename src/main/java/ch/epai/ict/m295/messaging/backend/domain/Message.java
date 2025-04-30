package ch.epai.ict.m295.messaging.backend.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class Message {
    private long id;
    private long conversationId;
    private long senderId;
    private String body;
    private LocalDateTime sendAt;
    private List<MessageStatus> messageStatus;


    public Message(long id, long conversationId, long senderId, String body, List<MessageStatus> messageStatus) {
        this(id, conversationId, senderId, body, LocalDateTime.now(), messageStatus);
    }

    public Message(long id, long conversationId, long senderId, String content, LocalDateTime sendAt, List<MessageStatus> messageStatus) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.body = content;
        this.sendAt = sendAt;
        this.messageStatus = messageStatus;
    }

    public long getId() {
        return id;
    }

    public long getConversationId() {
        return conversationId;
    }

    public long getSenderId() {
        return senderId;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getSentDateTime() {
        return sendAt;
    }

    public List<MessageStatus> getMessageStatus() {
        return Collections.unmodifiableList(messageStatus);
    }
}