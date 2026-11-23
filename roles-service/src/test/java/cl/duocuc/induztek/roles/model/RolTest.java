package cl.duocuc.induztek.roles.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolTest {

    @Test
    void exponeDatosActualizablesYMarcasDeAuditoria() throws InterruptedException {
        Rol rol = new Rol("Técnico", "Inspecciones");
        rol.alCrear();
        var creado = rol.getCreadoEn();
        var actualizado = rol.getActualizadoEn();

        rol.setNombre("Ingeniero");
        rol.setDescripcion("Gestión técnica");
        rol.setActivo(false);
        Thread.sleep(1);
        rol.alActualizar();

        assertEquals("Ingeniero", rol.getNombre());
        assertEquals("Gestión técnica", rol.getDescripcion());
        assertFalse(rol.isActivo());
        assertNotNull(creado);
        assertTrue(rol.getActualizadoEn().isAfter(actualizado));
    }
}
