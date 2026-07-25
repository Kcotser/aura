package com.example.myapplication.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.AuraApplication
import com.example.myapplication.ui.components.AvatarPlaceholder
import com.example.myapplication.ui.components.IconButtonSlot
import com.example.myapplication.ui.components.StatusChip
import com.example.myapplication.ui.theme.OnSuccessContainer
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.SuccessContainer
import com.example.myapplication.ui.theme.AuraTheme
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Screen

/** Pantalla de inicio en "modo listo": botón grande de activación de protección. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InicioScreen(nav: Nav) {
    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication

    // El nombre viene de la cuenta del backend. Se lee del caché para pintar el saludo de
    // inmediato y se refresca en segundo plano; si falla (sin red, sesión vencida) se queda el
    // último conocido en vez de parpadear a un genérico.
    var nombre by remember { mutableStateOf(app.authRepository.nombre) }
    LaunchedEffect(Unit) {
        app.authRepository.refrescarPerfil()
        nombre = app.authRepository.nombre
    }

    val saludo = nombre?.let { "Hola, $it" } ?: "Hola"

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Encabezado con saludo ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm)
        ) {
            AvatarPlaceholder(initial = nombre?.take(1)?.uppercase() ?: "?", size = 44.dp)
            Spacer(Modifier.size(Spacing.sm))
            Text(
                saludo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButtonSlot(
                icon = Icons.Filled.Notifications,
                onClick = {},
                contentDescription = "Notificaciones"
            )
        }

        // --- Cuerpo centrado ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = Spacing.md)
        ) {
            Spacer(Modifier.height(Spacing.md))
            StatusChip(
                text = "Protección lista",
                contentColor = OnSuccessContainer,
                containerColor = SuccessContainer,
                leadingDot = true
            )

            Spacer(Modifier.weight(1f))

            // Botón circular grande de activación
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { nav.push(Screen.TransicionActivando) }
                    )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Text(
                        "Mantén\npresionado\npara activar",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            Text(
                "El video y el audio se graban durante la alerta y quedan guardados en tu teléfono.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.lg)
            )

            Spacer(Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InicioPreview() {
    AuraTheme { InicioScreen(com.example.myapplication.ui.nav.rememberNav()) }
}

