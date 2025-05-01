package ch.epai.ict.m295.messaging.backend.api.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.epai.ict.m295.messaging.backend.api.dto.CreateMessageDto;
import ch.epai.ict.m295.messaging.backend.api.dto.MessageResponseDto;
import ch.epai.ict.m295.messaging.backend.api.dto.MessageStatusDto;
import ch.epai.ict.m295.messaging.backend.domain.ConversationRepository;
import ch.epai.ict.m295.messaging.backend.domain.Message;
import ch.epai.ict.m295.messaging.backend.domain.MessageBuilder;
import ch.epai.ict.m295.messaging.backend.domain.MessageRepository;
import ch.epai.ict.m295.messaging.backend.domain.MessageStatus;
import ch.epai.ict.m295.messaging.backend.domain.User;

@RestController
public class MessageController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public MessageController(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public CollectionModel<MessageResponseDto> getMessages(@PathVariable long conversationId, @RequestAttribute User principal) {
        List<Message> messages = messageRepository.getMessages(conversationId);
        return CollectionModel.of(
                messages.stream()
                    .map(conversation -> toMessageResponse(conversation))
                    .collect(Collectors.toList()),
                linkTo(methodOn(MessageController.class).getMessages(conversationId, principal)).withSelfRel());
    }

    @PostMapping("/conversations/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponseDto createMessages(@PathVariable long conversationId, @RequestBody CreateMessageDto createMessageDto, @RequestAttribute User principal) {
        Message message = MessageBuilder.create()
            .setConversation(conversationRepository.getConversation(conversationId))
            .setSenderId(principal.getId())
            .setBody(createMessageDto.body())
            .build();
        messageRepository.createMessage(message);
        return toMessageResponse(message);
    }

    @PatchMapping("/conversations/{conversationId}/messages/{messageId}")
    public MessageResponseDto getMessage(@PathVariable long conversationId, @PathVariable long messageId, User principal) {
        return null;
    }

    private MessageResponseDto toMessageResponse(Message message) {
        return new MessageResponseDto(
            message.getId(), 
            message.getSenderId(),
            message.getBody(),
            message.getSentDateTime(), 
            message.getMessageStatus()
                .stream()
                .map(messageStatus -> toMessageStatusDto(messageStatus))
                .collect(Collectors.toList()))
            .add(linkTo(methodOn(MessageController.class).getMessage(message.getConversationId(), message.getId(), null)).withSelfRel());

    }

    private MessageStatusDto toMessageStatusDto(MessageStatus messageStatus) {
        return new MessageStatusDto(
            messageStatus.getUserId(),
            messageStatus.getReadAt(),
            messageStatus.isDeleted());
    }
}