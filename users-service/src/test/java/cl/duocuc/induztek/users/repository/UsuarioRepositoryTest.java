package cl.duocuc.induztek.users.repository;

import cl.duocuc.induztek.users.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    @Test
    void busquedaPorNombreOCorreoIncluyeSoloCuentasActivas() {
        Usuario activa = new Usuario("Ana Torres", "ana@induztek.cl", "hash",
                null, null, "Técnico", 4L);
        Usuario desactivada = new Usuario("Ana Desactivada", "inactiva@induztek.cl", "hash",
                null, null, "Técnico", 4L);
        desactivada.setActivo(false);
        repository.saveAll(List.of(activa, desactivada));

        List<Usuario> encontrados =
                repository.findAllByActivoTrueAndNombreContainingIgnoreCaseOrActivoTrueAndCorreoContainingIgnoreCaseOrderByNombreAsc(
                        "ana", "ana");

        assertEquals(1, encontrados.size());
        assertEquals("ana@induztek.cl", encontrados.getFirst().getCorreo());
    }
}
