package com.example.myapplication.capture

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.myapplication.AuraApplication
import com.example.myapplication.data.AuthRepository
import com.example.myapplication.data.SesionExpiradaException
import com.example.myapplication.network.AuraApi
import com.example.myapplication.network.AuraApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Sube al backend la evidencia que dejó [SosCaptureService] cuando terminó la alerta.
 *
 * Corre en WorkManager y no dentro del servicio de captura por una razón concreta: el servicio
 * muere apenas se cierran los archivos, y la subida de dos videos por red móvil puede tardar
 * minutos y fallar a mitad de camino. WorkManager espera a que haya red, reintenta con backoff
 * y sobrevive a que el proceso se muera — que es exactamente lo que uno quiere de la evidencia
 * de una emergencia.
 *
 * El trabajo son dos llamadas encadenadas: abrir el incidente (`/incidents/activate`) y subir
 * los archivos a `/incidents/{id}/evidence`. Como un reintento vuelve a ejecutar `doWork()`
 * entero, el id del incidente se memoriza en disco: sin eso, cada reintento fallido de la
 * subida dejaría un incidente huérfano más en la base.
 */
class EvidenceUploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    // La instancia compartida de la Application, no una nueva: el candado que serializa el
    // refresh del token es por instancia, y dos AuthRepository distintos podrían refrescar a la
    // vez y dejar al segundo usando un refresh token que el primero ya hizo revocar.
    private val auth: AuthRepository
        get() = (applicationContext as AuraApplication).authRepository

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val frontal = inputData.getString(KEY_FRONTAL)?.let(Uri::parse)
        val trasera = inputData.getString(KEY_TRASERA)?.let(Uri::parse)
        val audio = inputData.getString(KEY_AUDIO)?.let(Uri::parse)

        if (frontal == null && trasera == null && audio == null) {
            Log.w(TAG, "No hay archivos para subir; nada que hacer")
            return@withContext Result.success()
        }

        if (runAttemptCount >= MAX_INTENTOS) {
            Log.e(TAG, "Se agotaron los $MAX_INTENTOS intentos de subida; se abandona")
            limpiarIncidenteRecordado()
            return@withContext Result.failure()
        }

        try {
            val token = auth.accessTokenValido()

            val incidentId = incidenteRecordado() ?: AuraApi.activarIncidente(token).also {
                recordarIncidente(it)
                Log.i(TAG, "Incidente abierto en el backend: $it")
            }

            AuraApi.subirEvidencia(
                context = applicationContext,
                accessToken = token,
                incidentId = incidentId,
                camaraFrontal = frontal,
                camaraTrasera = trasera,
                audioAmbiente = audio
            )

            Log.i(TAG, "Evidencia subida para el incidente $incidentId")
            limpiarIncidenteRecordado()
            Result.success()
        } catch (e: SesionExpiradaException) {
            // Reintentar no sirve: hace falta que la usuaria vuelva a iniciar sesión.
            Log.e(TAG, "Sesión inválida, no se puede subir la evidencia", e)
            limpiarIncidenteRecordado()
            Result.failure()
        } catch (e: AuraApiException) {
            if (e.esTransitorio) {
                Log.w(TAG, "Fallo transitorio subiendo evidencia (intento ${runAttemptCount + 1}): ${e.message}")
                Result.retry()
            } else {
                Log.e(TAG, "El backend rechazó la evidencia: ${e.message}", e)
                limpiarIncidenteRecordado()
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado subiendo evidencia", e)
            Result.retry()
        }
    }

    /* ------------------------------------------------------- memoria entre reintentos */

    private val prefs get() = applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun incidenteRecordado(): String? = prefs.getString(id.toString(), null)

    private fun recordarIncidente(incidentId: String) {
        prefs.edit().putString(id.toString(), incidentId).commit()
    }

    private fun limpiarIncidenteRecordado() {
        prefs.edit().remove(id.toString()).apply()
    }

    companion object {
        private const val TAG = "EvidenceUpload"
        private const val PREFS = "aura_uploads_en_curso"
        private const val MAX_INTENTOS = 5

        private const val KEY_FRONTAL = "frontal"
        private const val KEY_TRASERA = "trasera"
        private const val KEY_AUDIO = "audio"

        /**
         * Encola la subida de lo que se haya grabado. Los parámetros son opcionales porque no
         * siempre hay tres archivos: en modo simple no hay cámara frontal, y sin permiso de
         * micrófono no hay audio.
         */
        fun encolar(context: Context, frontal: Uri?, trasera: Uri?, audio: Uri?) {
            if (frontal == null && trasera == null && audio == null) {
                Log.w(TAG, "No se encola nada: la captura no dejó archivos")
                return
            }

            val datos = Data.Builder()
                .putString(KEY_FRONTAL, frontal?.toString())
                .putString(KEY_TRASERA, trasera?.toString())
                .putString(KEY_AUDIO, audio?.toString())
                .build()

            val solicitud = OneTimeWorkRequestBuilder<EvidenceUploadWorker>()
                .setInputData(datos)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(TAG_TRABAJO)
                .build()

            WorkManager.getInstance(context).enqueue(solicitud)
            EstadoSubida.idTrabajo = solicitud.id
            Log.i(TAG, "Subida de evidencia encolada")
        }

        /** Etiqueta para observar el estado de las subidas desde la UI. */
        const val TAG_TRABAJO = "aura_subida_evidencia"
    }
}
