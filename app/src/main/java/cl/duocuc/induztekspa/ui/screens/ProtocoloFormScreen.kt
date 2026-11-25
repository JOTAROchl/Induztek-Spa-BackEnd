package cl.duocuc.induztekspa.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.induztekspa.model.Protocolo
import cl.duocuc.induztekspa.permitions.Permisos
import cl.duocuc.induztekspa.ui.components.BotonPrincipal
import cl.duocuc.induztekspa.ui.components.CampoTexto
import cl.duocuc.induztekspa.ui.components.MapaUbicacionCard
import cl.duocuc.induztekspa.ui.theme.Dimens
import cl.duocuc.induztekspa.viewmodel.ProtocolosViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtocoloFormScreen(
    correoUsuario: String,
    rolUsuario: String,
    protocoloAEditar: Protocolo?,
    onGuardadoExitoso: () -> Unit,
    viewModel: ProtocolosViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val formState by viewModel.formState.collectAsState()

    var permisoUbicacionConcedido by remember {
        mutableStateOf(Permisos.checkPermiso(context, Manifest.permission.ACCESS_FINE_LOCATION))
    }
    var intentosPermisoUbicacion by remember { mutableStateOf(0) }
    var direccionNominatim by remember { mutableStateOf("") }

    LaunchedEffect(protocoloAEditar) {
        viewModel.iniciarFormulario(protocoloAEditar)
        if (protocoloAEditar == null && formState.fechaHora.isBlank()) {
            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            viewModel.onFechaHoraChanged(fechaActual)
        }
    }

    var mostrarCalendario by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val launcherUbicacion = Permisos.rememberLocationPermissionLauncher(
        onGranted = {
            permisoUbicacionConcedido = true
            val latSimulada = "-33.2842"
            val lngSimulada = "-70.7301"
            viewModel.onUbicacionCapturada(latSimulada, lngSimulada) { direccion ->
                direccionNominatim = direccion
                Toast.makeText(context, "Geolocalización Nominatim: $direccion", Toast.LENGTH_LONG).show()
            }
        },
        onDenied = {
            permisoUbicacionConcedido = false
            intentosPermisoUbicacion++
            if (intentosPermisoUbicacion >= 3) {
                Toast.makeText(context, "Permiso de GPS denegado. Habilítalo en los Ajustes de tu teléfono para usar el mapa.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Se requiere permiso de ubicación para el mapa GPS (Intento $intentosPermisoUbicacion/3)", Toast.LENGTH_SHORT).show()
            }
        }
    )

    var expandedTipo by remember { mutableStateOf(false) }
    val opcionesTipoEquipo = listOf("Transformador", "Interruptor", "Celda MT", "Cable de poder", "Relé de protección")

    var expandedEstado by remember { mutableStateOf(false) }
    val opcionesEstado = if (rolUsuario == "Jefe técnico" || rolUsuario == "Ingeniero") {
        listOf("Borrador", "Registrado en terreno", "Observado", "Formalizado", "Anulado")
    } else {
        listOf("Borrador", "Registrado en terreno")
    }

    if (mostrarCalendario) {
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val hoy = Calendar.getInstance()
                        val seleccion = Calendar.getInstance().apply { timeInMillis = millis }

                        val esHoy = hoy.get(Calendar.YEAR) == seleccion.get(Calendar.YEAR) &&
                                    hoy.get(Calendar.DAY_OF_YEAR) == seleccion.get(Calendar.DAY_OF_YEAR)

                        if (protocoloAEditar == null && !esHoy) {
                            Toast.makeText(context, "Para nuevos protocolos la fecha debe ser del día de hoy", Toast.LENGTH_LONG).show()
                            return@TextButton
                        }

                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val horaActual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        viewModel.onFechaHoraChanged("${sdf.format(Date(millis))} $horaActual")
                    }
                    mostrarCalendario = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .verticalScroll(rememberScrollState())
            .padding(Dimens.paddingMedium)
    ) {
        Text(
            text = if (protocoloAEditar == null) "Nuevo Protocolo" else "Editar Protocolo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Código Asignado: ${formState.codigo}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        CampoTexto(
            label = "TAG del Equipo (Ej: TR-01) *",
            value = formState.equipo,
            onValueChange = { viewModel.onEquipoChanged(it) },
            isError = formState.errorEquipo != null,
            errorMessage = formState.errorEquipo
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedTipo,
            onExpandedChange = { expandedTipo = !expandedTipo }
        ) {
            OutlinedTextField(
                value = formState.tipoEquipo,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo de Equipo *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedTipo,
                onDismissRequest = { expandedTipo = false }
            ) {
                opcionesTipoEquipo.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            viewModel.onTipoEquipoChanged(opcion)
                            expandedTipo = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CampoTexto(
            label = "Lugar / Ciudad de la Prueba (Ej: Planta Lampa)",
            value = formState.instalacion,
            onValueChange = { viewModel.onInstalacionChanged(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        CampoTexto(
            label = "Dirección / Número (Ej: Av. La Montaña 1234)",
            value = formState.direccion,
            onValueChange = { viewModel.onDireccionChanged(it) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box {
            OutlinedTextField(
                value = formState.fechaHora,
                onValueChange = { },
                readOnly = true,
                label = { Text("Fecha y Hora de Prueba *") },
                modifier = Modifier.fillMaxWidth(),
                isError = formState.errorFecha != null,
                trailingIcon = {
                    IconButton(onClick = { mostrarCalendario = true }) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = null)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        MapaUbicacionCard(
            latitud = formState.latitud,
            longitud = formState.longitud,
            direccionNominatim = direccionNominatim,
            permisoHabilitado = permisoUbicacionConcedido,
            onCapturarGps = {
                if (intentosPermisoUbicacion >= 3 && !permisoUbicacionConcedido) {
                    Toast.makeText(context, "Permiso GPS denegado. Para usar el mapa debes activarlo en los Ajustes de tu teléfono.", Toast.LENGTH_LONG).show()
                } else {
                    Permisos.solicitarPermisoUbicacion(
                        context = context,
                        launcher = launcherUbicacion,
                        onGranted = {
                            permisoUbicacionConcedido = true
                            viewModel.onUbicacionCapturada("-33.2842", "-70.7301") { direccion ->
                                direccionNominatim = direccion
                            }
                        }
                    )
                }
            },
            onMoverPin = { latNueva, lngNueva ->
                viewModel.onUbicacionCapturada(
                    String.format(Locale.US, "%.4f", latNueva),
                    String.format(Locale.US, "%.4f", lngNueva)
                ) { direccion ->
                    direccionNominatim = direccion
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.weight(1f)) {
                CampoTexto(
                    label = "T° Amb. (°C)",
                    value = formState.temperatura,
                    onValueChange = { viewModel.onTemperaturaChanged(it) }
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                CampoTexto(
                    label = "Humedad (%)",
                    value = formState.humedad,
                    onValueChange = { viewModel.onHumedadChanged(it) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            AssistChip(onClick = { viewModel.onTemperaturaChanged("18 °C") }, label = { Text("18 °C") })
            AssistChip(onClick = { viewModel.onTemperaturaChanged("20 °C") }, label = { Text("20 °C") })
            AssistChip(onClick = { viewModel.onHumedadChanged("45 %") }, label = { Text("45 %") })
            AssistChip(onClick = { viewModel.onHumedadChanged("55 %") }, label = { Text("55 %") })
        }

        Spacer(modifier = Modifier.height(8.dp))

        CampoTexto(
            label = "Lectura de Resistencia / Medición (MΩ)",
            value = formState.medicionValor,
            onValueChange = { viewModel.onMedicionValorChanged(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            AssistChip(onClick = { viewModel.onMedicionValorChanged("6100") }, label = { Text("6.1 GΩ (6100)") })
            AssistChip(onClick = { viewModel.onMedicionValorChanged("9800") }, label = { Text("9.8 GΩ (9800)") })
            AssistChip(onClick = { viewModel.onMedicionValorChanged("12500") }, label = { Text("12.5 GΩ (12500)") })
        }

        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedEstado,
            onExpandedChange = { expandedEstado = !expandedEstado }
        ) {
            OutlinedTextField(
                value = formState.estado,
                onValueChange = {},
                readOnly = true,
                label = { Text("Estado del Protocolo *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEstado) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedEstado,
                onDismissRequest = { expandedEstado = false }
            ) {
                opcionesEstado.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            viewModel.onEstadoChanged(opcion)
                            expandedEstado = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        CampoTexto(
            label = "Observaciones",
            value = formState.observaciones,
            onValueChange = { viewModel.onObservacionesChanged(it) }
        )

        Spacer(modifier = Modifier.height(Dimens.paddingExtraLarge))

        BotonPrincipal(
            text = if (protocoloAEditar == null) "Guardar Protocolo" else "Actualizar Protocolo",
            onClick = {
                viewModel.guardarProtocolo(
                    correoUsuario = correoUsuario,
                    protocoloAEditar = protocoloAEditar,
                    onSuccess = {
                        Toast.makeText(context, "Protocolo guardado exitosamente", Toast.LENGTH_SHORT).show()
                        onGuardadoExitoso()
                    },
                    onError = { msj ->
                        Toast.makeText(context, msj, Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}
