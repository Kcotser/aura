package com.example.myapplication.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.myapplication.AuraApplication
import com.example.myapplication.data.MetodoAcceso
import com.example.myapplication.gesture.VolumeKey
import com.example.myapplication.gesture.VolumeKeyBus
import com.example.myapplication.ui.components.AuraTopBar
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Screen
import com.example.myapplication.ui.nav.rememberNav
import com.example.myapplication.ui.theme.OnSuccessContainer
import com.example.myapplication.ui.theme.PrimaryContainer
import com.example.myapplication.ui.theme.Secondary
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.SuccessContainer
import com.example.myapplication.ui.theme.AuraTheme
import kotlinx.coroutines.launch

/* =============================================================================
 * 1. PERMISOS ESENCIALES
 * ========================================================================== */

private data class Permiso(val icon: ImageVector, val titulo: String, val desc: String, val permission: String)

@Composable
fun PermisosEsencialesScreen(nav: Nav) {
    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication
    val scope = rememberCoroutineScope()

    val permisos = remember {
        buildList {
            add(Permiso(Icons.Filled.CameraAlt, "Cámara", "Para transmisiones de emergencia seguras.", Manifest.permission.CAMERA))
            add(Permiso(Icons.Filled.Mic, "Audio", "Detección de palabras clave de auxilio.", Manifest.permission.RECORD_AUDIO))
            add(Permiso(Icons.Filled.LocationOn, "Ubicación", "Seguimiento en tiempo real por tu red.", Manifest.permission.ACCESS_FINE_LOCATION))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Permiso(Icons.Filled.Notifications, "Notificaciones", "Para avisarte del estado de tus alertas.", Manifest.permission.POST_NOTIFICATIONS))
            }
        }
    }

    fun concedidosActuales(): Set<Int> = permisos.indices.filter { i ->
        ContextCompat.checkSelfPermission(context, permisos[i].permission) == PackageManager.PERMISSION_GRANTED
    }.toSet()

    var concedidos by remember { mutableStateOf(emptySet<Int>()) }
    LaunchedEffect(Unit) { concedidos = concedidosActuales() }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        concedidos = concedidosActuales()
        scope.launch {
            app.profileRepository.setPermisosSolicitados(true)
            nav.push(Screen.AccesoBiometrico)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuraTopBar(title = "AURA", onBack = { nav.pop() })
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
        ) {
            Text(
                "Para protegerte, necesitamos acceso a:",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Tu seguridad depende de nuestra capacidad para actuar rápido en situaciones críticas.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.lg))

            permisos.forEachIndexed { index, p ->
                val checked = index in concedidos
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shape = Shapes.card,
                    border = if (checked) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Shapes.card)
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
                                .background(PrimaryContainer.copy(alpha = 0.15f))
                        ) {
                            Icon(p.icon, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.size(Spacing.md))
                        Column(Modifier.weight(1f)) {
                            Text(
                                p.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                p.desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (checked) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .border(
                                    1.5.dp,
                                    if (checked) Color.Transparent else MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                        ) {
                            if (checked) {
                                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(Spacing.sm))
            }

            Spacer(Modifier.height(Spacing.sm))
            // Ilustración de marca
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(Shapes.card)
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.surfaceContainerHighest)
                        )
                    )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Shield,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        "PERMISOS ESENCIALES",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            PrimaryButton(
                text = "Otorgar permisos y continuar",
                onClick = { launcher.launch(permisos.map { it.permission }.toTypedArray()) },
                icon = Icons.AutoMirrored.Filled.ArrowForward
            )
            Spacer(Modifier.height(Spacing.md))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text(
                    "PRIVACIDAD ENCRIPTADA",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

/* =============================================================================
 * 2. ACCESO BIOMÉTRICO
 * ========================================================================== */

@Composable
fun AccesoBiometricoScreen(nav: Nav) {
    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication
    val scope = rememberCoroutineScope()

    val biometriaDisponible = remember {
        BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }
    var usarBiometria by remember { mutableStateOf(biometriaDisponible) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuraTopBar(title = "AURA", onBack = { nav.pop() })
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.md)
        ) {
            Spacer(Modifier.height(Spacing.xl))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer.copy(alpha = 0.2f))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                ) {
                    Icon(
                        Icons.Filled.Fingerprint,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(
                "Protege tu app",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Configura un acceso seguro para garantizar que solo tú puedas ver tu información.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.lg))

            AuthOptionRow(
                icon = Icons.Filled.Fingerprint,
                title = "Usar Huella / Face ID",
                subtitle = if (biometriaDisponible) "Acceso instantáneo y seguro" else "No configurada en este dispositivo",
                badge = if (biometriaDisponible) "MÁS RÁPIDO" else null,
                selected = usarBiometria,
                onClick = { if (biometriaDisponible) usarBiometria = true }
            )
            Spacer(Modifier.height(Spacing.sm))
            AuthOptionRow(
                icon = Icons.Filled.Dialpad,
                title = "Usar Código PIN",
                subtitle = "Código numérico de 4 dígitos",
                badge = null,
                selected = !usarBiometria,
                onClick = { usarBiometria = false }
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "Si eliges Huella/Face ID igual vas a configurar un PIN, por si la biometría falla.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.weight(1f))
            PrimaryButton(
                text = "Confirmar y continuar",
                onClick = {
                    val metodo = if (usarBiometria) MetodoAcceso.BIOMETRICO else MetodoAcceso.PIN
                    scope.launch {
                        app.profileRepository.setMetodoAcceso(metodo)
                        nav.push(Screen.CrearPin)
                    }
                },
                icon = Icons.AutoMirrored.Filled.ArrowForward
            )
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun AuthOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) PrimaryContainer.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = Shapes.card,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(Shapes.card)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.md)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.size(Spacing.md))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (badge != null) {
                Surface(color = SuccessContainer, shape = Shapes.chip) {
                    Text(
                        badge,
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSuccessContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = Spacing.xs, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.size(Spacing.xs))
            }
            Icon(
                if (selected) Icons.Filled.CheckCircle else Icons.Filled.CheckCircle,
                null,
                tint = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
            )
        }
    }
}

/* =============================================================================
 * 3. CALIBRACIÓN DE GESTO RÁPIDO
 * ========================================================================== */

private const val VENTANA_GESTO_MS = 4000L
private const val PULSACIONES_REQUERIDAS = 3

private fun vibrarCorto(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(150)
    }
}

@Composable
fun CalibracionGestoRapidoScreen(nav: Nav) {
    val context = LocalContext.current
    var subeCount by remember { mutableStateOf(0) }
    var bajaCount by remember { mutableStateOf(0) }
    var ultimoEvento by remember { mutableStateOf(0L) }
    var detectado by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        VolumeKeyBus.events.collect { key ->
            val ahora = System.currentTimeMillis()
            if (ahora - ultimoEvento > VENTANA_GESTO_MS) {
                subeCount = 0
                bajaCount = 0
            }
            ultimoEvento = ahora
            when (key) {
                VolumeKey.UP -> subeCount = (subeCount + 1).coerceAtMost(PULSACIONES_REQUERIDAS)
                VolumeKey.DOWN -> bajaCount = (bajaCount + 1).coerceAtMost(PULSACIONES_REQUERIDAS)
            }
            if (subeCount >= PULSACIONES_REQUERIDAS && bajaCount >= PULSACIONES_REQUERIDAS && !detectado) {
                detectado = true
                vibrarCorto(context)
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AuraTopBar(
            title = "AURA",
            onBack = { nav.pop() },
            actions = {
                Surface(color = Secondary.copy(alpha = 0.15f), shape = Shapes.chip) {
                    Text(
                        "Modo Seguro",
                        style = MaterialTheme.typography.labelMedium,
                        color = Secondary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 6.dp)
                    )
                }
            }
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
        ) {
            Text(
                "Tu gesto de activación",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Presiona subir y bajar volumen, 3 veces cada uno, para pedir ayuda sin mirar la pantalla.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.md))

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = Shapes.card,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md)
                ) {
                    Text(
                        "Zona de práctica",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Probalo ahora con los botones físicos de tu teléfono",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(Spacing.md))
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(
                                if (detectado) SuccessContainer
                                else PrimaryContainer.copy(alpha = 0.15f)
                            )
                    ) {
                        Icon(
                            if (detectado) Icons.Filled.Check else Icons.AutoMirrored.Filled.VolumeUp,
                            null,
                            tint = if (detectado) OnSuccessContainer else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    Spacer(Modifier.height(Spacing.md))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                        ContadorGesto(label = "Subir volumen", cuenta = subeCount, total = PULSACIONES_REQUERIDAS)
                        ContadorGesto(label = "Bajar volumen", cuenta = bajaCount, total = PULSACIONES_REQUERIDAS)
                    }
                }
            }

            Spacer(Modifier.height(Spacing.md))
            listOf(
                "Presiona el botón de subir volumen 3 veces.",
                "Presiona el botón de bajar volumen 3 veces.",
                "El orden no importa; cuando completes ambos, el teléfono vibra."
            ).forEachIndexed { i, step ->
                Row(modifier = Modifier.padding(vertical = Spacing.xs)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(PrimaryContainer.copy(alpha = 0.3f))
                    ) {
                        Text("${i + 1}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.size(Spacing.sm))
                    Text(step, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(Modifier.height(Spacing.lg))
            PrimaryButton(
                text = "Guardar configuración",
                onClick = { nav.push(Screen.OnboardingCompletado) },
                icon = Icons.Filled.Check
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "Podrás practicar esto de nuevo en Ajustes en cualquier momento.",
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
private fun ContadorGesto(label: String, cuenta: Int, total: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(total) { i ->
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (i < cuenta) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/* =============================================================================
 * 4. ONBOARDING COMPLETADO
 * ========================================================================== */

@Composable
fun OnboardingCompletadoScreen(nav: Nav) {
    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication
    val scope = rememberCoroutineScope()
    val gradient = Brush.verticalGradient(listOf(Color(0xFF3F0075), Color(0xFF2B0052)))
    Box(
        Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.xl))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(
                "Estás protegida",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "AURA está lista para actuar por ti",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f)
            )

            Spacer(Modifier.weight(1f))

            Surface(
                color = Color.White.copy(alpha = 0.08f),
                shape = Shapes.card,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(Spacing.md)) {
                    ResumenRow("Permisos", "Activo")
                    ResumenDivider()
                    ResumenRow("Biométrico", "Vinculado")
                    ResumenDivider()
                    ResumenRow("Contactos", "+1 agregado")
                }
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    scope.launch {
                        app.profileRepository.setOnboardingCompletado(true)
                        nav.popToMain()
                    }
                },
                shape = Shapes.button,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Ir a Inicio", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "Tu seguridad es nuestra prioridad.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun ResumenRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Icon(Icons.Filled.CheckCircle, null, tint = com.example.myapplication.ui.theme.Success, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.size(Spacing.sm))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = Color.White, modifier = Modifier.weight(1f))
        Surface(color = Color.White.copy(alpha = 0.12f), shape = Shapes.chip) {
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                color = com.example.myapplication.ui.theme.Success,
                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ResumenDivider() {
    androidx.compose.material3.HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PermisosPreview() {
    AuraTheme { PermisosEsencialesScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun BiometricoPreview() {
    AuraTheme { AccesoBiometricoScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CalibracionGestoPreview() {
    AuraTheme { CalibracionGestoRapidoScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingCompletadoPreview() {
    AuraTheme { OnboardingCompletadoScreen(rememberNav()) }
}
