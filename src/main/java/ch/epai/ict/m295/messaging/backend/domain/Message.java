package ch.epai.ict.m295.messaging.backend.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class Message {
    private long id;
    private long conversationId;
    private long senderId;
    private String content;
    private LocalDateTime sendAt;
    private List<MessageStatus> messageStatus;


    public Message(long id, long conversationId, long senderId, String content, List<MessageStatus> messageStatus) {
        this(id, conversationId, senderId, content, LocalDateTime.now(), messageStatus);
    }

    public Message(long id, long conversationId, long senderId, String content, LocalDateTime sendAt, List<MessageStatus> messageStatus) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentDateTime() {
        return sendAt;
    }

    public List<MessageStatus> getMessageStatus() {
        return Collections.unmodifiableList(messageStatus);
    }
}