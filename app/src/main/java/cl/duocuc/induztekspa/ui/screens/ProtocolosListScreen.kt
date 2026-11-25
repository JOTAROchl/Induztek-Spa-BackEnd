package cl.duocuc.induztekspa.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.induztekspa.model.Protocolo
import cl.duocuc.induztekspa.ui.components.MapaUbicacionCard
import cl.duocuc.induztekspa.ui.styles.EstilosTarjeta
import cl.duocuc.induztekspa.ui.theme.Dimens
import cl.duocuc.induztekspa.viewmodel.ProtocolosViewModel
import kotlin.math.abs

@Composable
fun ProtocolosListScreen(
    correoUsuario: String,
    rolUsuario: String,
    onEdit: (Protocolo) -> Unit,
    viewModel: ProtocolosViewModel = viewModel()
) {
    val protocolos by viewModel.protocolos.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarProtocolos(correoUsuario, rolUsuario)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingMedium)
    ) {
        Text(
            text = if (rolUsuario == "Jefe técnico" || rolUsuario == "Ingeniero") "Todos los Protocolos" else "Mis Protocolos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        if (protocolos.isEmpty()) {
            Text(
                text = "No hay protocolos registrados aún.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn {
                items(protocolos) { protocolo ->
                    ProtocoloCard(
                        protocolo = protocolo,
                        viewModel = viewModel,
                        onEdit = { onEdit(protocolo) },
                        onDelete = {
                            viewModel.eliminarProtocolo(protocolo.id, correoUsuario, rolUsuario)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProtocoloCard(
    protocolo: Protocolo,
    viewModel: ProtocolosViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val protocoloAnterior = remember(protocolo) {
        viewModel.obtenerProtocoloAnterior(protocolo.equipo, protocolo.id)
    }

    var variacionPorcentaje: Double? = null
    var esVariacionSignificativa = false

    if (protocoloAnterior != null && protocoloAnterior.medicionValor > 0 && protocolo.medicionValor > 0) {
        val previa = protocoloAnterior.medicionValor
        val actual = protocolo.medicionValor
        val diff = ((actual - previa) / previa) * 100
        variacionPorcentaje = diff
        if (abs(diff) >= 25.0) {
            esVariacionSignificativa = true
        }
    }

    val (colorEstado, textoEstado) = when (protocolo.estado) {
        "Formalizado" -> Color(0xFF2E7D32) to "Formalizado"
        "Registrado en terreno" -> Color(0xFF1565C0) to "Registrado en terreno"
        "Observado" -> Color(0xFFFF9800) to "Observado"
        "Anulado" -> Color(0xFFC62828) to "Anulado"
        else -> Color(0xFF757575) to "Borrador"
    }

    Card(
        modifier = EstilosTarjeta.modifierLista,
        shape = EstilosTarjeta.formaTarjeta,
        colors = EstilosTarjeta.coloresTarjeta()
    ) {
        Column(
            modifier = Modifier.padding(Dimens.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${protocolo.equipo} (${protocolo.tipoEquipo})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Código: ${protocolo.codigo} | Instalación: ${protocolo.instalacion}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (protocolo.direccion.isNotBlank()) {
                        Text(
                            text = "Dirección: ${protocolo.direccion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Responsable: ${protocolo.correoUsuario}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(colorEstado.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Estado: $textoEstado",
                    color = colorEstado,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Fecha/Hora: ${protocolo.fechaHora}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (protocolo.temperatura.isNotBlank() || protocolo.humedad.isNotBlank()) {
                Text(
                    text = "Condiciones: T° ${protocolo.temperatura} | Humedad: ${protocolo.humedad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (protocolo.latitud.isNotBlank() && protocolo.longitud.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                MapaUbicacionCard(
                    latitud = protocolo.latitud,
                    longitud = protocolo.longitud,
                    onCapturarGps = {},
                    readOnly = true
                )
            }

            if (protocolo.medicionValor > 0) {
                Text(
                    text = "Medición Aislación: ${protocolo.medicionValor} MΩ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(visible = esVariacionSignificativa) {
                val signo = if ((variacionPorcentaje ?: 0.0) > 0) "+" else ""
                val varTexto = String.format(java.util.Locale.US, "%.1f", variacionPorcentaje)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFFEBEE))
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "⚠️ Variación significativa: $signo$varTexto% respecto a prueba anterior (${protocoloAnterior?.medicionValor} MΩ)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (protocolo.observaciones.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Obs: ${protocolo.observaciones}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
