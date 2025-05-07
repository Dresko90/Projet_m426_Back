package ch.epai.ict.m295.messaging.backend.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
@ConfigurationProperties(prefix = "springdoc.info")
@OpenAPIDefinition(
    info = @Info(
        title = "${springdoc.info.title}",
        description = "${springdoc.info.description}",
        version = "${springdoc.info.version}",
        contact = @Contact(
            name = "${springdoc.info.contact.name}"
        ),
        license = @License(
            name = "${springdoc.info.license.name}",
            url  = "${springdoc.info.license.url}"
        )),
    security = @SecurityRequirement(name = "BearerAuth"),
    tags = {
        @Tag(name = "user", description = "Gestion des utilisateurs"),
        @Tag(name = "token", description = "Gestion de l'authentification"),
        @Tag(name = "conversation", description = "Gestion des conversations"),
        @Tag(name = "participant", description = "Gestion des participants"),
        @Tag(name = "message", description = "Gestion des messages"),
    })
@SecurityScheme(
    name = "BearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer"
)
public class OpenApiConfig {
}