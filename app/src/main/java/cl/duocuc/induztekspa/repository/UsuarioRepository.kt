package cl.duocuc.induztekspa.repository

import android.content.Context
import cl.duocuc.induztekspa.data.UsuarioDbHelper
import cl.duocuc.induztekspa.model.Usuario

class UsuarioRepository(context: Context) {
    private val dbHelper = UsuarioDbHelper(context)

    fun registrarUsuario(usuario: Usuario): Long {
        return dbHelper.insertarUsuario(usuario)
    }

    fun autenticarYObtenerUsuario(correo: String, contrasena: String): Usuario? {
        return dbHelper.autenticarYObtenerUsuario(correo, contrasena)
    }

    fun buscarPorCorreo(correo: String): Usuario? {
        return dbHelper.buscarPorCorreo(correo)
    }

    fun recuperarContrasena(correo: String, nuevaContrasena: String): Boolean {
        return dbHelper.recuperarContrasena(correo, nuevaContrasena)
    }

    fun obtenerUsuarios(): List<Usuario> {
        return dbHelper.obtenerUsuarios()
    }

    fun actualizarRol(id: Int, nuevoRol: String): Boolean {
        return dbHelper.actualizarRol(id, nuevoRol)
    }

    fun aprobarUsuario(id: Int): Boolean {
        return dbHelper.aprobarUsuario(id)
    }

    fun actualizarPerfilExtendido(correoAntiguo: String, nuevoCorreo: String, rut: String, telefono: String, cargo: String): Boolean {
        return dbHelper.actualizarPerfilExtendido(correoAntiguo, nuevoCorreo, rut, telefono, cargo)
    }

    fun eliminarUsuario(id: Int): Boolean {
        return dbHelper.eliminarUsuario(id)
    }
}
