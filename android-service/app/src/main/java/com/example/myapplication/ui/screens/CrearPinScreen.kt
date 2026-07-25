package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.AuraApplication
import com.example.myapplication.ui.components.AuraTopBar
import com.example.myapplication.ui.components.PinDots
import com.example.myapplication.ui.components.PinKeypad
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Screen
import com.example.myapplication.ui.nav.rememberNav
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.AuraTheme
import kotlinx.coroutines.delay

/** Configura el PIN de 4 dígitos: es el método principal o el respaldo si falla la biometría. */
@Composable
fun CrearPinScreen(nav: Nav) {
    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication

    var pin by remember { mutableStateOf("") }
    var pinConfirmado by remember { mutableStateOf("") }
    var confirmando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }

    val pinActual = if (confirmando) pinConfirmado else pin

    LaunchedEffect(pin) {
        if (!confirmando && pin.length == 4) {
            delay(150)
            confirmando = true
        }
    }

    LaunchedEffect(pinConfirmado) {
        if (confirmando && pinConfirmado.length == 4) {
            if (pinConfirmado == pin) {
                app.pinRepository.setPin(pin)
                nav.push(Screen.CalibracionGesto)
            } else {
                error = true
                delay(500)
                error = false
                pinConfirmado = ""
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
            onBack = {
                if (confirmando) {
                    confirmando = false
                    pinConfirmado = ""
                    pin = ""
                } else {
                    nav.pop()
                }
            }
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                if (confirmando) "Confirma tu PIN" else "Crea un PIN de 4 dígitos",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                if (confirmando) "Vuelve a ingresarlo para confirmar."
                else "Lo vas a usar para entrar a AURA y, si eliges biometría, como respaldo si falla.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.lg))
            PinDots(length = pinActual.length, error = error)
            Spacer(Modifier.weight(1f))
            PinKeypad(
                onDigit = { d ->
                    if (confirmando) {
                        if (pinConfirmado.length < 4) pinConfirmado += d
                    } else {
                        if (pin.length < 4) pin += d
                    }
                },
                onBackspace = {
                    if (confirmando) {
                        if (pinConfirmado.isNotEmpty()) pinConfirmado = pinConfirmado.dropLast(1)
                    } else {
                        if (pin.isNotEmpty()) pin = pin.dropLast(1)
                    }
                }
            )
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CrearPinPreview() {
    AuraTheme { CrearPinScreen(rememberNav()) }
}
