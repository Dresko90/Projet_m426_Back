package ch.epai.ict.m295.messaging.backend.api.controllers;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.beans.factory.annotation.Value;
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
import ch.epai.ict.m295.messaging.backend.api.dto.UserDto;
import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserBuilder;
import ch.epai.ict.m295.messaging.backend.domain.UserRepository;
import ch.epai.ict.m295.messaging.backend.domain.UserRoles;

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
    public List<UserDto> handleGetUsers() {
        return createListOfUserDtoFromListOfUser(this.userRepository.getUsers());
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
    public UserDto handlePostUsers(@RequestBody CreateUserDto createUserDto) {
        User user = UserBuilder.create()
            .setEmail(createUserDto.email())
            .setDisplayName(createUserDto.displayName())
            .build();
        this.userRepository.createUser(user, createUserDto.password());

        UserDto res = new UserDto(
                                user.getId(),
                                user.getEmail(),
                                user.getDisplayName());

        return res;
    }

    @Operation(summary = "Récupère un utilisateur")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Utilisateur récupéré avec succès."),
        @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(path = "/users/{id}", produces = "application/json")
    public UserDto handleGetUser(@PathVariable long id, @RequestAttribute User principal) {
        if (principal.getId() == id || principal.getRole() == UserRoles.ADMIN ) {
            return createUserDtoFromUser(this.userRepository.getUser(id));
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
    public void handleDeleteUser(@PathVariable long id) {
        this.userRepository.deleteUser(id);
    }

    private List<UserDto> createListOfUserDtoFromListOfUser(List<User> userList) {
        List<UserDto> res = new ArrayList<>();
        for(User user : userList) {
            res.add(createUserDtoFromUser(user));
        }
        return res;
    }

    private UserDto createUserDtoFromUser(User user) {
        return new UserDto(
            user.getId(),
            user.getEmail(),
            user.getDisplayName());
    }
}
