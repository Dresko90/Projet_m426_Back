package ch.epai.ict.m295.messaging.backend.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageBuilder {
    private Long id;
    private long conversationId;
    private Conversation conversation;
    private Long senderId;
    private String content;
    private LocalDateTime sentAt;
    private List<MessageStatus> messageStatus;

    public static MessageBuilder create() {
        return new MessageBuilder();
    }

    private MessageBuilder() {
        this.sentAt = LocalDateTime.now();
    }

    public MessageBuilder setId(long id) {
        this.id = id;
        return this;
    }

    public MessageBuilder setConversation(Conversation conversation) {
        this.conversation = conversation;
        return this;
    }

    public MessageBuilder setConversationId(long conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public MessageBuilder setSenderId(long senderId) {
        this.senderId = senderId;
        return this;
    }

    public MessageBuilder setContent(String content) {
        this.content = content;
        return this;
    }

    public MessageBuilder setSentDateTime(LocalDateTime dateTime) {
        this.sentAt = dateTime;
        return this;
    }

    public MessageBuilder setMessageStatus(List<MessageStatus> messageStatus) {
        this.messageStatus = messageStatus;
        return this;
    }

    public Message build() {
        if (id == null) {
            id = IdGeneratorManager.get(Message.class).getNextId();
        }
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
        if (conversation != null) {
            this.messageStatus = new ArrayList<>();
            for(Participant participant : conversation.getParticipants()) {
                messageStatus.add(
                    MessageStatusBuilder.create()
                        .setUserId(participant.getUserId())
                        .setReadAt(null)
                        .setDeleted(false)
                        .build());
            }
            conversationId = conversation.getId();
        }

        if (messageStatus == null) {
            throw new IllegalArgumentException("MessageStatus cannot be null");
        }

        return new Message(id, conversation.getId(), senderId, content, sentAt, messageStatus);
    }
}