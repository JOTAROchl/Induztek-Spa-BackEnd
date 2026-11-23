package cl.duocuc.induztek.users.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordConfigTest {

    @Test
    void encoderGuardaYVerificaHashBCrypt() {
        var encoder = new PasswordConfig().passwordEncoder();
        String hash = encoder.encode("Clave123!");

        assertTrue(hash.startsWith("$2"));
        assertTrue(encoder.matches("Clave123!", hash));
        assertFalse(encoder.matches("otra-clave", hash));
    }
}
