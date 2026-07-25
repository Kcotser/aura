package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.myapplication.AuraApplication
import com.example.myapplication.capture.DualCameraSupport
import com.example.myapplication.ui.components.AvatarPlaceholder
import com.example.myapplication.ui.components.CriticalButton
import com.example.myapplication.ui.components.IconButtonSlot
import com.example.myapplication.ui.components.SectionHeader
import com.example.myapplication.ui.components.AuraTopBar
import com.example.myapplication.ui.theme.OnSuccessContainer
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.Success
import com.example.myapplication.ui.theme.SuccessContainer
import com.example.myapplication.ui.theme.AuraTheme
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Screen
import com.example.myapplication.ui.nav.Sheet
import com.example.myapplication.ui.nav.rememberNav
import kotlinx.coroutines.launch

@Composable
fun AjustesScreen(nav: Nav) {
    var camuflaje by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication
    val scope = rememberCoroutineScope()
    val dualDisponible = remember { DualCameraSupport.estaDisponible(context) }
    val perfil by app.profileRepository.profile.collectAsState(initial = null)
    val grabacionDual = perfil?.grabacionDual == true

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuraTopBar(
            title = "Ajustes",
            actions = { IconButtonSlot(icon = Icons.AutoMirrored.Filled.HelpOutline, onClick = {}) }
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
        ) {
            // Tarjeta de perfil
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = Shapes.card,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(Spacing.md)
                ) {
                    AvatarPlaceholder(initial = "E", size = 64.dp)
                    Spacer(Modifier.size(Spacing.md))
                    Column {
                        Text(
                            "Elena Soriano",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Cuenta verificada • Nivel 3",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))

            // Estado del sistema
            Surface(
                color = SuccessContainer,
                shape = Shapes.card,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(Spacing.md)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Success.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Filled.VerifiedUser, null, tint = OnSuccessContainer)
                    }
                    Spacer(Modifier.size(Spacing.md))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Estado del Sistema",
                            style = MaterialTheme.typography.titleMedium,
                            color = OnSuccessContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Cifrado 256-bit activo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSuccessContainer.copy(alpha = 0.8f)
                        )
                    }
                    Surface(color = Success, shape = Shapes.chip) {
                        Text(
                            "SEGURO",
                            style = MaterialTheme.typography.labelMedium,
                            color = androidx.compose.ui.graphics.Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 6.dp)
                        )
                    }
                }
            }

            SectionHeader("Seguridad y Privacidad")
            SettingRow(
                Icons.Filled.ManageAccounts, "Cuenta y Seguridad",
                onClick = { nav.push(Screen.AjustesBiometrico) }
            )
            Spacer(Modifier.height(Spacing.xs))
            SettingRow(
                Icons.Filled.Shield, "Privacidad de Datos",
                onClick = { nav.push(Screen.PoliticasAutodestruccion) }
            )
            Spacer(Modifier.height(Spacing.xs))
            SettingRow(
                Icons.Filled.Fingerprint, "Seguridad Biométrica",
                onClick = { nav.push(Screen.AjustesBiometrico) }
            )

            SectionHeader("Preferencias")
            SettingRow(Icons.Filled.NotificationsActive, "Notificaciones", onClick = {})
            Spacer(Modifier.height(Spacing.xs))
            SettingRow(
                icon = Icons.Filled.VisibilityOff,
                title = "Modo Camuflaje",
                onClick = { nav.push(Screen.AjustesCamuflaje) },
                trailing = {
                    Switch(
                        checked = camuflaje,
                        onCheckedChange = { camuflaje = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            )
            Spacer(Modifier.height(Spacing.xs))
            SettingRow(
                Icons.Filled.TouchApp, "Atajo de Activación Rápida",
                onClick = { nav.showSheet(Sheet.ConfigurarAtajo) }
            )
            Spacer(Modifier.height(Spacing.xs))
            SettingRow(
                Icons.Filled.GraphicEq, "Calibración de Audio y Video",
                onClick = { nav.push(Screen.CalibracionAV) }
            )
            Spacer(Modifier.height(Spacing.xs))
            SettingRow(
                icon = Icons.Filled.FlipCameraAndroid,
                title = "Grabar con ambas cámaras",
                subtitle = if (dualDisponible) {
                    "Frontal y trasera a la vez durante la alerta"
                } else {
                    DualCameraSupport.motivoNoDisponible(context)
                },
                enabled = dualDisponible,
                onClick = {
                    if (dualDisponible) scope.launch { app.profileRepository.setGrabacionDual(!grabacionDual) }
                },
                trailing = {
                    Switch(
                        checked = grabacionDual && dualDisponible,
                        enabled = dualDisponible,
                        onCheckedChange = { valor ->
                            scope.launch { app.profileRepository.setGrabacionDual(valor) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            )

            Spacer(Modifier.height(Spacing.xl))
            CriticalButton(
                text = "Cerrar sesión",
                onClick = { nav.showSheet(Sheet.GestionCuenta) },
                icon = Icons.AutoMirrored.Filled.Logout
            )
            Spacer(Modifier.height(Spacing.md))
            Text(
                "Versión 2.4.0 (Build 844)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true,
    trailing: @Composable (() -> Unit)? = null
) {
    val contentAlpha = if (enabled) 1f else 0.45f
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = Shapes.card,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.card)
            .clickable(enabled = enabled) { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(horizontal = Spacing.md, vertical = Spacing.xs)
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = contentAlpha))
            Spacer(Modifier.size(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha)
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                    )
                }
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(
                    Icons.Filled.ChevronRight,
                    null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AjustesPreview() {
    AuraTheme { AjustesScreen(rememberNav()) }
}

