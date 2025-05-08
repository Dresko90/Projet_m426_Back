package ch.epai.ict.m295.messaging.backend.api.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantResponseDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantUpdateDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantsAddDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantsResponseDto;
import ch.epai.ict.m295.messaging.backend.domain.Conversation;
import ch.epai.ict.m295.messaging.backend.domain.ConversationRepository;
import ch.epai.ict.m295.messaging.backend.domain.Participant;
import ch.epai.ict.m295.messaging.backend.domain.ParticipantBuilder;
import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@Tag(name = "participant")
@RequestMapping("/conversation/{conversationId}")
public class ParticipantController {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ParticipantController(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    @Operation(
        operationId = "add-participant", 
        summary = "Ajoute un·e participant·e à une conversation.", 
        description = """
            Ajoute des participant·e·s à une conversation de goupe à partir de leur identifiant, de leur adresse email ou de leur numéro de téléphone au format E.164. Les participant·e·s déjà présent·e·s dans la conversation sont ignoré·e·s.
            
            Il n'est pas possible d'ajouter un·e participant·e à une conversation privée.

            Pour effectuer cette opération, il faut être propriétaire de la conversation.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Participant·e ajouté·e avec succès", 
            content = @Content(
                schema = @Schema(implementation = ParticipantResponseDto.class),
                examples = 
                    @ExampleObject(
                        value = """
                            {
                                "_embedded": {
                                    "participants": [
                                        {
                                            "userId": 13,
                                            "username": "siona@example.com",
                                            "role": "MEMBER",
                                            "status": "ACTIVE",
                                            "_links": {
                                                "self": {
                                                    "href": "/api/v1/conversations/1003/participants/13"
                                                }
                                            }
                                        },
                                        {
                                            "userId": 15,
                                            "username": "fallom@example.com",
                                            "role": "MEMBER",
                                            "status": "ACTIVE",
                                            "_links": {
                                                "self": {
                                                    "href": "/api/v1/conversations/1003/participants/15"
                                                }
                                            }
                                        },
                                        {
                                            "userId": 11,
                                            "username": "sheana@example.com",
                                            "role": "OWNER",
                                            "status": "ACTIVE",
                                            "_links": {
                                                "self": {
                                                    "href": "/api/v1/conversations/1003/participants/11"
                                                }
                                            }
                                        },
                                        {
                                            "userId": 14,
                                            "username": "trevize@example.com",
                                            "role": "MEMBER",
                                            "status": "ACTIVE",
                                            "_links": {
                                                "self": {
                                                    "href": "/api/v1/conversations/1003/participants/14"
                                                }
                                            }
                                        },
                                        {
                                            "userId": 16,
                                            "username": "daneel@example.com",
                                            "role": "MEMBER",
                                            "status": "ACTIVE",
                                            "_links": {
                                                "self": {
                                                    "href": "/api/v1/conversations/1003/participants/16"
                                                }
                                            }
                                        }
                                    ]
                                },
                                "_links": {
                                    "self": {
                                        "href": "/api/v1/conversations/1003/participants"
                                    },
                                    "conversation": {
                                        "href": "/api/v1/conversations/1003"
                                    }
                                }
                            }

                            """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
            @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping(path = "/participants", produces = "application/hal+json")
    public ParticipantsResponseDto addParticipants(
            @Parameter(
                description = "Identifiant de la conversation",
                example = "104"
            )
            @PathVariable long conversationId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Utilisateur·rice à ajouter",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples =
                        @ExampleObject(
                            value = """
                            {
                                "participants": [
                                    {
                                        "username": "daneel@example.com",
                                        "role": "OWNER"
                                    },
                                    {
                                        "userId": 12
                                    }
                                ]
                            }
                            """)))
            @RequestBody ParticipantsAddDto participantAddDto,
            @RequestAttribute User principal) {

        Conversation conversation = conversationRepository.getConversationById(conversationId);
        if (!conversation.isOwner(principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seul·e un·e propriétaire peut ajouter des participant·e·s à une conversation de groupe.");
        }


        List<Participant> participants = new ArrayList<>();
        for (ParticipantDto participantDto : participantAddDto.getParticipants()) {
            User user = findUserByParticipant(participantDto);
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur·rice introuvable");
            }
            if (!conversation.isParticipant(user.getId())) {
                    participants.add(
                        ParticipantBuilder.create()  
                            .setId(user.getId())
                            .setUsername(user.getUsername())
                            .setRole(
                                Participant.Role.valueOf(
                                    Objects.requireNonNullElse(
                                        participantDto.getRole(),
                                        ParticipantDto.Role.MEMBER).toString()))
                            .setStatus(      
                                Participant.Status.valueOf(                       
                                    Objects.requireNonNullElse(
                                        participantDto.getStatus(),
                                        ParticipantDto.Status.ACTIVE).toString()))
                            .build());
            }
        }

        conversationRepository.addParticipants(conversation.getId(), participants);

        Conversation updatedConversation = this.conversationRepository.getConversationById(conversationId);
        return new ParticipantsResponseDto(
            updatedConversation.getParticipants().stream()
                        .map(participant -> toParticipantResponse(conversation, participant))
                        .collect(Collectors.toList()),
                linkTo(methodOn(ParticipantController.class).addParticipants(conversationId, null, principal)).withSelfRel(),
                linkTo(methodOn(ConversationController.class).getConversation(conversationId, null)).withSelfRel());
    }

    @Operation(
        operationId = "update-participant", 
        summary = "Modifie le rôle ou le statut d'un·e participant·e.", 
        description =
            """
            Modifie le rôle ou le statut d'un·e participant·e à partir de son identifiant.

            * Si la conversation est une conversation de groupe, le ou la propriétaire de la conversation peut bloquer un·e participant·e non propriétaire ou lui attribuer le rôle de propriétaire.

            * Si la conversation est une conversation privée, un·e participant·e peut changer son propre status de "ACTIVE" à "INACTIVE". Cette opération est irréversible.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rôle ou statut de lea participant·e modifié avec succès.", 
            content = @Content(
                schema = @Schema(implementation = ParticipantResponseDto.class),
                examples = 
                    @ExampleObject(
                        value = """
                            {
                                "userId": 15,
                                "username": "fallom@example.com",
                                "role": "MEMBER",
                                "status": "INACTIVE",
                                "_links": {
                                    "self": {
                                        "href": "/api/v1/conversation/110/participants/15"
                                    }
                                }
                            }
                            """))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
            @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PatchMapping(path = "/participants/{participantId}", produces = "application/hal+json")
    public ParticipantResponseDto updateParticipant(
            @Parameter(
                description = "Identifiant de la conversation",
                example = "100"
            )
            @PathVariable long conversationId,
            @Parameter(
                description = "Identifiant de la conversation",
                example = "11"
            )
            @PathVariable long participantId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Données de lea participant·e à modifier",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "Bloquer un utilisateur",
                            value = """
                            {
                                "status": "BLOCKED"
                            }
                            """),
                         @ExampleObject(
                            name = "Modifier le nom d'utilisateur·rice",
                            value = """
                            {
                                "username": "+41799999123"
                            }
                            """),
                        @ExampleObject(
                            name = "Modifier le nom affichable",
                            value = """
                            {
                                "displayName": "Sheana"
                            }
                            """)
                        }))

            @RequestBody ParticipantUpdateDto updateParticipantDto,
            @RequestAttribute User principal) {

        Conversation conversation = conversationRepository.getConversationById(conversationId);

        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        if (!conversation.isParticipant(participantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found");
        }

        if (principal.getId() == participantId) {
            if (updateParticipantDto.status() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required.");
            }
            if (updateParticipantDto.status() != ParticipantDto.Status.INACTIVE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "For own status, the only accepted value is INACTIVE.");
            }
            if (updateParticipantDto.role() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't change own role.");
            }

            Participant participant = conversation.getParticipant(participantId);
            if (!updateParticipantDto.hasSameStatus(participant)) {
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role and status cannot be changed simultaneously.");
            }
            if (updateParticipantDto.status() != null) {
                if (!(updateParticipantDto.isActive() || updateParticipantDto.isBlocked())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only ACTIVE and BLOCKED status are allowed");
                }
            }

            Participant participant = conversation.getParticipant(participantId);
            if (updateParticipantDto.role() != null) {
                if (!updateParticipantDto.hasSameRole(participant)) {
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
                if (!updateParticipantDto.hasSameStatus(participant)) {
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

    private ParticipantResponseDto toParticipantResponse(
            Conversation conversation,
            Participant participant) {

        return new ParticipantResponseDto(
                participant.getUserId(),
                participant.getUserName(),
                participant.getRole(),
                participant.getStatus(),
                linkTo(methodOn(ParticipantController.class).updateParticipant(conversation.getId(), participant.getUserId(), null, null)).withSelfRel());
    }

    private User findUserByParticipant(ParticipantDto participantDto) {
        User user = null;
        if (participantDto.getUserId() != null) {
            user = userRepository.getUserById(participantDto.getUserId());
        } else {
            user = userRepository.getUserByUsername(participantDto.getUsername());
        }
        return user;
    }
}
