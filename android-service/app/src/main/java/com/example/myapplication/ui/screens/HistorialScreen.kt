package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.IconButtonSlot
import com.example.myapplication.ui.components.AuraTopBar
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.Success
import com.example.myapplication.ui.theme.AuraTheme
import com.example.myapplication.ui.theme.Warning
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Screen
import com.example.myapplication.ui.nav.rememberNav

private data class Caso(
    val titulo: String,
    val estado: String,
    val estadoColor: Color,
    val descripcion: String,
    val fecha: String,
    val metaIcon: ImageVector,
    val meta: String
)

@Composable
fun HistorialScreen(nav: Nav) {
    val primary = MaterialTheme.colorScheme.primary
    val casos = remember {
        listOf(
            Caso("Caso #4829 - Incidente nocturno", "En proceso", Warning,
                "Grabación de audio iniciada y ubicación compartida en tiempo real.",
                "Hoy, 22:45", Icons.Filled.LocationOn, "C. Mayor, 12"),
            Caso("Reporte: Acoso Callejero", "Borrador listo", primary,
                "Evidencias recopiladas y testimonio transcrito por Aura.",
                "14 Oct 2023", Icons.Filled.Mic, "4:20 min"),
            Caso("Denuncia Finalizada", "Exportado", Success,
                "Documento legal generado y enviado a los contactos de confianza.",
                "10 Oct 2023", Icons.Filled.IosShare, "PDF, MP3"),
            Caso("Incidente Parada Bus", "Borrador listo", primary,
                "Descripción de agresor y vehículo guardada en la caja fuerte digital.",
                "02 Oct 2023", Icons.Filled.Shield, "Protegido")
        )
    }

    val filtros = listOf("Todos", "En proceso", "Borrador listo", "Exportado")
    var filtroSel by remember { mutableStateOf("Todos") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(Modifier.background(MaterialTheme.colorScheme.background)) {
            Column {
                AuraTopBar(
                    title = "Historial",
                    actions = {
                        IconButtonSlot(icon = Icons.Filled.Search, onClick = {})
                    }
                )
            }
        }

        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = Spacing.md, end = Spacing.md, bottom = Spacing.md
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            item {
                // Barra de búsqueda (visual)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = Shapes.input,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = Spacing.md)
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.size(Spacing.sm))
                        Text(
                            "Buscar por título o fecha...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                // Chips de filtro
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    filtros.forEach { f ->
                        FiltroChip(
                            text = f,
                            selected = f == filtroSel,
                            onClick = { filtroSel = f }
                        )
                    }
                }
            }

            items(casos) { caso ->
                CasoCard(caso, onClick = { nav.push(Screen.DetalleDeCaso) })
            }
        }
    }
}

@Composable
private fun FiltroChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = Shapes.chip,
        modifier = Modifier
            .height(40.dp)
            .clip(Shapes.chip)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = Spacing.md)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CasoCard(caso: Caso, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = Shapes.card,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.card)
            .clickable { onClick() }
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            // Borde de color a la izquierda
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(caso.estadoColor)
            )
            Column(Modifier.padding(Spacing.md)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        caso.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.size(Spacing.xs))
                    Text(
                        caso.estado.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = caso.estadoColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    caso.descripcion,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spacing.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MetaItem(Icons.Filled.CalendarMonth, caso.fecha)
                    Spacer(Modifier.size(Spacing.md))
                    MetaItem(caso.metaIcon, caso.meta)
                }
            }
        }
    }
}

@Composable
private fun MetaItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HistorialPreview() {
    AuraTheme { HistorialScreen(rememberNav()) }
}

