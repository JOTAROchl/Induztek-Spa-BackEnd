package cl.duocuc.induztek.protocols.service;

import cl.duocuc.induztek.protocols.client.UsuarioClient;
import cl.duocuc.induztek.protocols.dto.ProtocoloRequest;
import cl.duocuc.induztek.protocols.model.Protocolo;
import cl.duocuc.induztek.protocols.repository.ProtocoloRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProtocoloServiceTest {

    private ProtocoloRepository repository;
    private UsuarioClient usuarioClient;
    private ProtocoloService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProtocoloRepository.class);
        usuarioClient = mock(UsuarioClient.class);
        service = new ProtocoloService(repository, usuarioClient);
    }

    @Test
    void crearRechazaEstadoNoAdmitidoAntesDeConsultarAlUsuario() {
        ProtocoloRequest request = new ProtocoloRequest(
                "TR-01", LocalDate.of(2026, 11, 22), "En progreso", null,
                "ana@induztek.cl", null, null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.crear(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(usuarioClient, never()).obtenerActivo("ana@induztek.cl");
    }

    @Test
    void crearAceptaMedicionesVaciasDeLaAppComoValoresOpcionales() {
        when(repository.save(org.mockito.ArgumentMatchers.any(Protocolo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ProtocoloRequest request = new ProtocoloRequest(
                "TR-01", LocalDate.of(2026, 11, 22), "Borrador", null,
                "ana@induztek.cl", "", "");

        var result = service.crear(request);

        assertNull(result.temperatura());
        assertNull(result.humedad());
    }

    @Test
    void leeFechaYMedicionesEnLosFormatosQueEnvíaLaApp() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String json = """
                {
                  "equipo": "TR-01",
                  "fecha": "22/11/2026",
                  "estado": "Borrador",
                  "correoUsuario": "ana@induztek.cl",
                  "temperatura": "",
                  "humedad": "45"
                }
                """;

        ProtocoloRequest request = mapper.readValue(json, ProtocoloRequest.class);

        assertEquals(LocalDate.of(2026, 11, 22), request.fecha());
        assertEquals("", request.temperatura());
        assertEquals("45", request.humedad());
    }

    @Test
    void crearRechazaMedicionFueraDeRangoSinGuardar() {
        ProtocoloRequest request = new ProtocoloRequest(
                "TR-01", LocalDate.of(2026, 11, 22), "Borrador", null,
                "ana@induztek.cl", "151", "45");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.crear(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any(Protocolo.class));
        verify(usuarioClient, never()).obtenerActivo("ana@induztek.cl");
    }

    @Test
    void buscarNoPermiteLeerProtocoloDeOtroResponsable() {
        when(repository.findByIdAndCorreoUsuarioIgnoreCase(15L, "ana@induztek.cl"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.obtener(15L, "Ana@Induztek.cl"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void eliminarSoloElProtocoloDelResponsableIndicado() {
        when(repository.findByIdAndCorreoUsuarioIgnoreCase(15L, "ana@induztek.cl"))
                .thenReturn(Optional.of(new Protocolo("TR-01", LocalDate.now(), "Borrador",
                        null, "ana@induztek.cl", null, null)));

        service.eliminar(15L, "ana@induztek.cl");

        verify(repository).delete(org.mockito.ArgumentMatchers.any(Protocolo.class));
    }
}
