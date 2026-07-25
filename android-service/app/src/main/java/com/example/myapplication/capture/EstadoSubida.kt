package com.example.myapplication.capture

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

/**
 * Puente entre [SosCaptureService], que encola la subida, y la pantalla que la muestra.
 *
 * Es memoria del proceso y no disco, igual que [com.example.myapplication.gesture.SosTrigger]:
 * solo sirve para que la pantalla que aparece justo después de la alerta sepa qué trabajo mirar.
 * Si el proceso muere, WorkManager sigue subiendo por su cuenta — lo único que se pierde es
 * poder mostrar el progreso, no la subida.
 */
object EstadoSubida {

    /**
     * Trabajo de subida de la última alerta, o `null` si todavía se están cerrando los archivos.
     *
     * La captura tarda en cerrar los MP4 y extraer el `.m4a`, así que la pantalla de envío se
     * abre antes de que esto tenga valor: ese hueco es el estado "guardando".
     */
    var idTrabajo: UUID? by mutableStateOf(null)

    /** Empieza una alerta nueva: descarta el trabajo de la anterior. */
    fun reiniciar() {
        idTrabajo = null
    }
}
