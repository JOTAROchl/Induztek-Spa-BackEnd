package cl.duocuc.induztek.protocols.service;

import cl.duocuc.induztek.protocols.client.UsuarioClient;
import cl.duocuc.induztek.protocols.dto.ProtocoloRequest;
import cl.duocuc.induztek.protocols.model.Protocolo;
import cl.duocuc.induztek.protocols.repository.ProtocoloRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
