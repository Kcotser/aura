package com.aura.iam.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI auraOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Aura API")
                        .description("Multimodal personal safety platform — Backend REST API")
                        .version("v0.1.0")
                        .contact(new Contact()
                                .name("Aura Team")
                                .email("team@aura.com"))
                        .license(new License()
                                .name("Private — Hackathon Project")))
                // Sin requisito global de seguridad: aplicarlo aqui ponia candado tambien en
                // /auth/register, /auth/login y /auth/refresh, que son publicos, y Swagger les
                // mandaba un Authorization que el cliente real no manda. Cada controller declara
                // su propio @SecurityRequirement.
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT access token")));
    }
}
