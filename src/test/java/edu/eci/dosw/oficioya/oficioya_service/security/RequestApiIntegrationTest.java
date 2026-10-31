package edu.eci.dosw.oficioya.oficioya_service.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

/**
 * Pruebas de integracion del API de solicitudes sobre PostgreSQL real.
 *
 * El servicio es abierto: no se envia token, el usuario se indica con el header
 * X-User-Id. Tambien se verifica que un JWT valido siga funcionando sin header.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class RequestApiIntegrationTest {

    private static final String BASE = "/api/v1/requests";
    private static final String USER_HEADER = "X-User-Id";

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

    @Test
    @DisplayName("400: listar sin header X-User-Id")
    void listarSinUsuario() throws Exception {
        mvc.perform(get(BASE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("400: aceptar, rechazar y resolver sin header X-User-Id")
    void accionesSinUsuario() throws Exception {
        UUID id = UUID.randomUUID();
        mvc.perform(patch(BASE + "/" + id + "/accept")).andExpect(status().isBadRequest());
        mvc.perform(patch(BASE + "/" + id + "/reject")).andExpect(status().isBadRequest());
        mvc.perform(patch(BASE + "/" + id + "/accept-conflict")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests solo devuelve las solicitudes del usuario indicado")
    void listarSoloLasPropias() throws Exception {
        Request deA = guardar(contratanteA, trabajadorA, RequestState.PENDING, LocalDateTime.now());
        guardar(contratanteB, trabajadorB, RequestState.PENDING, LocalDateTime.now());

        mvc.perform(get(BASE).header(USER_HEADER, trabajadorA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(deA.getId().toString()));

        mvc.perform(get(BASE).header(USER_HEADER, contratanteA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(deA.getId().toString()));

        mvc.perform(get(BASE).header(USER_HEADER, UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("200: el trabajador asignado acepta su solicitud PENDING")
    void trabajadorAsignadoAcepta() throws Exception {
        Request r = guardar(contratanteA, trabajadorA, RequestState.PENDING, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + r.getId() + "/accept").header(USER_HEADER, trabajadorA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACCEPTED"));

        assertEquals(RequestState.ACCEPTED, estadoActual(r));
    }

    @Test
    @DisplayName("200: el trabajador asignado rechaza su solicitud PENDING")
    void trabajadorAsignadoRechaza() throws Exception {
        Request r = guardar(contratanteA, trabajadorA, RequestState.PENDING, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + r.getId() + "/reject").header(USER_HEADER, trabajadorA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("REJECTED"));

        assertEquals(RequestState.REJECTED, estadoActual(r));
    }

    @Test
    @DisplayName("200: el contratante duenio resuelve su conflicto")
    void contratanteDuenoResuelve() throws Exception {
        Request r = guardar(contratanteA, trabajadorA, RequestState.CONFLICT, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + r.getId() + "/accept-conflict").header(USER_HEADER, contratanteA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ACCEPTED"));

        assertEquals(RequestState.ACCEPTED, estadoActual(r));
    }

    @Test
    @DisplayName("404: un trabajador no puede aceptar ni rechazar una solicitud ajena")
    void trabajadorNoOperaSolicitudAjena() throws Exception {
        Request deA = guardar(contratanteA, trabajadorA, RequestState.PENDING, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + deA.getId() + "/accept").header(USER_HEADER, trabajadorB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Solicitud no encontrada"));
        mvc.perform(patch(BASE + "/" + deA.getId() + "/reject").header(USER_HEADER, trabajadorB))
                .andExpect(status().isNotFound());

        assertEquals(RequestState.PENDING, estadoActual(deA));
    }

    @Test
    @DisplayName("404: un contratante no puede resolver el conflicto de otro")
    void contratanteNoResuelveConflictoAjeno() throws Exception {
        Request deA = guardar(contratanteA, trabajadorA, RequestState.CONFLICT, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + deA.getId() + "/accept-conflict").header(USER_HEADER, contratanteB))
                .andExpect(status().isNotFound());

        assertEquals(RequestState.CONFLICT, estadoActual(deA));
    }

    @Test
    @DisplayName("409: aceptar una solicitud que ya no esta PENDING")
    void aceptarSolicitudYaRespondida() throws Exception {
        Request r = guardar(contratanteA, trabajadorA, RequestState.REJECTED, LocalDateTime.now());

        mvc.perform(patch(BASE + "/" + r.getId() + "/accept").header(USER_HEADER, trabajadorA))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("409: aceptar despues de los 30 minutos")
    void aceptarFueraDeVentana() throws Exception {
        Request r = guardar(contratanteA, trabajadorA, RequestState.PENDING,
                LocalDateTime.now().minusMinutes(31));

        mvc.perform(patch(BASE + "/" + r.getId() + "/accept").header(USER_HEADER, trabajadorA))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("El login sigue siendo publico y entrega un token")
    void loginEsPublico() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"cualquiera@eci.edu.co\",\"password\":\"x\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("Un JWT valido sigue funcionando cuando no se envia X-User-Id")
    void tokenSigueFuncionandoSinHeader() throws Exception {
        mvc.perform(get(BASE).header("Authorization",
                "Bearer " + jwtUtil.generateToken(trabajadorA, "TRABAJADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    private Request guardar(UUID contratante, UUID trabajador, RequestState estado, LocalDateTime creada) {
        Request r = new Request();
        r.setId(UUID.randomUUID());
        r.setRootId(UUID.randomUUID());
        r.setContractorId(contratante);
        r.setWorkerId(trabajador);
        r.setDescription("Arreglo de una fuga");
        r.setNeighborhood("Usaquen");
        r.setExactAddress("Calle 1 # 2-3");
        r.setScheduledDateTime(LocalDateTime.now().plusDays(3));
        r.setState(estado);
        r.setDateCreated(creada);
        return requestRepository.save(r);
    }

    private RequestState estadoActual(Request r) {
        return requestRepository.findById(r.getId()).orElseThrow().getState();
    }
}
