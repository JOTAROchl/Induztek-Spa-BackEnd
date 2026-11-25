package cl.duocuc.induztekspa.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.induztekspa.ui.components.BotonPrincipal
import cl.duocuc.induztekspa.ui.components.CampoContrasena
import cl.duocuc.induztekspa.ui.components.CampoTexto
import cl.duocuc.induztekspa.ui.state.NivelFuerza
import cl.duocuc.induztekspa.ui.theme.Dimens
import cl.duocuc.induztekspa.viewmodel.AuthViewModel

@Composable
fun RegistroScreen(
    onNavigateBack: () -> Unit,
    onRegistroSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val state by viewModel.registroState.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .verticalScroll(rememberScrollState())
                .padding(Dimens.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Crear Cuenta Técnico",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Regístrate en Induztek SpA",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            CampoTexto(
                label = "Nombre Completo *",
                value = state.nombre,
                onValueChange = { viewModel.onNombreRegistroChanged(it) },
                isError = state.errorNombre != null,
                errorMessage = state.errorNombre,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            CampoTexto(
                label = "Correo Electrónico *",
                value = state.correo,
                onValueChange = { viewModel.onCorreoRegistroChanged(it) },
                isError = state.errorCorreo != null,
                errorMessage = state.errorCorreo,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            CampoTexto(
                label = "RUT (Ej: 12.345.678-9)",
                value = state.rut,
                onValueChange = { viewModel.onRutRegistroChanged(it) },
                isError = state.errorRut != null,
                errorMessage = state.errorRut,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            CampoTexto(
                label = "Teléfono de Contacto",
                value = state.telefono,
                onValueChange = { viewModel.onTelefonoRegistroChanged(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            CampoContrasena(
                label = "Contraseña *",
                value = state.contrasena,
                onValueChange = { viewModel.onContrasenaRegistroChanged(it) },
                isError = state.errorContrasena != null,
                errorMessage = state.errorContrasena,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
            )

            // Indicador de fuerza de contraseña
            AnimatedVisibility(visible = state.contrasena.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    val (colorBarra, textoFuerza) = when (state.fuerzaContrasena) {
                        NivelFuerza.DEBIL -> Color.Red to "Contraseña Débil"
                        NivelFuerza.MEDIA -> Color(0xFFFF9800) to "Contraseña Media"
                        NivelFuerza.FUERTE -> Color(0xFF4CAF50) to "Contraseña Fuerte"
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(colorBarra)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = "Seguridad: $textoFuerza",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorBarra,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            CampoContrasena(
                label = "Confirmar Contraseña *",
                value = state.confirmarContrasena,
                onValueChange = { viewModel.onConfirmarContrasenaRegistroChanged(it) },
                isError = state.errorConfirmarContrasena != null,
                errorMessage = state.errorConfirmarContrasena,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            Text(
                text = "📌 Nota: Las cuentas nuevas quedan en estado 'Pendiente de Aprobación' por el Jefe Técnico antes de poder iniciar sesión.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            BotonPrincipal(
                text = "Registrarme",
                onClick = {
                    focusManager.clearFocus()
                    viewModel.ejecutarRegistro(
                        onSuccess = {
                            Toast.makeText(context, "Registro exitoso. Tu cuenta requiere aprobación del Jefe Técnico.", Toast.LENGTH_LONG).show()
                            onRegistroSuccess()
                        },
                        onError = { msj ->
                            Toast.makeText(context, msj, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            TextButton(onClick = onNavigateBack) {
                Text(text = "¿Ya tienes cuenta? Inicia Sesión")
            }
        }
    }
}
