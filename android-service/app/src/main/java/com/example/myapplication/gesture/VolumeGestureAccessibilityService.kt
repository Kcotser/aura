package com.example.myapplication.gesture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.myapplication.MainActivity

private const val VENTANA_MS = 4000L
private const val PULSACIONES_REQUERIDAS = 3

/**
 * Detecta el gesto de activación (subir y bajar volumen, 3 veces cada uno) fuera de la app
 * y con la pantalla bloqueada. Requiere que el usuario active el servicio manualmente en
 * Ajustes del sistema > Accesibilidad — Android no permite habilitarlo desde la propia app.
 *
 * Solo cuenta pulsaciones mientras la app NO está en primer plano: cuando está abierta, la
 * pantalla de calibración ya escucha las mismas teclas directamente vía [MainActivity].
 */
class VolumeGestureAccessibilityService : AccessibilityService() {

    private var subeCount = 0
    private var bajaCount = 0
    private var ultimoEvento = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo?.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
        // Foreground service: le sube la prioridad al proceso ante el sistema para que
        // sobreviva a un swipe en Recientes (un Force Stop manual sigue matándolo, eso Android
        // no lo deja evitar — y además desactiva el propio permiso de Accesibilidad).
        startForeground(DebugNotifier.ID_ESTADO, DebugNotifier.construirEstado(this, estadoTexto()))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!AppForegroundState.isForeground && event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> registrar(VolumeKey.UP)
                KeyEvent.KEYCODE_VOLUME_DOWN -> registrar(VolumeKey.DOWN)
            }
        }
        return false
    }

    private fun registrar(key: VolumeKey) {
        val ahora = System.currentTimeMillis()
        if (ahora - ultimoEvento > VENTANA_MS) {
            subeCount = 0
            bajaCount = 0
        }
        ultimoEvento = ahora
        when (key) {
            VolumeKey.UP -> subeCount = (subeCount + 1).coerceAtMost(PULSACIONES_REQUERIDAS)
            VolumeKey.DOWN -> bajaCount = (bajaCount + 1).coerceAtMost(PULSACIONES_REQUERIDAS)
        }
        if (subeCount >= PULSACIONES_REQUERIDAS && bajaCount >= PULSACIONES_REQUERIDAS) {
            subeCount = 0
            bajaCount = 0
            dispararSos()
        } else {
            DebugNotifier.mostrarEstado(this, estadoTexto())
        }
    }

    private fun estadoTexto(): String = "Escuchando · Subir $subeCount/$PULSACIONES_REQUERIDAS · Bajar $bajaCount/$PULSACIONES_REQUERIDAS"

    private fun dispararSos() {
        DebugNotifier.mostrarEvento(this, "Gesto completo, abriendo AURA...")
        DebugNotifier.mostrarEstado(this, estadoTexto())
        SosTrigger.pending = true
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        startActivity(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
