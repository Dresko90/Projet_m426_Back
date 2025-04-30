package ch.epai.ict.m295.messaging.backend.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "messages")
public class MessageResponseDto extends RepresentationModel<MessageResponseDto> {
    private long id;
    private long senderId;
    private String body;
    private LocalDateTime sentAt;
    private List<MessageStatusDto> participantStatus;

    public MessageResponseDto(long id, long senderId, String body, LocalDateTime sendAt, List<MessageStatusDto> participantStatus) {
        this.id = id;
        this.senderId = senderId;
        this.body = body;
        this.sentAt = sendAt;
        this.participantStatus = participantStatus;
    }

    public long getId() {
        return id;
    }

    public long getSenderId() {
        return senderId;
    }
    public String getBody() {
        return body;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public List<MessageStatusDto> getParticipantStatus() {
        return participantStatus;
    }    
}
