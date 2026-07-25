package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Context.contactosDataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_contactos")

/** Una persona de la red de apoyo. El [id] es interno y no cambia al editar. */
data class ContactoConfianza(
    val id: String,
    val nombre: String,
    val telefono: String,
    val relacion: String
) {
    /** Letra para el avatar. */
    val inicial: String get() = nombre.trim().take(1).uppercase().ifBlank { "?" }

    /** "Mamá • Contacto SOS", o solo "Contacto SOS" si no se indicó relación. */
    val descripcion: String
        get() = if (relacion.isBlank()) "Contacto SOS" else "$relacion • Contacto SOS"
}

/**
 * Contactos de confianza, guardados solo en el dispositivo.
 *
 * Se serializan como un JSON dentro de DataStore en vez de usar una tabla: son como mucho
 * [MAXIMO] registros que siempre se leen completos, así que una base de datos solo agregaría
 * dependencias y migraciones para nada.
 *
 * Va en un DataStore aparte del perfil a propósito — "borrar mis contactos" no debería poder
 * llevarse por delante el onboarding, ni al revés.
 */
class ContactosRepository(private val context: Context) {

    private object Keys {
        val LISTA = stringPreferencesKey("lista")
    }

    val contactos: Flow<List<ContactoConfianza>> =
        context.contactosDataStore.data.map { prefs -> deserializar(prefs[Keys.LISTA]) }

    /**
     * Agrega un contacto nuevo.
     *
     * @return false si ya se llegó al máximo; en ese caso no se guarda nada.
     */
    suspend fun agregar(nombre: String, telefono: String, relacion: String): Boolean {
        val actuales = contactos.first()
        if (actuales.size >= MAXIMO) return false

        val nuevo = ContactoConfianza(
            id = UUID.randomUUID().toString(),
            nombre = nombre.trim(),
            telefono = telefono.trim(),
            relacion = relacion.trim()
        )
        guardar(actuales + nuevo)
        return true
    }

    /** Reemplaza los datos de un contacto ya existente. Si el id no está, no hace nada. */
    suspend fun actualizar(contacto: ContactoConfianza) {
        val actuales = contactos.first()
        if (actuales.none { it.id == contacto.id }) return

        guardar(
            actuales.map {
                if (it.id == contacto.id) {
                    contacto.copy(
                        nombre = contacto.nombre.trim(),
                        telefono = contacto.telefono.trim(),
                        relacion = contacto.relacion.trim()
                    )
                } else {
                    it
                }
            }
        )
    }

    suspend fun eliminar(id: String) {
        guardar(contactos.first().filterNot { it.id == id })
    }

    private suspend fun guardar(lista: List<ContactoConfianza>) {
        context.contactosDataStore.edit { it[Keys.LISTA] = serializar(lista) }
    }

    /* --------------------------------------------------------------- serialización */

    private fun serializar(lista: List<ContactoConfianza>): String {
        val array = JSONArray()
        lista.forEach { contacto ->
            array.put(
                JSONObject()
                    .put("id", contacto.id)
                    .put("nombre", contacto.nombre)
                    .put("telefono", contacto.telefono)
                    .put("relacion", contacto.relacion)
            )
        }
        return array.toString()
    }

    private fun deserializar(json: String?): List<ContactoConfianza> {
        if (json.isNullOrBlank()) return emptyList()

        // Si el JSON quedara corrupto, es preferible una lista vacía que un crash en bucle al
        // abrir la pestaña: los contactos se pueden volver a cargar, la app no se puede reinstalar
        // en medio de una emergencia.
        return runCatching {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.optJSONObject(i) ?: return@mapNotNull null
                ContactoConfianza(
                    id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
                    nombre = obj.optString("nombre"),
                    telefono = obj.optString("telefono"),
                    relacion = obj.optString("relacion")
                )
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        /** Tope de contactos, como muestra el contador de la pantalla. */
        const val MAXIMO = 5
    }
}
