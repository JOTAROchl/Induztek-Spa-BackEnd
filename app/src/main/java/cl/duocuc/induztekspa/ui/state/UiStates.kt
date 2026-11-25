package cl.duocuc.induztekspa.ui.state

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val mensajeErrorGeneral: String? = null,
    val esPendienteAprobacion: Boolean = false,
    val loginExitoso: Boolean = false
)

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val rut: String = "",
    val telefono: String = "",
    val cargo: String = "Técnico de Terreno",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorRut: String? = null,
    val errorContrasena: String? = null,
    val errorConfirmarContrasena: String? = null,
    val fuerzaContrasena: NivelFuerza = NivelFuerza.DEBIL,
    val registroExitoso: Boolean = false
)

enum class NivelFuerza {
    DEBIL, MEDIA, FUERTE
}

data class ProtocoloFormUiState(
    val codigo: String = "",
    val equipo: String = "",
    val tipoEquipo: String = "Transformador",
    val instalacion: String = "Planta Lampa",
    val direccion: String = "",
    val fechaHora: String = "",
    val estado: String = "Borrador",
    val observaciones: String = "",
    val temperatura: String = "",
    val humedad: String = "",
    val latitud: String = "",
    val longitud: String = "",
    val medicionValor: String = "",
    val errorEquipo: String? = null,
    val errorFecha: String? = null,
    val guardadoExitoso: Boolean = false
)
