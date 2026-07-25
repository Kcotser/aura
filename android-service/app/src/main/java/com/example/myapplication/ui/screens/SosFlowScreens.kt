package com.example.myapplication.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.capture.SosCaptureService
import com.example.myapplication.ui.components.IconButtonSlot
import com.example.myapplication.ui.components.StatusChip
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Screen
import com.example.myapplication.ui.nav.rememberNav
import com.example.myapplication.ui.theme.ErrorColor
import com.example.myapplication.ui.theme.PrimaryContainer
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.Success
import com.example.myapplication.ui.theme.AuraTheme
import kotlinx.coroutines.delay

/* =============================================================================
 * 1. TRANSICIÓN ACTIVANDO (pantalla intermedia, avanza sola)
 * ========================================================================== */

@Composable
fun TransicionActivandoScreen(nav: Nav) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        // Arranca la grabación real (video trasero + audio) apenas se activa la protección;
        // se corta en ProcesandoIncidenteScreen (fin del incidente) o si se cierra la app.
        SosCaptureService.start(context)
        delay(1600)
        nav.push(Screen.ConfirmandoSOS)
    }

    val gradient = androidx.compose.ui.graphics.Brush.verticalGradient(
        listOf(Color(0xFF5A189A), Color(0xFF2B0052))
    )
    val items = listOf(
        Icons.Filled.VideoCall to "Cámara frontal",
        Icons.Filled.CameraAlt to "Cámara trasera",
        Icons.Filled.Mic to "Audio ambiental",
        Icons.Filled.LocationOn to "Ubicación GPS"
    )

    Box(Modifier.fillMaxSize().background(gradient)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.lg))
            Text("AURA", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(Icons.Filled.Shield, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(
                "Activando Protección",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Configurando entorno seguro y protocolos de emergencia.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.75f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))
            Surface(color = Color.White.copy(alpha = 0.08f), shape = Shapes.card, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Spacing.md)) {
                    items.forEach { (icon, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.xs)
                        ) {
                            Icon(icon, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.size(Spacing.sm))
                            Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.Shield, null, tint = Success, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(3) { i ->
                    Box(
                        Modifier
                            .size(if (i == 1) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = if (i == 1) 1f else 0.4f))
                    )
                }
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "La pantalla cambiará automáticamente",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

/* =============================================================================
 * 2. CONFIRMANDO SOS (hold-to-cancel, sobre la pantalla de Inicio)
 * ========================================================================== */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConfirmandoSOSScreen(nav: Nav) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceDim)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            Text(
                "AURA",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButtonSlot(icon = Icons.Filled.MoreVert, onClick = {})
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Spacer(Modifier.weight(1f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(ErrorColor)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { nav.push(Screen.ProcesandoIncidente) }
                    )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.LocationOn, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    Text("SOS", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(
                "Activando Alerta",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Mantén presionado para confirmar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.md))
            StatusChip(
                text = "Modo Discreto Activo",
                contentColor = MaterialTheme.colorScheme.secondary,
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
            )
            Spacer(Modifier.weight(1f))
        }
    }
}

/* =============================================================================
 * 3. PROCESANDO INCIDENTE (barra de progreso animada, avanza sola)
 * ========================================================================== */

@Composable
fun ProcesandoIncidenteScreen(nav: Nav) {
    val context = LocalContext.current
    var progresoEntorno by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (progresoEntorno < 1f) {
            delay(60)
            progresoEntorno = (progresoEntorno + 0.02f).coerceAtMost(1f)
        }
        delay(400)
        // Fin del incidente: se corta la grabación acá (el archivo queda listo en
        // AURA_SOS/); subirlo al backend es el siguiente paso, todavía no implementado.
        SosCaptureService.stop(context)
        nav.replace(Screen.FichaIncidente)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            Text(
                "AURA",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
        ) {
            Spacer(Modifier.height(Spacing.lg))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                StatusChip(
                    text = "Audio Live",
                    contentColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                CircularProgressIndicator(
                    progress = { progresoEntorno },
                    modifier = Modifier.size(260.dp),
                    strokeWidth = 6.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                Box(
                    Modifier
                        .size(210.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            StatusChip(
                text = "Cifrado AES-256",
                contentColor = Color.White,
                containerColor = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(Spacing.lg))
            Text(
                "Analizando tu incidente...",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Estamos procesando los datos capturados para brindarte la mejor asistencia inmediata.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.lg))

            ProcesoItem("Transcribiendo audio", 1f)
            Spacer(Modifier.height(Spacing.sm))
            ProcesoItem("Analizando entorno", progresoEntorno)
            Spacer(Modifier.height(Spacing.sm))
            ProcesoItem("Generando reporte", 0f)

            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                InfoPill(Icons.Filled.Shield, "Procesado de forma cifrada", Modifier.weight(1f))
            }
            Spacer(Modifier.height(Spacing.sm))
            InfoPill(Icons.Filled.VisibilityOff, "Modo Discreto Activado", Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun ProcesoItem(label: String, progress: Float) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(Shapes.chip),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}

@Composable
private fun InfoPill(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = Shapes.chip,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.sm)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(6.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun TransicionPreview() {
    AuraTheme { TransicionActivandoScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ConfirmandoPreview() {
    AuraTheme { ConfirmandoSOSScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ProcesandoPreview() {
    AuraTheme { ProcesandoIncidenteScreen(rememberNav()) }
}
