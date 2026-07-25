package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.AuraTopBar
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.rememberNav
import com.example.myapplication.ui.theme.PrimaryContainer
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.AuraTheme

private data class OpcionCamuflaje(val icon: ImageVector, val titulo: String, val subtitulo: String, val disponible: Boolean = true)

private val opcionesCamuflaje = listOf(
    OpcionCamuflaje(Icons.Filled.Calculate, "Calculadora", "Funcional y discreta"),
    OpcionCamuflaje(Icons.AutoMirrored.Filled.Notes, "Notas", "Lista de pendientes"),
    OpcionCamuflaje(Icons.Filled.MusicNote, "Música", "Reproductor neutro"),
    OpcionCamuflaje(Icons.Filled.Cloud, "Clima", "Pronóstico local", disponible = false)
)

/* =============================================================================
 * 1. SELECTOR DE CAMUFLAJE (onboarding / primera vez)
 * ========================================================================== */

@Composable
fun SelectorCamuflajeScreen(nav: Nav) {
    var seleccion by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuraTopBar(title = "AURA", onBack = { nav.pop() })
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
        ) {
            Text(
                "Elige tu pantalla de camuflaje",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Esta interfaz se mostrará automáticamente si necesitas ocultar la aplicación rápidamente.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.md))

            for (row in opcionesCamuflaje.chunked(2)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    row.forEachIndexed { i, opcion ->
                        val globalIndex = opcionesCamuflaje.indexOf(opcion)
                        CamuflajeOptionCard(
                            opcion = opcion,
                            selected = seleccion == globalIndex,
                            modifier = Modifier.weight(1f),
                            onClick = { if (opcion.disponible) seleccion = globalIndex }
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.sm))
            }
            Spacer(Modifier.height(Spacing.md))
            PrimaryButton(text = "Guardar preferencia", onClick = { nav.pop() })
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun CamuflajeOptionCard(
    opcion: OpcionCamuflaje,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = Shapes.card,
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .clip(Shapes.card)
            .clickable(enabled = opcion.disponible) { onClick() }
    ) {
        Column {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.1f)
                    .background(if (opcion.titulo == "Música") Color(0xFF1B1F26) else MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Icon(
                    opcion.icon,
                    null,
                    tint = if (opcion.titulo == "Música") Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
                if (selected) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(Spacing.xs)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
                if (!opcion.disponible) {
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)))
                }
            }
            Column(Modifier.padding(Spacing.sm)) {
                Text(opcion.titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(opcion.subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/* =============================================================================
 * 2. AJUSTES DE CAMUFLAJE (desde Ajustes, con auto-activación)
 * ========================================================================== */

@Composable
fun AjustesCamuflajeScreen(nav: Nav) {
    var seleccion by remember { mutableStateOf(0) }
    var autoActivar by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuraTopBar(
            title = "Ajustes",
            onBack = { nav.pop() },
            actions = { Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = Spacing.sm)) }
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
        ) {
            Text("Modo Camuflaje", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Selecciona una interfaz alternativa para ocultar AURA. Al activarse, la aplicación cambiará su icono y pantalla principal por una de estas opciones.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.md))

            for (row in opcionesCamuflaje.chunked(2)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    row.forEach { opcion ->
                        val globalIndex = opcionesCamuflaje.indexOf(opcion)
                        CamuflajeOptionCard(
                            opcion = opcion,
                            selected = seleccion == globalIndex,
                            modifier = Modifier.weight(1f),
                            onClick = { if (opcion.disponible) seleccion = globalIndex }
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.sm))
            }

            Spacer(Modifier.height(Spacing.sm))
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = Shapes.card, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.md)) {
                    Column(Modifier.weight(1f)) {
                        Text("Activar automáticamente", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Text("Activa el camuflaje tras 5 min de inactividad", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = autoActivar, onCheckedChange = { autoActivar = it }, colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary))
                }
            }

            Spacer(Modifier.height(Spacing.md))
            PrimaryButton(text = "Guardar preferencia", onClick = { nav.pop() }, icon = Icons.Filled.Check)

            Spacer(Modifier.height(Spacing.md))
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = Shapes.card, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(Spacing.md)) {
                    Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(Spacing.sm))
                    Text(
                        "Tu seguridad es nuestra prioridad. El camuflaje se puede desactivar manteniendo presionado el logo durante 3 segundos en la pantalla oculta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SelectorCamuflajePreview() {
    AuraTheme { SelectorCamuflajeScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AjustesCamuflajePreview() {
    AuraTheme { AjustesCamuflajeScreen(rememberNav()) }
}
