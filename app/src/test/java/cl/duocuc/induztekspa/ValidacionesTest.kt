package cl.duocuc.induztekspa

import cl.duocuc.induztekspa.utils.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidacionesTest {

    @Test
    fun validarRUT_rutValido_retornaNull() {
        val resultado = Validaciones.validarRUT("13.871.905-7")
        assertNull(resultado)
    }

    @Test
    fun validarRUT_rutInvalido_retornaMensajeError() {
        val resultado = Validaciones.validarRUT("12.345.678-0")
        assertNotNull(resultado)
        assertEquals("Dígito verificador incorrecto", resultado)
    }

    @Test
    fun validarRUT_rutCorto_retornaErrorLongitud() {
        val resultado = Validaciones.validarRUT("1-9")
        assertNotNull(resultado)
        assertEquals("RUT demasiado corto", resultado)
    }

    @Test
    fun validarCorreo_dominioPermitido_retornaNull() {
        val resultado1 = Validaciones.validarCorreo("tecnico@induztek.cl")
        val resultado2 = Validaciones.validarCorreo("usuario@gmail.com")
        assertNull(resultado1)
        assertNull(resultado2)
    }

    @Test
    fun validarCorreo_dominioNoPermitido_retornaError() {
        val resultado = Validaciones.validarCorreo("usuario@hotmail.com")
        assertNotNull(resultado)
        assertEquals("Dominio no permitido. Use @induztek.cl, @gmail.com o .ejemplo", resultado)
    }

    @Test
    fun validarContrasena_claveSegura_retornaNull() {
        val resultado = Validaciones.validarContrasena("Induztek2026!")
        assertNull(resultado)
    }

    @Test
    fun validarContrasena_sinCaracterEspecial_retornaError() {
        val resultado = Validaciones.validarContrasena("Induztek2026")
        assertNotNull(resultado)
        assertEquals("La contraseña debe contener al menos 1 carácter especial.", resultado)
    }
}
