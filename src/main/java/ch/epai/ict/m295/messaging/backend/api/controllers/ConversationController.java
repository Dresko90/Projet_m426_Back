package ch.epai.ict.m295.messaging.backend.api.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.epai.ict.m295.messaging.backend.api.dto.ConversationResponseDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ConversationsResponseDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ConversationCreateDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantResponseDto;
import ch.epai.ict.m295.messaging.backend.domain.Conversation;
import ch.epai.ict.m295.messaging.backend.domain.ConversationBuilder;
import ch.epai.ict.m295.messaging.backend.domain.ConversationRepository;
import ch.epai.ict.m295.messaging.backend.domain.Participant;
import ch.epai.ict.m295.messaging.backend.domain.ParticipantBuilder;
import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import  io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@Tag(name = "conversation")
public class ConversationController {
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public ConversationController(ConversationRepository conversationRepository, UserRepository userRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }


    @Operation(
        operationId = "get-conversations", 
        summary = "Récupère les conversations de l'utilisateur·rice connecté·e.", 
        description = "Récupère les conversations dans lesquelles l'utilisateur·rice connecté·e est actif·ve. Les conversations qu'iel a quittées ou dans lesquelles iel est bloqué·e n'apparaissent pas dans la liste."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Conversations récupérées avec succès.", 
            content = @Content(
                schema = @Schema(implementation = ConversationsResponseDto.class),
                examples = 
                    @ExampleObject(
                        value = """
                            {
                                "_embedded": {
                                    "conversations": [
                                        {
                                            "id": 100,
                                            "group": false,
                                            "_embedded": {
                                                "participants": [
                                                    {
                                                        "userId": 11,
                                                        "username": "sheana@example.com",
                                                        "role": "MEMBER",
                                                        "status": "ACTIVE",
                                                        "_links": {
                                                            "self": {
                                                                "href": "/api/v1/conversations/100/participants/11"
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
                                                                "href": "/api/v1/conversations/100/participants/12"
                                                            }
                                                        }
                                                    }
                                                ]
                                            },
                                            "_links": {
                                                "self": {
                                                    "href": "/api/v1/conversations/100"
                                                },
                                                "participants": {
                                                    "href": "/api/v1/conversations/100/participants"
                                                }
                                            }
                                        }
                                    ]
                                },
                                "_links": {
                                    "self": {
                                        "href": "/api/v1/conversations?page=0&size=20"
                                    }
                                },
                                "page": {
                                    "size": 20,
                                    "totalElements": 1,
                                    "totalPages": 1,
                                    "number": 0
                                }
                            }
                        """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping(path = "/conversations", produces = "application/hal+json")
    public ConversationsResponseDto getConversations(
            @Parameter(
                description = "Numéro de la page à récupérer (la première page est 0).",
                required = true)
            @RequestParam(name = "page",defaultValue = "0") int pageNumber,
            @Parameter(
                description = "Nombre d'éléments de la page à récupérer (≤ 100).",
                required = true
            )
            @RequestParam(name = "size", defaultValue = "20") int pageSize,
            @RequestAttribute User principal) {
        
        return new ConversationsResponseDto(
            conversationRepository.getConversationsByUser(principal, pageNumber, pageSize ).stream()
                .map(conversation -> toConversationResponse(conversation))
                .collect(Collectors.toList()),
            new PageMetadata(
                    pageSize, 
                    pageNumber, 
                    conversationRepository.getNumberOfConvesationForUser(principal)),
            linkTo(methodOn(ConversationController.class).getConversations(pageNumber, pageSize, principal)).withSelfRel());
    }


    @Operation(
        operationId = "create-conversation", 
        summary = "Crée une nouvelle conversation.", 
        description = """
            Crée une nouvelle conversation pour l'utilisateur·rice connecté·e et une ou plusieurs autre personne.

            Un·e participant·e peut être spécifié·e par son adresse e-mail, son numéro de téléphone au format E.164, ou l'identifiant de son compte (userId). 
            
            * Dans le cas d'une conversation privée, s’il existe déjà une conversation entre les deux interlocuteur·rice·s, elle est retournée. 
            
            * Dans le cas d'une conversation à plusieurs, une nouvelle conversation est créée à chaque appel.
            """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Conversation a été créée avec succès.", 
            content = @Content(
                schema = @Schema(implementation = ConversationResponseDto.class),
                examples = { 
                    @ExampleObject(
                        name = "Conversation privée",
                        value = """
                            {
                                "id": 1003,
                                "group": false,
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
                                            "userId": 11,
                                            "username": "sheana@example.com",
                                            "role": "MEMBER",
                                            "status": "ACTIVE",
                                            "_links": {
                                                "self": {
                                                    "href": "/api/v1/conversations/1003/participants/11"
                                                }
                                            }
                                        }
                                    ]
                                },
                                "_links": {
                                    "self": {
                                        "href": "/api/v1/conversations/1003"
                                    },
                                    "participants": {
                                        "href": "/api/v1/conversations/1003/participants"
                                    }
                                }
                            }
                            """),
                    @ExampleObject(
                        name = "Conversation de groupe",
                        value = """
                            {
                                "id": 1003,
                                "group": true,
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
                                        }
                                    ]
                                },
                                "_links": {
                                    "self": {
                                        "href": "/api/v1/conversations/1003"
                                    },
                                    "participants": {
                                        "href": "/api/v1/conversations/1003/participants"
                                    }
                                }
                            }
                            """)
                })),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping(path = "/conversations", produces = "application/hal+json")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponseDto createConversation(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Données de la conversation à créer",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Nouvelle conversation privée (username)",
                        value = """
                        {
                            "username": "siona@example.com"
                        }
                        """),
                     @ExampleObject(
                        name = "Nouvelle conversation de groupe (tableau de participants)",
                        value = """
                        {
                            "participants": [
                                {
                                    "username": "siona@example.com"
                                },
                                {
                                    "userId": 15
                                }
                            ]
                        }
                        """)
                    }))
            @RequestBody ConversationCreateDto createConversationDto,
            @RequestAttribute User principal) {

        List<ParticipantDto> participantDtoList = createConversationDto.getAllParticipant(principal);

        if (!createConversationDto.isGroup()) {
            List<Conversation> existingConversation = 
                conversationRepository.getConversationsByParticipants(
                    toParticipant(participantDtoList.get(0)),
                    toParticipant(participantDtoList.get(1)));
            if (!existingConversation.isEmpty()) {
                return toConversationResponse(existingConversation.get(0));
            }
        }

        Conversation conversation = ConversationBuilder.create()
                .setIsGroup(createConversationDto.isGroup())
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
        description = "Récupère les détails d'une conversation à partir de son identifiant."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Détails de la conversation récupérée avec succès.", 
            content = @Content(
                schema = @Schema(implementation = ConversationResponseDto.class),
                examples = 
                    @ExampleObject(
                        value = """
                            {
                                "id": 100,
                                "group": false,
                                "_embedded": {
                                    "participants": [
                                        {
                                            "userId": 11,
                                            "username": "sheana@example.com",
                                            "role": "MEMBER",
                                            "status": "ACTIVE",
                                            "_links": {
                                                "self": {
                                                    "href": "/api/v1/conversation/100/participants/11"
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
                                                    "href": "/api/v1/conversation/100/participants/12"
                                                }
                                            }
                                        }
                                    ]
                                },
                                "_links": {
                                    "self": {
                                        "href": "/api/v1/conversations/100"
                                    },
                                    "participants": {
                                        "href": "/api/v1/conversation/100/participants"
                                    }
                                }
                            }
                            """))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conversation non trouvée", content = @Content),
            @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
            @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
            @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping(path = "/conversations/{conversationId}", produces = "application/hal+json")
    public ConversationResponseDto getConversation(
            @Parameter(
                description = "Identifiant de la conversation",
                example = "100"
            )
            @PathVariable long conversationId,
            @RequestAttribute User principal) {

        Conversation conversation = conversationRepository.getConversationById(conversationId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation non trouvée");
        }
        if (!conversation.isParticipant(principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access à cette conversation non autorisé");
        }
        return toConversationResponse(conversation);
    }

    private ConversationResponseDto toConversationResponse(Conversation conversation) {
        return new ConversationResponseDto(
                conversation.getId(),
                conversation.isGroup(),
                conversation.getParticipants().stream()
                        .map(participant -> toParticipantResponse(conversation, participant))
                        .collect(Collectors.toList()),
                linkTo(methodOn(ConversationController.class).getConversation(conversation.getId(), null)).withSelfRel(),
                linkTo(methodOn(ParticipantController.class).addParticipants(conversation.getId(), null, null)).withRel("participants"));
}

    private ParticipantResponseDto toParticipantResponse(Conversation conversation, Participant participant) {
        return new ParticipantResponseDto(
                participant.getUserId(),
                participant.getUserName(),
                participant.getRole(),
                participant.getStatus(),
                linkTo(methodOn(ParticipantController.class).updateParticipant(conversation.getId(), participant.getUserId(), null, null)).withSelfRel());
    }

    private Participant toParticipant(ParticipantDto participantDto) {
        User user = findUserByParticipant(participantDto);
        return ParticipantBuilder.create()
                .setId(user.getId())
                .setUsername(user.getUsername())
                .setRole(Participant.Role.valueOf(participantDto.getRole().toString()))
                .setStatus(Participant.Status.valueOf(participantDto.getStatus().toString()))
                .build();
    }

    private User findUserByParticipant(ParticipantDto participantDto) {
        User user = null;
        if (participantDto.getUserId() != null) {
            user = userRepository.getUserById(participantDto.getUserId());
        } else {
            user = userRepository.getUserByUsername(participantDto.getUsername());
        }
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User ID or username is required");
        }
        return user;
    }
}