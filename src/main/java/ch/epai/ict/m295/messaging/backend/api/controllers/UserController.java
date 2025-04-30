package ch.epai.ict.m295.messaging.backend.api.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ch.epai.ict.m295.messaging.backend.api.dto.CreateUserDto;
import ch.epai.ict.m295.messaging.backend.api.dto.UserResponseDto;
import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserBuilder;
import ch.epai.ict.m295.messaging.backend.domain.UserRepository;
import ch.epai.ict.m295.messaging.backend.domain.UserRoles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
public class UserController {

    private UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    @PostMapping(path = "/users", 
            consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<UserResponseDto> createUser(@RequestBody CreateUserDto createUserDto, @RequestAttribute User principal) {
        User user = UserBuilder.create()
            .setUsername(createUserDto.username())
            .build();
        this.userRepository.createUser(user, createUserDto.password());
        return toUserResponse(user, principal);
    }

    @Operation(summary = "Récupère un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Utilisateur récupéré avec succès."),
        @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(path = "/users/{id}", produces = "application/json")
    public EntityModel<UserResponseDto> getUser(@PathVariable long id, @RequestAttribute User principal) {
        if (principal.getId() == id || principal.getRole() == UserRoles.ADMIN ) {
            return toUserResponse(this.userRepository.getUser(id), principal);
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
                .map(user -> toUserResponse(user, principal))
                .collect(Collectors.toList()));
    }

    private EntityModel<UserResponseDto> toUserResponse(User user, User principal) {
        return EntityModel.of(
            new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName()))
            .add(linkTo(methodOn(UserController.class).getUser(user.getId(), principal)).withSelfRel());
    }
}
