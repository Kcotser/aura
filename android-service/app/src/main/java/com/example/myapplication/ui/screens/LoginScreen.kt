package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import com.example.myapplication.AuraApplication
import com.example.myapplication.network.AuraApiException
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.WarningBanner
import com.example.myapplication.ui.theme.PrimaryContainer
import com.example.myapplication.ui.theme.Spacing
import kotlinx.coroutines.launch

/**
 * Inicio de sesión contra el backend de AURA.
 *
 * Es la única cuenta "real" de la app: el PIN y el nombre del perfil siguen siendo locales, pero
 * sin sesión el backend rechaza la subida de evidencia, así que sin esto la grabación se queda
 * en el teléfono.
 *
 * La misma pantalla hace login y registro para no duplicar el formulario: el modo alterna con el
 * enlace de abajo y solo cambia qué campos se piden.
 *
 * @param onListo se llama cuando la sesión quedó establecida.
 */
@Composable
fun LoginScreen(onListo: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication
    val scope = rememberCoroutineScope()

    var modoRegistro by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val formularioValido = email.isNotBlank() && password.length >= 8 &&
        (!modoRegistro || (nombre.isNotBlank() && apellido.isNotBlank()))

    fun enviar() {
        error = null
        cargando = true
        scope.launch {
            val resultado = runCatching {
                if (modoRegistro) {
                    app.authRepository.registrar(email, password, nombre, apellido, telefono.ifBlank { null })
                } else {
                    app.authRepository.login(email, password)
                }
            }
            cargando = false
            resultado.fold(
                onSuccess = { onListo() },
                onFailure = { e ->
                    error = when {
                        e is AuraApiException && e.code == 0 ->
                            "No se pudo contactar al servidor. Revisa tu conexión y la URL del backend."
                        e is AuraApiException && e.code == 401 ->
                            "Correo o contraseña incorrectos."
                        e is AuraApiException && e.code == 429 ->
                            "Demasiados intentos. Espera un minuto y vuelve a probar."
                        else -> e.message ?: "No se pudo completar la operación."
                    }
                }
            )
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.height(Spacing.lg))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer.copy(alpha = 0.18f))
            ) {
                Icon(
                    Icons.Filled.CloudUpload,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(
                if (modoRegistro) "Crea tu cuenta" else "Inicia sesión",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Tu cuenta es lo que permite que la evidencia de una alerta se respalde fuera del teléfono.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.lg))

            val mensajeError = error
            if (mensajeError != null) {
                WarningBanner(title = "No se pudo continuar", text = mensajeError)
                Spacer(Modifier.height(Spacing.md))
            }

            if (modoRegistro) {
                AppTextField(nombre, { nombre = it }, "Nombre", enabled = !cargando)
                Spacer(Modifier.height(Spacing.sm))
                AppTextField(apellido, { apellido = it }, "Apellido", enabled = !cargando)
                Spacer(Modifier.height(Spacing.sm))
                AppTextField(telefono, { telefono = it }, "Teléfono (opcional)", isPhone = true, enabled = !cargando)
                Spacer(Modifier.height(Spacing.sm))
            }

            AppTextField(email, { email = it }, "Correo electrónico", isEmail = true, enabled = !cargando)
            Spacer(Modifier.height(Spacing.sm))
            AppTextField(password, { password = it }, "Contraseña", isPassword = true, enabled = !cargando)

            if (modoRegistro) {
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    "Mínimo 8 caracteres.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(Spacing.lg))
        }

        Column(Modifier.padding(horizontal = Spacing.lg)) {
            if (cargando) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                PrimaryButton(
                    text = if (modoRegistro) "Crear cuenta" else "Entrar",
                    enabled = formularioValido,
                    onClick = { enviar() }
                )
            }
            TextButton(
                onClick = {
                    modoRegistro = !modoRegistro
                    error = null
                },
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (modoRegistro) "Ya tengo cuenta" else "No tengo cuenta, quiero crear una",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(Spacing.sm))
        }
    }
}
