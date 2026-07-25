package com.example.myapplication.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Si ya se autenticó (PIN/biometría) para ver el resto de la app más allá de Inicio, en esta
 * sesión. Inicio (el botón de SOS) es siempre accesible sin PIN — la fricción está reservada
 * para el resto de las pestañas (Historial, Red de Apoyo, Ajustes) y lo que cuelga de ellas.
 * Vuelve a `false` al relockear por tiempo en segundo plano o al cerrar sesión.
 */
object SessionState {
    var tabsUnlocked: Boolean by mutableStateOf(false)
}
