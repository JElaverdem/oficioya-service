package edu.eci.dosw.oficioya.oficioya_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI oficioyaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("OficioYa - Servicio de Solicitudes")
                        .version("v1")
                        .description("""
                                API del dominio Service de OficioYa.

                                Flujo principal: iniciar sesión en /api/v1/auth/login, copiar el
                                accessToken y usar el botón Authorize con el esquema bearerAuth.

                                Los endpoints de /api/v1/requests requieren un JWT con rol
                                CONTRATANTE o TRABAJADOR según la operación.
                                """)
                        .contact(new Contact()
                                .name("Equipo OficioYa")
                                .url("https://github.com/JElaverdem/oficioya-service")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenido en POST /api/v1/auth/login")));
    }
}
