package edu.eci.dosw.oficioya.oficioya_service.config;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Verifica que la documentacion OpenAPI y Swagger UI queden publicadas y que el
 * servicio no declare autenticacion. Usa el perfil test con H2 y sin Mongo, sin
 * necesidad de Docker.
 */
@SpringBootTest(properties = "spring.flyway.enabled=false")
@ActiveProfiles("test")
class OpenApiDocumentationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    @DisplayName("GET /v3/api-docs publica las rutas sin esquema de seguridad")
    void apiDocsExponeRutasSinSeguridad() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.info.title").value("OficioYa - Servicio de Solicitudes"))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.paths['/api/v1/auth/login'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/requests'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/requests/{id}/accept'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/v1/requests/{id}/reject'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/v1/requests/{id}/accept-conflict'].patch").exists())
                .andExpect(jsonPath("$.components.securitySchemes").doesNotExist())
                .andExpect(jsonPath("$.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/requests'].get.security").doesNotExist());
    }

    @Test
    @DisplayName("El header X-User-Id aparece documentado y es opcional")
    void elHeaderDeUsuarioEstaDocumentado() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/requests'].get.parameters[0].name").value("X-User-Id"))
                .andExpect(jsonPath("$.paths['/api/v1/requests'].get.parameters[0].in").value("header"))
                .andExpect(jsonPath("$.paths['/api/v1/requests'].get.parameters[0].required").value(false));
    }

    @Test
    @DisplayName("Swagger UI responde sin token")
    void swaggerUiEsPublica() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
    }

    @Test
    @DisplayName("/swagger-ui.html redirige a la interfaz")
    void swaggerUiRedirige() throws Exception {
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }

    @Test
    @DisplayName("GET /requests responde 400 si falta el header X-User-Id")
    void solicitudesSinUsuarioDevuelve400() throws Exception {
        mvc.perform(get("/api/v1/requests"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("GET /requests responde 200 con el header X-User-Id y sin token")
    void solicitudesConHeaderRespondenSinToken() throws Exception {
        mvc.perform(get("/api/v1/requests").header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
