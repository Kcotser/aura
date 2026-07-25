package com.example.myapplication.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.capture.AlertaGrabada
import com.example.myapplication.capture.ArchivoEvidencia
import com.example.myapplication.capture.RegistroEvidencia
import com.example.myapplication.capture.TipoEvidencia
import com.example.myapplication.ui.components.AuraTopBar
import com.example.myapplication.ui.components.IconButtonSlot
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.rememberNav
import com.example.myapplication.ui.theme.AuraTheme
import com.example.myapplication.ui.theme.PrimaryContainer
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Registro de la evidencia que quedó guardada en el teléfono, una tarjeta por alerta.
 *
 * Lee de MediaStore vía [RegistroEvidencia], así que muestra lo que realmente existe en el
 * dispositivo: si un archivo se borró desde la galería, desaparece de acá. No hay lista de casos
 * de ejemplo ni estados inventados — mientras no haya backend conectado, esto es lo único que la
 * app sabe de verdad sobre sus incidentes.
 */
@Composable
fun HistorialScreen(nav: Nav) {
    val context = LocalContext.current
    var alertas by remember { mutableStateOf<List<AlertaGrabada>?>(null) }
    var recarga by remember { mutableIntStateOf(0) }

    LaunchedEffect(recarga) {
        alertas = RegistroEvidencia.listarAlertas(context)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuraTopBar(
            title = "Historial",
            actions = {
                IconButtonSlot(
                    icon = Icons.Filled.Refresh,
                    contentDescription = "Actualizar",
                    onClick = { recarga++ }
                )
            }
        )

        val lista = alertas
        when {
            lista == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            lista.isEmpty() -> SinGrabaciones()

            else -> LazyColumn(
                contentPadding = PaddingValues(
                    start = Spacing.md,
                    end = Spacing.md,
                    bottom = Spacing.md
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                item { Resumen(lista) }
                items(lista, key = { it.id }) { alerta ->
                    AlertaCard(alerta, onAbrir = { abrirArchivo(context, it) })
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ secciones */

@Composable
private fun Resumen(alertas: List<AlertaGrabada>) {
    val archivos = alertas.sumOf { it.archivos.size }
    val bytes = alertas.sumOf { it.bytesTotales }

    Text(
        "${contar(alertas.size, "alerta", "alertas")} · " +
            "${contar(archivos, "archivo", "archivos")} · ${formatearTamano(bytes)}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = Spacing.xs)
    )
}

@Composable
private fun SinGrabaciones() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.lg)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(PrimaryContainer.copy(alpha = 0.18f))
        ) {
            Icon(
                Icons.Filled.VideoLibrary,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
        }
        Spacer(Modifier.height(Spacing.md))
        Text(
            "Todavía no hay grabaciones",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "Cuando actives una alerta, el video y el audio quedarán guardados aquí.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AlertaCard(alerta: AlertaGrabada, onAbrir: (ArchivoEvidencia) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = Shapes.card,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().clip(Shapes.card)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            // Franja de color, igual que el resto de tarjetas de la app.
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary)
            )
            Column(Modifier.padding(Spacing.md)) {
                Text(
                    formatearFecha(alerta.instante),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    listOfNotNull(
                        contar(alerta.videos, "video", "videos").takeIf { alerta.videos > 0 },
                        contar(alerta.audios, "audio", "audios").takeIf { alerta.audios > 0 },
                        formatearDuracion(alerta.duracionMs).takeIf { alerta.duracionMs > 0 },
                        formatearTamano(alerta.bytesTotales)
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(Spacing.sm))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHigh)

                alerta.archivos.forEach { archivo ->
                    ArchivoRow(archivo, onClick = { onAbrir(archivo) })
                }
            }
        }
    }
}

@Composable
private fun ArchivoRow(archivo: ArchivoEvidencia, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = Spacing.sm)
    ) {
        Icon(
            iconoDe(archivo.tipo),
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.size(Spacing.sm))
        Column(Modifier.weight(1f)) {
            Text(
                archivo.tipo.etiqueta,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                listOfNotNull(
                    formatearDuracion(archivo.duracionMs).takeIf { archivo.duracionMs > 0 },
                    formatearTamano(archivo.bytes)
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.Filled.PlayCircleOutline,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

/* -------------------------------------------------------------------- helpers */

private fun iconoDe(tipo: TipoEvidencia): ImageVector = when (tipo) {
    TipoEvidencia.VIDEO_FRONTAL -> Icons.Filled.VideoCall
    TipoEvidencia.VIDEO_TRASERO -> Icons.Filled.CameraAlt
    TipoEvidencia.AUDIO -> Icons.Filled.Mic
}

/** Abre el archivo con el reproductor del sistema. */
private fun abrirArchivo(context: Context, archivo: ArchivoEvidencia) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(archivo.uri, if (archivo.esVideo) "video/mp4" else "audio/mp4")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(intent) }.onFailure {
        Toast.makeText(context, "No hay una app para abrir este archivo", Toast.LENGTH_SHORT).show()
    }
}

private fun contar(n: Int, singular: String, plural: String) = "$n ${if (n == 1) singular else plural}"

private fun formatearDuracion(ms: Long): String {
    val totalSegundos = ms / 1000
    return "%d:%02d".format(totalSegundos / 60, totalSegundos % 60)
}

private fun formatearTamano(bytes: Long): String = when {
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1024 -> "%.0f KB".format(bytes / 1024.0)
    else -> "$bytes B"
}

private val HORA = SimpleDateFormat("HH:mm", Locale.getDefault())
private val FECHA_COMPLETA = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())

/** "Hoy, 22:45" / "Ayer, 08:12" / "14 oct 2025, 19:03". */
private fun formatearFecha(instante: Long): String {
    if (instante == 0L) return "Fecha desconocida"

    val fecha = Calendar.getInstance().apply { timeInMillis = instante }
    val hoy = Calendar.getInstance()
    val ayer = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    fun mismoDia(a: Calendar, b: Calendar) =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    return when {
        mismoDia(fecha, hoy) -> "Hoy, ${HORA.format(Date(instante))}"
        mismoDia(fecha, ayer) -> "Ayer, ${HORA.format(Date(instante))}"
        else -> FECHA_COMPLETA.format(Date(instante))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HistorialPreview() {
    AuraTheme { HistorialScreen(rememberNav()) }
}
