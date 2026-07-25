package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.PrimaryContainer
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Sizes
import com.example.myapplication.ui.theme.Spacing
import com.example.myapplication.ui.theme.AuraTheme

/** Pantalla de bienvenida / onboarding inicial (fondo violeta con degradado). */
@Composable
fun SplashScreen(
    onComenzar: () -> Unit
) {
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF5A189A), Color(0xFF3F0075), Color(0xFF2B0052))
    )
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
            Spacer(Modifier.weight(1f))

            // Escudo de marca
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Icon(
                    Icons.Filled.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            Text(
                "Aura",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spacing.sm))
            Text(
                "Tu testigo silencioso, siempre contigo.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))

            // Tarjeta de privacidad
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.card)
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(Spacing.md)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryContainer)
                ) {
                    Icon(Icons.Filled.Shield, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.size(Spacing.md))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Privacidad Total",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Encriptación de grado militar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    Icons.Filled.Fingerprint,
                    null,
                    tint = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(Spacing.lg))

            // CTA principal (blanco sobre violeta)
            Button(
                onClick = onComenzar,
                shape = Shapes.button,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Sizes.buttonHeight)
            ) {
                Text("Comenzar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }

            Spacer(Modifier.height(Spacing.lg))
            Text(
                "Al continuar, aceptas nuestros Términos de Servicio y Política de Privacidad.",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.md)
            )
            Spacer(Modifier.height(Spacing.md))
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SplashPreview() {
    AuraTheme { SplashScreen(onComenzar = {}) }
}

