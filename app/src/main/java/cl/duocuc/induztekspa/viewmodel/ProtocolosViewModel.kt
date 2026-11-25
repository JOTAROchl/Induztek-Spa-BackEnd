package cl.duocuc.induztekspa.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duocuc.induztekspa.data.remote.RetrofitClient
import cl.duocuc.induztekspa.model.Protocolo
import cl.duocuc.induztekspa.repository.ProtocoloRepository
import cl.duocuc.induztekspa.ui.state.ProtocoloFormUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProtocolosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProtocoloRepository(application)
    
    private val _protocolos = MutableStateFlow<List<Protocolo>>(emptyList())
    val protocolos: StateFlow<List<Protocolo>> = _protocolos.asStateFlow()

    private val _formState = MutableStateFlow(ProtocoloFormUiState())
    val formState: StateFlow<ProtocoloFormUiState> = _formState.asStateFlow()

    fun cargarProtocolos(correoUsuario: String, rol: String) {
        _protocolos.value = repository.obtenerProtocolos(correoUsuario, rol)
    }

    fun iniciarFormulario(protocolo: Protocolo?) {
        if (protocolo != null) {
            _formState.value = ProtocoloFormUiState(
                codigo = protocolo.codigo,
                equipo = protocolo.equipo,
                tipoEquipo = protocolo.tipoEquipo,
                instalacion = protocolo.instalacion,
                direccion = protocolo.direccion,
                fechaHora = protocolo.fechaHora,
                estado = protocolo.estado,
                observaciones = protocolo.observaciones,
                temperatura = protocolo.temperatura,
                humedad = protocolo.humedad,
                latitud = protocolo.latitud,
                longitud = protocolo.longitud,
                medicionValor = if (protocolo.medicionValor > 0) protocolo.medicionValor.toString() else ""
            )
        } else {
            val numCod = (100..999).random()
            _formState.value = ProtocoloFormUiState(
                codigo = "PT-2026-$numCod",
                estado = "Borrador"
            )
        }
    }

    fun onEquipoChanged(v: String) {
        _formState.update { it.copy(equipo = v, errorEquipo = null) }
    }

    fun onTipoEquipoChanged(v: String) {
        _formState.update { it.copy(tipoEquipo = v) }
    }

    fun onInstalacionChanged(v: String) {
        _formState.update { it.copy(instalacion = v) }
        buscarCoordenadasPorDireccion(v, _formState.value.direccion)
    }

    fun onDireccionChanged(v: String) {
        _formState.update { it.copy(direccion = v) }
        buscarCoordenadasPorDireccion(_formState.value.instalacion, v)
    }

    fun buscarCoordenadasPorDireccion(instalacion: String, direccion: String) {
        val consultaCompleta = listOf(direccion, instalacion, "Chile").filter { it.isNotBlank() }.joinToString(", ")
        if (consultaCompleta.isBlank() || consultaCompleta == "Chile") return

        viewModelScope.launch {
            try {
                val resultados = RetrofitClient.nominatimApi.forwardGeocode(consultaCompleta)
                if (resultados.isNotEmpty()) {
                    val primerRes = resultados[0]
                    val lat = primerRes.lat
                    val lon = primerRes.lon
                    if (lat != null && lon != null) {
                        _formState.update { it.copy(latitud = lat, longitud = lon) }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun onFechaHoraChanged(v: String) {
        _formState.update { it.copy(fechaHora = v, errorFecha = null) }
    }

    fun onEstadoChanged(v: String) {
        _formState.update { it.copy(estado = v) }
    }

    fun onObservacionesChanged(v: String) {
        _formState.update { it.copy(observaciones = v) }
    }

    fun onTemperaturaChanged(v: String) {
        _formState.update { it.copy(temperatura = v) }
    }

    fun onHumedadChanged(v: String) {
        _formState.update { it.copy(humedad = v) }
    }

    fun onUbicacionCapturada(lat: String, lng: String, onDireccionResuelta: (String) -> Unit = {}) {
        _formState.update { it.copy(latitud = lat, longitud = lng) }
        
        val latNum = lat.toDoubleOrNull()
        val lngNum = lng.toDoubleOrNull()
        
        if (latNum != null && lngNum != null) {
            viewModelScope.launch {
                try {
                    val respuesta = RetrofitClient.nominatimApi.reverseGeocode(latNum, lngNum)
                    val direccionTexto = respuesta.displayName ?: "Lat: $lat, Lng: $lng"
                    
                    val ciudadOFaena = respuesta.address?.town ?: respuesta.address?.city ?: respuesta.address?.suburb ?: ""
                    if (ciudadOFaena.isNotBlank() && _formState.value.instalacion.isBlank()) {
                        _formState.update { it.copy(instalacion = "Faena $ciudadOFaena") }
                    }
                    onDireccionResuelta(direccionTexto)
                } catch (_: Exception) {
                    onDireccionResuelta("Lat: $lat, Lng: $lng")
                }
            }
        }
    }

    fun onMedicionValorChanged(v: String) {
        _formState.update { it.copy(medicionValor = v) }
    }

    fun guardarProtocolo(
        correoUsuario: String,
        protocoloAEditar: Protocolo?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val s = _formState.value
        val errEq = if (s.equipo.isBlank()) "Campo obligatorio" else null
        val errFec = if (s.fechaHora.isBlank()) "Campo obligatorio" else null

        _formState.update { it.copy(errorEquipo = errEq, errorFecha = errFec) }

        if (errEq == null && errFec == null) {
            val medVal = s.medicionValor.toDoubleOrNull() ?: 0.0
            val p = Protocolo(
                id = protocoloAEditar?.id ?: 0,
                codigo = s.codigo,
                equipo = s.equipo.trim().uppercase(),
                tipoEquipo = s.tipoEquipo,
                instalacion = s.instalacion,
                direccion = s.direccion,
                fechaHora = s.fechaHora,
                estado = s.estado,
                observaciones = s.observaciones,
                correoUsuario = protocoloAEditar?.correoUsuario ?: correoUsuario,
                temperatura = s.temperatura,
                humedad = s.humedad,
                latitud = s.latitud,
                longitud = s.longitud,
                medicionValor = medVal
            )

            val esNuevo = protocoloAEditar == null
            val exito = if (esNuevo) {
                repository.insertarProtocolo(p) > -1
            } else {
                repository.actualizarProtocolo(p)
            }

            if (exito) {
                _formState.update { it.copy(guardadoExitoso = true) }
                onSuccess()
            } else {
                onError("Error al guardar el protocolo en base de datos.")
            }
        } else {
            onError("Corrige los campos obligatorios del formulario.")
        }
    }

    fun eliminarProtocolo(id: Int, correoUsuario: String, rol: String) {
        repository.eliminarProtocolo(id)
        cargarProtocolos(correoUsuario, rol)
    }

    fun obtenerProtocoloAnterior(equipo: String, protocoloActualId: Int): Protocolo? {
        return repository.obtenerProtocoloAnterior(equipo, protocoloActualId)
    }
}
