package ch.epai.ict.m295.messaging.backend.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.epai.ict.m295.messaging.backend.api.dto.CredentialDto;
import ch.epai.ict.m295.messaging.backend.api.dto.TokenDto;
import ch.epai.ict.m295.messaging.backend.domain.User;
import ch.epai.ict.m295.messaging.backend.domain.UserRepository;
import ch.epai.ict.m295.messaging.backend.domain.security.Token;
import ch.epai.ict.m295.messaging.backend.domain.security.TokenRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@Tag(name = "token")
public class TokenController {

    private TokenRepository tokenRepository;
    private UserRepository userRepository;

    public TokenController(TokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Operation(
        operationId = "login",
        summary = "Crée un token d'authentification (connexion)",
        description = "Endpoint de connexion. Reçoit un nom d'utilisateur et un mot de passe, puis renvoie un token d'authentification si les identifiants sont valides.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Jeton créé avec succès",
            content = @Content(schema = @Schema(implementation = TokenDto.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    })
    @PostMapping(path = "/tokens", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenDto handlePostToken(@RequestBody CredentialDto credentials, @RequestAttribute(required = false) String token) {
        if (token != null) {
            this.tokenRepository.deleteToken(Token.fromString(token));
        }
        if (this.userRepository.validate(credentials.username(), credentials.password())) {
            User user = this.userRepository.getUserByUsername(credentials.username());
            Token newToken = Token.randomToken();
            this.tokenRepository.addToken(newToken, user);
            return new TokenDto(newToken.toString());
        }
        throw new ResponseStatusException(HttpStatusCode.valueOf(401));
    }

    @Operation(
        operationId = "logout",
        summary = "Supprime le token de l'utilisateur·rice connecté·e (déconnexion).",
        description = "Supprime le token de l'utilisateur·rice connecté·e (déconnexion). Si le token est invalide ou expiré, la requête ne produit pas d'effet.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Token supprimé avec succès."),
        @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
        @ApiResponse(responseCode = "406", description = "Not Acceptable", content = @Content),
        @ApiResponse(responseCode = "415", description = "Unsupported Media Type", content = @Content),
        @ApiResponse(responseCode = "429", description = "Too Many Requests", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @DeleteMapping(path = "/tokens/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleDeleteToken(@RequestAttribute String token) {
        this.tokenRepository.deleteToken(Token.fromString(token));
    }
}
