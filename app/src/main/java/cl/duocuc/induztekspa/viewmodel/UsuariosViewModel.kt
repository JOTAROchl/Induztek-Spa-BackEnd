package cl.duocuc.induztekspa.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.induztekspa.model.Usuario
import cl.duocuc.induztekspa.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UsuariosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UsuarioRepository(application)
    
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    fun cargarUsuarios() {
        _usuarios.value = repository.obtenerUsuarios()
    }

    fun aprobarUsuario(id: Int) {
        repository.aprobarUsuario(id)
        cargarUsuarios()
    }

    fun actualizarRol(id: Int, nuevoRol: String) {
        repository.actualizarRol(id, nuevoRol)
        cargarUsuarios()
    }

    fun eliminarUsuario(id: Int) {
        repository.eliminarUsuario(id)
        cargarUsuarios()
    }
}
