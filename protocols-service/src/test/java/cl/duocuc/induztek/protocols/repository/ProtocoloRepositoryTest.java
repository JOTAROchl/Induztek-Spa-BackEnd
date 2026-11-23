package cl.duocuc.induztek.protocols.repository;

import cl.duocuc.induztek.protocols.model.Protocolo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ProtocoloRepositoryTest {

    @Autowired
    private ProtocoloRepository repository;

    @Test
    void listaSoloAlPropietarioEnOrdenDeFechaDescendente() {
        repository.save(new Protocolo("TR-01", LocalDate.of(2026, 1, 1),
                "Borrador", null, "ana@induztek.cl", null, null));
        repository.save(new Protocolo("TR-02", LocalDate.of(2026, 3, 1),
                "Formalizado", null, "ANA@INDUZTEK.CL", null, null));
        repository.save(new Protocolo("TR-03", LocalDate.of(2026, 2, 1),
                "Observado", null, "otra@induztek.cl", null, null));

        var result = repository.findAllByCorreoUsuarioIgnoreCaseOrderByFechaDescIdDesc("ana@induztek.cl");

        assertEquals(2, result.size());
        assertEquals("TR-02", result.getFirst().getEquipo());
        assertEquals("TR-01", result.getLast().getEquipo());
    }

    @Test
    void buscaPorIdYPropietarioSinExponerProtocolosDeOtroUsuario() {
        Protocolo protocolo = repository.save(new Protocolo("TR-01", LocalDate.of(2026, 1, 1),
                "Borrador", null, "ana@induztek.cl", null, null));

        assertTrue(repository.findByIdAndCorreoUsuarioIgnoreCase(protocolo.getId(), "otra@induztek.cl").isEmpty());
        assertTrue(repository.findByIdAndCorreoUsuarioIgnoreCase(protocolo.getId(), "ANA@INDUZTEK.CL").isPresent());
    }
}
