package ch.epai.ict.m295.messaging.backend.api.controllers;

import org.springframework.hateoas.CollectionModel;
import org.springframework.web.bind.annotation.*;

import ch.epai.ict.m295.messaging.backend.api.dto.*;
import ch.epai.ict.m295.messaging.backend.domain.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class MessageController {

    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public CollectionModel<MessageResponseDto> getMessages(@PathVariable long conversationId, @RequestAttribute User principal) {
        List<Message> messages = messageRepository.getMessages(conversationId);
        return CollectionModel.of(
                messages.stream()
                    .map(conversation -> toMessageResponse(conversation, principal))
                    .collect(Collectors.toList()),
                linkTo(methodOn(MessageController.class).getMessages(conversationId, principal)).withSelfRel());
    }

    @PatchMapping("/conversations/{conversationId}/messages/{messageId}")
    public MessageResponseDto getMessage(@PathVariable long conversationId, @PathVariable long messageId, User principal) {
        return null;
    }

    private MessageResponseDto toMessageResponse(Message message, User principal) {
        return new MessageResponseDto(
            message.getId(), 
            message.getSenderId(),
            message.getBody(),
            message.getSentDateTime(), 
            message.getMessageStatus()
                .stream()
                .map(messageStatus -> toMessageStatusDto(messageStatus))
                .collect(Collectors.toList()))
            .add(linkTo(methodOn(MessageController.class).getMessage(message.getConversationId(), message.getId(),  principal)).withSelfRel());

    }



    private MessageStatusDto toMessageStatusDto(MessageStatus messageStatus) {
        return new MessageStatusDto(
            messageStatus.getUserId(),
            messageStatus.getReadAt(),
            messageStatus.isDeleted());
    }

    // // POST /conversations
    // @PostMapping
    // @ResponseStatus(HttpStatus.CREATED)
    // public EntityModel<ConversationResponseDto> createConversation(
    //         @RequestBody CreateConversationDto createConversationDto, 
    //         @RequestAttribute User principal) {

    //     ConversationBuilder builder = ConversationBuilder.create();
    //     builder.addParticipant(
    //         new Participant(
    //             principal.getId(), 
    //             principal.getUsername(), 
    //             Participant.Role.OWNER, 
    //             Participant.Status.ACTIVE));

    //     for (Long participantId : createConversationDto.getParticipants()) {
    //         User participant = userRepository.getUser(participantId);
    //         builder.addParticipant(
    //             new Participant(
    //                 participant.getId(), 
    //                 participant.getUsername(), 
    //                 Participant.Role.MEMBER, 
    //                 Participant.Status.INVITED));
    //     }

    //     Conversation conversation = builder.build();
    //     conversationRepository.createConversation(conversation);

    //     return toConversationResponse(conversation, principal);
    // }

    // // GET /conversations/{conversationId}
    // @GetMapping("/{conversationId}")
    // public EntityModel<ConversationResponseDto> getConversation(@PathVariable long conversationId, @RequestAttribute User principal) {
    //     Conversation conversation = conversationRepository.getConversation(conversationId);

    //     if (conversation.getParticipants().stream().noneMatch(p -> p.getUserId() == principal.getId())) {
    //         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation");
    //     }

    //     return toConversationResponse(conversation, principal);
    // }

    // // GET /conversations/{conversationId}/participants
    // @GetMapping("/{conversationId}/participants")
    // public CollectionModel<EntityModel<ParticipantResponseDto>> getParticipants(@PathVariable long conversationId, @RequestAttribute User principal) {
    //     Conversation conversation = conversationRepository.getConversation(conversationId);

    //     if (conversation.getParticipants().stream().noneMatch(p -> p.getUserId() == principal.getId())) {
    //         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation");
    //     }

    //     List<EntityModel<ParticipantResponseDto>> participantModels = conversation.getParticipants().stream()
    //             .map(participant -> toParticipantResponse(conversation, participant, principal))
    //             .collect(Collectors.toList());

    //     return CollectionModel.of(participantModels,
    //             linkTo(methodOn(MessageController.class).getParticipants(conversationId, principal)).withSelfRel());
    // }

    // // GET /conversations/{conversationId}/participants/{participantId}
    // @GetMapping("/{conversationId}/participants/{participantId}")
    // public EntityModel<ParticipantResponseDto> getParticipant(@PathVariable long conversationId, @PathVariable long participantId, @RequestAttribute User principal) {
    //     Conversation conversation = conversationRepository.getConversation(conversationId);

    //     if (conversation.getParticipants().stream().noneMatch(p -> p.getUserId() == principal.getId())) {
    //         throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation");
    //     }

    //     Participant participant = conversation.getParticipants().stream()
    //             .filter(p -> p.getUserId() == participantId)
    //             .findFirst()
    //             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));

    //     return toParticipantResponse(conversation, participant, principal);
    // }

    // private EntityModel<ConversationResponseDto> toConversationResponse(Conversation conversation, User principal) {
    //     return EntityModel.of(
    //         new ConversationResponseDto(
    //             conversation.getId(),
    //             conversation.getParticipants().stream()
    //                     .map(participant -> toParticipantResponse(conversation, participant, principal))
    //                     .collect(Collectors.toList())))
    //         .add(linkTo(methodOn(MessageController.class).getConversation(conversation.getId(), principal)).withSelfRel())
    //         .add(linkTo(methodOn(MessageController.class).getParticipants(conversation.getId(), principal)).withRel("participants"));
    // }

    // private EntityModel<ParticipantResponseDto> toParticipantResponse(Conversation conversation, Participant participant, User principal) {
    //     return EntityModel.of(
    //         new ParticipantResponseDto(
    //             participant.getUserId(),
    //             participant.getUserName(),
    //             participant.getRole().toString(),
    //             participant.getStatus().toString()))
    //         .add(linkTo(methodOn(MessageController.class).getParticipant(conversation.getId(), participant.getUserId(), principal)).withSelfRel());
    // }
}