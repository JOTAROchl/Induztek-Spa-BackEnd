package cl.duocuc.induztekspa.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duocuc.induztekspa.R
import cl.duocuc.induztekspa.ui.components.BotonPrincipal
import cl.duocuc.induztekspa.ui.components.CampoContrasena
import cl.duocuc.induztekspa.ui.components.CampoTexto
import cl.duocuc.induztekspa.ui.theme.Dimens
import cl.duocuc.induztekspa.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegistro: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val loginState by viewModel.loginState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    
    var mostrarDialogoRecuperar by remember { mutableStateOf(false) }
    var correoRecuperar by remember { mutableStateOf("") }
    var nuevaClave by remember { mutableStateOf("") }
    var confirmarNuevaClave by remember { mutableStateOf("") }

    if (mostrarDialogoRecuperar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoRecuperar = false },
            title = { Text("Restablecer Contraseña") },
            text = {
                Column {
                    Text("Ingresa tu correo y la nueva contraseña que deseas asignar:")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    CampoTexto(
                        label = "Correo Electrónico *",
                        value = correoRecuperar,
                        onValueChange = { correoRecuperar = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    CampoContrasena(
                        label = "Nueva Contraseña *",
                        value = nuevaClave,
                        onValueChange = { nuevaClave = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    CampoContrasena(
                        label = "Confirmar Nueva Contraseña *",
                        value = confirmarNuevaClave,
                        onValueChange = { confirmarNuevaClave = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.restablecerContrasenaDirecta(
                        correo = correoRecuperar,
                        nuevaContrasena = nuevaClave,
                        confirmarContrasena = confirmarNuevaClave
                    ) { exito, msj ->
                        Toast.makeText(context, msj, Toast.LENGTH_LONG).show()
                        if (exito) {
                            mostrarDialogoRecuperar = false
                            correoRecuperar = ""
                            nuevaClave = ""
                            confirmarNuevaClave = ""
                        }
                    }
                }) {
                    Text("Actualizar Contraseña")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoRecuperar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.paddingLarge)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_induztek),
                contentDescription = null,
                modifier = Modifier.size(140.dp)
            )

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            Text(
                text = stringResource(id = R.string.login_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = stringResource(id = R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            AnimatedVisibility(visible = loginState.mensajeErrorGeneral != null) {
                loginState.mensajeErrorGeneral?.let { msj ->
                    Text(
                        text = msj,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = Dimens.paddingMedium)
                    )
                }
            }

            CampoTexto(
                label = stringResource(id = R.string.username_label),
                value = loginState.correo,
                onValueChange = { viewModel.onCorreoLoginChanged(it) },
                isError = loginState.errorCorreo != null,
                errorMessage = loginState.errorCorreo,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            CampoContrasena(
                label = stringResource(id = R.string.password_label),
                value = loginState.contrasena,
                onValueChange = { viewModel.onContrasenaLoginChanged(it) },
                isError = loginState.errorContrasena != null,
                errorMessage = loginState.errorContrasena,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { 
                        focusManager.clearFocus()
                        viewModel.ejecutarLogin(onLoginSuccess)
                    }
                )
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { 
                    correoRecuperar = loginState.correo
                    mostrarDialogoRecuperar = true 
                }) {
                    Text(text = "¿Olvidaste tu contraseña?", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            BotonPrincipal(
                text = stringResource(id = R.string.login_button),
                onClick = {
                    focusManager.clearFocus()
                    viewModel.ejecutarLogin(onLoginSuccess)
                }
            )

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            TextButton(onClick = onNavigateToRegistro) {
                Text(text = "¿No tienes cuenta? Regístrate aquí")
            }
        }
    }
}
