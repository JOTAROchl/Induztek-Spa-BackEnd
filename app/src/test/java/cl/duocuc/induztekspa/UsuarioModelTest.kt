package cl.duocuc.induztekspa

import cl.duocuc.induztekspa.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsuarioModelTest {

    @Test
    fun usuarioNuevo_porDefecto_requiereAprobacionyTieneRolTecnico() {
        val u = Usuario(
            nombre = "Juan Pérez",
            correo = "juan@induztek.cl",
            contrasena = "Pass123!",
            rut = "12.345.678-9",
            telefono = "+56 9 1111 2222",
            cargo = "Técnico de Terreno"
        )
        assertEquals("Técnico", u.rol)
        assertFalse(u.estaAprobado)
    }

    @Test
    fun usuarioAdmin_porDefecto_estaAprobado() {
        val admin = Usuario(
            nombre = "Administrador Principal",
            correo = "admin@induztek.cl",
            contrasena = "Admin123!",
            rol = "Jefe técnico",
            estaAprobado = true
        )
        assertEquals("Jefe técnico", admin.rol)
        assertTrue(admin.estaAprobado)
    }
}
