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

@RestController
public class TokenController {

    private TokenRepository tokenRepository;
    private UserRepository userRepository;

    public TokenController(TokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Crée un token pour un utilisateur (login)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Le jeton a été créer avec succès"),
        @ApiResponse(responseCode = "401", description = "Non authorisé", content = @Content)
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

    @Operation(summary = "Supprime un token (logout)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Token supprimé avec succès.")
    })
    @DeleteMapping(path = "/tokens/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleDeleteToken(@RequestBody TokenDto tokenDto) {
        this.tokenRepository.deleteToken(Token.fromString(tokenDto.token()));
    }
}
