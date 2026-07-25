package com.example.myapplication.capture

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Graba video (trasera) + audio local mientras dura la alerta SOS y lo guarda en
 * `getExternalFilesDir(null)/AURA_SOS/` (carpeta propia de la app, sin permisos extra de
 * almacenamiento). No sube nada a ningún backend todavía — eso es el siguiente paso, una vez
 * que el incidente "termina" (ver [stop]).
 *
 * Corre como foreground service (`camera|microphone`) para sobrevivir a que la pantalla se
 * bloquee o la Activity se destruya, pero se corta solo si el usuario cierra la app desde
 * Recientes ([onTaskRemoved]) — a propósito, para no llenar el almacenamiento durante pruebas.
 */
class SosCaptureService : LifecycleService() {

    private var cameraProvider: ProcessCameraProvider? = null
    private var recording: Recording? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startForeground(NOTIF_ID, buildNotification())
        if (intent?.action == ACTION_STOP) {
            detenerCaptura()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } else {
            iniciarCaptura()
        }
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.i(TAG, "App cerrada desde Recientes: cortando grabación")
        detenerCaptura()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        detenerCaptura()
        super.onDestroy()
    }

    private fun iniciarCaptura() {
        val tieneCamara = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (!tieneCamara) {
            Log.w(TAG, "Sin permiso de cámara, no se puede grabar")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        val outputDir = File(getExternalFilesDir(null), "AURA_SOS").apply { mkdirs() }
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            val provider = providerFuture.get()
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
                return@addListener
            }

            val nombreArchivo = "SOS_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.mp4"
            val outputFile = File(outputDir, nombreArchivo)
            val outputOptions = FileOutputOptions.Builder(outputFile).build()
            val tieneAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

            var pendingRecording = videoCapture.output.prepareRecording(this, outputOptions)
            if (tieneAudio) pendingRecording = pendingRecording.withAudioEnabled()

            recording = pendingRecording.start(ContextCompat.getMainExecutor(this)) { event ->
                when (event) {
                    is VideoRecordEvent.Start ->
                        Log.i(TAG, "Grabación iniciada -> ${outputFile.absolutePath}")
                    is VideoRecordEvent.Finalize -> {
                        if (event.hasError()) {
                            Log.e(TAG, "Grabación finalizada con error: ${event.error}")
                        } else {
                            Log.i(TAG, "Grabación guardada: ${outputFile.absolutePath} (${outputFile.length()} bytes)")
                        }
                    }
                    else -> {}
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun detenerCaptura() {
        recording?.stop()
        recording = null
        cameraProvider?.unbindAll()
        cameraProvider = null
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

        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, SosCaptureService::class.java))
        }

        fun stop(context: Context) {
            val intent = Intent(context, SosCaptureService::class.java).setAction(ACTION_STOP)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
