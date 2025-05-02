package ch.epai.ict.m295.messaging.backend.api.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
import ch.epai.ict.m295.messaging.backend.api.dto.UpdateParticipantDto;
import ch.epai.ict.m295.messaging.backend.domain.Conversation;
import ch.epai.ict.m295.messaging.backend.domain.ConversationBuilder;
import ch.epai.ict.m295.messaging.backend.domain.ConversationRepository;
import ch.epai.ict.m295.messaging.backend.domain.Participant;
import ch.epai.ict.m295.messaging.backend.domain.ParticipantBuilder;
import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
public class ConversationController {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationController(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @Operation(
        operationId = "get-conversations",
        summary = "Récupère les conversations de l'utilisateur·rice.",
        description = "Récupère les conversations dans lesquelles l'utilisateur·rice connecté·e est actif·ve.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    @GetMapping("/conversations")
    public CollectionModel<EntityModel<ConversationResponseDto>> getConversations(@RequestAttribute User principal) {
        List<Conversation> conversations = conversationRepository.findConversationsByUser(principal);

        return CollectionModel.of(
                conversations.stream()
                        .map(conversation -> toConversationResponse(conversation))
                        .collect(Collectors.toList()),
                linkTo(methodOn(ConversationController.class).getConversations(principal)).withSelfRel());
    }

    @Operation(
        operationId = "create-conversation",
        summary = "Crée une nouvelle conversation.",
        description = "Crée une nouvelle conversation avec un ou plusieurs participant·e·s. Le nom d'utilisateur peut être soit une adresse e-mail, soit un numéro de téléphone au format E.164.")
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

    @Operation(
        operationId = "get-conversation",
        summary = "Récupère les détails d'une conversation.",
        description = "Récupère les détails d'une conversation à partir de son identifiant.")
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

    @Operation(
        operationId = "get-participants",
        summary = "Récupère les participant·e·s d'une conversation.",
        description = "Récupère la liste des participant·e·s d'une conversation à partir de son identifiant.")
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
                        .updateParticipant(conversation.getId(), participant.getUserId(), null, null))
                        .withSelfRel());
    }

    @PatchMapping("/conversations/{conversationId}/participants/{participantId}")
    public EntityModel<ParticipantResponseDto> updateParticipant(
            @PathVariable long conversationId,
            @PathVariable long participantId,
            @RequestBody UpdateParticipantDto updateParticipantDto,
            @RequestAttribute User principal) {

        Conversation conversation = conversationRepository.getConversation(conversationId);

        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        if (!conversation.isParticipant(participantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found");
        }

        if (principal.getId() == participantId) {
            if (updateParticipantDto.status() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
            }
            if (updateParticipantDto.role() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role cannot be updated");
            }

            Participant participant = toParticipant(principal.getId(), updateParticipantDto);
            if (isSame(updateParticipantDto.status(), participant.getStatus())) {
                participant = ParticipantBuilder.create()
                    .setId(participant.getUserId())
                    .setUsername(participant.getUserName())
                    .setRole(participant.getRole())
                    .setStatus(Participant.Status.valueOf(updateParticipantDto.status().toString()))
                    .build();
                    conversationRepository.updateParticipant(conversationId, participant);
            }
            return toParticipantResponse(conversation, participant);
        }

        if (conversation.isOwner(principal)) {
            if (updateParticipantDto.role() != null && updateParticipantDto.status() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role and status cannot be updated at the same time");
            }
 
            Participant participant = toParticipant(participantId, updateParticipantDto);
            if (updateParticipantDto.role() != null) {
                if (!isSame(updateParticipantDto.role(), participant.getRole())) {
                    participant = ParticipantBuilder.create()
                        .setId(participant.getUserId())
                        .setUsername(participant.getUserName())
                        .setRole(Participant.Role.valueOf(updateParticipantDto.role().toString()))
                        .setStatus(participant.getStatus())
                        .build();
                    conversationRepository.updateParticipant(conversationId, participant);
                }
                return toParticipantResponse(conversation, participant);
            }
            if (updateParticipantDto.status() != null) {
                if (!(updateParticipantDto.isActive() || updateParticipantDto.isBlocked())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only ACTIVE and BLOCKED status are allowed");
                }
                if (!isSame(updateParticipantDto.status(), participant.getStatus())) {
                    participant = ParticipantBuilder.create()
                        .setId(participant.getUserId())
                        .setUsername(participant.getUserName())
                        .setRole(participant.getRole())
                        .setStatus(Participant.Status.valueOf(updateParticipantDto.status().toString()))
                        .build();
                    conversationRepository.updateParticipant(conversationId, participant);
                }
                return toParticipantResponse(conversation, participant);
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
    }
    
    private boolean isSame(ParticipantDto.Role role1, Participant.Role role2) {
        switch (role1) {
            case OWNER:
                return role2 == Participant.Role.OWNER;
            case MEMBER:
                return role2 == Participant.Role.MEMBER;
            default:
                throw new IllegalArgumentException("Unknown role: " + role1);
        }
    }

    private boolean isSame(ParticipantDto.Status status1, Participant.Status status2) {
        switch (status1) {
            case ACTIVE:
                return status2 == Participant.Status.ACTIVE;
            case INACTIVE:
                return status2 == Participant.Status.INACTIVE;
            case BLOCKED:
                return status2 == Participant.Status.BLOCKED;
            case INVITED:
                return status2 == Participant.Status.INVITED;
            default:
                throw new IllegalArgumentException("Unknown status: " + status1);
        }
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

    private Participant toParticipant(long userId, UpdateParticipantDto updateParticipantDto) {
        User user = userRepository.getUser(userId);
        return ParticipantBuilder.create()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setRole(Participant.Role.valueOf(updateParticipantDto.role().toString()))
                .setStatus(Participant.Status.valueOf(updateParticipantDto.status().toString()))
                .build();
    }
}