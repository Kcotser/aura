package com.example.myapplication

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.myapplication.gesture.SosTrigger
import com.example.myapplication.gesture.VolumeKey
import com.example.myapplication.gesture.VolumeKeyBus
import com.example.myapplication.ui.AuraApp
import com.example.myapplication.ui.theme.AuraTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SosTrigger.pending) mostrarSobrePantallaBloqueada()
        enableEdgeToEdge()
        setContent {
            AuraTheme {
                AuraApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (SosTrigger.pending) mostrarSobrePantallaBloqueada()
    }

    override fun onPause() {
        super.onPause()
        // Solo debe mostrarse sobre el bloqueo cuando el SOS se disparó desde fuera de la app;
        // se limpia acá para que una apertura normal (tocar el ícono) no salte el PIN/biometría.
        limpiarSobrePantallaBloqueada()
    }

    private fun mostrarSobrePantallaBloqueada() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    private fun limpiarSobrePantallaBloqueada() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
            setTurnScreenOn(false)
        } else {
            @Suppress("DEPRECATION")
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }

    // Detecta las pulsaciones físicas de volumen para el gesto de activación rápida mientras
    // la app está en primer plano (calibración); fuera de la app lo hace VolumeGestureAccessibilityService.
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> VolumeKeyBus.emit(VolumeKey.UP)
                KeyEvent.KEYCODE_VOLUME_DOWN -> VolumeKeyBus.emit(VolumeKey.DOWN)
            }
        }
        return super.dispatchKeyEvent(event)
    }
}

