package com.example.myapplication.capture

import android.content.Context

/**
 * Qué incidente del backend corresponde a cada alerta grabada en el teléfono.
 *
 * Hace falta porque los dos lados identifican lo mismo de forma distinta: en el dispositivo una
 * alerta es la marca de tiempo del nombre de archivo (`20260725_143012`), y en el backend es un
 * `incidentId` que recién existe cuando [EvidenceUploadWorker] abre el incidente. Sin este puente
 * no hay forma de pedir el análisis que le corresponde a un video concreto.
 *
 * El vínculo se guarda al subir y **no se borra**: es lo que permite seguir consultando el
 * análisis mucho después, cuando el trabajo de subida ya desapareció de WorkManager.
 */
object VinculoIncidentes {

    private const val PREFS = "aura_vinculo_incidentes"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun guardar(context: Context, alertaId: String, incidentId: String) {
        prefs(context).edit().putString(alertaId, incidentId).apply()
    }

    /** @return el incidente del backend, o null si esa alerta nunca llegó a subirse. */
    fun incidentIdDe(context: Context, alertaId: String): String? =
        prefs(context).getString(alertaId, null)
}
