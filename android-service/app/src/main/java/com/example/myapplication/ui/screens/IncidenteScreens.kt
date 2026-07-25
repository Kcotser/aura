package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.AuraTopBar
import com.example.myapplication.ui.components.CriticalButton
import com.example.myapplication.ui.components.IconButtonSlot
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryOutlineButton
import com.example.myapplication.ui.components.StatusChip
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Screen
import com.example.myapplication.ui.nav.Sheet
import com.example.myapplication.ui.nav.rememberNav
import com.example.myapplication.ui.theme.ErrorColor
import com.example.myapplication.ui.theme.OnSuccessContainer
import com.example.myapplication.ui.theme.Secondary
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.Success
import com.example.myapplication.ui.theme.SuccessContainer
import com.example.myapplication.ui.theme.AuraTheme

/* =============================================================================
 * 1. DETALLE DE CASO
 * ========================================================================== */

@Composable
fun DetalleDeCasoScreen(nav: Nav) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuraTopBar(
            title = "Historial",
            onBack = { nav.pop() },
            actions = {
                Text(
                    "Caso #0047",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = Spacing.sm)
                )
            }
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = Shapes.card,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(Spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Detalle de Incidente", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("14 Octubre 2023 • 22:15", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusChip(text = "Exportado", contentColor = OnSuccessContainer, containerColor = SuccessContainer)
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, tint = Secondary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Av. Insurgentes Sur 1450, CDMX", style = MaterialTheme.typography.bodyMedium, color = Secondary)
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Grabaciones de Video", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Text("EVIDENCIA CRUDA", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                VideoThumb("CÁMARA FRONTAL", Modifier.weight(1f)) { nav.push(Screen.VisorMultimedia) }
                VideoThumb("CÁMARA TRASERA", Modifier.weight(1f)) { nav.push(Screen.VisorMultimedia) }
            }

            Spacer(Modifier.height(Spacing.lg))
            Text("Registro de Audio", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(Spacing.sm))
            Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest, shape = Shapes.card, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.md)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.PlayCircle, null, tint = Color.White)
                    }
                    Spacer(Modifier.size(Spacing.md))
                    Column(Modifier.weight(1f)) {
                        // Onda de audio simulada
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            listOf(8, 16, 10, 22, 14, 18, 9, 20, 12, 16, 8).forEach { h ->
                                Box(Modifier.width(3.dp).height(h.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("0:00", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("0:42", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            Text("Documentos", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(Spacing.sm))
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = Shapes.card,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.card)
                    .clickable { nav.showSheet(Sheet.ExportarReporte) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.md)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(com.example.myapplication.ui.theme.ErrorContainer)
                    ) {
                        Icon(Icons.Filled.PictureAsPdf, null, tint = ErrorColor)
                    }
                    Spacer(Modifier.size(Spacing.md))
                    Column(Modifier.weight(1f)) {
                        Text("Reporte_Caso_0047.pdf", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text("Generado automáticamente • 1.2 MB", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            PrimaryButton(text = "Descargar Todo el Paquete", onClick = {}, icon = Icons.Filled.Download)
            Spacer(Modifier.height(Spacing.sm))
            CriticalButton(text = "Solicitar eliminación", onClick = { nav.showSheet(Sheet.EliminarArchivos) })
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "La eliminación es irreversible una vez aprobada por el sistema de seguridad.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Composable
private fun VideoThumb(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(140.dp)
            .clip(Shapes.card)
            .background(Color(0xFF1B1F26))
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.25f))
        ) {
            Icon(Icons.Filled.PlayCircle, null, tint = Color.White)
        }
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(Spacing.xs)
        )
    }
}

/* =============================================================================
 * 2. FICHA DEL INCIDENTE (borrador generado por IA)
 * ========================================================================== */

@Composable
fun FichaIncidenteScreen(nav: Nav) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuraTopBar(
            title = "Borrador del Incidente",
            onBack = { nav.popToMain() },
            actions = {
                StatusChip(
                    text = "Pendiente",
                    contentColor = com.example.myapplication.ui.theme.Warning,
                    containerColor = com.example.myapplication.ui.theme.WarningContainer
                )
            }
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
        ) {
            SectionCard(title = "Datos Generales", icon = Icons.Filled.Description) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Fecha y Hora", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("24 Oct, 2023 • 22:15", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Precisión GPS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.GpsFixed, null, tint = Success, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.size(4.dp))
                            Text("Alta (3m)", style = MaterialTheme.typography.bodyLarge, color = Success, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.sm))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(Shapes.input)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                )
                Spacer(Modifier.height(Spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("Calle de la Victoria, 12, Madrid", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(Modifier.height(Spacing.md))
            SectionCard(title = "Transcripción", icon = Icons.Filled.Person) {
                TranscripcionBubble("Víctima", "\"Por favor, aléjate de mí. No te conozco y me estás siguiendo desde el metro.\"", isAgresor = false)
                Spacer(Modifier.height(Spacing.sm))
                TranscripcionBubble("Agresor", "\"[Inaudible] ...solo quiero hablar contigo, no te pongas así.\"", isAgresor = true)
            }

            Spacer(Modifier.height(Spacing.md))
            SectionCard(title = "Análisis del Entorno", icon = Icons.Filled.Warning) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Box(Modifier.weight(1f).height(90.dp).clip(Shapes.input).background(Color(0xFF1B1F26)))
                    Box(Modifier.weight(1f).height(90.dp).clip(Shapes.input).background(Color(0xFF1B1F26)))
                }
                Spacer(Modifier.height(Spacing.sm))
                InfoStripe("Descripción del Agresor", "Varón, aprox. 1.80m, complexión media. Viste sudadera oscura con capucha y pantalones vaqueros.", MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(Spacing.xs))
                InfoStripe("Entorno Detectado", "Zona poco iluminada, escaso tránsito peatonal. Presencia de cámaras de seguridad a 50 metros.", Secondary)
            }

            Spacer(Modifier.height(Spacing.md))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                shape = Shapes.card,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(Spacing.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccountBalance, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.size(Spacing.xs))
                        Text("Entidad de Apoyo Sugerida", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        StatusChip(text = "Recomendado", contentColor = Color.White, containerColor = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.AccountBalance, null, tint = Color.White)
                        }
                        Spacer(Modifier.size(Spacing.sm))
                        Column(Modifier.weight(1f)) {
                            Text("Unidad de Atención a la Familia", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("Especialistas en violencia de género", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(Spacing.md))
        }

        // Barra de acciones inferior
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = 8.dp) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm)
            ) {
                ActionTab(Icons.Filled.Edit, "Editar") { nav.push(Screen.EditarIncidente) }
                ActionTab(Icons.Filled.Check, "Aprobar", highlighted = true) { nav.showSheet(Sheet.ConfirmacionAprobacion) }
                ActionTab(Icons.Filled.PictureAsPdf, "PDF") {}
                ActionTab(Icons.AutoMirrored.Filled.Send, "Enviar", accent = true) { nav.showSheet(Sheet.EnviarAContacto) }
            }
        }
    }
}

@Composable
private fun ActionTab(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, highlighted: Boolean = false, accent: Boolean = false, onClick: () -> Unit) {
    val bg = when {
        highlighted -> MaterialTheme.colorScheme.primary
        accent -> Secondary
        else -> Color.Transparent
    }
    val fg = if (highlighted || accent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(Shapes.button)
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = Spacing.md, vertical = Spacing.xs)
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = fg, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SectionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest, shape = Shapes.card, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(Spacing.xs))
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(Spacing.sm))
            content()
        }
    }
}

@Composable
private fun TranscripcionBubble(who: String, text: String, isAgresor: Boolean) {
    Row(
        horizontalArrangement = if (isAgresor) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            color = if (isAgresor) com.example.myapplication.ui.theme.ErrorContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = Shapes.card,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Column(Modifier.padding(Spacing.sm)) {
                Text(
                    who,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isAgresor) ErrorColor else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun InfoStripe(title: String, text: String, color: Color) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(color))
        Column(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .background(color.copy(alpha = 0.08f))
                .padding(start = Spacing.sm, top = Spacing.xs, bottom = Spacing.xs, end = Spacing.sm)
        ) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = color, fontWeight = FontWeight.Bold)
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

/* =============================================================================
 * 3. EDITAR DATOS DEL INCIDENTE
 * ========================================================================== */

@Composable
fun EditarIncidenteScreen(nav: Nav) {
    var fecha by remember { mutableStateOf("10/27/2023, 10:30 PM") }
    var ubicacion by remember { mutableStateOf("Calle de la Princesa, 25, Madrid") }
    var transcripcion by remember {
        mutableStateOf("Me sentí seguida por un hombre de chaqueta oscura desde la salida del metro. El entorno estaba poco iluminado y no había mucha gente alrededor.")
    }
    var entorno by remember { mutableStateOf("Poca iluminación, zona solitaria, salida de metro cercana.") }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuraTopBar(title = "Editar Incidente", onBack = { nav.pop() })
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
        ) {
            AppTextField(value = fecha, onValueChange = { fecha = it }, label = "Fecha y hora")
            Spacer(Modifier.height(Spacing.md))
            Text("Ubicación del Incidente", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(Spacing.xs))
            Box(Modifier.fillMaxWidth().height(160.dp).clip(Shapes.card).background(MaterialTheme.colorScheme.surfaceContainerHigh))
            Spacer(Modifier.height(Spacing.xs))
            AppTextField(value = ubicacion, onValueChange = { ubicacion = it }, label = "Dirección")

            Spacer(Modifier.height(Spacing.md))
            AppTextField(value = transcripcion, onValueChange = { transcripcion = it }, label = "Transcripción de voz (IA)", singleLine = false, minLines = 4)
            Spacer(Modifier.height(Spacing.md))
            AppTextField(value = entorno, onValueChange = { entorno = it }, label = "Entorno y Detalles", singleLine = false, minLines = 2)

            Spacer(Modifier.height(Spacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                listOf("Calle solitaria", "Noche", "Acoso").forEach {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), shape = Shapes.chip) {
                        Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 6.dp))
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))
            Text("Entidad Sugerida para Reporte", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(Spacing.xs))
            Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = Shapes.input, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Policía Nacional - Unidad de Atención a la Familia",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(Spacing.md)
                )
            }
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "* AURA sugiere estas entidades basándose en la gravedad y naturaleza del incidente detectado.",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.lg))
        }
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest, shadowElevation = 8.dp) {
            Row(Modifier.fillMaxWidth().padding(Spacing.md), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                SecondaryOutlineButton(text = "Descartar", onClick = { nav.pop() }, modifier = Modifier.weight(1f))
                PrimaryButton(text = "Guardar cambios", onClick = { nav.pop() }, icon = Icons.Filled.Check, modifier = Modifier.weight(1f))
            }
        }
    }
}

/* =============================================================================
 * 4. VISOR MULTIMEDIA
 * ========================================================================== */

@Composable
fun VisorMultimediaScreen(nav: Nav) {
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(Spacing.sm)
        ) {
            IconButtonSlot(icon = Icons.AutoMirrored.Filled.ArrowBack, onClick = { nav.pop() }, tint = Color.White)
            Text(
                "Evidencia_20231024",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                modifier = Modifier.weight(1f).padding(start = Spacing.xs)
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFF14151A))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Filled.PlayCircle, null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }

        Column(Modifier.padding(Spacing.md)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("01:24", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                Text("03:12", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
            }
            androidx.compose.material3.LinearProgressIndicator(
                progress = { 0.42f },
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "Sin editar · Hash integridad",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f)
            )
            Text(
                "sha256: 8f92b...e4a1c",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.35f)
            )
            Spacer(Modifier.height(Spacing.md))
            SecondaryOutlineButton(
                text = "Descargar archivo original",
                onClick = {},
                borderColor = Color.White.copy(alpha = 0.4f),
                contentColor = Color.White
            )
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DetalleDeCasoPreview() {
    AuraTheme { DetalleDeCasoScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun FichaIncidentePreview() {
    AuraTheme { FichaIncidenteScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun EditarIncidentePreview() {
    AuraTheme { EditarIncidenteScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun VisorMultimediaPreview() {
    AuraTheme { VisorMultimediaScreen(rememberNav()) }
}
