package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.AvatarPlaceholder
import com.example.myapplication.ui.components.IconButtonSlot
import com.example.myapplication.ui.components.AuraTopBar
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.Success
import com.example.myapplication.ui.theme.SuccessContainer
import com.example.myapplication.ui.theme.OnSuccessContainer
import com.example.myapplication.ui.theme.AuraTheme
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Sheet
import com.example.myapplication.ui.nav.rememberNav

private data class Contacto(val nombre: String, val relacion: String, val inicial: String)
private data class Institucion(val nombre: String, val desc: String, val icon: ImageVector)

@Composable
fun RedApoyoScreen(nav: Nav) {
    val contactos = listOf(
        Contacto("Elena Valdivia", "Mamá • Contacto SOS", "E"),
        Contacto("Lucía Ferreyra", "Mejor Amiga • Contacto SOS", "L"),
        Contacto("Carlos Ruiz", "Hermano • Contacto SOS", "C")
    )
    val instituciones = listOf(
        Institucion("Línea 100", "Violencia familiar y sexual", Icons.Filled.SupportAgent),
        Institucion("PNP", "Emergencias policiales (105)", Icons.Filled.LocalPolice),
        Institucion("Serenazgo", "Seguridad ciudadana local", Icons.Filled.Shield)
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuraTopBar(
            title = "Red de Apoyo",
            actions = { IconButtonSlot(icon = Icons.Filled.MoreVert, onClick = {}) }
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
        ) {
            // Encabezado de sección con contador
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.sm)
            ) {
                Text(
                    "Contactos de Confianza",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                    shape = Shapes.chip
                ) {
                    Text(
                        "3/5 ACTIVOS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 6.dp)
                    )
                }
            }

            contactos.forEach { c ->
                ContactoCard(c, onEdit = { nav.showSheet(Sheet.EditarContacto) })
                Spacer(Modifier.height(Spacing.sm))
            }

            // Agregar contacto (borde punteado simulado con borde sólido tenue)
            Surface(
                color = Color.Transparent,
                shape = Shapes.card,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(
                        1.5.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        Shapes.card
                    )
                    .clip(Shapes.card)
                    .clickable { nav.showSheet(Sheet.AgregarContacto) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(Spacing.xs))
                    Text(
                        "Agregar contacto",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            Text(
                "Directorio Institucional",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.sm))

            instituciones.forEach { inst ->
                InstitucionCard(inst)
                Spacer(Modifier.height(Spacing.sm))
            }

            Spacer(Modifier.height(Spacing.xs))
            // Banner informativo verde
            Surface(
                color = SuccessContainer,
                shape = Shapes.card,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(Spacing.md)
                ) {
                    Icon(Icons.Filled.Info, null, tint = OnSuccessContainer)
                    Spacer(Modifier.size(Spacing.sm))
                    Text(
                        "Tu Red de Apoyo recibirá una alerta inmediata con tu ubicación en tiempo real si activas el modo SOS.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSuccessContainer
                    )
                }
            }
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun ContactoCard(c: Contacto, onEdit: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = Shapes.card,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.md)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(3.dp)
                ) {
                    AvatarPlaceholder(initial = c.inicial, size = 50.dp)
                }
                Box(
                    Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Success)
                        .border(2.dp, MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape)
                )
            }
            Spacer(Modifier.size(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    c.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    c.relacion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButtonSlot(
                icon = Icons.Filled.Edit,
                onClick = onEdit,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Call, "Llamar", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun InstitucionCard(inst: Institucion) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(inst.icon, null, tint = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.size(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    inst.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    inst.desc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = {},
                shape = Shapes.chip,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text("Llamar", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RedApoyoPreview() {
    AuraTheme { RedApoyoScreen(rememberNav()) }
}

