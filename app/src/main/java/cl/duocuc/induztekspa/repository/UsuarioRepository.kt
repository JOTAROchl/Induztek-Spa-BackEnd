package cl.duocuc.induztekspa.repository

import android.content.Context
import cl.duocuc.induztekspa.data.UsuarioDbHelper
import cl.duocuc.induztekspa.data.remote.RetrofitClient
import cl.duocuc.induztekspa.data.remote.RolDto
import cl.duocuc.induztekspa.model.Usuario

class UsuarioRepository(context: Context) {
    private val dbHelper = UsuarioDbHelper(context)

    suspend fun loginRemote(correo: String, contrasena: String): Result<Usuario> {
        return try {
            val resp = RetrofitClient.usuarioApi.login(mapOf("correo" to correo, "contrasena" to contrasena))
            if (resp.isSuccessful && resp.body() != null) {
                Result.success(resp.body()!!)
            } else {
                val localUser = dbHelper.autenticarYObtenerUsuario(correo, contrasena)
                if (localUser != null) {
                    Result.success(localUser)
                } else {
                    Result.failure(Exception("Error de autenticación"))
                }
            }
        } catch (_: Exception) {
            val localUser = dbHelper.autenticarYObtenerUsuario(correo, contrasena)
            if (localUser != null) {
                Result.success(localUser)
            } else {
                Result.failure(Exception("Sin conexión con servidor de usuarios"))
            }
        }
    }

    suspend fun registrarUsuarioRemote(usuario: Usuario): Result<Usuario> {
        return try {
            val resp = RetrofitClient.usuarioApi.registrarUsuario(usuario)
            if (resp.isSuccessful && resp.body() != null) {
                dbHelper.insertarUsuario(usuario)
                Result.success(resp.body()!!)
            } else {
                Result.failure(Exception("Error al registrar usuario."))
            }
        } catch (_: Exception) {
            val id = dbHelper.insertarUsuario(usuario)
            if (id > -1) {
                Result.success(usuario)
            } else {
                Result.failure(Exception("Error al registrar usuario."))
            }
        }
    }

    suspend fun obtenerUsuariosRemote(): Result<List<Usuario>> {
        return try {
            val resp = RetrofitClient.usuarioApi.obtenerUsuarios()
            if (resp.isSuccessful && resp.body() != null) {
                Result.success(resp.body()!!)
            } else {
                Result.success(dbHelper.obtenerUsuarios())
            }
        } catch (_: Exception) {
            Result.success(dbHelper.obtenerUsuarios())
        }
    }

    suspend fun aprobarUsuarioRemote(id: Int): Result<Boolean> {
        return try {
            val resp = RetrofitClient.usuarioApi.aprobarUsuario(id)
            dbHelper.aprobarUsuario(id)
            Result.success(resp.isSuccessful)
        } catch (_: Exception) {
            dbHelper.aprobarUsuario(id)
            Result.success(true)
        }
    }

    suspend fun actualizarRolRemote(id: Int, rolId: Int, nuevoRol: String): Result<Boolean> {
        return try {
            val resp = RetrofitClient.usuarioApi.actualizarRol(id, mapOf("rolId" to rolId, "rol" to nuevoRol))
            dbHelper.actualizarRol(id, nuevoRol)
            Result.success(resp.isSuccessful)
        } catch (_: Exception) {
            dbHelper.actualizarRol(id, nuevoRol)
            Result.success(true)
        }
    }

    suspend fun obtenerRolesRemote(): Result<List<RolDto>> {
        return try {
            val resp = RetrofitClient.rolApi.obtenerRoles()
            if (resp.isSuccessful && resp.body() != null) {
                Result.success(resp.body()!!)
            } else {
                Result.success(listOf(RolDto(1, "Jefe técnico"), RolDto(2, "Ingeniero"), RolDto(3, "Técnico"), RolDto(4, "Cliente")))
            }
        } catch (_: Exception) {
            Result.success(listOf(RolDto(1, "Jefe técnico"), RolDto(2, "Ingeniero"), RolDto(3, "Técnico"), RolDto(4, "Cliente")))
        }
    }

    fun buscarPorCorreo(correo: String): Usuario? {
        return dbHelper.buscarPorCorreo(correo)
    }

    fun recuperarContrasena(correo: String, nuevaContrasena: String): Boolean {
        return dbHelper.recuperarContrasena(correo, nuevaContrasena)
    }

    fun actualizarPerfilExtendido(correoAntiguo: String, nuevoCorreo: String, rut: String, telefono: String, cargo: String): Boolean {
        return dbHelper.actualizarPerfilExtendido(correoAntiguo, nuevoCorreo, rut, telefono, cargo)
    }

    fun eliminarUsuario(id: Int): Boolean {
        return dbHelper.eliminarUsuario(id)
    }
}
