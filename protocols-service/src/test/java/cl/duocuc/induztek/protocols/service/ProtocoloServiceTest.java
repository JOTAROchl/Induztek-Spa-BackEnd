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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
    void listaProtocolosPorPropietarioNormalizandoCorreo() {
        Protocolo protocolo = protocolo("ana@induztek.cl");
        when(repository.findAllByCorreoUsuarioIgnoreCaseOrderByFechaDescIdDesc("ana@induztek.cl"))
                .thenReturn(List.of(protocolo));

        var result = service.listar(" ANA@INDUZTEK.CL ");

        assertEquals(1, result.size());
        assertEquals("TR-01", result.getFirst().equipo());
        verify(repository).findAllByCorreoUsuarioIgnoreCaseOrderByFechaDescIdDesc("ana@induztek.cl");
    }

    @Test
    void creaProtocoloValidaUsuarioYNormalizaCampos() {
        when(repository.save(any(Protocolo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.crear(request("ana@induztek.cl", "23/11/2026"));

        assertEquals("TR-01", result.equipo());
        assertEquals("23/11/2026", result.fecha());
        assertEquals("ana@induztek.cl", result.correoUsuario());
        assertEquals("Revisión anual", result.observaciones());
        assertEquals("22.5", result.temperatura());
        verify(usuarioClient).validarUsuarioAprobado("ana@induztek.cl");
        verify(repository).save(any(Protocolo.class));
    }

    @Test
    void rechazaFechaImposibleAntesDeConsultarUsuario() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.crear(request("ana@induztek.cl", "31/02/2026")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(usuarioClient, never()).validarUsuarioAprobado(any());
        verify(repository, never()).save(any());
    }

    @Test
    void rechazaFechaNula() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.crear(request("ana@induztek.cl", null)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(usuarioClient, never()).validarUsuarioAprobado(any());
    }

    @Test
    void noPermiteBuscarProtocoloDeOtroUsuario() {
        when(repository.findByIdAndCorreoUsuarioIgnoreCase(9L, "ana@induztek.cl"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.obtener(9L, "ana@induztek.cl"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void actualizaCamposPeroMantienePropietario() {
        Protocolo protocolo = protocolo("ana@induztek.cl");
        when(repository.findByIdAndCorreoUsuarioIgnoreCase(1L, "ana@induztek.cl"))
                .thenReturn(Optional.of(protocolo));
        when(repository.save(protocolo)).thenReturn(protocolo);

        var result = service.actualizar(1L, "ana@induztek.cl",
                new ProtocoloRequest("TR-02", "24/11/2026", "Formalizado",
                        "Terminado", "ANA@INDUZTEK.CL", null, "43"));

        assertEquals("TR-02", result.equipo());
        assertEquals("ana@induztek.cl", result.correoUsuario());
        assertEquals("Formalizado", result.estado());
        assertEquals("24/11/2026", result.fecha());
        verify(repository).save(protocolo);
    }

    @Test
    void rechazaCambiarPropietarioAlActualizar() {
        Protocolo protocolo = protocolo("ana@induztek.cl");
        when(repository.findByIdAndCorreoUsuarioIgnoreCase(1L, "ana@induztek.cl"))
                .thenReturn(Optional.of(protocolo));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.actualizar(1L, "ana@induztek.cl",
                        request("otra@induztek.cl", "24/11/2026")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void eliminaSoloElProtocoloDelPropietario() {
        Protocolo protocolo = protocolo("ana@induztek.cl");
        when(repository.findByIdAndCorreoUsuarioIgnoreCase(1L, "ana@induztek.cl"))
                .thenReturn(Optional.of(protocolo));

        service.eliminar(1L, "ana@induztek.cl");

        verify(repository).delete(protocolo);
    }

    @Test
    void rechazaCorreoVacioEnConsultas() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.listar(" "));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private ProtocoloRequest request(String correo, String fecha) {
        return new ProtocoloRequest(" TR-01 ", fecha, "Registrado en terreno",
                " Revisión anual ", correo, " 22.5 ", "");
    }

    private Protocolo protocolo(String correo) {
        return new Protocolo("TR-01", LocalDate.of(2026, 11, 23),
                "Registrado en terreno", "Revisión anual", correo, "22.5", null);
    }
}
