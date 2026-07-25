package com.example.myapplication.ui.screens

import androidx.biometric.BiometricManager
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.AuraApplication
import com.example.myapplication.data.MetodoAcceso
import com.example.myapplication.ui.components.AuraTopBar
import com.example.myapplication.ui.components.PinDots
import com.example.myapplication.ui.components.PinKeypad
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SelectableOptionCard
import com.example.myapplication.ui.components.SwitchSettingRow
import com.example.myapplication.ui.components.WarningBanner
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.rememberNav
import com.example.myapplication.ui.theme.PrimaryContainer
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.AuraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/* =============================================================================
 * 1. AJUSTES: SEGURIDAD BIOMÉTRICA
 * ========================================================================== */

private enum class ModoAjusteAcceso { NORMAL, CONFIRMAR_PARA_METODO, CONFIRMAR_PARA_PIN, NUEVO_PIN, CONFIRMAR_NUEVO_PIN }

/**
 * Método de acceso único (PIN o biometría, nunca los dos mezclados en la misma vista) +
 * cambio de PIN, que siempre existe como respaldo aunque el método principal sea biometría.
 */
@Composable
fun AjustesBiometricoScreen(nav: Nav) {
    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication
    val scope = rememberCoroutineScope()

    val biometriaDisponible = remember {
        BiometricManager.from(context)
            .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    var metodoActual by remember { mutableStateOf(MetodoAcceso.PIN) }
    var authHistorial by remember { mutableStateOf(false) }
    var modo by remember { mutableStateOf(ModoAjusteAcceso.NORMAL) }
    var metodoPendiente by remember { mutableStateOf<MetodoAcceso?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var nuevoPin by remember { mutableStateOf("") }
    var nuevoPinConfirmar by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        metodoActual = app.profileRepository.profile.first().metodoAcceso
    }

    fun reset() {
        modo = ModoAjusteAcceso.NORMAL
        metodoPendiente = null
        pinInput = ""
        nuevoPin = ""
        nuevoPinConfirmar = ""
        error = false
    }

    LaunchedEffect(pinInput) {
        if (pinInput.length == 4) {
            if (app.pinRepository.verifyPin(pinInput)) {
                when (modo) {
                    ModoAjusteAcceso.CONFIRMAR_PARA_METODO -> {
                        val nuevo = metodoPendiente
                        if (nuevo != null) {
                            scope.launch { app.profileRepository.setMetodoAcceso(nuevo) }
                            metodoActual = nuevo
                        }
                        reset()
                    }
                    ModoAjusteAcceso.CONFIRMAR_PARA_PIN -> {
                        pinInput = ""
                        modo = ModoAjusteAcceso.NUEVO_PIN
                    }
                    else -> reset()
                }
            } else {
                error = true
                delay(500)
                error = false
                pinInput = ""
            }
        }
    }

    LaunchedEffect(nuevoPin) {
        if (modo == ModoAjusteAcceso.NUEVO_PIN && nuevoPin.length == 4) {
            delay(150)
            modo = ModoAjusteAcceso.CONFIRMAR_NUEVO_PIN
        }
    }

    LaunchedEffect(nuevoPinConfirmar) {
        if (modo == ModoAjusteAcceso.CONFIRMAR_NUEVO_PIN && nuevoPinConfirmar.length == 4) {
            if (nuevoPinConfirmar == nuevoPin) {
                app.pinRepository.setPin(nuevoPin)
                reset()
            } else {
                error = true
                delay(500)
                error = false
                nuevoPinConfirmar = ""
            }
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuraTopBar(
            title = "Ajustes",
            onBack = { if (modo != ModoAjusteAcceso.NORMAL) reset() else nav.pop() }
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
        ) {
            when (modo) {
                ModoAjusteAcceso.NORMAL -> {
                    Spacer(Modifier.height(Spacing.md))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainer.copy(alpha = 0.18f))
                        ) {
                            Icon(Icons.Filled.Fingerprint, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        }
                        Spacer(Modifier.height(Spacing.sm))
                        Text("Acceso a la app", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text(
                            "Elige cómo desbloquear AURA. Solo un método a la vez.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(Spacing.lg))

                    SelectableOptionCard(
                        title = "Huella / Face ID",
                        subtitle = if (biometriaDisponible) "Acceso instantáneo y seguro" else "No configurada en este dispositivo",
                        selected = metodoActual == MetodoAcceso.BIOMETRICO,
                        onClick = {
                            if (biometriaDisponible && metodoActual != MetodoAcceso.BIOMETRICO) {
                                metodoPendiente = MetodoAcceso.BIOMETRICO
                                modo = ModoAjusteAcceso.CONFIRMAR_PARA_METODO
                            }
                        }
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    SelectableOptionCard(
                        title = "Código PIN",
                        subtitle = "Código numérico de 4 dígitos",
                        selected = metodoActual == MetodoAcceso.PIN,
                        onClick = {
                            if (metodoActual != MetodoAcceso.PIN) {
                                metodoPendiente = MetodoAcceso.PIN
                                modo = ModoAjusteAcceso.CONFIRMAR_PARA_METODO
                            }
                        }
                    )
                    Spacer(Modifier.height(Spacing.sm))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = Shapes.card,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(Shapes.card)
                            .clickable { modo = ModoAjusteAcceso.CONFIRMAR_PARA_PIN }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.md)) {
                            Icon(Icons.Filled.Dialpad, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.size(Spacing.sm))
                            Column(Modifier.weight(1f)) {
                                Text("Cambiar PIN", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "El PIN siempre queda disponible, aunque uses biometría.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(Spacing.sm))

                    SwitchSettingRow(
                        title = "Autenticar al abrir Historial",
                        subtitle = "Privacidad máxima para tus rutas",
                        checked = authHistorial,
                        onCheckedChange = { authHistorial = it }
                    )
                    Spacer(Modifier.height(Spacing.md))

                    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = Shapes.card, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(Spacing.md)) {
                            Icon(Icons.Filled.Fingerprint, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(Spacing.sm))
                            Text(
                                "Tu PIN y tus datos biométricos se guardan cifrados solo en este dispositivo. AURA no los envía ni los almacena en servidores.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(Spacing.lg))
                }

                ModoAjusteAcceso.CONFIRMAR_PARA_METODO, ModoAjusteAcceso.CONFIRMAR_PARA_PIN -> {
                    Spacer(Modifier.height(Spacing.xl))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Ingresa tu PIN actual", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(Spacing.xs))
                        Text("Para confirmar este cambio.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(Spacing.lg))
                        PinDots(length = pinInput.length, error = error)
                        Spacer(Modifier.height(Spacing.lg))
                        PinKeypad(
                            onDigit = { d -> if (pinInput.length < 4) pinInput += d },
                            onBackspace = { if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1) }
                        )
                    }
                }

                ModoAjusteAcceso.NUEVO_PIN -> {
                    Spacer(Modifier.height(Spacing.xl))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Crea tu nuevo PIN", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(Spacing.lg))
                        PinDots(length = nuevoPin.length, error = error)
                        Spacer(Modifier.height(Spacing.lg))
                        PinKeypad(
                            onDigit = { d -> if (nuevoPin.length < 4) nuevoPin += d },
                            onBackspace = { if (nuevoPin.isNotEmpty()) nuevoPin = nuevoPin.dropLast(1) }
                        )
                    }
                }

                ModoAjusteAcceso.CONFIRMAR_NUEVO_PIN -> {
                    Spacer(Modifier.height(Spacing.xl))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Confirma tu nuevo PIN", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(Spacing.lg))
                        PinDots(length = nuevoPinConfirmar.length, error = error)
                        Spacer(Modifier.height(Spacing.lg))
                        PinKeypad(
                            onDigit = { d -> if (nuevoPinConfirmar.length < 4) nuevoPinConfirmar += d },
                            onBackspace = { if (nuevoPinConfirmar.isNotEmpty()) nuevoPinConfirmar = nuevoPinConfirmar.dropLast(1) }
                        )
                    }
                }
            }
        }
    }
}

/* =============================================================================
 * 2. CALIBRACIÓN DE AUDIO Y VIDEO
 * ========================================================================== */

@Composable
fun CalibracionAVScreen(nav: Nav) {
    var resolucion by remember { mutableFloatStateOf(0.5f) }
    var priorizarEnvio by remember { mutableStateOf(true) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuraTopBar(title = "Ajustes", onBack = { nav.pop() })
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
        ) {
            Text("Calibración de Audio y Video", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Configura la calidad de transmisión para situaciones de emergencia.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.md))

            Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest, shape = Shapes.card, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Spacing.md)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Resolución", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Icon(Icons.Filled.Speed, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = resolucion,
                        onValueChange = { resolucion = it },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ahorro", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Balanceado", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text("Máxima", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Surface(color = PrimaryContainer.copy(alpha = 0.15f), shape = Shapes.card, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(Spacing.sm)) {
                            Icon(Icons.Filled.Bolt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(Spacing.xs))
                            Column {
                                Text("Modo Ahorro Activo", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                                Text("Envío de video 3x más rápido en zonas de baja cobertura.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(Spacing.sm))

            SwitchSettingRow(
                title = "Priorizar envío rápido",
                subtitle = "Inicia el streaming antes aunque la calidad sea menor inicialmente.",
                checked = priorizarEnvio,
                onCheckedChange = { priorizarEnvio = it }
            )

            Spacer(Modifier.height(Spacing.sm))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(Shapes.card)
                    .background(androidx.compose.ui.graphics.Color(0xFF1B1F26))
            )

            Spacer(Modifier.height(Spacing.lg))
            PrimaryButton(text = "Guardar configuración", onClick = { nav.pop() }, icon = Icons.Filled.Save)
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

/* =============================================================================
 * 3. POLÍTICAS DE AUTODESTRUCCIÓN
 * ========================================================================== */

@Composable
fun PoliticasAutodestruccionScreen(nav: Nav) {
    var seleccion by remember { mutableStateOf(0) }
    val opciones = listOf(
        Triple("Eliminar tras exportar", "Recomendado para máxima privacidad", true),
        Triple("30 días", null as String?, false),
        Triple("90 días", null as String?, false),
        Triple("Nunca", null as String?, false)
    )

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AuraTopBar(title = "Ajustes", onBack = { nav.pop() })
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
        ) {
            Text("Políticas de Autodestrucción", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Configura cuánto tiempo deseas conservar tus datos de seguridad antes de que se eliminen permanentemente.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.md))

            opciones.forEachIndexed { index, (titulo, subtitulo, _) ->
                SelectableOptionCard(
                    title = titulo,
                    subtitle = subtitulo,
                    selected = seleccion == index,
                    onClick = { seleccion = index }
                )
                Spacer(Modifier.height(Spacing.sm))
            }

            Spacer(Modifier.height(Spacing.sm))
            WarningBanner(
                title = "No recuperable",
                text = "Una vez transcurrido el tiempo seleccionado, los archivos se borrarán de forma segura y no podrán ser recuperados ni por ti ni por AURA."
            )

            Spacer(Modifier.height(Spacing.lg))
            PrimaryButton(text = "Guardar política", onClick = { nav.pop() })
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AjustesBiometricoPreview() {
    AuraTheme { AjustesBiometricoScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CalibracionAVPreview() {
    AuraTheme { CalibracionAVScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun PoliticasAutodestruccionPreview() {
    AuraTheme { PoliticasAutodestruccionScreen(rememberNav()) }
}
