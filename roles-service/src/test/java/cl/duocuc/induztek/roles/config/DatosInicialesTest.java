package cl.duocuc.induztek.roles.config;

import cl.duocuc.induztek.roles.model.Rol;
import cl.duocuc.induztek.roles.repository.RolRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DatosInicialesTest {

    @Test
    void creaRolesInduztekFaltantesAlIniciar() throws Exception {
        RolRepository repository = mock(RolRepository.class);
        when(repository.existsByNombreIgnoreCase(any())).thenReturn(false);

        new DatosIniciales().inicializarRoles(repository).run();

        verify(repository, times(5)).save(any(Rol.class));
    }

    @Test
    void noSobrescribeRolesExistentes() throws Exception {
        RolRepository repository = mock(RolRepository.class);
        when(repository.existsByNombreIgnoreCase(any())).thenReturn(true);

        new DatosIniciales().inicializarRoles(repository).run();

        verify(repository, never()).save(any(Rol.class));
    }

    @Test
    void rolesInicialesCoincidenConLaApp() throws Exception {
        RolRepository repository = mock(RolRepository.class);
        when(repository.existsByNombreIgnoreCase(any())).thenReturn(false);
        when(repository.save(any(Rol.class))).thenAnswer(invocation -> invocation.getArgument(0));

        new DatosIniciales().inicializarRoles(repository).run();

        var roles = org.mockito.ArgumentCaptor.forClass(Rol.class);
        verify(repository, times(5)).save(roles.capture());
        List<String> nombres = roles.getAllValues().stream().map(Rol::getNombre).toList();
        org.junit.jupiter.api.Assertions.assertEquals(
                List.of("Administrador", "Jefe técnico", "Ingeniero", "Técnico", "Cliente"), nombres);
    }
}
