package cl.duocuc.induztek.protocols.controller;

import cl.duocuc.induztek.protocols.dto.ProtocoloRequest;
import cl.duocuc.induztek.protocols.dto.ProtocoloResponse;
import cl.duocuc.induztek.protocols.service.ProtocoloService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProtocoloControllerTest {

    private ProtocoloService service;
    private ProtocoloController controller;
    private ProtocoloResponse response;

    @BeforeEach
    void setUp() {
        service = mock(ProtocoloService.class);
        controller = new ProtocoloController(service);
        response = new ProtocoloResponse(1L, "TR-01", "23/11/2026",
                "Borrador", null, "ana@induztek.cl", null, null, null, null);
    }

    @Test
    void consultaProtocolosDeUsuario() {
        when(service.listar("ana@induztek.cl")).thenReturn(List.of(response));
        when(service.obtener(1L, "ana@induztek.cl")).thenReturn(response);

        assertEquals(List.of(response), controller.listar("ana@induztek.cl"));
        assertEquals(response, controller.obtener(1L, "ana@induztek.cl"));
        verify(service).listar("ana@induztek.cl");
        verify(service).obtener(1L, "ana@induztek.cl");
    }

    @Test
    void creaActualizaYEliminaProtocolos() {
        ProtocoloRequest request = new ProtocoloRequest("TR-01", "23/11/2026",
                "Borrador", null, "ana@induztek.cl", null, null);
        when(service.crear(request)).thenReturn(response);
        when(service.actualizar(1L, "ana@induztek.cl", request)).thenReturn(response);

        assertEquals(response, controller.crear(request));
        assertEquals(response, controller.actualizar(1L, "ana@induztek.cl", request));
        controller.eliminar(1L, "ana@induztek.cl");
        verify(service).eliminar(1L, "ana@induztek.cl");
    }
}
