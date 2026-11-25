package cl.duocuc.induztekspa

import cl.duocuc.induztekspa.model.Protocolo
import cl.duocuc.induztekspa.ui.state.ProtocoloFormUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ProtocoloFormUiStateTest {

    @Test
    fun protocoloFormUiState_valoresIniciales_estadoCorrecto() {
        val state = ProtocoloFormUiState()
        assertEquals("", state.equipo)
        assertEquals("Transformador", state.tipoEquipo)
        assertEquals("Borrador", state.estado)
        assertNull(state.errorEquipo)
        assertFalse(state.guardadoExitoso)
    }

    @Test
    fun protocolo_calculoVariacionPorcentaje_mayorA25PorcientoActivaAlerta() {
        val pAnterior = Protocolo(id = 1, codigo = "PT-01", equipo = "TR-01", fechaHora = "01/01/2025", correoUsuario = "admin@induztek.cl", medicionValor = 10000.0)
        val pActual = Protocolo(id = 2, codigo = "PT-02", equipo = "TR-01", fechaHora = "02/01/2025", correoUsuario = "admin@induztek.cl", medicionValor = 6000.0)

        val previa = pAnterior.medicionValor
        val actual = pActual.medicionValor
        val variacion = ((actual - previa) / previa) * 100

        assertEquals(-40.0, variacion, 0.01)
        assertTrue(abs(variacion) >= 25.0)
    }

    @Test
    fun protocolo_codigoAsignadoFormatoCorrecto() {
        val p = Protocolo(codigo = "PT-2026-999", equipo = "CB-02", fechaHora = "15/05/2026 14:00", correoUsuario = "tecnico@induztek.cl")
        assertTrue(p.codigo.startsWith("PT-2026-"))
        assertEquals("CB-02", p.equipo)
    }
}
