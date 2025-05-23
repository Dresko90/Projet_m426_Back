package ch.epai.ict.m295.messaging.backend.api.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.epai.ict.m295.messaging.backend.api.dto.ConversationsResponseDto;
import ch.epai.ict.m295.messaging.backend.api.dto.MessageCreateDto;
import ch.epai.ict.m295.messaging.backend.api.dto.MessageResponseDto;
import ch.epai.ict.m295.messaging.backend.api.dto.MessageStatusDto;
import ch.epai.ict.m295.messaging.backend.api.dto.MessageUpdateDto;
import ch.epai.ict.m295.messaging.backend.api.dto.MessagesResponseDto;
import ch.epai.ict.m295.messaging.backend.api.dto.ParticipantResponseDto;
import ch.epai.ict.m295.messaging.backend.domain.Conversation;
import ch.epai.ict.m295.messaging.backend.domain.ConversationRepository;
import ch.epai.ict.m295.messaging.backend.domain.Message;
import ch.epai.ict.m295.messaging.backend.domain.MessageBuilder;
import ch.epai.ict.m295.messaging.backend.domain.MessageStatus;
import ch.epai.ict.m295.messaging.backend.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "message")
@RestController
@RequestMapping("/conversation/{conversationId}")
public class MessageController {

    private final ConversationRepository conversationRepository;

    public MessageController(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }


    @Operation(
        operationId = "get-messages", 
        summary = "Récupère les messages d'une conversation",
        description = "Récupère les messages d'une conversation donnée. L'utilisateur·rice connecté·e doit être membre de la conversation."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste de messages récupérée avec succès.", 
            content = @Content(
                schema = @Schema(implementation = ConversationsResponseDto.class),
                examples = 
                    @ExampleObject(
                        value = """
                            {
                                "_embedded": {
                                    "messages": [
                                        {
                                            "id": 100,
                                            "senderId": 11,
                                            "body": "Hi Idaho!",
                                            "participantStatus": [
                                                {
                                                    "userId": 11,
                                                    "readAt": null,
                                                    "deleted": false
                                                },
                                                {
                                                    "userId": 12,
                                                    "readAt": null,
                                                    "deleted": false
                                                }
                                            ],
                                            "sentAt": "2025-05-07T13:48:31",
                                            "_links": {
                                                "self": {
                                                    "href": "/api/v1/conversation/100/messages/100"
                                                }
                                            }
                                        },
                                        {
                                            "id": 101,
                                            "senderId": 12,
                                            "body": "Hey Sheana, good to hear from you.",
                                            "participantStatus": [
                                                {
                                                    "userId": 11,
                                                    "readAt": null,
                                                    "deleted": false
                                                },
                                                {
                                                    "userId": 12,
                                                    "readAt": null,
                                                    "deleted": false
                                                }
                                            ],
                                            "sentAt": "2025-05-07T13:48:31",
                                            "_links": {
                                                "self": {
                                                    "href": "/api/v1/conversation/100/messages/101"
                                                }
                                            }
                                        }
                                    ]
                                },
                                "_links": {
                                    "self": {
                                        "href": "/api/v1/conversation/100/messages?page=0&size=20"
                                    },
                                    "first": {
                                        "href": "/api/v1/conversation/100/messages?page=0&size=20"
                                    },
                                    "last": {
                                        "href": "/api/v1/conversation/100/messages?page=0&size=20"
                                    }
                                },
                                "page": {
                                    "size": 20,
                                    "totalElements": 2,
                                    "totalPages": 1,
                                    "number": 0
                                }
                            }
                            """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "404", description = "Conversation non trouvée", content = @Content),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })    
    @GetMapping(path = "/messages", produces = "application/hal+json")
    public MessagesResponseDto getMessages(
            @Parameter(
                description = "Identifiant de la conversation", 
                example = "100")   
            @PathVariable long conversationId,
            @RequestParam(name = "page", defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "20") int pageSize,
            @RequestAttribute User principal) {
        
        Conversation conversation = conversationRepository.getConversationById(conversationId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation non trouvée");
        }
        if (!conversation.isParticipant(principal.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "L'utilisateur n'est pas membre de la conversation");
        }

        List<Message> messages = conversationRepository.getMessages(conversationId, principal.getId(), pageNumber, pageSize);
        long totalElements = conversationRepository.getNumberOfMessagesForConversation(conversationId);
        MessagesResponseDto response = new MessagesResponseDto(
                messages.stream()
                    .map(message -> toMessageResponse(message))
                    .collect(Collectors.toList()),
                new PageMetadata(pageSize, pageNumber, totalElements),
                linkTo(methodOn(MessageController.class).getMessages(conversationId, pageNumber, pageSize, principal)).withSelfRel());

        if (pageNumber > 0) {
            response.add(linkTo(methodOn(MessageController.class).getMessages(conversationId, pageNumber - 1, pageSize, principal)).withRel("previous"));
        }
        if (pageNumber < (totalElements / pageSize)) {
            response.add(linkTo(methodOn(MessageController.class).getMessages(conversationId, pageNumber + 1, pageSize, principal)).withRel("next"));
        }
        if (totalElements > 0) {
            response.add(linkTo(methodOn(MessageController.class).getMessages(conversationId, 0, pageSize, principal)).withRel("first"));
            response.add(linkTo(methodOn(MessageController.class).getMessages(conversationId, (int) (totalElements / pageSize), pageSize, principal)).withRel("last"));
        }
    
        return response;
    }


    @Operation(
        operationId = "create-message",
        summary = "Ajoute un message à la conversation",
        description = "Ajoute un message à la conversation à partir de son identifiant."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Message créé avec succès.", 
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
        @ApiResponse(responseCode = "404", description = "Conversation non trouvée", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping(path = "/messages", produces = "application/hal+json")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponseDto createMessages(
            @Parameter(
                description = "Identifiant de la conversation",
                example = "100"
            )
            @PathVariable long conversationId, 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            value = """
                            {
                                "message": "Salut! Quoi de neuf?"
                            }
                            """),

                        }))
            @RequestBody MessageCreateDto messageCreateDto, 
            @RequestAttribute User principal) {

        Conversation conversation = conversationRepository.getConversationById(conversationId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation non trouvée");
        }
        Message message = MessageBuilder.create()
            .setConversation(conversationRepository.getConversationById(conversationId))
            .setSenderId(principal.getId())
            .setBody(messageCreateDto.message())
            .build();
        conversationRepository.createMessage(message);
        return toMessageResponse(message);
    }

    @Operation(
        operationId = "update-messages-status", 
        summary = "Modifie le statut du message pour un participant", 
        description = 
            """
            Modifie le statut d’un message pour l’utilisateur·rice connecté·e.

            * Pour marquer le message comme lu, le champ `read` est mis à true. Si le message a déjà été lu, l'opération n'a pas d'effet.

            * Pour marquer le message comme supprimé, le champ `deleted` est mis à `true`. Cette opération est irréversible et entraîne la suppression logique du message pour cette personne.

            Il n'est pas permis de marquer un message à la fois comme lu et supprimé. Si les deux champs sont renseignés, une erreur 400 est renvoyée.

            Il est en revanche obligatoire de renseigner au moins un des deux champs. Si aucun d'eux n'est renseigné, une erreur 400 est renvoyée.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statut·du message modifié avec succès.", 
            content = @Content(
                schema = @Schema(implementation = ParticipantResponseDto.class),
                examples = 
                    @ExampleObject(
                        value = """
                            {
                                "id": 100,
                                "senderId": 11,
                                "body": "Hi Idaho!",
                                "participantStatus": [
                                    {
                                        "userId": 11,
                                        "readAt": "2025-05-07T20:55:17",
                                        "deleted": false
                                    },
                                    {
                                        "userId": 12,
                                        "readAt": null,
                                        "deleted": false
                                    }
                                ],
                                "sentAt": "2025-05-07T20:08:12",
                                "_links": {
                                    "self": {
                                        "href": "/api/v1/conversation/100/messages/100"
                                    }
                                }
                            }
                            """))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "404", description = "Conversation ou message non trouvé", content = @Content),
        @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PatchMapping(path = "/messages/{messageId}", produces = "application/hal+json")
    public MessageResponseDto updateMessageStatus(
            @Parameter(
                description = "Identifiant de la conversation",
                example = "100"
            )
            @PathVariable long conversationId, 
            @Parameter(
                description = "Identifiant du message",
                example = "100"
            )
            @PathVariable long messageId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "Marquer le message comme lu.",
                            value = """
                            {
                                "read": true
                            }
                            """),
                        @ExampleObject(
                            name = "Marquer le message comme supprimer.",
                            value = """
                            {
                                "delete": true
                            }
                            """),
                        }))
            @RequestBody MessageUpdateDto messageUpdateDto,
            @RequestAttribute User principal) {

        Conversation conversation = conversationRepository.getConversationById(conversationId);
        if (conversation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation non trouvée");
        }
        Message message = conversationRepository.getMessageById(messageId);
        if (message == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message non trouvé");
        }
        if (messageUpdateDto.read() != null && messageUpdateDto.delete() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Impossible de modifier le statut du message à la fois comme lu et supprimé");
        }
        if (messageUpdateDto.read() == null && messageUpdateDto.delete() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un des champs `read` ou `delete` doit être renseigné");  
        }

        if (messageUpdateDto.read() != null && messageUpdateDto.read()) {
            conversationRepository.markMessageAsReadForUser(messageId, principal.getId());
        }
        if (messageUpdateDto.delete() != null && messageUpdateDto.delete()) {
            conversationRepository.markMessageAsDeletedForUser(messageId, principal.getId());
        }
        return toMessageResponse(conversationRepository.getMessageById(messageId));
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
            .add(linkTo(methodOn(MessageController.class).updateMessageStatus(message.getConversationId(), message.getId(), null, null)).withSelfRel());

    }

    private MessageStatusDto toMessageStatusDto(MessageStatus messageStatus) {
        return new MessageStatusDto(
            messageStatus.getUserId(),
            messageStatus.getReadAt(),
            messageStatus.isDeleted());
    }
}