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

import ch.epai.ict.m295.messaging.backend.api.dto.ConversationsReponseDto;
import ch.epai.ict.m295.messaging.backend.api.dto.MessageCreateDto;
import ch.epai.ict.m295.messaging.backend.api.dto.MessageResponseDto;
import ch.epai.ict.m295.messaging.backend.api.dto.MessageStatusDto;
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
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste de messages récupérée avec succès.", 
            content = @Content(
                schema = @Schema(implementation = ConversationsReponseDto.class),
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found");
        }
        
        List<Message> messages = conversationRepository.getMessages(conversationId, pageNumber, pageSize);
        long totalElements = conversationRepository.getNumberOfMessagesForConversation(conversationId);
        return new MessagesResponseDto(
                messages.stream()
                    .map(message -> toMessageResponse(message))
                    .collect(Collectors.toList()),
                new PageMetadata(pageSize, pageNumber, totalElements),
                linkTo(methodOn(MessageController.class).getMessages(conversationId, pageNumber, pageSize, principal)).withSelfRel());
    }


    @Operation(
        operationId = "create-messages", 
        summary = "", 
        description = ""
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
    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponseDto createMessages(@PathVariable long conversationId, @RequestBody MessageCreateDto createMessageDto, @RequestAttribute User principal) {
        Message message = MessageBuilder.create()
            .setConversation(conversationRepository.getConversationById(conversationId))
            .setSenderId(principal.getId())
            .setBody(createMessageDto.body())
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

            * Si le message est lu, le champ `readAt` est mis à jour avec la date et l’heure de lecture.

            * Si le message doit être supprimé pour l'utilisateur·rice, le champ `deleted` est mis à `true`. Cette opération est irréversible et entraîne la suppression logique du message pour cette personne uniquement.
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
    @PatchMapping("/messages/{messageId}")
    public MessageResponseDto updateMessageStatus(@PathVariable long conversationId, @PathVariable long messageId, User principal) {
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
            .add(linkTo(methodOn(MessageController.class).updateMessageStatus(message.getConversationId(), message.getId(), null)).withSelfRel());

    }

    private MessageStatusDto toMessageStatusDto(MessageStatus messageStatus) {
        return new MessageStatusDto(
            messageStatus.getUserId(),
            messageStatus.getReadAt(),
            messageStatus.isDeleted());
    }
}