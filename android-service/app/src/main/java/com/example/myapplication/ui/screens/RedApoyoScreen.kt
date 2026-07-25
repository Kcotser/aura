package com.example.myapplication.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.AuraApplication
import com.example.myapplication.data.ContactoConfianza
import com.example.myapplication.data.ContactosRepository
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

private data class Institucion(
    val nombre: String,
    val desc: String,
    val icon: ImageVector,
    val telefono: String
)

/**
 * Red de Apoyo: contactos de confianza (reales, guardados en el dispositivo vía
 * [com.example.myapplication.data.ContactosRepository]) y directorio institucional.
 *
 * Los botones de llamar abren el marcador con el número puesto, no llaman solos: se usa
 * `ACTION_DIAL` en vez de `ACTION_CALL` para que nadie dispare una llamada sin querer y para no
 * tener que pedir el permiso `CALL_PHONE`.
 */
@Composable
fun RedApoyoScreen(nav: Nav) {
    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication
    val contactos by app.contactosRepository.contactos.collectAsState(initial = emptyList())

    val instituciones = listOf(
        Institucion("Línea 100", "Violencia familiar y sexual", Icons.Filled.SupportAgent, "100"),
        Institucion("PNP", "Emergencias policiales (105)", Icons.Filled.LocalPolice, "105"),
        Institucion("Bomberos", "Emergencias médicas y rescate (116)", Icons.Filled.Shield, "116")
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
                        "${contactos.size}/${ContactosRepository.MAXIMO} ACTIVOS",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 6.dp)
                    )
                }
            }

            if (contactos.isEmpty()) {
                SinContactos()
                Spacer(Modifier.height(Spacing.sm))
            }

            contactos.forEach { c ->
                ContactoCard(
                    c,
                    onEdit = { nav.showSheet(Sheet.EditarContacto(c.id)) },
                    onLlamar = { marcar(context, c.telefono) }
                )
                Spacer(Modifier.height(Spacing.sm))
            }

            // Agregar contacto (borde punteado simulado con borde sólido tenue)
            val hayEspacio = contactos.size < ContactosRepository.MAXIMO
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
                    .clickable(enabled = hayEspacio) { nav.showSheet(Sheet.AgregarContacto) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val tinte = MaterialTheme.colorScheme.primary.copy(alpha = if (hayEspacio) 1f else 0.4f)
                    if (hayEspacio) {
                        Icon(Icons.Filled.Add, null, tint = tinte)
                        Spacer(Modifier.size(Spacing.xs))
                    }
                    Text(
                        if (hayEspacio) "Agregar contacto" else "Llegaste al máximo de contactos",
                        style = MaterialTheme.typography.labelLarge,
                        color = tinte,
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
                InstitucionCard(inst, onLlamar = { marcar(context, inst.telefono) })
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
                        "Tus contactos se guardan solo en este teléfono. El aviso automático al " +
                            "activar el SOS todavía no está conectado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSuccessContainer
                    )
                }
            }
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

/** Estado vacío: sin esto, la pantalla arranca con un botón suelto y sin explicar para qué sirve. */
@Composable
private fun SinContactos() {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = Shapes.card,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(Spacing.md)) {
            Text(
                "Todavía no agregaste a nadie",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Agrega hasta ${ContactosRepository.MAXIMO} personas de confianza para tenerlas " +
                    "a mano cuando las necesites.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Abre el marcador con el número cargado. No llama: la usuaria confirma en su teléfono. */
private fun marcar(context: Context, telefono: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", telefono, null))
    runCatching { context.startActivity(intent) }.onFailure {
        Toast.makeText(context, "No hay una app de teléfono disponible", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun ContactoCard(c: ContactoConfianza, onEdit: () -> Unit, onLlamar: () -> Unit) {
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
                    c.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    c.telefono,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButtonSlot(
                icon = Icons.Filled.Edit,
                onClick = onEdit,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = "Editar contacto"
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { onLlamar() }
            ) {
                Icon(Icons.Filled.Call, "Llamar", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun InstitucionCard(inst: Institucion, onLlamar: () -> Unit) {
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
                onClick = onLlamar,
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

