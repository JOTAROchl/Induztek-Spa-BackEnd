package cl.duocuc.induztekspa.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.induztekspa.repository.ProtocoloRepository
import cl.duocuc.induztekspa.repository.UsuarioRepository

class PerfilViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPref = application.getSharedPreferences("perfil_prefs", Context.MODE_PRIVATE)
    private val usuarioRepo = UsuarioRepository(application)
    private val protocoloRepo = ProtocoloRepository(application)

    fun obtenerDato(clave: String, valorPorDefecto: String): String {
        return sharedPref.getString(clave, valorPorDefecto) ?: valorPorDefecto
    }

    fun guardarPerfilCompleto(
        correoOriginal: String,
        correoNuevo: String,
        rut: String,
        telefono: String,
        cargo: String,
        descripcion: String,
        genero: String,
        fechaNac: String,
        avatarBase64: String,
        bannerBase64: String
    ): Boolean {
        val nuevoCorreoLimpio = correoNuevo.trim().lowercase()

        // Actualizar en SQLite
        val actualizado = usuarioRepo.actualizarPerfilExtendido(correoOriginal, nuevoCorreoLimpio, rut, telefono, cargo)
        if (actualizado && nuevoCorreoLimpio != correoOriginal) {
            protocoloRepo.actualizarCorreoUsuario(correoOriginal, nuevoCorreoLimpio)
        }

        // Guardar en SharedPreferences
        sharedPref.edit()
            .putString("correo", nuevoCorreoLimpio)
            .putString("rut", rut)
            .putString("telefono", telefono)
            .putString("cargo", cargo)
            .putString("desc", descripcion)
            .putString("genero", genero)
            .putString("fecha_nac", fechaNac)
            .putString("avatar", avatarBase64)
            .putString("banner", bannerBase64)
            .apply()

        return true
    }
}
