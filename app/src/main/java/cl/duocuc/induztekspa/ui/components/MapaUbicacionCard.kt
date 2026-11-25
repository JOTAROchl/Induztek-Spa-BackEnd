package cl.duocuc.induztekspa.ui.components

import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import cl.duocuc.induztekspa.ui.theme.Dimens
import java.util.Locale

class MapWebAppInterface(val onLocationChanged: (Double, Double) -> Unit) {
    @JavascriptInterface
    fun onMarkerMoved(lat: Double, lng: Double) {
        onLocationChanged(lat, lng)
    }
}

@Composable
fun MapaUbicacionCard(
    latitud: String,
    longitud: String,
    direccionNominatim: String = "",
    permisoHabilitado: Boolean = true,
    onCapturarGps: () -> Unit,
    onMoverPin: (Double, Double) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    readOnly: Boolean = false
) {
    val lat = latitud.toDoubleOrNull() ?: -33.2842
    val lng = longitud.toDoubleOrNull() ?: -70.7301
    val tieneUbicacion = latitud.isNotBlank() && longitud.isNotBlank() && permisoHabilitado

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=yes" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map { width: 100%; height: 100%; margin: 0; padding: 0; background: #e2e8f0; }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {zoomControl: true, dragging: true, tap: true, touchZoom: true}).setView([$lat, $lng], 15);
                L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    maxZoom: 19
                }).addTo(map);
                var marker = L.marker([$lat, $lng], {draggable: ${!readOnly}}).addTo(map);
                marker.on('dragend', function(e) {
                    var pos = marker.getLatLng();
                    if (window.AndroidInterface) {
                        window.AndroidInterface.onMarkerMoved(pos.lat, pos.lng);
                    }
                });
            </script>
        </body>
        </html>
    """.trimIndent()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.paddingSmall),
        shape = RoundedCornerShape(Dimens.cornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingSmall)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (permisoHabilitado) Icons.Default.Map else Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = if (permisoHabilitado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (!permisoHabilitado) "Mapa Bloqueado (Sin Permiso)" else if (tieneUbicacion) "Mapa Retrofit / Nominatim API" else "Mapa de Ubicación GPS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (!permisoHabilitado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!readOnly) {
                    OutlinedButton(
                        onClick = onCapturarGps,
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GPS", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(Dimens.cornerRadius))
                    .background(if (permisoHabilitado) Color(0xFFE2E8F0) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (permisoHabilitado) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewClient = WebViewClient()
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                }
                                addJavascriptInterface(MapWebAppInterface(onMoverPin), "AndroidInterface")
                                loadDataWithBaseURL("https://nominatim.openstreetmap.org", htmlContent, "text/html", "utf-8", null)
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL("https://nominatim.openstreetmap.org", htmlContent, "text/html", "utf-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Permiso GPS Denegado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Mapa bloqueado. Ingresa el lugar y dirección manualmente en el formulario.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (tieneUbicacion && permisoHabilitado) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(
                        text = "📍 Coordenadas: ${String.format(Locale.US, "%.4f", lat)}, ${String.format(Locale.US, "%.4f", lng)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (direccionNominatim.isNotBlank()) {
                        Text(
                            text = "Dirección Retrofit: $direccionNominatim",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
