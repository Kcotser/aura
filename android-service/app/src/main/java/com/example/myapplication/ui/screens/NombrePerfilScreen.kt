package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.AuraApplication
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Screen
import com.example.myapplication.ui.nav.rememberNav
import com.example.myapplication.ui.theme.PrimaryContainer
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.AuraTheme
import kotlinx.coroutines.launch

/** Primera pregunta del perfil local: cómo te llamas. Se guarda solo en el dispositivo. */
@Composable
fun NombrePerfilScreen(nav: Nav) {
    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .systemBarsPadding()
                .padding(horizontal = Spacing.lg)
        ) {
            Spacer(Modifier.weight(1f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer.copy(alpha = 0.18f))
            ) {
                Icon(
                    Icons.Filled.Shield,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(
                "¿Cómo te llamas?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                "Así te vamos a saludar dentro de la app. Este perfil se queda solo en tu dispositivo.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spacing.lg))
            AppTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = "Tu nombre"
            )
            Spacer(Modifier.weight(1f))
        }
        Column(Modifier.padding(horizontal = Spacing.lg)) {
            PrimaryButton(
                text = "Continuar",
                enabled = nombre.isNotBlank(),
                onClick = {
                    val nombreFinal = nombre.trim()
                    scope.launch {
                        app.profileRepository.setNombre(nombreFinal)
                        nav.push(Screen.PermisosEsenciales)
                    }
                }
            )
            Spacer(Modifier.height(Spacing.lg))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun NombrePerfilPreview() {
    AuraTheme { NombrePerfilScreen(rememberNav()) }
}
