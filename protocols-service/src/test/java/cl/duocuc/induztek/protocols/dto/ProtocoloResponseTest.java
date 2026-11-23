package cl.duocuc.induztek.protocols.dto;

import cl.duocuc.induztek.protocols.model.Protocolo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProtocoloResponseTest {

    @Test
    void representaLaFechaEnFormatoCompatibleConLaApp() {
        Protocolo protocolo = new Protocolo("TR-01", LocalDate.of(2026, 11, 23),
                "Registrado en terreno", null, "ana@induztek.cl", null, null);

        ProtocoloResponse response = ProtocoloResponse.from(protocolo);

        assertEquals("23/11/2026", response.fecha());
        assertEquals("ana@induztek.cl", response.correoUsuario());
    }
}
