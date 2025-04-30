package ch.epai.ict.m295.messaging.backend.domain;

import java.time.LocalDateTime;

public class MessageStatusBuilder {
    private long userId;
    private LocalDateTime readAt;
    private boolean deleted;

    public static MessageStatusBuilder create() {
        return new MessageStatusBuilder();
    }

    public MessageStatusBuilder setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public MessageStatusBuilder setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
        return this;
    }

    public MessageStatusBuilder setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public MessageStatus build() {
        return new MessageStatus(userId, readAt, deleted);
    }
}
