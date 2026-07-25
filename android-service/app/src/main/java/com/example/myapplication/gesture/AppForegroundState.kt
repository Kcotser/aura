package com.example.myapplication.gesture

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Si la app está en primer plano. La actualiza [com.example.myapplication.ui.AuraApp] con
 * ProcessLifecycleOwner; la usa [VolumeGestureAccessibilityService] para no detectar el gesto
 * dos veces (una por la Activity y otra por el servicio) mientras la app está abierta.
 */
object AppForegroundState {
    var isForeground: Boolean by mutableStateOf(false)
}
