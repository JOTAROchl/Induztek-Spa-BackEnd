package cl.duocuc.induztek.roles.service;

import cl.duocuc.induztek.roles.dto.RolRequest;
import cl.duocuc.induztek.roles.model.Rol;
import cl.duocuc.induztek.roles.repository.RolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RolServiceTest {

    private RolRepository repository;
    private RolService service;

    @BeforeEach
    void setUp() {
        repository = mock(RolRepository.class);
        service = new RolService(repository);
    }

    @Test
    void crearRechazaNombresDuplicadosIgnorandoMayusculas() {
        when(repository.existsByNombreIgnoreCase("Técnico")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.crear(new RolRequest(" Técnico ", null)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(repository, never()).save(any(Rol.class));
    }

    @Test
    void obtenerInformaCuandoElRolNoExisteOEstaInactivo() {
        when(repository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.obtener(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void desactivarEsBorradoLogico() {
        Rol rol = new Rol("Técnico", "Inspecciones");
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(rol));
        when(repository.save(rol)).thenReturn(rol);

        service.desactivar(1L);

        assertFalse(rol.isActivo());
        verify(repository).save(rol);
    }

    @Test
    void listarDevuelveSoloRolesActivos() {
        when(repository.findAllByActivoTrueOrderByNombreAsc())
                .thenReturn(java.util.List.of(new Rol("Técnico", "Inspección")));

        var response = service.listar();

        assertEquals(1, response.size());
        assertEquals("Técnico", response.getFirst().nombre());
    }

    @Test
    void crearNormalizaNombreYDescripcionOpcional() {
        when(repository.existsByNombreIgnoreCase("Técnico")).thenReturn(false);
        when(repository.save(any(Rol.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.crear(new RolRequest(" Técnico ", " Inspección "));

        assertEquals("Técnico", response.nombre());
        assertEquals("Inspección", response.descripcion());
    }

    @Test
    void crearAceptaDescripcionVaciaComoNula() {
        when(repository.existsByNombreIgnoreCase("Cliente")).thenReturn(false);
        when(repository.save(any(Rol.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.crear(new RolRequest("Cliente", " "));

        assertNull(response.descripcion());
    }

    @Test
    void actualizarPermiteConservarNombreIgnorandoMayusculas() {
        Rol rol = new Rol("Técnico", "Anterior");
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(rol));
        when(repository.save(rol)).thenReturn(rol);

        var response = service.actualizar(1L, new RolRequest("técnico", null));

        assertEquals("técnico", response.nombre());
        assertNull(response.descripcion());
        verify(repository, never()).existsByNombreIgnoreCase("técnico");
    }

    @Test
    void actualizarRechazaNombreUtilizadoPorOtroRol() {
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(new Rol("Técnico", null)));
        when(repository.existsByNombreIgnoreCase("Ingeniero")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.actualizar(1L, new RolRequest("Ingeniero", null)));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(repository, never()).save(any(Rol.class));
    }

    @Test
    void desactivarNoGuardaSiElRolNoExiste() {
        when(repository.findByIdAndActivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.desactivar(99L));

        verify(repository, never()).save(any(Rol.class));
    }

    @Test
    void actualizarNombreDisponibleGuardaElCambio() {
        Rol rol = new Rol("Técnico", "Anterior");
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(rol));
        when(repository.existsByNombreIgnoreCase("Ingeniero")).thenReturn(false);
        when(repository.save(rol)).thenReturn(rol);

        var response = service.actualizar(1L, new RolRequest("Ingeniero", "Gestión"));

        assertEquals("Ingeniero", response.nombre());
        assertEquals("Gestión", response.descripcion());
        verify(repository, times(1)).existsByNombreIgnoreCase("Ingeniero");
    }
}
