package cl.duocuc.induztek.users.service;

import cl.duocuc.induztek.users.client.RolClient;
import cl.duocuc.induztek.users.dto.ActualizarUsuarioRequest;
import cl.duocuc.induztek.users.dto.CambiarContrasenaRequest;
import cl.duocuc.induztek.users.dto.LoginRequest;
import cl.duocuc.induztek.users.dto.RegistroRequest;
import cl.duocuc.induztek.users.dto.RolDto;
import cl.duocuc.induztek.users.model.Usuario;
import cl.duocuc.induztek.users.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private UsuarioRepository repository;
    private RolClient rolClient;
    private PasswordEncoder passwordEncoder;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        repository = mock(UsuarioRepository.class);
        rolClient = mock(RolClient.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UsuarioService(repository, rolClient, passwordEncoder);
    }

    @Test
    void registrarUsaRolTecnicoPendienteYAlmacenaHash() {
        when(repository.existsByCorreoIgnoreCase("ana@induztek.cl")).thenReturn(false);
        when(rolClient.rolTecnicoPredeterminado()).thenReturn(new RolDto(4L, "Técnico", null, true));
        when(passwordEncoder.encode("Clave123!")).thenReturn("bcrypt-hash");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.registrar(new RegistroRequest(
                " Ana Torres ", "ANA@INDUZTEK.CL", "Clave123!", "12345678-5", " +569 ",
                " "));

        assertEquals("ana@induztek.cl", response.correo());
        assertEquals("Técnico", response.rolNombre());
        assertFalse(response.aprobado());
        verify(repository).save(org.mockito.ArgumentMatchers.argThat(usuario ->
                usuario.getContrasenaHash().equals("bcrypt-hash")
                        && usuario.getRolId().equals(4L)
                        && usuario.getTelefono().equals("+569")
                        && usuario.getCargo().equals("Técnico de Terreno")
                        && !usuario.isAprobado()));
    }

    @Test
    void registrarRechazaCorreoDuplicadoSinConsultarRoles() {
        when(repository.existsByCorreoIgnoreCase("repetido@induztek.cl")).thenReturn(true);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.registrar(new RegistroRequest(
                        "Ana Torres", "repetido@induztek.cl", "Clave123!", null, null, null)));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(rolClient, never()).rolTecnicoPredeterminado();
    }

    @Test
    void registroRechazaContrasenasQueExcedenElLimiteDeBytesDeBCrypt() {
        when(repository.existsByCorreoIgnoreCase("ana@induztek.cl")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.registrar(new RegistroRequest(
                        "Ana Torres", "ana@induztek.cl", "á".repeat(37), null, null, null)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(rolClient, never()).rolTecnicoPredeterminado();
    }

    @Test
    void listarFiltraYResuelveNombreDeRolSinConsultarBaseAjena() {
        Usuario usuario = usuario();
        when(repository.findAllByActivoTrueAndNombreContainingIgnoreCaseOrActivoTrueAndCorreoContainingIgnoreCaseOrderByNombreAsc(
                "ana", "ana")).thenReturn(List.of(usuario));
        when(rolClient.listarActivos()).thenReturn(List.of(new RolDto(4L, "Técnico", null, true)));

        var result = service.listar(" ana ");

        assertEquals(1, result.size());
        assertEquals("Técnico", result.getFirst().rolNombre());
        verify(rolClient).listarActivos();
    }

    @Test
    void listarSinFiltroConservaUsuariosConRolNoDisponible() {
        when(repository.findAllByOrderByAprobadoAscNombreAsc()).thenReturn(List.of(usuario()));
        when(rolClient.listarActivos()).thenReturn(List.of());

        var result = service.listar("  ");

        assertEquals("Rol no disponible", result.getFirst().rolNombre());
    }

    @Test
    void obtenerIdInexistenteDevuelveNotFound() {
        when(repository.findByIdAndActivoTrue(50L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.obtener(50L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void buscarPorCorreoInexistenteDevuelveNotFoundParaOtrosServicios() {
        when(repository.findByCorreoIgnoreCaseAndActivoTrue("ana@induztek.cl")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.obtenerPorCorreo(" ANA@INDUZTEK.CL "));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void loginValidoDevuelveEstadoPendienteSinExponerHash() {
        Usuario usuario = usuario();
        when(repository.findByCorreoIgnoreCaseAndActivoTrue("ana@induztek.cl")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Clave123!", "hash-anterior")).thenReturn(true);
        when(rolClient.obtenerActivo(4L)).thenReturn(new RolDto(4L, "Técnico", null, true));

        var response = service.iniciarSesion(new LoginRequest("ANA@INDUZTEK.CL", "Clave123!"));

        assertFalse(response.aprobado());
        assertEquals("Técnico", response.rolNombre());
        assertFalse(response.toString().contains("hash-anterior"));
    }

    @Test
    void loginConClaveIncorrectaDevuelve401() {
        when(repository.findByCorreoIgnoreCaseAndActivoTrue("ana@induztek.cl")).thenReturn(Optional.of(usuario()));
        when(passwordEncoder.matches("incorrecta", "hash-anterior")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.iniciarSesion(new LoginRequest("ana@induztek.cl", "incorrecta")));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void actualizarNormalizaCorreoYRechazaDuplicado() {
        Usuario usuario = usuario();
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(usuario));
        when(repository.existsByCorreoIgnoreCase("otra@induztek.cl")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.actualizar(1L, new ActualizarUsuarioRequest(
                        "Ana", "otra@induztek.cl", null, null, null)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void actualizarPerfilGuardaLosCamposNormalizados() {
        Usuario usuario = usuario();
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(usuario)).thenReturn(usuario);
        when(rolClient.obtenerActivo(4L)).thenReturn(new RolDto(4L, "Técnico", null, true));

        var response = service.actualizar(1L, new ActualizarUsuarioRequest(
                " Ana ", " ANA@INDUZTEK.CL ", " ", " +569 ", ""));

        assertEquals("Ana", response.nombre());
        assertEquals("ana@induztek.cl", response.correo());
        assertEquals(null, response.rut());
        assertEquals("Técnico de Terreno", response.cargo());
    }

    @Test
    void cambiarRolYEstadoDeAprobacionPersistenCambios() {
        Usuario usuario = usuario();
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(usuario));
        when(rolClient.obtenerActivo(3L)).thenReturn(new RolDto(3L, "Ingeniero", null, true));
        when(repository.save(usuario)).thenReturn(usuario);

        var rolActualizado = service.cambiarRol(1L, 3L);
        var aprobado = service.cambiarAprobacion(1L, true);

        assertEquals("Ingeniero", rolActualizado.rolNombre());
        assertTrue(aprobado.aprobado());
        verify(repository, org.mockito.Mockito.times(2)).save(usuario);
    }

    @Test
    void cambioContrasenaRequiereClaveActualYAlmacenaHashNuevo() {
        Usuario usuario = usuario();
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave-mala", "hash-anterior")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                service.cambiarContrasena(1L, new CambiarContrasenaRequest("clave-mala", "Nueva123!")));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        when(passwordEncoder.matches("Clave123!", "hash-anterior")).thenReturn(true);
        when(passwordEncoder.encode("Nueva123!")).thenReturn("hash-nuevo");
        when(repository.save(usuario)).thenReturn(usuario);
        service.cambiarContrasena(1L, new CambiarContrasenaRequest("Clave123!", "Nueva123!"));

        assertEquals("hash-nuevo", usuario.getContrasenaHash());
        verify(repository).save(usuario);
    }

    @Test
    void desactivarMarcaUsuarioInactivoSinBorrarlo() {
        Usuario usuario = usuario();
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(usuario));
        when(repository.save(usuario)).thenReturn(usuario);

        service.desactivar(1L);

        assertFalse(usuario.isActivo());
        verify(repository).save(usuario);
        verify(repository, never()).delete(any(Usuario.class));
    }

    private Usuario usuario() {
        return new Usuario("Ana", "ana@induztek.cl", "hash-anterior",
                "12345678-5", "123", "Técnico", 4L);
    }
}
