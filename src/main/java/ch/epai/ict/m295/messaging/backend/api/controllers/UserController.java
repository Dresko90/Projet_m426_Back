package ch.epai.ict.m295.messaging.backend.api.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.epai.ict.m295.messaging.backend.api.dto.UserCreateDto;
import ch.epai.ict.m295.messaging.backend.api.dto.UserResponseDto;
import ch.epai.ict.m295.messaging.backend.api.dto.UserUpdateDto;
import ch.epai.ict.m295.messaging.backend.api.dto.UsersResponseDto;
import ch.epai.ict.m295.messaging.backend.domain.ConversationRepository;
import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserBuilder;
import ch.epai.ict.m295.messaging.backend.domain.UserRepository;
import ch.epai.ict.m295.messaging.backend.domain.UserRoles;
import ch.epai.ict.m295.messaging.backend.domain.security.TokenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import  io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name ="user")
public class UserController {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final ConversationRepository conversationRepository;


    public UserController(UserRepository userRepository, TokenRepository tokenRepository, ConversationRepository conversationRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.conversationRepository = conversationRepository;
    }
    
    @Operation(
        operationId = "get-users",
        summary = "Récupère la liste des utilisateur·rice·s",
        description = "Récupère la liste des utilisateur·rice·s avec pagination."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
            content = @Content(
                schema = @Schema(implementation = UsersResponseDto.class),
                examples = @ExampleObject(
                    value ="""
                        {
                            "_embedded": {
                                "users": [
                                    {
                                        "id": 1,
                                        "username": "admin@example.com",
                                        "displayName": "Administrator",
                                        "_links": {
                                            "self": {
                                                "href": "/api/v1/users/1"
                                            }
                                        }
                                    },
                                    {
                                        "id": 11,
                                        "username": "sheana@example.com",
                                        "displayName": "sheana",
                                        "_links": {
                                            "self": {
                                                "href": "/api/v1/users/11"
                                            }
                                        }
                                    }
                                ]
                            },
                            "_links": {
                                "self": {
                                    "href": "/api/v1/users?page=0&size=100"
                                }
                            },
                            "page": {
                                "size": 20,
                                "totalElements": 2,
                                "totalPages": 1,
                                "number": 0
                            }
                        }                    
                        """)
                )),           
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    })
    @GetMapping(path = "/users", produces = "application/hal+json")
    public UsersResponseDto getUsers(
            @RequestParam(name = "page", defaultValue = "0") int pageNumber,
            @RequestParam(name = "size", defaultValue = "20") int pageSize,
            @RequestAttribute User principal) {

        return toUsersResponse(
            this.userRepository.getUsers(pageNumber, pageSize), 
            pageNumber, 
            pageSize, 
            this.userRepository.getNumberOfUsers(), 
            principal);
    }


    @Operation(
        operationId = "create-users",
        summary = "Crée un compte d'utilisateur·rice.",
        description = "Crée un compte avec un nom d'utilisateur·rice et un mot de passe. Le nom peut être soit une adresse e-mail, soit un numéro de téléphone au format E.164.")
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Utilisateur·rice créé",
            content = @Content(
                schema = @Schema(implementation = UserResponseDto.class),
                examples = 
                    @ExampleObject(
                        value = """
                            {
                                "id": 11,
                                "username": "sheana@example.com",
                                "displayName": "sheana",
                                "_links": {
                                    "self": {
                                        "href": "/api/v1/users/11"
                                    }
                                }
                            }
                            """))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
        @ApiResponse(responseCode = "409", description = "Conflict", content = @Content),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),    })
    @PostMapping(path = "/users", consumes = "application/json", produces = "application/hal+json")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Données de l'utilisateur·rice à créer",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                        value = """
                        {
                            "username": "hari@example.com",
                            "displayName": "Hari",
                            "password": "epai321"
                        }
                        """)))
            @RequestBody UserCreateDto createUserDto) {

        if (this.userRepository.getUserByUsername(createUserDto.username()) != null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(409));
        }

        User user = UserBuilder.create()
            .setUsername(createUserDto.username())
            .setDisplayName(createUserDto.displayName())
            .build();
        this.userRepository.createUser(user, createUserDto.password());
        return toUserResponse(user);
    }

    @Operation(
        operationId = "get-user",
        summary = "Récupère les informations d’un·e utilisateur·rice.",
        description = "Récupère les informations d'un·e utilisateur·rice à partir de son identifiant.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilisateur·rice récupéré avec succès.",
            content = @Content(
                schema = @Schema(implementation = UserResponseDto.class),
                examples = 
                    @ExampleObject(
                        value = """
                            {
                                "id": 11,
                                "username": "sheana@example.com",
                                "displayName": "sheana",
                                "_links": {
                                    "self": {
                                        "href": "/api/v1/users/11"
                                    }
                                }
                            }
                            """))),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "404", description = "Utilisateur·rice non trouvé·e", content = @Content),
        @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping(path = "/users/{userId}", produces = "application/hal+json")
    public UserResponseDto getUser(
            @Parameter(
                description = "Identifiant de l'utilisateur·rice",
                example = "11"
            )
            @PathVariable() long userId,
            @RequestAttribute User principal) {

        if (principal.getId() == userId || principal.getRole() == UserRoles.ADMIN ) {
            return toUserResponse(this.userRepository.getUserById(userId));
        }
        throw new ResponseStatusException(HttpStatusCode.valueOf(403));
    }

    @Operation(
        operationId = "update-user",
        summary = "Modifie les informations d'un·e utilisateur·rice.",
        description = "Modifie les informations d'un·e utilisateur·rice à partir de son identifiant.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Données modifiées avec succès.",
            content = @Content(
                schema = @Schema(implementation = UserResponseDto.class),
                examples = 
                    @ExampleObject(
                        value = """
                            {
                                "id": 11,
                                "username": "sheana@example.com",
                                "displayName": "sheana",
                                "_links": {
                                    "self": {
                                        "href": "/api/v1/users/11"
                                    }
                                }
                            }
                            """))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "404", description = "Utilisateur·rice non trouvé·e", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),    })
    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping(path = "/users/{userId}", consumes = "application/json", produces = "application/hal+json")
    public UserResponseDto modifyUser(
            @Parameter(
                description = "Identifiant de l'utilisateur·rice",
                example = "11"
            )
            @PathVariable long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Données de l'utilisateur·rice à modifier",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "Modifier le mot de passe",
                            value = """
                            {
                                "password": "Epai123"
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
            @RequestBody UserUpdateDto updateUserDto, 
            @RequestAttribute User principal) {
        
        if (principal.getId() != userId && principal.getRole() != UserRoles.ADMIN) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403));
        }

        User user = this.userRepository.getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404));
        }

        this.userRepository.updateUser(
            UserBuilder.create()
                .setId(userId)
                .setUsername(
                    Objects.requireNonNullElse(
                        updateUserDto.username(), 
                        user.getUsername()))
                .setDisplayName(
                    Objects.requireNonNullElse(
                        updateUserDto.displayName(), 
                        user.getDisplayName()))
                .build());

        if (updateUserDto.password() != null) {
            this.userRepository.updateUserPassword(user, updateUserDto.password());
            this.tokenRepository.deleteAllUserToken(user);
        }
        return toUserResponse(this.userRepository.getUserById(userId));
    }


    @Operation(
            operationId = "delete-user",
            summary = "Supprime un·e utilisateur·rice.",
            description = "Supprime un·e utilisateur·rice à partir de son identifiant. Cette opération est irréversible et entraîne l’anonymisation de tous les messages envoyés par cette personne." )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Utilisateur·rice supprimé avec succès."),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "404", description = "Utilisateur·rice non trouvé·e", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping(path = "/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long userId, @RequestAttribute User principal) {

        if (principal.getId() != userId && principal.getRole() != UserRoles.ADMIN ) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(403));
        }

        User user = this.userRepository.getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(404));
        }

        this.conversationRepository.anonymizeAllUserMessages(userId);
        this.tokenRepository.deleteAllUserToken(user);
        this.userRepository.deleteUser(user);
    }

    @Operation(
        operationId = "get-my-data",
        summary = "Récupère les informations de l'utilisateur·rice authentifié·e.",
        description = "Récupère les informations de l'utilisateur·rice authentifié·e à partir de son jeton d'authentification."
    )
    @SecurityRequirement(name = "BearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
            content = @Content(
                schema = @Schema(implementation = UsersResponseDto.class),
                examples = @ExampleObject(
                    value ="""
                        {
                            "id": 11,
                            "username": "sheana@example.com",
                            "displayName": "sheana",
                            "_links": {
                                "self": {
                                    "href": "/api/v1/users/me"
                                }
                            }
                        }
                        """)
                )),           
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    })
    @GetMapping(path = "/users/me", produces = "application/hal+json")
    public UserResponseDto getMyData(
            @RequestAttribute User principal) {

        return new UserResponseDto(
                        principal.getId(),
                        principal.getUsername(),
                        principal.getDisplayName())
                    .add(linkTo(methodOn(UserController.class).getMyData(null)).withSelfRel());
    }

    @Operation(
        operationId = "update-my-data",
        summary = "Modifie les informations de utilisateur·rice authentifié·e.",
        description = "Modifie les informations de l'utilisateur·rice authentifié·e à partir de son jeton d'authentification.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Données modifiées avec succès.",
            content = @Content(
                schema = @Schema(implementation = UserResponseDto.class),
                examples = 
                    @ExampleObject(
                        value = """
                            {
                                "id": 11,
                                "username": "sheana@example.com",
                                "displayName": "sheana",
                                "_links": {
                                    "self": {
                                        "href": "/api/v1/users/11"
                                    }
                                }
                            }
                            """))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "404", description = "Utilisateur·rice non trouvé·e", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),    })
    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping(path = "/users/me", consumes = "application/json", produces = "application/hal+json")
    public UserResponseDto modifyMyData(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Données de l'utilisateur·rice à modifier",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "Modifier le mot de passe",
                            value = """
                            {
                                "password": "Epai123"
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
            @RequestBody UserUpdateDto updateUserDto, 
            @RequestAttribute User principal) {

        return modifyUser(principal.getId(), updateUserDto, principal);
    }
            

    private UsersResponseDto toUsersResponse(List<User> userList, int pageNumber, int pageSize, long totalElements, User principal) {
        return new UsersResponseDto(
            userList.stream()
                .map(user -> toUserResponse(user))
                .collect(Collectors.toList()),
            new PageMetadata(pageSize, pageNumber, totalElements),
            linkTo(methodOn(UserController.class).getUsers(pageNumber, pageSize, principal)).withSelfRel());
    }

    private UserResponseDto toUserResponse(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName())
            .add(linkTo(methodOn(UserController.class).getUser(user.getId(), null)).withSelfRel());
    }
}