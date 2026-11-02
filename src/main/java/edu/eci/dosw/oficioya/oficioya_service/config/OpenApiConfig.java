package edu.eci.dosw.oficioya.oficioya_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Documentación Swagger / OpenAPI.
 *
 * - Agrega el botón "Authorize" para pegar el JWT (esquema bearerAuth).
 * - Deja públicas solo las rutas de la documentación con una cadena de filtros propia y de mayor
 *   prioridad, sin modificar SecurityConfig. En prod la documentación está deshabilitada
 *   (springdoc.api-docs.enabled=false), así que esas rutas responden 404.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "OficioYa - Service API", version = "v1",
                description = "Solicitudes de servicio: aceptar, rechazar y resolver conflictos."),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
