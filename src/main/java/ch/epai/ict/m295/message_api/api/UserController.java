package ch.epai.ict.m295.message_api.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.epai.ict.m295.message_api.domain.User;
import ch.epai.ict.m295.message_api.domain.UserBuilder;
import ch.epai.ict.m295.message_api.domain.UserDirectory;
import ch.epai.ict.m295.message_api.domain.UserRoles;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

@RestController
public class UserController {
    
    @Value("${app.jwt.secretkey}")
    private String secretKey;

    private UserDirectory userDir;

    public UserController(UserDirectory userDirectory) {
        this.userDir = userDirectory;
    }

    @Operation(summary = "Demande d'authentification")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Le jeton a été créer avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authorisé", content = @Content)
    })
    @PostMapping(path = "/tokens", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenDto handlePostToken(@RequestBody CredentialDto credentials) {
        if (this.userDir.validate(credentials.email, credentials.password)) {

            User user = this.userDir.getUserByEmail(credentials.email);
            List<String> roles = List.of(user.getRole().name());

            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

            return new TokenDto(Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("roles", roles) 
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 36000000)) // 10 heures
                .signWith(key)
                .compact());
        }
        throw new ResponseStatusException(HttpStatusCode.valueOf(401));
    }

    
    @Operation(summary = "Récupère la liste des utilisateurs")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès"),
        @ApiResponse(responseCode = "403", description = "Accès interdit", content = @Content)
    })
    @GetMapping(path = "/users", produces = "application/json")
    public List<UserDto> handleGetUsers() {
        return createListOfUserDtoFromListOfUser(this.userDir.getUsers());
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
            .setEmail(createUserDto.email)
            .setDisplayName(createUserDto.displayName)
            .build();
        this.userDir.createUser(user, createUserDto.password);

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
            return createUserDtoFromUser(this.userDir.getUser(id));
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
        this.userDir.deleteUser(id);
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
