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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
}
