package cl.duocuc.induztekspa

import cl.duocuc.induztekspa.ui.state.LoginUiState
import cl.duocuc.induztekspa.ui.state.NivelFuerza
import cl.duocuc.induztekspa.ui.state.RegistroUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class AuthUiStateTest {

    @Test
    fun loginUiState_valoresPorDefecto_estadoInicialCorrecto() {
        val state = LoginUiState()
        assertEquals("", state.correo)
        assertEquals("", state.contrasena)
        assertNull(state.errorCorreo)
        assertNull(state.errorContrasena)
        assertFalse(state.loginExitoso)
        assertFalse(state.esPendienteAprobacion)
    }

    @Test
    fun registroUiState_evaluacionFuerzaContrasena_asignaNivelFuerza() {
        val stateDebil = RegistroUiState(fuerzaContrasena = NivelFuerza.DEBIL)
        val stateFuerte = RegistroUiState(fuerzaContrasena = NivelFuerza.FUERTE)

        assertEquals(NivelFuerza.DEBIL, stateDebil.fuerzaContrasena)
        assertEquals(NivelFuerza.FUERTE, stateFuerte.fuerzaContrasena)
    }

    @Test
    fun registroUiState_confirmacionContrasenaErronia_mantieneError() {
        val state = RegistroUiState(
            contrasena = "Clave123!",
            confirmarContrasena = "Clave456!",
            errorConfirmarContrasena = "Las contraseñas no coinciden"
        )
        assertEquals("Las contraseñas no coinciden", state.errorConfirmarContrasena)
    }
}
