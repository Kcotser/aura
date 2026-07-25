package com.example.myapplication.gesture

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Señal compartida: el gesto de volumen se detectó fuera de la app (o con la pantalla
 * bloqueada) y hay que abrir el flujo de SOS. [VolumeGestureAccessibilityService] la enciende;
 * [com.example.myapplication.ui.AuraApp] la consume y la apaga.
 */
object SosTrigger {
    var pending: Boolean by mutableStateOf(false)
}
