package cl.duocuc.induztek.protocols.client;

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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioClientTest {

    private MockWebServer server;
    private UsuarioClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = new UsuarioClient(WebClient.builder(), server.url("/").toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void aceptaUsuarioAprobadoAunqueRespuestaIncluyaCamposAdicionales() {
        server.enqueue(json("""
                {"id": 8, "correo": "ana@induztek.cl", "aprobado": true, "nombre": "Ana"}
                """));

        assertDoesNotThrow(() -> client.validarUsuarioAprobado("ana@induztek.cl"));
    }

    @Test
    void rechazaUsuarioPendiente() {
        server.enqueue(json("""
                {"id": 8, "correo": "ana@induztek.cl", "aprobado": false}
                """));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> client.validarUsuarioAprobado("ana@induztek.cl"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void transformaUsuarioInexistenteEnErrorDeValidacion() {
        server.enqueue(new MockResponse().setResponseCode(404));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> client.validarUsuarioAprobado("noexiste@induztek.cl"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void informaCaidaDelServicioDeUsuarios() {
        server.enqueue(new MockResponse().setResponseCode(500));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> client.validarUsuarioAprobado("ana@induztek.cl"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void informaFallaDeConexion() throws IOException {
        server.shutdown();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> client.validarUsuarioAprobado("ana@induztek.cl"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void rechazaRespuestaVaciaDelServicioDeUsuarios() {
        server.enqueue(new MockResponse().setResponseCode(204));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> client.validarUsuarioAprobado("ana@induztek.cl"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    private MockResponse json(String body) {
        return new MockResponse().setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
