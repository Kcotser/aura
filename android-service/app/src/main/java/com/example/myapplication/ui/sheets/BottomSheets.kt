package com.example.myapplication.ui.sheets

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.myapplication.data.SessionState
import com.example.myapplication.gesture.VolumeGestureAccessibilityService
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.AvatarPlaceholder
import com.example.myapplication.ui.components.CriticalButton
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryOutlineButton
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Screen
import com.example.myapplication.ui.theme.OnSuccessContainer
import com.example.myapplication.ui.theme.Secondary
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.Success
import com.example.myapplication.ui.theme.SuccessContainer

/** Contenedor común: padding horizontal + espacio inferior para la barra de navegación. */
@Composable
private fun SheetContainer(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.md)
            .navigationBarsPadding()
            .padding(bottom = Spacing.md)
    ) {
        content()
    }
}

/* =============================================================================
 * 1. AGREGAR CONTACTO
 * ========================================================================== */

@Composable
fun AgregarContactoSheetContent(nav: Nav) {
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    SheetContainer {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Agregar Contacto", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { nav.hideSheet() })
        }
        Spacer(Modifier.height(Spacing.md))
        SecondaryOutlineButton(text = "Importar desde contactos", onClick = {})
        Spacer(Modifier.height(Spacing.md))
        AppTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre completo")
        Spacer(Modifier.height(Spacing.sm))
        AppTextField(value = telefono, onValueChange = { telefono = it }, label = "Número de teléfono", isPhone = true)
        Spacer(Modifier.height(Spacing.sm))
        AppTextField(value = "", onValueChange = {}, label = "Relación")
        Spacer(Modifier.height(Spacing.md))
        InfoNote("Este contacto recibirá una notificación por SMS en caso de que actives una alerta SOS de emergencia.")
        Spacer(Modifier.height(Spacing.md))
        PrimaryButton(text = "Agregar contacto", onClick = { nav.hideSheet() }, icon = Icons.Filled.Check)
    }
}

/* =============================================================================
 * 2. EDITAR CONTACTO
 * ========================================================================== */

@Composable
fun EditarContactoSheetContent(nav: Nav) {
    var nombre by remember { mutableStateOf("Elena Aguilar") }
    var telefono by remember { mutableStateOf("+34 600 000 000") }

    SheetContainer {
        Text("Editar Contacto", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "Modifica los detalles de tu contacto de confianza.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.md))
        AppTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre completo")
        Spacer(Modifier.height(Spacing.sm))
        AppTextField(value = telefono, onValueChange = { telefono = it }, label = "Teléfono móvil", isPhone = true)
        Spacer(Modifier.height(Spacing.md))
        PrimaryButton(text = "Actualizar contacto", onClick = { nav.hideSheet() })
        Spacer(Modifier.height(Spacing.sm))
        Text(
            "Eliminar de mi Red de Apoyo",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable { nav.hideSheet() }.padding(Spacing.sm)
        )
        Text(
            "Cancelar",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().clickable { nav.hideSheet() }.padding(Spacing.xs)
        )
    }
}

/* =============================================================================
 * 3. ENVIAR A CONTACTO
 * ========================================================================== */

private data class DestinatarioSheet(val nombre: String, val relacion: String, var seleccionado: Boolean)

@Composable
fun EnviarAContactoSheetContent(nav: Nav) {
    var destinatarios by remember {
        mutableStateOf(
            listOf(
                DestinatarioSheet("Elena García", "Confianza • Círculo Cercano", true),
                DestinatarioSheet("Marta Ruiz", "Hermana • Red de Apoyo", true),
                DestinatarioSheet("Carla Soto", "Amiga • Contacto SOS", false)
            )
        )
    }

    SheetContainer {
        Text("Enviar a Contacto", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Selecciona a quién notificar tu estado actual.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(Spacing.md))

        destinatarios.forEachIndexed { index, d ->
            Surface(
                color = if (d.seleccionado) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = Shapes.card,
                border = if (d.seleccionado) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.card)
                    .clickable {
                        destinatarios = destinatarios.toMutableList().also {
                            it[index] = d.copy(seleccionado = !d.seleccionado)
                        }
                    }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.sm)) {
                    AvatarPlaceholder(initial = d.nombre, size = 44.dp)
                    Spacer(Modifier.size(Spacing.sm))
                    Column(Modifier.weight(1f)) {
                        Text(d.nombre, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text(d.relacion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (d.seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        if (d.seleccionado) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(Modifier.height(Spacing.xs))
        }

        Spacer(Modifier.height(Spacing.sm))
        Text("Previsualización del mensaje", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = Shapes.card, modifier = Modifier.fillMaxWidth()) {
            Text(
                "\"Hola, estoy usando AURA. Mi ubicación actual es compartida contigo por seguridad. Todo está bien, solo quiero que estés al tanto.\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(Spacing.md)
            )
        }
        Spacer(Modifier.height(Spacing.md))
        PrimaryButton(text = "Enviar ahora", onClick = { nav.hideSheet() }, icon = Icons.AutoMirrored.Filled.Send)
    }
}

/* =============================================================================
 * 4. EXPORTAR REPORTE
 * ========================================================================== */

@Composable
fun ExportarReporteSheetContent(nav: Nav) {
    SheetContainer {
        Text("Exportar Reporte", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(
            "Selecciona el medio para compartir el informe de seguridad.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.md))
        InfoNote("Incluye reporte narrativo y enlace cifrado para acceso seguro.")
        Spacer(Modifier.height(Spacing.md))
        ExportOption(Icons.Filled.PictureAsPdf, "Descargar PDF") { nav.hideSheet() }
        Spacer(Modifier.height(Spacing.xs))
        ExportOption(Icons.Filled.Email, "WhatsApp") { nav.hideSheet() }
        Spacer(Modifier.height(Spacing.xs))
        ExportOption(Icons.Filled.Email, "Correo electrónico") { nav.hideSheet() }
        Spacer(Modifier.height(Spacing.xs))
        ExportOption(Icons.AutoMirrored.Filled.Send, "Enviar a Autoridad") { nav.hideSheet() }
        Spacer(Modifier.height(Spacing.md))
        Text(
            "Cancelar",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(Shapes.button)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable { nav.hideSheet() }
                .padding(Spacing.md)
        )
    }
}

@Composable
private fun ExportOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = Shapes.card,
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.card)
            .clickable { onClick() }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.md)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(36.dp).clip(CircleShape).background(SuccessContainer)
            ) {
                Icon(icon, null, tint = OnSuccessContainer, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.size(Spacing.sm))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            Icon(Icons.Filled.Check, null, tint = Color.Transparent)
        }
    }
}

/* =============================================================================
 * 5. ELIMINAR ARCHIVOS (destructivo)
 * ========================================================================== */

@Composable
fun EliminarArchivosSheetContent(nav: Nav) {
    SheetContainer {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.WarningAmber, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "Eliminar Archivos Crudos",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Esta acción es permanente. Se eliminarán todas las grabaciones locales no procesadas de esta sesión para proteger tu privacidad.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.md))
            Surface(color = com.example.myapplication.ui.theme.ErrorContainer, shape = Shapes.card, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.sm)) {
                    Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(Spacing.xs))
                    Text(
                        "Protocolo de limpieza de seguridad activado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(Spacing.md))
            CriticalButton(text = "Sí, eliminar ahora", onClick = { nav.hideSheet() }, icon = Icons.Filled.DeleteForever)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Cancelar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { nav.hideSheet() }.padding(Spacing.sm)
            )
        }
    }
}

/* =============================================================================
 * 6. GESTIÓN DE CUENTA (cerrar sesión / eliminar cuenta)
 * ========================================================================== */

@Composable
fun GestionCuentaSheetContent(nav: Nav) {
    SheetContainer {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("Gestión de Cuenta", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Selecciona una acción para continuar con tu cuenta de AURA.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.md))
            SecondaryOutlineButton(
                text = "Cerrar Sesión",
                onClick = {
                    SessionState.tabsUnlocked = false
                    nav.hideSheet()
                    nav.backStack.clear()
                    nav.push(Screen.Splash)
                }
            )
            Spacer(Modifier.height(Spacing.md))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                androidx.compose.material3.HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                Text(
                    "ZONA DE RIESGO",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = Spacing.sm)
                )
                androidx.compose.material3.HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
            }
            Spacer(Modifier.height(Spacing.md))
            Surface(color = com.example.myapplication.ui.theme.ErrorContainer, shape = Shapes.card, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Al eliminar tu cuenta, se borrarán permanentemente todos tus datos, historial de rutas y contactos de confianza. Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(Spacing.md)
                )
            }
            Spacer(Modifier.height(Spacing.md))
            CriticalButton(text = "Eliminar mi cuenta permanentemente", onClick = { nav.hideSheet() }, icon = Icons.Filled.PersonRemove)
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "Volver a Ajustes",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable { nav.hideSheet() }.padding(Spacing.sm)
            )
        }
    }
}

/* =============================================================================
 * 7. CONFIGURAR ATAJO DE ACTIVACIÓN RÁPIDA
 * ========================================================================== */

private fun isVolumeGestureServiceEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        ?: return false
    return manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any {
            it.resolveInfo.serviceInfo.packageName == context.packageName &&
                it.resolveInfo.serviceInfo.name == VolumeGestureAccessibilityService::class.java.name
        }
}

@Composable
fun ConfigurarAtajoSheetContent(nav: Nav) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var habilitado by remember { mutableStateOf(isVolumeGestureServiceEnabled(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                habilitado = isVolumeGestureServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    SheetContainer {
        Text("Atajo de Activación Rápida", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "Presiona subir y bajar volumen, 3 veces cada uno, para activar la alerta sin abrir la app — incluso con la pantalla bloqueada.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Spacing.md))

        Surface(
            color = if (habilitado) SuccessContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = Shapes.card,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.md)) {
                Icon(
                    if (habilitado) Icons.Filled.CheckCircle else Icons.Filled.WarningAmber,
                    null,
                    tint = if (habilitado) OnSuccessContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.size(Spacing.sm))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (habilitado) "Activado" else "Necesita permiso de Accesibilidad",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (habilitado) OnSuccessContainer else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    if (!habilitado) {
                        Text(
                            "Android exige activarlo a mano en Ajustes del sistema, no lo podemos activar por vos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(Spacing.md))
        if (habilitado) {
            SecondaryOutlineButton(
                text = "Administrar en Ajustes de Accesibilidad",
                onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            )
        } else {
            PrimaryButton(
                text = "Abrir Ajustes de Accesibilidad",
                onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            )
        }
        Spacer(Modifier.height(Spacing.sm))
        Text(
            "Cerrar",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { nav.hideSheet() }
                .padding(Spacing.sm)
        )
    }
}

/* =============================================================================
 * 8. CONFIRMACIÓN DE APROBACIÓN
 * ========================================================================== */

@Composable
fun ConfirmacionAprobacionSheetContent(nav: Nav) {
    SheetContainer {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.height(Spacing.sm))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp).clip(CircleShape).background(SuccessContainer)
            ) {
                Icon(Icons.Filled.CheckCircle, null, tint = Success, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(Spacing.sm))
            Text("Reporte Aprobado", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Text("Guardado en tu historial", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(Spacing.md))
            PrimaryButton(
                text = "Ver en mi Historial",
                onClick = {
                    nav.hideSheet()
                    nav.popToMain()
                }
            )
            Spacer(Modifier.height(Spacing.xs))
            SecondaryOutlineButton(
                text = "Volver a Inicio",
                onClick = {
                    nav.hideSheet()
                    nav.popToMain()
                }
            )
        }
    }
}

/* ---------------------------------------------------------------------------
 * Helpers compartidos
 * ------------------------------------------------------------------------- */

@Composable
private fun InfoNote(text: String) {
    Surface(color = Secondary.copy(alpha = 0.1f), shape = Shapes.card, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(Spacing.md)) {
            Icon(Icons.Filled.Info, null, tint = Secondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(Spacing.xs))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = Secondary)
        }
    }
}
