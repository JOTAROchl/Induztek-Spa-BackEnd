package cl.duocuc.induztek.protocols.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProtocoloTest {

    @Test
    void registraFechasDeAuditoriaYActualizaCamposSinCambiarPropietario() {
        Protocolo protocolo = new Protocolo("TR-01", LocalDate.of(2026, 11, 23),
                "Borrador", null, "ana@induztek.cl", null, null);
        protocolo.alCrear();

        protocolo.actualizar("TR-02", LocalDate.of(2026, 11, 24),
                "Formalizado", "Correcto", "20", "40");
        protocolo.alActualizar();

        assertEquals("TR-02", protocolo.getEquipo());
        assertEquals("ana@induztek.cl", protocolo.getCorreoUsuario());
        assertEquals("Formalizado", protocolo.getEstado());
        assertEquals("Correcto", protocolo.getObservaciones());
        assertEquals("20", protocolo.getTemperatura());
        assertEquals("40", protocolo.getHumedad());
        assertNotNull(protocolo.getCreadoEn());
        assertNotNull(protocolo.getActualizadoEn());
        assertEquals(LocalDate.of(2026, 11, 24), protocolo.getFecha());
    }
}
