package cl.duocuc.induztek.users.controller;

import cl.duocuc.induztek.users.dto.ActualizarUsuarioRequest;
import cl.duocuc.induztek.users.dto.AprobacionRequest;
import cl.duocuc.induztek.users.dto.CambiarContrasenaRequest;
import cl.duocuc.induztek.users.dto.CambiarRolRequest;
import cl.duocuc.induztek.users.dto.LoginRequest;
import cl.duocuc.induztek.users.dto.RegistroRequest;
import cl.duocuc.induztek.users.dto.UsuarioResponse;
import cl.duocuc.induztek.users.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioControllerTest {

    private UsuarioService service;
    private UsuarioController controller;
    private UsuarioResponse response;

    @BeforeEach
    void setUp() {
        service = mock(UsuarioService.class);
        controller = new UsuarioController(service);
        response = new UsuarioResponse(1L, "Ana", "ana@induztek.cl", null,
                "123", "Técnico", 4L, "Técnico", false, null, null);
    }

    @Test
    void consultasDeleganEnElServicio() {
        when(service.listar("ana")).thenReturn(List.of(response));
        when(service.obtener(1L)).thenReturn(response);
        when(service.obtenerPorCorreo("ana@induztek.cl")).thenReturn(response);

        assertEquals(1, controller.listar("ana").size());
        assertEquals(response, controller.obtener(1L));
        assertEquals(response, controller.obtenerPorCorreo("ana@induztek.cl"));
        verify(service).listar("ana");
        verify(service).obtener(1L);
        verify(service).obtenerPorCorreo("ana@induztek.cl");
    }

    @Test
    void registroYLoginDeleganSusSolicitudes() {
        RegistroRequest registro = new RegistroRequest(
                "Ana Torres", "ana@induztek.cl", "Clave123!", null, null, null);
        LoginRequest login = new LoginRequest("ana@induztek.cl", "Clave123!");
        when(service.registrar(registro)).thenReturn(response);
        when(service.iniciarSesion(login)).thenReturn(response);

        assertEquals(response, controller.registrar(registro));
        assertEquals(response, controller.iniciarSesion(login));
        verify(service).registrar(registro);
        verify(service).iniciarSesion(login);
    }

    @Test
    void actualizacionesDeleganRolAprobacionContrasenaYDesactivacion() {
        ActualizarUsuarioRequest actualizacion = new ActualizarUsuarioRequest(
                "Ana", "ana@induztek.cl", null, null, null);
        CambiarRolRequest rol = new CambiarRolRequest(3L);
        AprobacionRequest aprobacion = new AprobacionRequest(true);
        CambiarContrasenaRequest contrasena = new CambiarContrasenaRequest("anterior", "nueva1");
        when(service.actualizar(1L, actualizacion)).thenReturn(response);
        when(service.cambiarRol(1L, 3L)).thenReturn(response);
        when(service.cambiarAprobacion(1L, true)).thenReturn(response);

        assertEquals(response, controller.actualizar(1L, actualizacion));
        assertEquals(response, controller.cambiarRol(1L, rol));
        assertEquals(response, controller.cambiarAprobacion(1L, aprobacion));
        controller.cambiarContrasena(1L, contrasena);
        controller.desactivar(1L);
        verify(service).cambiarContrasena(1L, contrasena);
        verify(service).desactivar(1L);
    }
}
