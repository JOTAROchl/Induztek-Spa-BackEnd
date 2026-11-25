package cl.duocuc.induztekspa.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import cl.duocuc.induztekspa.model.Usuario
import cl.duocuc.induztekspa.repository.UsuarioRepository
import cl.duocuc.induztekspa.ui.state.LoginUiState
import cl.duocuc.induztekspa.ui.state.NivelFuerza
import cl.duocuc.induztekspa.ui.state.RegistroUiState
import cl.duocuc.induztekspa.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UsuarioRepository(application)
    private val sharedPref = application.getSharedPreferences("perfil_prefs", Context.MODE_PRIVATE)

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registroState = MutableStateFlow(RegistroUiState())
    val registroState: StateFlow<RegistroUiState> = _registroState.asStateFlow()

    fun onCorreoLoginChanged(correo: String) {
        _loginState.update { it.copy(correo = correo, errorCorreo = null, mensajeErrorGeneral = null) }
    }

    fun onContrasenaLoginChanged(contrasena: String) {
        _loginState.update { it.copy(contrasena = contrasena, errorContrasena = null, mensajeErrorGeneral = null) }
    }

    fun ejecutarLogin(onSuccess: () -> Unit) {
        val correo = _loginState.value.correo.trim().lowercase()
        val password = _loginState.value.contrasena

        if (correo.isBlank() || password.isBlank()) {
            _loginState.update { it.copy(mensajeErrorGeneral = "Por favor, complete todos los campos.") }
            return
        }

        val usuario = repository.autenticarYObtenerUsuario(correo, password)
        if (usuario != null) {
            if (!usuario.estaAprobado) {
                _loginState.update { 
                    it.copy(
                        esPendienteAprobacion = true, 
                        mensajeErrorGeneral = "Su cuenta está registrada pero PENDIENTE DE APROBACIÓN por el Jefe Técnico."
                    ) 
                }
                return
            }

            sharedPref.edit()
                .putString("nombre", usuario.nombre)
                .putString("correo", usuario.correo)
                .putString("rol", usuario.rol)
                .putString("rut", usuario.rut)
                .putString("telefono", usuario.telefono)
                .putString("cargo", usuario.cargo)
                .apply()

            _loginState.update { it.copy(loginExitoso = true) }
            onSuccess()
        } else {
            _loginState.update { 
                it.copy(mensajeErrorGeneral = "Correo o contraseña incorrectos. Si no tienes cuenta, por favor regístrate.") 
            }
        }
    }

    fun restablecerContrasenaDirecta(
        correo: String,
        nuevaContrasena: String,
        confirmarContrasena: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        val correoLimpio = correo.trim().lowercase()
        val errorCorreo = Validaciones.validarCorreo(correoLimpio)
        if (errorCorreo != null) {
            onResultado(false, errorCorreo)
            return
        }

        val usuario = repository.buscarPorCorreo(correoLimpio)
        if (usuario == null) {
            onResultado(false, "El correo no está registrado en Induztek SpA.")
            return
        }

        val errorPass = Validaciones.validarContrasena(nuevaContrasena)
        if (errorPass != null) {
            onResultado(false, errorPass)
            return
        }

        if (nuevaContrasena != confirmarContrasena) {
            onResultado(false, "Las contraseñas no coinciden.")
            return
        }

        val exito = repository.recuperarContrasena(correoLimpio, nuevaContrasena)
        if (exito) {
            onResultado(true, "Contraseña actualizada exitosamente. Ya puedes iniciar sesión.")
        } else {
            onResultado(false, "Error al actualizar la contraseña.")
        }
    }

    fun onNombreRegistroChanged(v: String) {
        val err = Validaciones.validarNombre(v)
        _registroState.update { it.copy(nombre = v, errorNombre = err) }
    }

    fun onCorreoRegistroChanged(v: String) {
        val err = Validaciones.validarCorreo(v)
        _registroState.update { it.copy(correo = v, errorCorreo = err) }
    }

    fun onRutRegistroChanged(v: String) {
        val err = if (v.isNotBlank()) Validaciones.validarRUT(v) else null
        _registroState.update { it.copy(rut = v, errorRut = err) }
    }

    fun onTelefonoRegistroChanged(v: String) {
        _registroState.update { it.copy(telefono = v) }
    }

    fun onCargoRegistroChanged(v: String) {
        _registroState.update { it.copy(cargo = v) }
    }

    fun onContrasenaRegistroChanged(v: String) {
        val err = Validaciones.validarContrasena(v)
        val fuerza = evaluarFuerzaContrasena(v)
        _registroState.update { it.copy(contrasena = v, errorContrasena = err, fuerzaContrasena = fuerza) }
    }

    fun onConfirmarContrasenaRegistroChanged(v: String) {
        val err = if (v != _registroState.value.contrasena) "Las contraseñas no coinciden" else null
        _registroState.update { it.copy(confirmarContrasena = v, errorConfirmarContrasena = err) }
    }

    private fun evaluarFuerzaContrasena(pass: String): NivelFuerza {
        if (pass.length < 6) return NivelFuerza.DEBIL
        val tieneMayus = pass.contains(Regex("[A-Z]"))
        val tieneMinus = pass.contains(Regex("[a-z]"))
        val tieneNum = pass.contains(Regex("[0-9]"))
        val tieneEspecial = pass.contains(Regex("[^A-Za-z0-9]"))

        val puntos = listOf(tieneMayus, tieneMinus, tieneNum, tieneEspecial).count { it }
        return when {
            pass.length >= 8 && puntos >= 3 -> NivelFuerza.FUERTE
            pass.length >= 6 && puntos >= 2 -> NivelFuerza.MEDIA
            else -> NivelFuerza.DEBIL
        }
    }

    fun ejecutarRegistro(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val s = _registroState.value
        val errNom = Validaciones.validarNombre(s.nombre)
        val errCor = Validaciones.validarCorreo(s.correo)
        val errRut = if (s.rut.isNotBlank()) Validaciones.validarRUT(s.rut) else null
        val errPass = Validaciones.validarContrasena(s.contrasena)
        val errConf = if (s.contrasena != s.confirmarContrasena) "Las contraseñas no coinciden" else null

        _registroState.update { 
            it.copy(
                errorNombre = errNom,
                errorCorreo = errCor,
                errorRut = errRut,
                errorContrasena = errPass,
                errorConfirmarContrasena = errConf
            ) 
        }

        if (errNom == null && errCor == null && errRut == null && errPass == null && errConf == null) {
            val nuevo = Usuario(
                nombre = s.nombre.trim(),
                correo = s.correo.trim().lowercase(),
                contrasena = s.contrasena,
                rol = "Técnico",
                rut = s.rut.trim(),
                telefono = s.telefono.trim(),
                cargo = s.cargo.trim().ifBlank { "Técnico de Terreno" },
                estaAprobado = false
            )

            val id = repository.registrarUsuario(nuevo)
            if (id > -1) {
                _registroState.update { it.copy(registroExitoso = true) }
                onSuccess()
            } else {
                onError("El correo electrónico ya está registrado o hubo un error.")
            }
        } else {
            onError("Por favor corrige los errores del formulario.")
        }
    }
}
