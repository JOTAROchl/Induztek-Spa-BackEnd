package cl.duocuc.induztek.roles.dto;

import cl.duocuc.induztek.roles.model.Rol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolResponseTest {

    @Test
    void convierteEntidadDeDominioEnRespuesta() {
        Rol rol = new Rol("Técnico", "Inspecciones");

        RolResponse response = RolResponse.from(rol);

        assertEquals("Técnico", response.nombre());
        assertEquals("Inspecciones", response.descripcion());
        assertTrue(response.activo());
    }
}
