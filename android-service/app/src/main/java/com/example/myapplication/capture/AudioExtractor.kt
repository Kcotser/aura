package com.example.myapplication.capture

import android.content.ContentValues
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.nio.ByteBuffer

/**
 * Saca la pista de audio de un video ya grabado y la guarda como `.m4a` aparte.
 *
 * Es un *remux*, no una recodificación: el MP4 ya guarda el audio como una pista AAC
 * independiente, así que se copian sus paquetes tal cual a un contenedor nuevo. Por eso no
 * hay pérdida de calidad ni costo de CPU relevante.
 *
 * Se hace en dos pasos (archivo temporal en caché y después copia a MediaStore) porque
 * [MediaMuxer] solo acepta un descriptor de archivo desde API 26, y el proyecto soporta
 * desde API 24.
 */
object AudioExtractor {

    private const val TAG = "AudioExtractor"
    private const val BUFFER_POR_DEFECTO = 256 * 1024

    /**
     * @return el [Uri] del audio generado, o `null` si el video no tenía pista de audio
     *   (por ejemplo si se grabó sin permiso de micrófono) o si algo falló.
     */
    fun extraer(context: Context, videoUri: Uri, nombreBase: String): Uri? {
        val temporal = File(context.cacheDir, "$nombreBase.m4a")
        return try {
            if (!remuxearAudio(context, videoUri, temporal)) return null
            publicarEnMediaStore(context, temporal, "$nombreBase.m4a")
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo extraer el audio de $videoUri", e)
            null
        } finally {
            temporal.delete()
        }
    }

    /** Copia la pista de audio del video a [destino]. Devuelve false si no había audio. */
    private fun remuxearAudio(context: Context, videoUri: Uri, destino: File): Boolean {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        try {
            extractor.setDataSource(context, videoUri, null)

            var pistaAudio = -1
            var formato: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                if (f.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                    pistaAudio = i
                    formato = f
                    break
                }
            }
            if (pistaAudio < 0 || formato == null) {
                Log.w(TAG, "El video no tiene pista de audio; no se genera .m4a")
                return false
            }

            extractor.selectTrack(pistaAudio)
            muxer = MediaMuxer(destino.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val pistaDestino = muxer.addTrack(formato)
            muxer.start()

            val tamMax = if (formato.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                formato.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } else {
                BUFFER_POR_DEFECTO
            }
            val buffer = ByteBuffer.allocate(tamMax)
            val info = MediaCodec.BufferInfo()

            while (true) {
                val leidos = extractor.readSampleData(buffer, 0)
                if (leidos < 0) break
                info.offset = 0
                info.size = leidos
                info.presentationTimeUs = extractor.sampleTime
                info.flags = if (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
                    MediaCodec.BUFFER_FLAG_KEY_FRAME
                } else {
                    0
                }
                muxer.writeSampleData(pistaDestino, buffer, info)
                extractor.advance()
            }
            return true
        } finally {
            runCatching { muxer?.stop() }
            runCatching { muxer?.release() }
            runCatching { extractor.release() }
        }
    }

    /** Mueve el archivo temporal a `Music/AURA/` para que sea visible desde el teléfono. */
    private fun publicarEnMediaStore(context: Context, origen: File, nombreArchivo: String): Uri? {
        val resolver = context.contentResolver
        val valores = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, nombreArchivo)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/${SosCaptureService.CARPETA}")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, valores)
        if (uri == null) {
            Log.e(TAG, "MediaStore rechazó la creación del archivo de audio")
            return null
        }

        resolver.openOutputStream(uri).use { salida ->
            if (salida == null) {
                Log.e(TAG, "No se pudo abrir el destino del audio")
                resolver.delete(uri, null, null)
                return null
            }
            origen.inputStream().use { it.copyTo(salida) }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.update(uri, ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) }, null, null)
        }
        return uri
    }
}
