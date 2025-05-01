package ch.epai.ict.m295.messaging.backend.api.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.epai.ict.m295.messaging.backend.api.dto.ConversationResponseDto;
import ch.epai.ict.m295.messaging.backend.api.dto.CreateConversationDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantResponseDto;
import ch.epai.ict.m295.messaging.backend.domain.Conversation;
import ch.epai.ict.m295.messaging.backend.domain.ConversationBuilder;
import ch.epai.ict.m295.messaging.backend.domain.ConversationRepository;
import ch.epai.ict.m295.messaging.backend.domain.Participant;
import ch.epai.ict.m295.messaging.backend.domain.ParticipantBuilder;
import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserRepository;

@RestController
public class ConversationController {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationController(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/conversations")
    public CollectionModel<EntityModel<ConversationResponseDto>> getConversations(@RequestAttribute User principal) {
        List<Conversation> conversations = conversationRepository.findConversationsByUser(principal);

        return CollectionModel.of(
                conversations.stream()
                        .map(conversation -> toConversationResponse(conversation))
                        .collect(Collectors.toList()),
                linkTo(methodOn(ConversationController.class).getConversations(principal)).withSelfRel());
    }

    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<ConversationResponseDto> createConversation(
            @RequestBody CreateConversationDto createConversationDto, 
            @RequestAttribute User principal) {

        List<ParticipantDto> participantDtoList = getParticipantDtoList(createConversationDto, principal);
        
        if (!createConversationDto.isGroup()) {
            if (participantDtoList.size() != 2) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Two participants are required for a one-on-one conversation");
            }
            
            List<Conversation> existingConversation = conversationRepository.findConversationsByParticipants(
                    toParticipant(participantDtoList.get(0)),
                    toParticipant(participantDtoList.get(1)));
            if (!existingConversation.isEmpty()) {
                return toConversationResponse(existingConversation.get(0));
            }
        }

        Conversation conversation = ConversationBuilder.create()
                .setParticipants(
                        participantDtoList.stream()
                                .map(participantDto -> toParticipant(participantDto))
                                .collect(Collectors.toList()))
                .build();
        conversationRepository.createConversation(conversation);
        return toConversationResponse(conversation);
    }

    @GetMapping("/conversations/{conversationId}")
    public EntityModel<ConversationResponseDto> getConversation(
            @PathVariable long conversationId,
            @RequestAttribute User principal) {

        Conversation conversation = conversationRepository.getConversation(conversationId);
        if (conversation.isParticipant(principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation");
        }
        return toConversationResponse(conversation);
    }

    @GetMapping("/conversations/{conversationId}/participants")
    public CollectionModel<EntityModel<ParticipantResponseDto>> getParticipants(
            @PathVariable long conversationId,
            @RequestAttribute User principal) {

        Conversation conversation = conversationRepository.getConversation(conversationId);
        if (conversation.isParticipant(principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation");
        }
        return CollectionModel.of(
                conversation.getParticipants().stream()
                        .map(participant -> toParticipantResponse(conversation, participant))
                        .collect(Collectors.toList()),
                linkTo(methodOn(ConversationController.class).getParticipants(conversationId, principal))
                        .withSelfRel());
    }

    @GetMapping("/conversations/{conversationId}/participants/{participantId}")
    public EntityModel<ParticipantResponseDto> getParticipant(
            @PathVariable long conversationId,
            @PathVariable long participantId,
            @RequestAttribute User principal) {

        Conversation conversation = conversationRepository.getConversation(conversationId);
        if (conversation.isParticipant(principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation");
        }

        return toParticipantResponse(conversation, conversation.getParticipant(participantId));
    }

    private EntityModel<ConversationResponseDto> toConversationResponse(Conversation conversation) {
        return EntityModel.of(
                new ConversationResponseDto(
                        conversation.getId(),
                        conversation.isGroup(),
                        conversation.getParticipants().stream()
                                .map(participant -> toParticipantResponse(conversation, participant))
                                .collect(Collectors.toList())))
                .add(linkTo(methodOn(ConversationController.class).getConversation(conversation.getId(), null))
                        .withSelfRel())
                .add(linkTo(methodOn(ConversationController.class).getParticipants(conversation.getId(), null))
                        .withRel("participants"));
    }

    private EntityModel<ParticipantResponseDto> toParticipantResponse(
            Conversation conversation,
            Participant participant) {

        return EntityModel.of(
                new ParticipantResponseDto(
                        participant.getUserId(),
                        participant.getUserName(),
                        participant.getRole().toString(),
                        participant.getStatus().toString()))
                .add(linkTo(methodOn(ConversationController.class)
                        .getParticipant(conversation.getId(), participant.getUserId(), null))
                        .withSelfRel());
    }

    private List<ParticipantDto> getParticipantDtoList(CreateConversationDto createConversationDto, User principal) {
        List<ParticipantDto> participantDtolist = null;
        if (createConversationDto.isGroup()) {
            participantDtolist = createConversationDto.getParticipants();
            participantDtolist.add(new ParticipantDto(
                    principal.getId(),
                    principal.getUsername(),
                    ParticipantDto.Role.OWNER,
                    ParticipantDto.Status.ACTIVE));
        } else {
            participantDtolist = List.of(
                    new ParticipantDto(
                            createConversationDto.getUserId(),
                            createConversationDto.getUsername(),
                            ParticipantDto.Role.MEMBER,
                            ParticipantDto.Status.ACTIVE),
                    new ParticipantDto(
                            principal.getId(),
                            principal.getUsername(),
                            ParticipantDto.Role.MEMBER,
                            ParticipantDto.Status.ACTIVE));
        }
        return participantDtolist;
    }

    private User findUser(ParticipantDto participantDto) {
        User user = null;
        if (participantDto.getUserId() != null) {
            user = userRepository.getUser(participantDto.getUserId());
        } else {
            user = userRepository.getUserByUsername(participantDto.getUsername());
        }
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID or username is required");
        }
        return user;
    }

    private Participant toParticipant(ParticipantDto participantDto) {
        User user = findUser(participantDto);
        return ParticipantBuilder.create()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setRole(Participant.Role.valueOf(participantDto.getRole()))
                .setStatus(Participant.Status.valueOf(participantDto.getStatus()))
                .build();
    }
}