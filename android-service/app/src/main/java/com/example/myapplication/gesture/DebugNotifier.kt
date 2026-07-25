package com.example.myapplication.gesture

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

/**
 * Notificaciones puramente de debug para ver en vivo qué está detectando
 * [VolumeGestureAccessibilityService], incluso con la app cerrada.
 * Requiere el permiso POST_NOTIFICATIONS en Android 13+; si no está concedido,
 * simplemente no se muestran (no rompe nada).
 */
object DebugNotifier {
    private const val CHANNEL_ESTADO_ID = "aura_debug_estado"
    private const val CHANNEL_EVENTO_ID = "aura_debug_evento"
    const val ID_ESTADO = 1001
    private const val ID_EVENTO = 1002

    private fun asegurarCanales(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ESTADO_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(CHANNEL_ESTADO_ID, "Debug: estado del atajo", NotificationManager.IMPORTANCE_LOW)
                )
            }
            if (manager.getNotificationChannel(CHANNEL_EVENTO_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(CHANNEL_EVENTO_ID, "Debug: gesto detectado", NotificationManager.IMPORTANCE_HIGH)
                )
            }
        }
    }

    /** Construye (sin publicar) la notificación de estado — la usa también el foreground service. */
    fun construirEstado(context: Context, texto: String): Notification {
        asegurarCanales(context)
        return NotificationCompat.Builder(context, CHANNEL_ESTADO_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("AURA · Atajo de volumen")
            .setContentText(texto)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    /** Notificación fija (ongoing) que muestra el estado/conteo actual del gesto. Visible en la pantalla de bloqueo. */
    fun mostrarEstado(context: Context, texto: String) {
        runCatching { NotificationManagerCompat.from(context).notify(ID_ESTADO, construirEstado(context, texto)) }
    }

    /** Notificación puntual (alta prioridad: suena/vibra) cuando el gesto se completó. Visible en la pantalla de bloqueo. */
    fun mostrarEvento(context: Context, texto: String) {
        asegurarCanales(context)
        val notif = NotificationCompat.Builder(context, CHANNEL_EVENTO_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("AURA · Gesto detectado")
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(ID_EVENTO, notif) }
    }
}
