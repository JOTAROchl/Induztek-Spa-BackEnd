package cl.duocuc.induztek.roles.controller;

import cl.duocuc.induztek.roles.dto.RolRequest;
import cl.duocuc.induztek.roles.dto.RolResponse;
import cl.duocuc.induztek.roles.service.RolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RolControllerTest {

    private RolService service;
    private RolController controller;
    private RolResponse response;

    @BeforeEach
    void setUp() {
        service = mock(RolService.class);
        controller = new RolController(service);
        response = new RolResponse(1L, "Técnico", null, true, null, null);
    }

    @Test
    void consultaRolesYObtienePorId() {
        when(service.listar()).thenReturn(List.of(response));
        when(service.obtener(1L)).thenReturn(response);

        assertEquals(List.of(response), controller.listar());
        assertEquals(response, controller.obtener(1L));
        verify(service).listar();
        verify(service).obtener(1L);
    }

    @Test
    void creaActualizaYDesactivaRoles() {
        RolRequest request = new RolRequest("Técnico", "Inspección");
        when(service.crear(request)).thenReturn(response);
        when(service.actualizar(1L, request)).thenReturn(response);

        assertEquals(response, controller.crear(request));
        assertEquals(response, controller.actualizar(1L, request));
        controller.desactivar(1L);
        verify(service).desactivar(1L);
    }
}
