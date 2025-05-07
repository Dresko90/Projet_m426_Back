package ch.epai.ict.m295.messaging.backend.api.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantResponseDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantUpdateDto;
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

    public ParticipantController(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
    }

    @Operation(
        operationId = "get-participants", 
        summary = "Récupèrer la liste des participant·e·s d'une conversation.", 
        description = "Récupère la liste des participant·e·s d'une conversation à partir de son identifiant."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Liste de participants récupérée avec succès.", 
            content = @Content(
                schema = @Schema(implementation = ParticipantResponseDto.class),
                examples = 
                    @ExampleObject(
                        value = """
                            {
                                "_embedded": {
                                    "participants": [
                                        {
                                            "userId": 11,
                                            "username": "sheana@example.com",
                                            "role": "MEMBER",
                                            "status": "ACTIVE",
                                            "_links": {
                                                "self": {
                                                    "href": "http://localhost:8080/api/v1/conversation/100/participants/11"
                                                }
                                            }
                                        },
                                        {
                                            "userId": 12,
                                            "username": "idaho@example.com",
                                            "role": "MEMBER",
                                            "status": "ACTIVE",
                                            "_links": {
                                                "self": {
                                                    "href": "http://localhost:8080/api/v1/conversation/100/participants/12"
                                                }
                                            }
                                        }
                                    ]
                                },
                                "_links": {
                                    "self": {
                                        "href": "http://localhost:8080/api/v1/conversation/100/participants"
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
    @GetMapping(path = "/participants", produces = "application/hal+json")
    public CollectionModel<ParticipantResponseDto> getParticipants(
            @Parameter(
                description = "Identifiant de la conversation",
                example = "110"
            )
            @PathVariable long conversationId,
            @RequestAttribute User principal) {

        Conversation conversation = conversationRepository.getConversationById(conversationId);
        if (!conversation.isParticipant(principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this conversation");
        }
        return CollectionModel.of(
                conversation.getParticipants().stream()
                        .map(participant -> toParticipantResponse(conversation, participant))
                        .collect(Collectors.toList()),
                linkTo(methodOn(ParticipantController.class).getParticipants(conversationId, principal))
                        .withSelfRel());
    }

    @Operation(
        operationId = "update-participant", 
        summary = "Modifie le rôle ou le statut d'un·e participant·e.", 
        description =
            """
            Modifie le rôle ou le statut d'un·e participant·e à partir de son identifiant.

            * Si la conversation est une conversation de groupe, le ou la propriétaire de la conversation peut bloquer un·e participant·e non propriétaire ou lui attribuer le rôle de propriétaire.

            * Si la conversation est une conversation privée, un·e participant·e peut changer son propre status d'actif à inactif. Cette opération est irréversible.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rôle ou statut de lea participant·modifié avec succès.", 
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
                                        "href": "http://localhost:8080/api/v1/conversation/110/participants/15"
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
                description = "Utilisateur·rice à créer",
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
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Status is required.");
            }
            if (updateParticipantDto.status() != ParticipantDto.Status.INACTIVE) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "For own status, the only accepted value is INACTIVE.");
            }
            if (updateParticipantDto.role() != null) {
                throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "Can't change own role.");
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
                participant.getRole().toString(),
                participant.getStatus().toString(),
                linkTo(methodOn(ParticipantController.class).updateParticipant(conversation.getId(), participant.getUserId(), null, null)).withSelfRel());
    }
}
