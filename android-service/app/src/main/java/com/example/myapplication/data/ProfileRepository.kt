package com.example.myapplication.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.profileDataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_profile")

enum class MetodoAcceso { PIN, BIOMETRICO }

data class Profile(
    val nombre: String,
    val onboardingCompletado: Boolean,
    val permisosSolicitados: Boolean,
    val metodoAcceso: MetodoAcceso,
    /** Grabar con las dos cámaras a la vez durante el SOS. Depende del hardware; ver DualCameraSupport. */
    val grabacionDual: Boolean
)

/** Perfil local único (sin login/backend): nombre, flags de onboarding y método de acceso elegido. */
class ProfileRepository(private val context: Context) {

    private object Keys {
        val NOMBRE = stringPreferencesKey("nombre")
        val ONBOARDING_COMPLETADO = booleanPreferencesKey("onboarding_completado")
        val PERMISOS_SOLICITADOS = booleanPreferencesKey("permisos_solicitados")
        val METODO_ACCESO = stringPreferencesKey("metodo_acceso")
        val GRABACION_DUAL = booleanPreferencesKey("grabacion_dual")
    }

    val profile: Flow<Profile> = context.profileDataStore.data.map { prefs ->
        Profile(
            nombre = prefs[Keys.NOMBRE] ?: "",
            onboardingCompletado = prefs[Keys.ONBOARDING_COMPLETADO] ?: false,
            permisosSolicitados = prefs[Keys.PERMISOS_SOLICITADOS] ?: false,
            metodoAcceso = prefs[Keys.METODO_ACCESO]
                ?.let { runCatching { MetodoAcceso.valueOf(it) }.getOrNull() }
                ?: MetodoAcceso.PIN,
            grabacionDual = prefs[Keys.GRABACION_DUAL] ?: false
        )
    }

    suspend fun setNombre(nombre: String) {
        context.profileDataStore.edit { it[Keys.NOMBRE] = nombre }
    }

    suspend fun setPermisosSolicitados(value: Boolean) {
        context.profileDataStore.edit { it[Keys.PERMISOS_SOLICITADOS] = value }
    }

    suspend fun setMetodoAcceso(metodo: MetodoAcceso) {
        context.profileDataStore.edit { it[Keys.METODO_ACCESO] = metodo.name }
    }

    suspend fun setOnboardingCompletado(value: Boolean) {
        context.profileDataStore.edit { it[Keys.ONBOARDING_COMPLETADO] = value }
    }

    suspend fun setGrabacionDual(value: Boolean) {
        context.profileDataStore.edit { it[Keys.GRABACION_DUAL] = value }
    }
}
