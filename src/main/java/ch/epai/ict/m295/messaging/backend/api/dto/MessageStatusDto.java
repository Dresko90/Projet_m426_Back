package ch.epai.ict.m295.messaging.backend.api.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageStatusDto {
    private long userId;
    private LocalDateTime readAt;
    private boolean deleted;

    public MessageStatusDto(long userId, LocalDateTime readAt, boolean deleted) {
        this.userId = userId;
        this.readAt = readAt;
        this.deleted = deleted;
    }

    @JsonProperty("user_id")
    public long getUserId() {
        return userId;
    }

    @JsonProperty("read_at")
    public LocalDateTime getReadAt() {
        return readAt;
    }

    @JsonProperty("deleted")
    public boolean isDeleted() {
        return deleted;
    }
}
