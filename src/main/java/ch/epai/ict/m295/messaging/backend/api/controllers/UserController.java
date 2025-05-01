package ch.epai.ict.m295.messaging.backend.api.controllers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.epai.ict.m295.messaging.backend.api.dto.CreateUserDto;
import ch.epai.ict.m295.messaging.backend.api.dto.UpdateUserDto;
import ch.epai.ict.m295.messaging.backend.api.dto.UserResponseDto;
import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserBuilder;
import ch.epai.ict.m295.messaging.backend.domain.UserRepository;
import ch.epai.ict.m295.messaging.backend.domain.UserRoles;
import ch.epai.ict.m295.messaging.backend.domain.security.TokenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
public class UserController {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    public UserController(UserRepository userRepository, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }
    
    @Operation(summary = "Récupère la liste des utilisateurs")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    @GetMapping(path = "/users", produces = "application/json")
    public CollectionModel<EntityModel<UserResponseDto>> getUsers(@RequestAttribute User principal) {
        return toUsersResponse(this.userRepository.getUsers(), principal);
    }

    @Operation(summary = "Ajoute un nouvel utilisateur")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Utilisateur créé"),
        @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    @PostMapping(path = "/users", consumes = "application/json", produces = "application/hal+json")
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<UserResponseDto> createUser(@RequestBody CreateUserDto createUserDto) {
        User user = UserBuilder.create()
            .setUsername(createUserDto.username())
            .build();
        this.userRepository.createUser(user, createUserDto.password());
        return toUserResponse(user);
    }

    @Operation(summary = "Récupère un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Utilisateur récupéré avec succès."),
        @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(path = "/users/{id}", produces = "application/hal+json")
    public EntityModel<UserResponseDto> getUser(@PathVariable long id, @RequestAttribute User principal) {
        if (principal.getId() == id || principal.getRole() == UserRoles.ADMIN ) {
            return toUserResponse(this.userRepository.getUser(id));
        }
        throw new ResponseStatusException(HttpStatusCode.valueOf(403));
    }

    @Operation(summary = "Modifie les données d'une utilisatrice")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Données modifiées avec succès."),
        @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(path = "/users/{id}", consumes = "application/json", produces = "application/hal+json")
    public EntityModel<UserResponseDto> modifyUser(@PathVariable long id, @RequestBody UpdateUserDto updateUserDto, @RequestAttribute User principal) {
        String newUsername = principal.getUsername();
        String newDisplayName = principal.getDisplayName();

        if (principal.getId() == id) {
            if (updateUserDto.username() != null) {
                newUsername = updateUserDto.username();
            }
            if (updateUserDto.displayName() != null) {
                newDisplayName = updateUserDto.displayName();
            }
            this.userRepository.updateUser(
                UserBuilder.create()
                    .setId(id)
                    .setUsername(newUsername)
                    .setDisplayName(newDisplayName)
                    .build());

            if (updateUserDto.password() != null) {
                this.userRepository.updateUserPassword(id, updateUserDto.password());
                this.tokenRepository.deleteAllUserToken(principal);
            }
            return toUserResponse(this.userRepository.getUser(id));
        }
        throw new ResponseStatusException(HttpStatusCode.valueOf(403));
    }


    @Operation(summary = "Supprime un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Utilisateur supprimé avec succès."),
        @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(path = "/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long id) {
        this.userRepository.deleteUser(id);
    }

    private CollectionModel<EntityModel<UserResponseDto>> toUsersResponse(List<User> userList, User principal) {
        return CollectionModel.of(
            userList.stream()
                .map(user -> toUserResponse(user))
                .collect(Collectors.toList()));
    }

    private EntityModel<UserResponseDto> toUserResponse(User user) {
        return EntityModel.of(
            new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName()))
            .add(linkTo(methodOn(UserController.class).getUser(user.getId(), null)).withSelfRel());
    }
}