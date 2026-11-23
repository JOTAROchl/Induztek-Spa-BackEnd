package cl.duocuc.induztek.users.client;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RolClientTest {

    private MockWebServer server;
    private RolClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new RolClient(WebClient.builder(), server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void listaSoloLosRolesActivos() {
        server.enqueue(json("""
                [
                  {"id": 1, "nombre": "Técnico", "activo": true},
                  {"id": 2, "nombre": "Retirado", "activo": false}
                ]
                """));

        var roles = client.listarActivos();

        assertEquals(1, roles.size());
        assertEquals("Técnico", roles.getFirst().nombre());
    }

    @Test
    void obtieneRolActivoPorId() {
        server.enqueue(json("""
                {"id": 3, "nombre": "Ingeniero", "activo": true}
                """));

        assertEquals("Ingeniero", client.obtenerActivo(3L).nombre());
    }

    @Test
    void rolNoEncontradoSeConvierteEnErrorDeValidacion() {
        server.enqueue(new MockResponse().setResponseCode(404));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> client.obtenerActivo(99L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void errorRemotoSeReportaComoServicioNoDisponible() {
        server.enqueue(new MockResponse().setResponseCode(500));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> client.listarActivos());

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void noPermiteRegistrarSiElCatalogoNoIncluyeTecnico() {
        server.enqueue(json("""
                [{"id": 1, "nombre": "Administrador", "activo": true}]
                """));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> client.rolTecnicoPredeterminado());

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void errorDeConexionSeReportaComoServicioNoDisponible() throws IOException {
        server.shutdown();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> client.listarActivos());

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    private MockResponse json(String body) {
        return new MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
