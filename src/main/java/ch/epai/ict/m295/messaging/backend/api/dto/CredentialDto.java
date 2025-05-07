package ch.epai.ict.m295.messaging.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CredentialDto(
    @Schema(description = "Nom d'utilisateur·rice", example = "sheana@example.com")
    String username,
    @Schema(description = "Mot de passe", example = "epai321")
    String password) {
}