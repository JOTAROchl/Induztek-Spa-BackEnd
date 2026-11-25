package cl.duocuc.induztekspa.permitions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat

object Permisos {

    fun checkPermiso(context: Context, permiso: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permiso
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Composable
    fun rememberCameraPermissionLauncher(
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ): ManagedActivityResultLauncher<String, Boolean> {
        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }

    @Composable
    fun rememberLocationPermissionLauncher(
        onGranted: () -> Unit,
        onDenied: () -> Unit
    ): ManagedActivityResultLauncher<String, Boolean> {
        return rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }

    fun solicitarPermisoCamara(
        context: Context,
        launcher: ManagedActivityResultLauncher<String, Boolean>,
        onGranted: () -> Unit
    ) {
        if (checkPermiso(context, Manifest.permission.CAMERA)) {
            onGranted()
        } else {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    fun solicitarPermisoUbicacion(
        context: Context,
        launcher: ManagedActivityResultLauncher<String, Boolean>,
        onGranted: () -> Unit
    ) {
        if (checkPermiso(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            onGranted()
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
