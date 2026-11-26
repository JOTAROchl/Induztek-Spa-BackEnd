package cl.duocuc.induztekspa.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.induztekspa.data.remote.RolDto
import cl.duocuc.induztekspa.model.Usuario
import cl.duocuc.induztekspa.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsuariosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UsuarioRepository(application)
    
    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _roles = MutableStateFlow<List<RolDto>>(emptyList())
    val roles: StateFlow<List<RolDto>> = _roles.asStateFlow()

    fun cargarUsuarios() {
        viewModelScope.launch {
            val res = repository.obtenerUsuariosRemote()
            if (res.isSuccess) {
                _usuarios.value = res.getOrDefault(emptyList())
            }
        }
        cargarRoles()
    }

    fun cargarRoles() {
        viewModelScope.launch {
            val res = repository.obtenerRolesRemote()
            if (res.isSuccess) {
                _roles.value = res.getOrDefault(emptyList())
            }
        }
    }

    fun aprobarUsuario(id: Int) {
        viewModelScope.launch {
            repository.aprobarUsuarioRemote(id)
            cargarUsuarios()
        }
    }

    fun actualizarRol(id: Int, nuevoRol: String) {
        val rolObj = _roles.value.find { it.nombre.equals(nuevoRol, ignoreCase = true) }
        val rolId = rolObj?.id ?: 3
        viewModelScope.launch {
            repository.actualizarRolRemote(id, rolId, nuevoRol)
            cargarUsuarios()
        }
    }

    fun eliminarUsuario(id: Int) {
        repository.eliminarUsuario(id)
        cargarUsuarios()
    }
}
