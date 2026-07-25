package com.example.myapplication.capture

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log

/**
 * Averigua si el teléfono puede abrir dos cámaras a la vez.
 *
 * No es algo que se pueda asumir: la cámara concurrente es una capacidad opcional del hardware
 * (y del HAL del fabricante). La consulta se hace contra `getConcurrentCameraIds()`, que es la
 * API del sistema en la que CameraX se apoya para su modo concurrente, y que existe recién
 * desde Android 11.
 *
 * Que devuelva `true` significa que el dispositivo *declara* soportarlo; aun así el enlace puede
 * fallar por la combinación concreta de casos de uso o resolución, por eso
 * [SosCaptureService] siempre cae a una sola cámara si el bind falla.
 */
object DualCameraSupport {

    private const val TAG = "DualCameraSupport"

    fun estaDisponible(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        return try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            manager.concurrentCameraIds.any { it.size >= 2 }
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo consultar el soporte de cámara concurrente", e)
            false
        }
    }

    /** Motivo legible de por qué no está disponible, para mostrar en Ajustes. */
    fun motivoNoDisponible(context: Context): String = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.R ->
            "Requiere Android 11 o superior"
        else ->
            "Tu dispositivo no permite usar ambas cámaras a la vez"
    }
}
