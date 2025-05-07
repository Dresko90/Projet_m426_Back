package ch.epai.ict.m295.messaging.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenDto(    
    @Schema(description = "Token d'authentification", example = "865b1cb2-bd0e-4468-8370-a8ae9ae4bd11t")
    String token) {
}
