package edu.eci.dosw.oficioya.oficioya_service.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import edu.eci.dosw.oficioya.oficioya_service.TestcontainersConfiguration;
import edu.eci.dosw.oficioya.oficioya_service.entity.Request;
import edu.eci.dosw.oficioya.oficioya_service.model.RequestState;
import edu.eci.dosw.oficioya.oficioya_service.repository.ImageRepository;
import edu.eci.dosw.oficioya.oficioya_service.repository.RequestRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * SNOY-19: pruebas de seguridad sobre /api/v1/requests.
 *
 * Usa el contexto completo (Testcontainers + Postgres real) y la cadena de filtros real de
 * Spring Security, así que prueba JwtAuthFilter, SecurityConfig, @PreAuthorize y la
 * verificación de propiedad del recurso de punta a punta.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class SeguridadRequestTest {

    private static final String BASE = "/api/v1/requests";
    private static final String ISSUER = "oficioya";

    @MockitoBean
    ImageRepository imageRepository;

    @Autowired
    WebApplicationContext context;

    @Autowired
    FilterChainProxy springSecurityFilterChain;

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Value("${app.jwt.secret}")
    String secret;

    MockMvc mvc;

    UUID contratanteA;
    UUID contratanteB;
    UUID trabajadorA;
    UUID trabajadorB;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
        contratanteA = UUID.randomUUID();
        contratanteB = UUID.randomUUID();
        trabajadorA = UUID.randomUUID();
        trabajadorB = UUID.randomUUID();
    }

    @AfterEach
    void limpiar() {
        requestRepository.deleteAll();
    }

    // ---------------------------------------------------------------- 401

    @Test
    @DisplayName("401: sin token en GET /requests")
    void sinTokenListar() throws Exception {
        mvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("401: sin token en PATCH accept, reject y accept-conflict")
    void sinTokenAccionesProtegidas() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(patch(BASE + "/" + id + "/accept")).andExpect(status().isUnauthorized());
        mvc.perform(patch(BASE + "/" + id + "/reject")).andExpect(status().isUnauthorized());
        mvc.perform(patch(BASE + "/" + id + "/accept-conflict")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401: token expirado")
    void tokenExpirado() throws Exception {
        String expirado = construirToken(trabajadorA, "TRABAJADOR", clavePropia(), ISSUER,
                Instant.now().minus(2, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS));

        mvc.perform(get(BASE).header("Authorization", "Bearer " + expirado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401: token firmado con otra clave")
    void tokenFirmadoConOtraClave() throws Exception {
        SecretKey otra = Keys.hmacShaKeyFor(
                "una-clave-distinta-de-al-menos-256-bits-0123456789".getBytes(StandardCharsets.UTF_8));
        String falso = construirToken(trabajadorA, "TRABAJADOR", otra, ISSUER,
                Instant.now(), Instant.now().plus(30, ChronoUnit.MINUTES));

        mvc.perform(get(BASE).header("Authorization", "Bearer " + falso))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401: token alterado (escalada de CONTRATANTE a TRABAJADOR con la firma original)")
    void tokenAlterado() throws Exception {
        String original = jwtUtil.generateToken(contratanteA, "CONTRATANTE");
        String[] partes = original.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
        String payloadAlterado = payload.replace("CONTRATANTE", "TRABAJADOR");
        String forjado = partes[0] + "."
                + Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(payloadAlterado.getBytes(StandardCharsets.UTF_8))
                + "." + partes[2];

        mvc.perform(patch(BASE + "/" + UUID.randomUUID() + "/accept")
                .header("Authorization", "Bearer " + forjado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401: token con otro emisor")
    void tokenOtroEmisor() throws Exception {
        String token = construirToken(trabajadorA, "TRABAJADOR", clavePropia(), "otro-emisor",
                Instant.now(), Instant.now().plus(30, ChronoUnit.MINUTES));

        mvc.perform(get(BASE).header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401: el sub del token no es un UUID")
    void subNoEsUuid() throws Exception {
        String token = Jwts.builder()
                .subject("correo@eci.edu.co")
                .claim("role", "TRABAJADOR")
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(30, ChronoUnit.MINUTES)))
                .signWith(clavePropia(), Jwts.SIG.HS256)
                .compact();

        mvc.perform(get(BASE).header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401: token con formato inválido y esquema de autorización distinto de Bearer")
    void tokenMalformadoYEsquemaIncorrecto() throws Exception {
        mvc.perform(get(BASE).header("Authorization", "Bearer esto-no-es-un-jwt"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(BASE).header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------- 403

    @Test
    @DisplayName("403: CONTRATANTE no puede aceptar ni rechazar")
    void contratanteNoAceptaNiRechaza() throws Exception {
        Request r = guardar(contratanteA, trabajadorA, RequestState.PENDING, LocalDateTime.now());
        String token = tokenDe(contratanteA, "CONTRATANTE");

        mvc.perform(patch(BASE + "/" + r.getId() + "/accept").header("Authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        mvc.perform(patch(BASE + "/" + r.getId() + "/reject").header("Authorization", token))
                .andExpect(status().isForbidden());

        assertEquals(RequestState.PENDING, estadoActual(r));
    }

    @Test
    @DisplayName("403: TRABAJADOR no puede resolver conflictos")
    void trabajadorNoResuelveConflictos() throws Exception {
        Request r = guardar(contratanteA, trabajadorA, RequestState.CONFLICT, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + r.getId() + "/accept-conflict")
                .header("Authorization", tokenDe(trabajadorA, "TRABAJADOR")))
                .andExpect(status().isForbidden());

        assertEquals(RequestState.CONFLICT, estadoActual(r));
    }

    @Test
    @DisplayName("403: ADMIN no puede aceptar, rechazar ni listar solicitudes (hoy solo CONTRATANTE/TRABAJADOR)")
    void adminNoOperaSolicitudes() throws Exception {
        String admin = tokenDe(UUID.randomUUID(), "ADMIN");
        UUID id = UUID.randomUUID();

        mvc.perform(patch(BASE + "/" + id + "/accept").header("Authorization", admin))
                .andExpect(status().isForbidden());
        mvc.perform(patch(BASE + "/" + id + "/reject").header("Authorization", admin))
                .andExpect(status().isForbidden());
        mvc.perform(get(BASE).header("Authorization", admin))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("403: token sin claim de rol")
    void tokenSinRol() throws Exception {
        String sinRol = Jwts.builder()
                .subject(trabajadorA.toString())
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plus(30, ChronoUnit.MINUTES)))
                .signWith(clavePropia(), Jwts.SIG.HS256)
                .compact();

        mvc.perform(get(BASE).header("Authorization", "Bearer " + sinRol))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("403 / 401: /api/v1/admin/** exige rol ADMIN")
    void rutasDeAdmin() throws Exception {
        mvc.perform(get("/api/v1/admin/cualquier-cosa"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/admin/cualquier-cosa")
                .header("Authorization", tokenDe(contratanteA, "CONTRATANTE")))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------------- 404 (solicitud ajena)

    @Test
    @DisplayName("404: un trabajador no puede aceptar una solicitud asignada a otro trabajador")
    void trabajadorAceptaSolicitudAjena() throws Exception {
        Request deA = guardar(contratanteA, trabajadorA, RequestState.PENDING, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + deA.getId() + "/accept")
                .header("Authorization", tokenDe(trabajadorB, "TRABAJADOR")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Solicitud no encontrada"));

        assertEquals(RequestState.PENDING, estadoActual(deA));
    }

    @Test
    @DisplayName("404: un trabajador no puede rechazar una solicitud asignada a otro trabajador")
    void trabajadorRechazaSolicitudAjena() throws Exception {
        Request deA = guardar(contratanteA, trabajadorA, RequestState.PENDING, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + deA.getId() + "/reject")
                .header("Authorization", tokenDe(trabajadorB, "TRABAJADOR")))
                .andExpect(status().isNotFound());

        assertEquals(RequestState.PENDING, estadoActual(deA));
    }

    @Test
    @DisplayName("404: un contratante no puede resolver el conflicto de otro contratante")
    void contratanteResuelveConflictoAjeno() throws Exception {
        Request deA = guardar(contratanteA, trabajadorA, RequestState.CONFLICT, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + deA.getId() + "/accept-conflict")
                .header("Authorization", tokenDe(contratanteB, "CONTRATANTE")))
                .andExpect(status().isNotFound());

        assertEquals(RequestState.CONFLICT, estadoActual(deA));
    }

    @Test
    @DisplayName("404: solicitud ajena e inexistente responden igual (no revela que existe)")
    void ajenaEInexistenteSonIndistinguibles() throws Exception {
        Request deA = guardar(contratanteA, trabajadorA, RequestState.PENDING, LocalDateTime.now());
        String tokenB = tokenDe(trabajadorB, "TRABAJADOR");

        String ajena = mvc.perform(patch(BASE + "/" + deA.getId() + "/accept").header("Authorization", tokenB))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String inexistente = mvc.perform(patch(BASE + "/" + UUID.randomUUID() + "/accept")
                .header("Authorization", tokenB))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertEquals(
                jsonCampo(ajena, "mensaje"),
                jsonCampo(inexistente, "mensaje"));
    }

    @Test
    @DisplayName("GET /requests solo devuelve las solicitudes del usuario autenticado")
    void listarSoloLasPropias() throws Exception {
        Request deA = guardar(contratanteA, trabajadorA, RequestState.PENDING, LocalDateTime.now());
        guardar(contratanteB, trabajadorB, RequestState.PENDING, LocalDateTime.now());

        mvc.perform(get(BASE).header("Authorization", tokenDe(trabajadorA, "TRABAJADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(deA.getId().toString()));

        mvc.perform(get(BASE).header("Authorization", tokenDe(contratanteA, "CONTRATANTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(deA.getId().toString()));

        mvc.perform(get(BASE).header("Authorization", tokenDe(UUID.randomUUID(), "TRABAJADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------------------------------------------------------------- controles positivos

    @Test
    @DisplayName("200: el trabajador asignado acepta su solicitud PENDING")
    void trabajadorAsignadoAcepta() throws Exception {
        Request r = guardar(contratanteA, trabajadorA, RequestState.PENDING, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + r.getId() + "/accept")
                .header("Authorization", tokenDe(trabajadorA, "TRABAJADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACCEPTED"));

        assertEquals(RequestState.ACCEPTED, estadoActual(r));
    }

    @Test
    @DisplayName("200: el trabajador asignado rechaza su solicitud PENDING")
    void trabajadorAsignadoRechaza() throws Exception {
        Request r = guardar(contratanteA, trabajadorA, RequestState.PENDING, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + r.getId() + "/reject")
                .header("Authorization", tokenDe(trabajadorA, "TRABAJADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("REJECTED"));

        assertEquals(RequestState.REJECTED, estadoActual(r));
    }

    @Test
    @DisplayName("200: el contratante dueño resuelve su conflicto")
    void contratanteDuenoResuelve() throws Exception {
        Request r = guardar(contratanteA, trabajadorA, RequestState.CONFLICT, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + r.getId() + "/accept-conflict")
                .header("Authorization", tokenDe(contratanteA, "CONTRATANTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACCEPTED"));
    }

    @Test
    @DisplayName("/auth/login es público y el token que entrega abre /requests")
    void loginEsPublicoYEntregaTokenUtil() throws Exception {
        String respuesta = mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"cualquiera@eci.edu.co\",\"password\":\"x\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        String token = jsonCampo(respuesta, "token");
        if (token == null) {
            token = jsonCampo(respuesta, "accessToken");
        }

        mvc.perform(get(BASE).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------------- 409 vía HTTP

    @Test
    @DisplayName("409: aceptar una solicitud que ya no está PENDING")
    void aceptarSolicitudYaRespondida() throws Exception {
        Request r = guardar(contratanteA, trabajadorA, RequestState.REJECTED, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + r.getId() + "/accept")
                .header("Authorization", tokenDe(trabajadorA, "TRABAJADOR")))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("409: aceptar después de los 30 minutos")
    void aceptarFueraDeVentana() throws Exception {
        Request r = guardar(contratanteA, trabajadorA, RequestState.PENDING,
                LocalDateTime.now().minusMinutes(31));

        mvc.perform(patch(BASE + "/" + r.getId() + "/accept")
                .header("Authorization", tokenDe(trabajadorA, "TRABAJADOR")))
                .andExpect(status().isConflict());
    }

    // ---------------------------------------------------------------- utilidades

    private SecretKey clavePropia() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private String construirToken(UUID userId, String rol, SecretKey clave, String emisor,
                                  Instant emitido, Instant expira) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", rol)
                .issuer(emisor)
                .issuedAt(Date.from(emitido))
                .expiration(Date.from(expira))
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    /** Encabezado Authorization completo, con un token válido generado por JwtUtil. */
    private String tokenDe(UUID userId, String rol) {
        return "Bearer " + jwtUtil.generateToken(userId, rol);
    }

    private Request guardar(UUID contratante, UUID trabajador, RequestState estado, LocalDateTime creada) {
        Request r = new Request();
        r.setId(UUID.randomUUID());
        r.setRootId(UUID.randomUUID());
        r.setContractorId(contratante);
        r.setWorkerId(trabajador);
        r.setDescription("Arreglo de una fuga");
        r.setNeighborhood("Usaquén");
        r.setExactAddress("Calle 1 # 2-3");
        r.setScheduledDateTime(LocalDateTime.now().plusDays(3));
        r.setState(estado);
        r.setDateCreated(creada);
        return requestRepository.save(r);
    }

    private RequestState estadoActual(Request r) {
        return requestRepository.findById(r.getId()).orElseThrow().getState();
    }

    /** Extrae un campo de texto de un JSON plano sin depender de una librería concreta. */
    private String jsonCampo(String json, String campo) {
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("\"" + campo + "\"\\s*:\\s*\"([^\"]*)\"")
                .matcher(json);
        return m.find() ? m.group(1) : null;
    }
}