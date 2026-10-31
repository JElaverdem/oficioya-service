package edu.eci.dosw.oficioya.oficioya_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

/**
 * Documentacion OpenAPI del servicio.
 *
 * El servicio es abierto: no se declara ningun esquema de seguridad y los
 * endpoints no exigen JWT. Para operar en nombre de un usuario se usa el header
 * X-User-Id, que es identificacion, no autenticacion.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI oficioyaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("OficioYa - Servicio de Solicitudes")
                        .version("v1")
                        .description("""
                                API del dominio Service de OficioYa.

                                Servicio abierto: los endpoints no exigen autenticacion.

                                Para listar o responder solicitudes como un usuario, envia el
                                header X-User-Id con su UUID. Si no se envia ese header y hay un
                                JWT valido en Authorization, se usa el sub del token.

                                El login sigue disponible en /api/v1/auth/login, pero es opcional.
                                """)
                        .contact(new Contact()
                                .name("Equipo OficioYa")
                                .url("https://github.com/JElaverdem/oficioya-service")));
    }
}
