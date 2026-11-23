package cl.duocuc.induztek.protocols.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProtocoloRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void aceptaCamposDeInspeccionConFormatoDeLaApp() {
        ProtocoloRequest request = new ProtocoloRequest(" TR-01 ", "23/11/2026",
                "Registrado en terreno", "", "ana@induztek.cl", "22.5", "45");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rechazaDatosObligatoriosEstadoFechaYCorreoInvalidos() {
        ProtocoloRequest request = new ProtocoloRequest(" ", "2026-11-23",
                "Cerrado", null, "correo-invalido", null, null);

        assertFalse(validator.validate(request).isEmpty());
    }
}
