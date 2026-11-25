package cl.duocuc.induztekspa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.induztekspa.model.Usuario
import cl.duocuc.induztekspa.ui.styles.EstilosTarjeta
import cl.duocuc.induztekspa.ui.theme.Dimens
import cl.duocuc.induztekspa.viewmodel.UsuariosViewModel

@Composable
fun GestionUsuariosScreen(
    viewModel: UsuariosViewModel = viewModel()
) {
    val usuarios by viewModel.usuarios.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarUsuarios()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingMedium)
    ) {
        Text(
            text = "Gestión de Usuarios",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Aprobación de cuentas y asignación de roles",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

        LazyColumn {
            items(usuarios) { usuario ->
                UsuarioCard(
                    usuario = usuario,
                    onAprobar = { viewModel.aprobarUsuario(usuario.id) },
                    onEliminar = { viewModel.eliminarUsuario(usuario.id) },
                    onCambiarRol = { id, rol -> viewModel.actualizarRol(id, rol) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioCard(
    usuario: Usuario,
    onAprobar: () -> Unit,
    onEliminar: () -> Unit,
    onCambiarRol: (Int, String) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val rolesDisponibles = listOf("Jefe técnico", "Ingeniero", "Técnico", "Cliente")

    Card(
        modifier = EstilosTarjeta.modifierLista,
        shape = EstilosTarjeta.formaTarjeta,
        colors = EstilosTarjeta.coloresTarjeta()
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = usuario.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = usuario.correo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (usuario.rut.isNotBlank()) {
                        Text(
                            text = "RUT: ${usuario.rut}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (usuario.correo != "admin@induztek.cl") {
                    IconButton(onClick = { 
                        onEliminar()
                        Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            // Badge de Estado de Aprobación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (usuario.estaAprobado) {
                    Text(
                        text = "✓ Cuenta Aprobada",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "⏳ PENDIENTE DE APROBACIÓN",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            onAprobar()
                            Toast.makeText(context, "Cuenta aprobada exitosamente", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Aprobar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            if (usuario.correo != "admin@induztek.cl") {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = usuario.rol,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        rolesDisponibles.forEach { rolSeleccionado ->
                            DropdownMenuItem(
                                text = { Text(rolSeleccionado) },
                                onClick = {
                                    onCambiarRol(usuario.id, rolSeleccionado)
                                    expanded = false
                                    Toast.makeText(context, "Rol actualizado a $rolSeleccionado", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Rol: Jefe técnico (No modificable)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
