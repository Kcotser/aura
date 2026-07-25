package com.example.myapplication.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Screen
import com.example.myapplication.ui.nav.rememberNav
import com.example.myapplication.ui.theme.CamoAccent
import com.example.myapplication.ui.theme.CamoBlack
import com.example.myapplication.ui.theme.CamoDarkKey
import com.example.myapplication.ui.theme.CamoLightKey
import com.example.myapplication.ui.theme.CamoText
import com.example.myapplication.ui.theme.AuraTheme

/**
 * Pantallas señuelo del Modo Camuflaje. En la app real se activan con un gesto
 * discreto y se salen manteniendo presionado 3s el "logo" oculto; en esta
 * maqueta, mantener presionado en cualquier parte del contenido vuelve a Ajustes.
 */

/* =============================================================================
 * 1. CAMUFLAJE: CALCULADORA (funcional y discreta)
 * ========================================================================== */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CamuflajeCalculadoraScreen(nav: Nav) {
    var display by remember { mutableStateOf("0") }

    val keys = listOf(
        listOf("AC", "±", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("0", ",", "=")
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(CamoBlack)
            .combinedClickable(onClick = {}, onLongClick = { nav.popToMain() })
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(Spacing24())
        ) {
            Text(display, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(fontSize = 64.sp), color = CamoText)
        }

        keys.forEach { row ->
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                row.forEach { key ->
                    val isOperator = key in listOf("÷", "×", "−", "+", "=")
                    val isFunction = key in listOf("AC", "±", "%")
                    val bg = when {
                        isOperator -> CamoAccent
                        isFunction -> CamoLightKey
                        else -> CamoDarkKey
                    }
                    val fg = if (isFunction) Color.Black else Color.White
                    val weight = if (key == "0") 2f else 1f
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(weight)
                            .padding(4.dp)
                            .aspectRatio(if (key == "0") 2f else 1f)
                            .clip(CircleShape)
                            .background(bg)
                            .combinedClickable(
                                onClick = { display = if (key.length == 1 && key[0].isDigit()) (if (display == "0") key else display + key) else display },
                                onLongClick = { nav.popToMain() }
                            )
                    ) {
                        Text(key, style = androidx.compose.material3.MaterialTheme.typography.titleLarge, color = fg, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Spacing24() = 24.dp

/* =============================================================================
 * 2. CAMUFLAJE: MÚSICA (reproductor neutro)
 * ========================================================================== */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CamuflajeMusicaScreen(nav: Nav) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .combinedClickable(onClick = {}, onLongClick = { nav.popToMain() })
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.KeyboardArrowDown, null, tint = Color.White)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text("1:44", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text("Favoritos 2024", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Filled.MoreVert, null, tint = Color.White)
        }

        Spacer(Modifier.height(48.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2A2E38))
        )

        Spacer(Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                Text("Nocturnal Echoes", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Midnight Sessions", style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
            Icon(Icons.Filled.FavoriteBorder, null, tint = Color.White)
        }

        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(progress = { 0.3f }, modifier = Modifier.fillMaxWidth(), color = Color.White, trackColor = Color.DarkGray)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("1:42", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Text("3:58", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }

        Spacer(Modifier.height(24.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Shuffle, null, tint = Color.Gray)
            Icon(Icons.Filled.SkipPrevious, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .combinedClickable(onClick = {}, onLongClick = { nav.popToMain() })
            ) {
                Icon(Icons.Filled.Pause, null, tint = Color.Black, modifier = Modifier.size(32.dp))
            }
            Icon(Icons.Filled.SkipNext, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Icon(Icons.Filled.Repeat, null, tint = Color.Gray)
        }

        Spacer(Modifier.height(32.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Icon(Icons.Filled.Share, null, tint = Color.Gray)
            Icon(Icons.Filled.Share, null, tint = Color.Gray)
        }
    }
}

/* =============================================================================
 * 3. CAMUFLAJE: BLOQUEO (reloj tipo lockscreen)
 * ========================================================================== */

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CamuflajeBloqueoScreen(nav: Nav) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .combinedClickable(onClick = {}, onLongClick = { nav.popToMain() }),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "12:25",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium.copy(fontSize = 72.sp),
                color = Color.White,
                fontWeight = FontWeight.Light
            )
            Text(
                "viernes, 24 de julio",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CamuflajeCalculadoraPreview() {
    AuraTheme { CamuflajeCalculadoraScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CamuflajeMusicaPreview() {
    AuraTheme { CamuflajeMusicaScreen(rememberNav()) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun CamuflajeBloqueoPreview() {
    AuraTheme { CamuflajeBloqueoScreen(rememberNav()) }
}
