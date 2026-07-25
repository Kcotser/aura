package com.example.myapplication.capture

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Graba video (cámara trasera) + audio mientras dura la alerta SOS.
 *
 * **Dónde queda el archivo:** en `Movies/AURA/` del almacenamiento compartido, vía MediaStore,
 * para que sea visible desde la Galería y el explorador de archivos. Se eligió así para poder
 * verificar durante el MVP que la captura funciona de verdad; en producción la evidencia
 * debería vivir en almacenamiento privado de la app (o cifrada), porque un video de SOS
 * visible en la galería es justamente lo que un agresor con el teléfono en la mano vería.
 *
 * Corre como foreground service (`camera|microphone`) para sobrevivir a que la pantalla se
 * bloquee o la Activity se destruya, pero se corta solo si el usuario cierra la app desde
 * Recientes ([onTaskRemoved]), para no llenar el almacenamiento durante pruebas.
 */
class SosCaptureService : LifecycleService() {

    private var cameraProvider: ProcessCameraProvider? = null
    private var recording: Recording? = null

    /** Qué hacer cuando el archivo termine de escribirse (llega [VideoRecordEvent.Finalize]). */
    private var alFinalizar: (() -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        // En Android 14+ un FGS de cámara/micrófono lanzado desde segundo plano es rechazado
        // por el sistema; si pasa, no vale la pena arrastrar el servicio a medio arrancar.
        val arrancado = runCatching { startForeground(NOTIF_ID, buildNotification()) }.isSuccess
        if (!arrancado) {
            Log.e(TAG, "El sistema rechazó el foreground service de captura")
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_STOP) {
            detenerCaptura { apagarServicio() }
        } else {
            iniciarCaptura()
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.i(TAG, "App cerrada desde Recientes: cortando grabación")
        detenerCaptura { apagarServicio() }
    }

    override fun onDestroy() {
        // Último recurso: si el servicio muere sin pasar por detenerCaptura(), al menos se
        // pide el stop para que CameraX cierre el archivo.
        recording?.stop()
        recording = null
        cameraProvider?.unbindAll()
        cameraProvider = null
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun iniciarCaptura() {
        val tieneCamara = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (!tieneCamara) {
            Log.w(TAG, "Sin permiso de cámara, no se puede grabar")
            apagarServicio()
            return
        }

        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = runCatching { providerFuture.get() }.getOrElse {
                Log.e(TAG, "No se pudo obtener el ProcessCameraProvider", it)
                apagarServicio()
                return@addListener
            }
            cameraProvider = provider

            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(Quality.HD, FallbackStrategy.higherQualityOrLowerThan(Quality.SD))
                )
                .build()
            val videoCapture = VideoCapture.withOutput(recorder)

            try {
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, videoCapture)
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo enlazar la cámara", e)
                apagarServicio()
                return@addListener
            }

            val nombreArchivo = "SOS_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.mp4"
            val valores = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, nombreArchivo)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/$CARPETA")
                }
            }
            val outputOptions = MediaStoreOutputOptions
                .Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(valores)
                .build()

            val tieneAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (!tieneAudio) Log.w(TAG, "Sin permiso de micrófono: se graba solo video")

            var pendiente = videoCapture.output.prepareRecording(this, outputOptions)
            if (tieneAudio) pendiente = pendiente.withAudioEnabled()

            recording = runCatching {
                pendiente.start(ContextCompat.getMainExecutor(this)) { event ->
                    when (event) {
                        is VideoRecordEvent.Start ->
                            Log.i(TAG, "Grabación iniciada -> Movies/$CARPETA/$nombreArchivo (audio=$tieneAudio)")

                        is VideoRecordEvent.Finalize -> {
                            // Llegó el cierre real del archivo: la red de seguridad ya no aplica.
                            handler.removeCallbacksAndMessages(null)
                            // Recién ahora el MP4 está completo: se puede soltar la cámara.
                            cameraProvider?.unbindAll()
                            cameraProvider = null

                            if (event.hasError()) {
                                Log.e(TAG, "Grabación finalizada con error ${event.error}", event.cause)
                                terminarPendiente()
                            } else {
                                val videoUri = event.outputResults.outputUri
                                Log.i(TAG, "Video guardado: $videoUri")
                                extraerAudioYTerminar(videoUri, nombreArchivo.removeSuffix(".mp4"))
                            }
                        }

                        else -> {}
                    }
                }
            }.getOrElse {
                Log.e(TAG, "No se pudo iniciar la grabación", it)
                apagarServicio()
                null
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Genera el `.m4a` a partir del video recién cerrado y recién ahí deja morir el servicio.
     * Corre en un hilo aparte (es I/O de disco) y no en `lifecycleScope`, porque ese scope se
     * cancela al destruirse el servicio — justo lo que estamos por hacer al terminar.
     */
    private fun extraerAudioYTerminar(videoUri: android.net.Uri, nombreBase: String) {
        Thread {
            val audioUri = AudioExtractor.extraer(this, videoUri, nombreBase)
            if (audioUri != null) Log.i(TAG, "Audio extraído: $audioUri")
            handler.post { terminarPendiente() }
        }.start()
    }

    /** Ejecuta (una sola vez) lo que haya quedado pendiente para el cierre del servicio. */
    private fun terminarPendiente() {
        alFinalizar?.invoke()
        alFinalizar = null
    }

    /**
     * Pide el fin de la grabación y ejecuta [onListo] cuando el archivo quedó realmente escrito.
     * No se puede desenlazar la cámara ni matar el servicio antes de eso: `stop()` es asíncrono
     * y el MP4 recién queda reproducible cuando CameraX escribe su índice final.
     */
    private fun detenerCaptura(onListo: () -> Unit) {
        val actual = recording
        if (actual == null) {
            onListo()
            return
        }
        alFinalizar = onListo
        actual.stop()
        recording = null

        // Red de seguridad: si Finalize nunca llega, no dejamos el servicio colgado para siempre.
        handler.postDelayed({
            if (alFinalizar != null) {
                Log.w(TAG, "Finalize no llegó a tiempo; cerrando de todos modos")
                alFinalizar = null
                onListo()
            }
        }, TIMEOUT_FINALIZE_MS)
    }

    private fun apagarServicio() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(CHANNEL_ID, "Captura de evidencia SOS", NotificationManager.IMPORTANCE_LOW)
                )
            }
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentTitle("AURA · Grabando evidencia")
            .setContentText("Audio y video se están guardando mientras la alerta esté activa")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val TAG = "SosCaptureService"
        private const val CHANNEL_ID = "aura_sos_capture"
        private const val NOTIF_ID = 2001
        private const val ACTION_STOP = "com.example.myapplication.capture.action.STOP"
        private const val TIMEOUT_FINALIZE_MS = 5000L

        /** Subcarpeta dentro de Movies/ donde quedan los videos del SOS. */
        const val CARPETA = "AURA"

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, SosCaptureService::class.java))
        }

        fun stop(context: Context) {
            val intent = Intent(context, SosCaptureService::class.java).setAction(ACTION_STOP)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
