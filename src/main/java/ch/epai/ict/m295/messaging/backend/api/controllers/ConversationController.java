package ch.epai.ict.m295.messaging.backend.api.controllers;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import ch.epai.ict.m295.messaging.backend.api.dto.*;
import ch.epai.ict.m295.messaging.backend.domain.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationController(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    // GET /conversations
    @GetMapping
    public CollectionModel<EntityModel<ConversationResponseDto>> getConversations(@RequestAttribute User principal) {
        List<Conversation> conversations = conversationRepository.findConversationsByUser(principal);

        return CollectionModel.of(
                conversations.stream()
                    .map(conversation -> toConversationResponse(conversation, principal))
                    .collect(Collectors.toList()),
                linkTo(methodOn(ConversationController.class).getConversations(principal)).withSelfRel());
    }

    // POST /conversations
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<ConversationResponseDto> createConversation(
            @RequestBody CreateConversationDto createConversationDto, 
            @RequestAttribute User principal) {

        ConversationBuilder builder = ConversationBuilder.create();
        builder.addParticipant(
            new Participant(
                principal.getId(), 
                principal.getUsername(), 
                Participant.Role.OWNER, 
                Participant.Status.ACTIVE));

        for (Long participantId : createConversationDto.getParticipants()) {
            User participant = userRepository.getUser(participantId);
            builder.addParticipant(
                new Participant(
                    participant.getId(), 
                    participant.getUsername(), 
                    Participant.Role.MEMBER, 
                    Participant.Status.INVITED));
        }

        Conversation conversation = builder.build();
        conversationRepository.createConversation(conversation);

        return toConversationResponse(conversation, principal);
    }

    // GET /conversations/{conversationId}
    @GetMapping("/{conversationId}")
    public EntityModel<ConversationResponseDto> getConversation(@PathVariable long conversationId, @RequestAttribute User principal) {
        Conversation conversation = conversationRepository.getConversation(conversationId);

        if (conversation.getParticipants().stream().noneMatch(p -> p.getUserId() == principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation");
        }

        return toConversationResponse(conversation, principal);
    }

    // GET /conversations/{conversationId}/participants
    @GetMapping("/{conversationId}/participants")
    public CollectionModel<EntityModel<ParticipantResponseDto>> getParticipants(@PathVariable long conversationId, @RequestAttribute User principal) {
        Conversation conversation = conversationRepository.getConversation(conversationId);

        if (conversation.getParticipants().stream().noneMatch(p -> p.getUserId() == principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation");
        }

        List<EntityModel<ParticipantResponseDto>> participantModels = conversation.getParticipants().stream()
                .map(participant -> toParticipantResponse(conversation, participant, principal))
                .collect(Collectors.toList());

        return CollectionModel.of(participantModels,
                linkTo(methodOn(ConversationController.class).getParticipants(conversationId, principal)).withSelfRel());
    }

    // GET /conversations/{conversationId}/participants/{participantId}
    @GetMapping("/{conversationId}/participants/{participantId}")
    public EntityModel<ParticipantResponseDto> getParticipant(@PathVariable long conversationId, @PathVariable long participantId, @RequestAttribute User principal) {
        Conversation conversation = conversationRepository.getConversation(conversationId);

        if (conversation.getParticipants().stream().noneMatch(p -> p.getUserId() == principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation");
        }

        Participant participant = conversation.getParticipants().stream()
                .filter(p -> p.getUserId() == participantId)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));

        return toParticipantResponse(conversation, participant, principal);
    }

    private EntityModel<ConversationResponseDto> toConversationResponse(Conversation conversation, User principal) {
        return EntityModel.of(
            new ConversationResponseDto(
                conversation.getId(),
                conversation.getParticipants().stream()
                        .map(participant -> toParticipantResponse(conversation, participant, principal))
                        .collect(Collectors.toList())))
            .add(linkTo(methodOn(ConversationController.class).getConversation(conversation.getId(), principal)).withSelfRel())
            .add(linkTo(methodOn(ConversationController.class).getParticipants(conversation.getId(), principal)).withRel("participants"));
    }

    private EntityModel<ParticipantResponseDto> toParticipantResponse(Conversation conversation, Participant participant, User principal) {
        return EntityModel.of(
            new ParticipantResponseDto(
                participant.getUserId(),
                participant.getUserName(),
                participant.getRole().toString(),
                participant.getStatus().toString()))
            .add(linkTo(methodOn(ConversationController.class).getParticipant(conversation.getId(), participant.getUserId(), principal)).withSelfRel());
    }
}