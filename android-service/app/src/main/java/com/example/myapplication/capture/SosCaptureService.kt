package com.example.myapplication.capture

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ConcurrentCamera
import androidx.camera.core.UseCaseGroup
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
 * Graba video + audio mientras dura la alerta SOS.
 *
 * **Modo simple (por defecto):** una grabación con la cámara trasera y audio.
 *
 * **Modo dual (opcional, [EXTRA_DUAL]):** dos grabaciones simultáneas, frontal y trasera. Solo
 * la frontal lleva audio: dos grabadores pidiendo el micrófono a la vez no está garantizado en
 * Android y termina en silencio o en fallo según el fabricante. La cámara concurrente además
 * es una capacidad opcional del hardware (ver [DualCameraSupport]) y limita la resolución, así
 * que si el enlace falla se cae automáticamente al modo simple en vez de quedarse sin grabar.
 *
 * **Dónde quedan los archivos:** en `Movies/AURA/` vía MediaStore, para que sean visibles desde
 * la galería. Se eligió así para poder verificar durante el MVP que la captura funciona; en
 * producción la evidencia debería vivir en almacenamiento privado o cifrada, porque un video de
 * SOS visible en la galería es justamente lo que un agresor con el teléfono en la mano vería.
 *
 * Corre como foreground service (`camera|microphone`) para sobrevivir a que la pantalla se
 * bloquee o la Activity se destruya, pero se corta solo si el usuario cierra la app desde
 * Recientes ([onTaskRemoved]), para no llenar el almacenamiento durante pruebas.
 */
class SosCaptureService : LifecycleService() {

    private var cameraProvider: ProcessCameraProvider? = null
    private val grabaciones = mutableListOf<Recording>()

    /** Grabaciones que todavía no avisaron que cerraron su archivo. */
    private var pendientesDeCerrar = 0

    /** Datos de la grabación que lleva audio, para extraerle el `.m4a` cuando termine. */
    private var uriConAudio: Uri? = null
    private var nombreBaseAudio: String? = null

    /**
     * Archivos finales de la alerta, ya mapeados a las partes que espera el backend
     * (`frontCamera` / `backCamera` / `ambientAudio`). En modo simple [uriFrontal] queda en null,
     * y sin permiso de micrófono queda en null [uriAudio].
     */
    private var uriFrontal: Uri? = null
    private var uriTrasera: Uri? = null
    private var uriAudio: Uri? = null

    /** La subida se encola una sola vez, aunque el Finalize y el timeout de cierre se pisen. */
    private var subidaEncolada = false

    /** Qué hacer cuando todos los archivos terminen de escribirse. */
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
            iniciarCaptura(dualSolicitado = intent?.getBooleanExtra(EXTRA_DUAL, false) == true)
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
        // pide el stop para que CameraX cierre los archivos.
        grabaciones.forEach { runCatching { it.stop() } }
        grabaciones.clear()
        cameraProvider?.unbindAll()
        cameraProvider = null
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    /* ---------------------------------------------------------------------- arranque */

    private fun iniciarCaptura(dualSolicitado: Boolean) {
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

            val marcaDeTiempo = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val dualPosible = dualSolicitado && DualCameraSupport.estaDisponible(this)
            if (dualSolicitado && !dualPosible) {
                Log.w(TAG, "Modo dual pedido pero el dispositivo no lo soporta; se graba con una cámara")
            }

            val arrancoDual = dualPosible && intentarDual(provider, marcaDeTiempo)
            if (!arrancoDual) {
                if (dualPosible) Log.w(TAG, "El modo dual falló; se cae a una sola cámara")
                intentarSimple(provider, marcaDeTiempo)
            }

            if (grabaciones.isEmpty()) {
                Log.e(TAG, "No se pudo iniciar ninguna grabación")
                apagarServicio()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /** Enlaza frontal + trasera a la vez. Devuelve false si el dispositivo rechaza la combinación. */
    private fun intentarDual(provider: ProcessCameraProvider, marcaDeTiempo: String): Boolean {
        val frontal = VideoCapture.withOutput(construirRecorder())
        val trasera = VideoCapture.withOutput(construirRecorder())

        try {
            provider.unbindAll()
            provider.bindToLifecycle(
                listOf(
                    ConcurrentCamera.SingleCameraConfig(
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        UseCaseGroup.Builder().addUseCase(frontal).build(),
                        this
                    ),
                    ConcurrentCamera.SingleCameraConfig(
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        UseCaseGroup.Builder().addUseCase(trasera).build(),
                        this
                    )
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo enlazar la cámara concurrente", e)
            runCatching { provider.unbindAll() }
            return false
        }

        // Solo la frontal graba audio: dos capturadores de micrófono simultáneos no son fiables.
        val conAudio = hayPermisoDeAudio()
        val ok = arrancarGrabacion(frontal, "${marcaDeTiempo}_frontal", conAudio, esFrontal = true) or
            arrancarGrabacion(trasera, "${marcaDeTiempo}_trasera", conAudio = false, esFrontal = false)

        if (!ok) runCatching { provider.unbindAll() }
        return ok
    }

    /** Modo clásico: una sola cámara (trasera) con audio. */
    private fun intentarSimple(provider: ProcessCameraProvider, marcaDeTiempo: String) {
        val videoCapture = VideoCapture.withOutput(construirRecorder())
        try {
            provider.unbindAll()
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, videoCapture)
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo enlazar la cámara", e)
            return
        }
        arrancarGrabacion(videoCapture, marcaDeTiempo, conAudio = hayPermisoDeAudio(), esFrontal = false)
    }

    private fun construirRecorder(): Recorder = Recorder.Builder()
        // En modo concurrente el sistema limita la resolución de cada stream, así que se pide
        // HD (720p) con caída a SD antes que una calidad que el dispositivo vaya a rechazar.
        .setQualitySelector(
            QualitySelector.from(Quality.HD, FallbackStrategy.higherQualityOrLowerThan(Quality.SD))
        )
        .build()

    private fun hayPermisoDeAudio(): Boolean {
        val ok = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (!ok) Log.w(TAG, "Sin permiso de micrófono: se graba solo video")
        return ok
    }

    /**
     * Arranca una grabación hacia `Movies/AURA/SOS_<nombreBase>.mp4`.
     *
     * @param esFrontal de qué cámara sale, para saber si el archivo va como `frontCamera` o
     *   como `backCamera` al subirlo. En modo simple siempre es la trasera.
     * @return true si quedó grabando.
     */
    private fun arrancarGrabacion(
        videoCapture: VideoCapture<Recorder>,
        nombreBase: String,
        conAudio: Boolean,
        esFrontal: Boolean
    ): Boolean {
        val nombreArchivo = "SOS_$nombreBase.mp4"
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

        var pendiente = videoCapture.output.prepareRecording(this, outputOptions)
        if (conAudio) pendiente = pendiente.withAudioEnabled()

        val grabacion = runCatching {
            pendiente.start(ContextCompat.getMainExecutor(this)) { event ->
                when (event) {
                    is VideoRecordEvent.Start ->
                        Log.i(TAG, "Grabando -> Movies/$CARPETA/$nombreArchivo (audio=$conAudio)")

                    is VideoRecordEvent.Finalize -> {
                        if (event.hasError()) {
                            Log.e(TAG, "$nombreArchivo finalizó con error ${event.error}", event.cause)
                        } else {
                            val uri = event.outputResults.outputUri
                            Log.i(TAG, "Video guardado: $uri")
                            if (esFrontal) uriFrontal = uri else uriTrasera = uri
                            if (conAudio) {
                                uriConAudio = uri
                                nombreBaseAudio = "SOS_$nombreBase"
                            }
                        }
                        alCerrarUnArchivo()
                    }

                    else -> {}
                }
            }
        }.getOrElse {
            Log.e(TAG, "No se pudo iniciar la grabación de $nombreArchivo", it)
            return false
        }

        grabaciones += grabacion
        pendientesDeCerrar++
        return true
    }

    /* ------------------------------------------------------------------------ cierre */

    /**
     * Se llama por cada archivo que termina de escribirse. Recién cuando cerraron todos se
     * puede soltar la cámara y extraer el audio: en modo dual son dos archivos y desenlazar
     * con uno todavía abierto lo dejaría corrupto.
     */
    private fun alCerrarUnArchivo() {
        pendientesDeCerrar--
        if (pendientesDeCerrar > 0) return

        handler.removeCallbacksAndMessages(null)
        cameraProvider?.unbindAll()
        cameraProvider = null

        val uri = uriConAudio
        val base = nombreBaseAudio
        if (uri != null && base != null) {
            extraerAudioYTerminar(uri, base)
        } else {
            encolarSubida()
            terminarPendiente()
        }
    }

    /**
     * Genera el `.m4a` a partir del video que llevaba audio y recién ahí deja morir el servicio.
     * Corre en un hilo aparte (es I/O de disco) y no en `lifecycleScope`, porque ese scope se
     * cancela al destruirse el servicio — justo lo que estamos por hacer al terminar.
     */
    private fun extraerAudioYTerminar(videoUri: Uri, nombreBase: String) {
        Thread {
            val audioUri = AudioExtractor.extraer(this, videoUri, nombreBase)
            if (audioUri != null) Log.i(TAG, "Audio extraído: $audioUri")
            handler.post {
                uriAudio = audioUri
                encolarSubida()
                terminarPendiente()
            }
        }.start()
    }

    /**
     * Manda la evidencia al backend. Es un encolado, no una subida: [EvidenceUploadWorker] la
     * hace cuando haya red y reintenta si falla, porque este servicio está a punto de morir y
     * una subida a medias moriría con él.
     */
    private fun encolarSubida() {
        if (subidaEncolada) return
        // Sin ningún archivo todavía no hay nada que mandar, y marcarlo como encolado impediría
        // que un Finalize tardío (tras el timeout de cierre) alcance a subir lo que sí quedó.
        if (uriFrontal == null && uriTrasera == null && uriAudio == null) return

        subidaEncolada = true
        EvidenceUploadWorker.encolar(this, uriFrontal, uriTrasera, uriAudio)
    }

    /** Ejecuta (una sola vez) lo que haya quedado pendiente para el cierre del servicio. */
    private fun terminarPendiente() {
        alFinalizar?.invoke()
        alFinalizar = null
    }

    /**
     * Pide el fin de todas las grabaciones y ejecuta [onListo] cuando los archivos quedaron
     * realmente escritos. No se puede desenlazar la cámara ni matar el servicio antes de eso:
     * `stop()` es asíncrono y el MP4 recién queda reproducible cuando CameraX escribe su
     * índice final.
     */
    private fun detenerCaptura(onListo: () -> Unit) {
        if (grabaciones.isEmpty()) {
            onListo()
            return
        }
        alFinalizar = onListo
        grabaciones.forEach { runCatching { it.stop() } }
        grabaciones.clear()

        // Red de seguridad: si algún Finalize nunca llega, no dejamos el servicio colgado.
        handler.postDelayed({
            if (alFinalizar != null) {
                Log.w(TAG, "Finalize no llegó a tiempo ($pendientesDeCerrar pendientes); cerrando igual")
                pendientesDeCerrar = 0
                cameraProvider?.unbindAll()
                cameraProvider = null
                encolarSubida()
                terminarPendiente()
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
        private const val EXTRA_DUAL = "dual"
        private const val TIMEOUT_FINALIZE_MS = 5000L

        /** Subcarpeta dentro de Movies/ (y Music/, para el audio) donde queda la evidencia. */
        const val CARPETA = "AURA"

        /** @param dual grabar con ambas cámaras si el hardware lo permite. */
        fun start(context: Context, dual: Boolean = false) {
            val intent = Intent(context, SosCaptureService::class.java).putExtra(EXTRA_DUAL, dual)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, SosCaptureService::class.java).setAction(ACTION_STOP)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
