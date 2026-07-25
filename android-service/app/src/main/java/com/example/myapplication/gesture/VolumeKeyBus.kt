package com.example.myapplication.gesture

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

enum class VolumeKey { UP, DOWN }

/** Puente entre las teclas físicas de volumen (capturadas en la Activity) y los composables que las escuchan. */
object VolumeKeyBus {
    private val _events = MutableSharedFlow<VolumeKey>(extraBufferCapacity = 8)
    val events: SharedFlow<VolumeKey> = _events

    fun emit(key: VolumeKey) {
        _events.tryEmit(key)
    }
}
