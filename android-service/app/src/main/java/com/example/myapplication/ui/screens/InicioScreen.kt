package com.example.myapplication.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.AppCard
import com.example.myapplication.ui.components.AvatarPlaceholder
import com.example.myapplication.ui.components.IconButtonSlot
import com.example.myapplication.ui.components.StatusChip
import com.example.myapplication.ui.theme.OnSuccessContainer
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.SuccessContainer
import com.example.myapplication.ui.theme.AuraTheme
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Screen

/** Pantalla de inicio en "modo listo": botón grande de activación de protección. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InicioScreen(nav: Nav) {
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
            AvatarPlaceholder(initial = "C", size = 44.dp)
            Spacer(Modifier.size(Spacing.sm))
            Text(
                "Hola, Camila",
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
                "Tus contactos serán notificados automáticamente al activarse.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.lg)
            )

            Spacer(Modifier.weight(1f))

            // Tarjeta Red de Apoyo
            AppCard(Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(Spacing.md)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Red de Apoyo Activa",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(Spacing.xs))
                        AvatarStack(count = 3, extra = 2)
                    }
                    Spacer(Modifier.size(Spacing.sm))
                    Button(
                        onClick = {},
                        shape = Shapes.button,
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Filled.Group, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.size(Spacing.xs))
                        Text("Gestionar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

/** Fila de avatares superpuestos con contador "+N". */
@Composable
private fun AvatarStack(count: Int, extra: Int) {
    val initials = listOf("E", "L", "C", "M", "R")
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            Box(
                modifier = Modifier
                    .offset(x = (-8 * i).dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AvatarPlaceholder(initial = initials[i], size = 30.dp)
            }
        }
        Box(
            modifier = Modifier
                .offset(x = (-8 * count).dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "+$extra",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InicioPreview() {
    AuraTheme { InicioScreen(com.example.myapplication.ui.nav.rememberNav()) }
}

