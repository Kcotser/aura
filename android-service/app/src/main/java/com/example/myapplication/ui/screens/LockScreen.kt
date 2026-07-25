package com.example.myapplication.ui.screens

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.AuraApplication
import com.example.myapplication.data.MetodoAcceso
import com.example.myapplication.ui.components.PinDots
import com.example.myapplication.ui.components.PinKeypad
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.AuraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}

/**
 * Candado reutilizable: biometría (con fallback a PIN tras varios fallos) o PIN directo.
 * No navega por sí mismo — quien lo usa decide qué hacer al desbloquear vía [onUnlocked].
 */
@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication
    val activity = remember { context.findFragmentActivity() }

    var metodo by remember { mutableStateOf(MetodoAcceso.PIN) }
    var mostrarPin by remember { mutableStateOf(false) }
    var intentosFallidos by remember { mutableIntStateOf(0) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val perfil = app.profileRepository.profile.first()
        metodo = perfil.metodoAcceso
        mostrarPin = perfil.metodoAcceso == MetodoAcceso.PIN
    }

    fun desbloquear() {
        onUnlocked()
    }

    fun lanzarBiometrico() {
        val fragmentActivity = activity ?: return
        val prompt = BiometricPrompt(
            fragmentActivity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    desbloquear()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED
                    ) {
                        mostrarPin = true
                    } else {
                        intentosFallidos++
                        if (intentosFallidos >= 3) mostrarPin = true
                    }
                }

                override fun onAuthenticationFailed() {
                    intentosFallidos++
                    if (intentosFallidos >= 3) mostrarPin = true
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear AURA")
            .setSubtitle("Usa tu huella o Face ID")
            .setNegativeButtonText("Usar PIN")
            .build()
        prompt.authenticate(info)
    }

    LaunchedEffect(metodo, mostrarPin) {
        if (metodo == MetodoAcceso.BIOMETRICO && !mostrarPin) {
            lanzarBiometrico()
        }
    }

    LaunchedEffect(pin) {
        if (pin.length == 4) {
            if (app.pinRepository.verifyPin(pin)) {
                desbloquear()
            } else {
                error = true
                delay(500)
                error = false
                pin = ""
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = Spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Filled.Shield, null, tint = Color.White, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(Spacing.lg))
        Text(
            "AURA",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(Spacing.sm))

        if (mostrarPin) {
            Text(
                "Ingresa tu PIN",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spacing.lg))
            PinDots(length = pin.length, error = error)
            Spacer(Modifier.weight(1f))
            PinKeypad(
                onDigit = { d -> if (pin.length < 4) pin += d },
                onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) }
            )
        } else {
            Text(
                "Toca para desbloquear con biometría",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.lg))
            Icon(
                Icons.Filled.Fingerprint,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(72.dp)
                    .clickable { lanzarBiometrico() }
            )
            Spacer(Modifier.height(Spacing.lg))
            Text(
                "Usar PIN",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { mostrarPin = true }
            )
            Spacer(Modifier.weight(1f))
        }
        Spacer(Modifier.height(Spacing.lg))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LockPreview() {
    AuraTheme { LockScreen(onUnlocked = {}) }
}
