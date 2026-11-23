package cl.duocuc.induztek.users.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    @Test
    void conservaCamposYActualizaFechasDeAuditoria() throws InterruptedException {
        Usuario usuario = new Usuario("Ana", "ana@induztek.cl", "hash",
                "12345678-5", "+569", "Técnico", 4L);
        usuario.alCrear();
        var creado = usuario.getCreadoEn();
        var actualizado = usuario.getActualizadoEn();

        usuario.setNombre("Ana Torres");
        usuario.setCorreo("ana.torres@induztek.cl");
        usuario.setContrasenaHash("nuevo-hash");
        usuario.setRut("87654321-0");
        usuario.setTelefono("+568");
        usuario.setCargo("Ingeniera");
        usuario.setRolId(3L);
        usuario.setAprobado(true);
        usuario.setActivo(false);
        Thread.sleep(1);
        usuario.alActualizar();

        assertEquals("Ana Torres", usuario.getNombre());
        assertEquals("ana.torres@induztek.cl", usuario.getCorreo());
        assertEquals("nuevo-hash", usuario.getContrasenaHash());
        assertEquals("87654321-0", usuario.getRut());
        assertEquals("+568", usuario.getTelefono());
        assertEquals("Ingeniera", usuario.getCargo());
        assertEquals(3L, usuario.getRolId());
        assertTrue(usuario.isAprobado());
        assertFalse(usuario.isActivo());
        assertNotNull(creado);
        assertTrue(usuario.getActualizadoEn().isAfter(actualizado));
    }
}
