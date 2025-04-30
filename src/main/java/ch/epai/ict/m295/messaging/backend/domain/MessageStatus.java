package ch.epai.ict.m295.messaging.backend.domain;

import java.time.LocalDateTime;

public class MessageStatus {
    private long userId;
    private LocalDateTime readAt;
    private boolean deleted;

    public MessageStatus(long userId, LocalDateTime readAt, boolean deleted) {
        this.userId = userId;
        this.readAt = readAt;
        this.deleted = deleted;
    }

    public long getUserId() {
        return userId;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public boolean isDeleted() {
        return deleted;
    }
}
