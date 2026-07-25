package com.example.myapplication.capture

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

/** Qué es cada archivo dentro de una alerta. El orden del enum es el orden en que se listan. */
enum class TipoEvidencia(val etiqueta: String) {
    VIDEO_FRONTAL("Cámara frontal"),
    VIDEO_TRASERO("Cámara trasera"),
    AUDIO("Audio ambiental")
}

/** Un archivo concreto en el almacenamiento del teléfono. */
data class ArchivoEvidencia(
    val uri: Uri,
    val nombre: String,
    val tipo: TipoEvidencia,
    val bytes: Long,
    val duracionMs: Long
) {
    val esVideo: Boolean get() = tipo != TipoEvidencia.AUDIO
}

/** Todo lo que quedó grabado de una misma alerta. */
data class AlertaGrabada(
    /** Marca de tiempo del nombre de archivo (`20260725_143012`); identifica la alerta. */
    val id: String,
    val instante: Long,
    val archivos: List<ArchivoEvidencia>,
    /** Incidente del backend al que se subió, o null si nunca llegó a subirse. */
    val incidentId: String? = null
) {
    val bytesTotales: Long get() = archivos.sumOf { it.bytes }

    /** La duración de la alerta es la del archivo más largo, no la suma: se grabaron en paralelo. */
    val duracionMs: Long get() = archivos.maxOfOrNull { it.duracionMs } ?: 0L

    val videos: Int get() = archivos.count { it.esVideo }
    val audios: Int get() = archivos.count { !it.esVideo }
}

/**
 * Lee del dispositivo la evidencia que dejaron las alertas, agrupada por alerta.
 *
 * La fuente de verdad es MediaStore, no una base de datos propia: [SosCaptureService] ya escribe
 * ahí y el nombre de archivo lleva la marca de tiempo, así que el registro se puede reconstruir
 * sin duplicar estado que se podría desincronizar. Lo que se ve en Historial es literalmente lo
 * que existe en el teléfono — si un archivo se borró desde la galería, deja de aparecer.
 *
 * Nombres que produce la captura, y de dónde sale el agrupamiento:
 * ```
 * SOS_20260725_143012.mp4            simple  → cámara trasera
 * SOS_20260725_143012_frontal.mp4    dual    → cámara frontal
 * SOS_20260725_143012_trasera.mp4    dual    → cámara trasera
 * SOS_20260725_143012[_frontal].m4a          → audio extraído
 * ```
 */
object RegistroEvidencia {

    private const val TAG = "RegistroEvidencia"

    private val PATRON = Regex("""^SOS_(\d{8}_\d{6})(?:_(frontal|trasera))?\.(mp4|m4a)$""")

    /** `Locale.US` a propósito: el nombre lo genera la captura con dígitos ASCII fijos. */
    private val FORMATO_ID = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    suspend fun listarAlertas(context: Context): List<AlertaGrabada> = withContext(Dispatchers.IO) {
        val encontrados =
            consultar(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DURATION) +
                consultar(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DURATION)

        encontrados
            .groupBy({ it.first }, { it.second })
            .map { (id, archivos) ->
                AlertaGrabada(
                    id = id,
                    instante = instanteDe(id),
                    archivos = archivos.sortedBy { it.tipo.ordinal },
                    incidentId = VinculoIncidentes.incidentIdDe(context, id)
                )
            }
            .sortedByDescending { it.instante }
    }

    /** @return pares (id de alerta, archivo) de una colección de MediaStore. */
    private fun consultar(
        context: Context,
        coleccion: Uri,
        columnaDuracion: String
    ): List<Pair<String, ArchivoEvidencia>> {
        val columnas = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            columnaDuracion
        )

        // Se filtra por nombre y no por carpeta porque RELATIVE_PATH solo existe desde API 29 y
        // el proyecto soporta desde API 24. El prefijo SOS_ ya es específico de esta app.
        val resultado = mutableListOf<Pair<String, ArchivoEvidencia>>()

        try {
            context.contentResolver.query(
                coleccion,
                columnas,
                "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?",
                arrayOf("SOS_%"),
                null
            )?.use { cursor ->
                val idxId = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val idxNombre = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val idxTamano = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val idxDuracion = cursor.getColumnIndex(columnaDuracion)

                while (cursor.moveToNext()) {
                    val nombre = cursor.getString(idxNombre) ?: continue
                    val coincidencia = PATRON.find(nombre) ?: continue

                    val (marca, camara, extension) = coincidencia.destructured
                    val tipo = when {
                        extension == "m4a" -> TipoEvidencia.AUDIO
                        camara == "frontal" -> TipoEvidencia.VIDEO_FRONTAL
                        else -> TipoEvidencia.VIDEO_TRASERO
                    }

                    val archivo = ArchivoEvidencia(
                        uri = ContentUris.withAppendedId(coleccion, cursor.getLong(idxId)),
                        nombre = nombre,
                        tipo = tipo,
                        bytes = cursor.getLong(idxTamano),
                        duracionMs = if (idxDuracion >= 0) cursor.getLong(idxDuracion) else 0L
                    )
                    resultado += marca to archivo
                }
            }
        } catch (e: SecurityException) {
            // Hasta API 28 hace falta permiso de almacenamiento para consultar MediaStore; desde
            // API 29 los archivos propios se ven sin pedir nada. Sin permiso, Historial se ve
            // vacío en lugar de tirar la app abajo.
            Log.w(TAG, "Sin permiso para leer MediaStore; no se puede listar la evidencia", e)
        } catch (e: Exception) {
            Log.e(TAG, "Falló la consulta a MediaStore sobre $coleccion", e)
        }

        return resultado
    }

    private fun instanteDe(id: String): Long =
        runCatching { FORMATO_ID.parse(id)?.time }.getOrNull() ?: 0L
}
