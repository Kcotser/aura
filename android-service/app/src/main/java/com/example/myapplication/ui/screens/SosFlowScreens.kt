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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.myapplication.AuraApplication
import com.example.myapplication.capture.EstadoSubida
import com.example.myapplication.capture.SosCaptureService
import com.example.myapplication.ui.Tab
import com.example.myapplication.ui.components.IconButtonSlot
import com.example.myapplication.ui.components.PrimaryButton
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
import kotlinx.coroutines.flow.first

/* =============================================================================
 * 1. TRANSICIÓN ACTIVANDO (pantalla intermedia, avanza sola)
 * ========================================================================== */

@Composable
fun TransicionActivandoScreen(nav: Nav) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        // Arranca la grabación real apenas se activa la protección; se corta en
        // EnvioEvidenciaScreen (fin del incidente) o si se cierra la app. La preferencia
        // se lee acá y no dentro del servicio para no bloquear el arranque de la captura
        // leyendo DataStore en el hilo principal justo en el momento de la emergencia.
        val app = context.applicationContext as AuraApplication
        val dual = app.profileRepository.profile.first().grabacionDual
        // Descarta el trabajo de la alerta anterior para que la pantalla de cierre no muestre
        // el resultado de la subida pasada mientras esta todavía está grabando.
        EstadoSubida.reiniciar()
        SosCaptureService.start(context, dual = dual)
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
                        onLongClick = { nav.push(Screen.EnvioEvidencia) }
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
 * 3. ENVÍO DE EVIDENCIA (cierre de la alerta)
 * ========================================================================== */

/** En qué punto está la evidencia de la alerta que se acaba de cerrar. */
private enum class FaseEnvio { GUARDANDO, ENVIANDO, ENVIADA, SIN_CUENTA, FALLO }

/**
 * Cierre de la alerta: corta la grabación y acompaña el envío de la evidencia al backend.
 *
 * Lo que se muestra es el estado real del trabajo de WorkManager que encoló
 * [com.example.myapplication.capture.SosCaptureService], no una animación con temporizador. Por
 * eso la pantalla no avanza sola: mientras haya algo subiendo, se queda; y si falla, lo dice en
 * vez de fingir que salió bien.
 *
 * El botón de continuar está siempre habilitado a propósito. La subida sigue en segundo plano
 * aunque se salga de acá, y obligar a alguien que acaba de pasar por una emergencia a mirar una
 * barra de progreso sería exactamente la clase de fricción que esta pantalla no debería tener.
 */
@Composable
fun EnvioEvidenciaScreen(nav: Nav) {
    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication
    val tieneCuenta = remember { app.authRepository.estaLogueado }

    LaunchedEffect(Unit) {
        // Fin del incidente: se corta la grabación. El servicio cierra los archivos, extrae el
        // audio y recién ahí encola la subida, así que EstadoSubida.idTrabajo llega con retraso.
        SosCaptureService.stop(context)
    }

    val idTrabajo = EstadoSubida.idTrabajo
    val infoTrabajo by produceState<WorkInfo?>(initialValue = null, idTrabajo) {
        val id = idTrabajo ?: return@produceState
        WorkManager.getInstance(context).getWorkInfoByIdFlow(id).collect { value = it }
    }

    val fase = when {
        idTrabajo == null -> FaseEnvio.GUARDANDO
        infoTrabajo?.state == WorkInfo.State.SUCCEEDED -> FaseEnvio.ENVIADA
        infoTrabajo?.state == WorkInfo.State.FAILED ||
            infoTrabajo?.state == WorkInfo.State.CANCELLED ->
            if (tieneCuenta) FaseEnvio.FALLO else FaseEnvio.SIN_CUENTA
        else -> FaseEnvio.ENVIANDO
    }

    val (icono, tinte) = when (fase) {
        FaseEnvio.GUARDANDO -> Icons.Filled.Save to MaterialTheme.colorScheme.primary
        FaseEnvio.ENVIANDO -> Icons.Filled.CloudUpload to MaterialTheme.colorScheme.primary
        FaseEnvio.ENVIADA -> Icons.Filled.CloudDone to Success
        FaseEnvio.SIN_CUENTA, FaseEnvio.FALLO -> Icons.Filled.CloudOff to MaterialTheme.colorScheme.onSurfaceVariant
    }

    val titulo = when (fase) {
        FaseEnvio.GUARDANDO -> "Guardando tu grabación"
        FaseEnvio.ENVIANDO -> "Enviando tus datos"
        FaseEnvio.ENVIADA -> "Tu evidencia está a salvo"
        FaseEnvio.SIN_CUENTA -> "Guardado en tu teléfono"
        FaseEnvio.FALLO -> "No se pudo enviar todavía"
    }

    val mensaje = when (fase) {
        FaseEnvio.GUARDANDO ->
            "Ya terminó la alerta. Estamos cerrando el video y el audio para que no se pierda nada."
        FaseEnvio.ENVIANDO ->
            "Estamos respaldando el video y el audio fuera de tu teléfono. Puedes cerrar esta " +
                "pantalla: el envío continúa solo."
        FaseEnvio.ENVIADA ->
            "El video y el audio quedaron respaldados. Nadie puede borrarlos desde tu teléfono."
        FaseEnvio.SIN_CUENTA ->
            "La grabación quedó guardada en este dispositivo. Inicia sesión cuando puedas para " +
                "respaldarla fuera de él."
        FaseEnvio.FALLO ->
            "Tu grabación está guardada en este dispositivo y no se perdió. Volveremos a " +
                "intentar el envío cuando haya conexión."
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
            Spacer(Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                if (fase == FaseEnvio.GUARDANDO || fase == FaseEnvio.ENVIANDO) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(200.dp),
                        strokeWidth = 5.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(PrimaryContainer.copy(alpha = 0.18f))
                ) {
                    Icon(icono, null, tint = tinte, modifier = Modifier.size(72.dp))
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            Text(
                titulo,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                mensaje,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))

            InfoPill(Icons.Filled.Shield, "La grabación está guardada en tu teléfono", Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.md))
        }

        Column(Modifier.padding(horizontal = Spacing.md)) {
            PrimaryButton(
                text = "Continuar",
                onClick = { nav.popToMain(Tab.Historial) }
            )
            Spacer(Modifier.height(Spacing.lg))
        }
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
private fun EnvioEvidenciaPreview() {
    AuraTheme { EnvioEvidenciaScreen(rememberNav()) }
}
