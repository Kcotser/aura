package com.example.myapplication.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.myapplication.network.AuraApi
import com.example.myapplication.network.AuraApiException
import com.example.myapplication.network.AuthTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Sesión contra el backend de AURA: login, registro y entrega de un access token válido.
 *
 * Los tokens viven en [EncryptedSharedPreferences] y no en DataStore como el resto del perfil:
 * un refresh token en claro dentro de un archivo de preferencias es una credencial de 30 días
 * al alcance de cualquiera con acceso al dispositivo.
 *
 * El backend rota el refresh token en cada `/auth/refresh` (revoca la sesión anterior y emite
 * una nueva), así que **hay que guardar el par nuevo cada vez**; quedarse con el viejo deja la
 * sesión muerta al siguiente intento.
 */
class AuthRepository(private val context: Context) {

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "aura_auth",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    val estaLogueado: Boolean get() = prefs.getString(KEY_REFRESH, null) != null

    val email: String? get() = prefs.getString(KEY_EMAIL, null)

    /** Identificador estable de este dispositivo, para que el backend distinga sesiones. */
    val deviceId: String
        get() = prefs.getString(KEY_DEVICE_ID, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).commit()
        }

    suspend fun login(email: String, password: String) = withContext(Dispatchers.IO) {
        val tokens = AuraApi.login(email.trim(), password, deviceId)
        guardar(tokens, email.trim())
    }

    suspend fun registrar(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        telefono: String?
    ) = withContext(Dispatchers.IO) {
        AuraApi.register(email.trim(), password, nombre.trim(), apellido.trim(), telefono?.trim())
        // El registro no devuelve tokens, así que se encadena un login para dejar la sesión lista.
        val tokens = AuraApi.login(email.trim(), password, deviceId)
        guardar(tokens, email.trim())
    }

    fun cerrarSesion() {
        prefs.edit()
            .remove(KEY_ACCESS)
            .remove(KEY_REFRESH)
            .remove(KEY_EXPIRA_EN)
            .remove(KEY_EMAIL)
            .commit()
    }

    /**
     * Devuelve un access token utilizable, renovándolo contra `/auth/refresh` si está por vencer.
     *
     * Bloqueante: llamar desde un worker o desde `Dispatchers.IO`. Sincronizado porque la subida
     * de evidencia corre en un worker en paralelo a la UI y dos refresh simultáneos harían que el
     * segundo use un refresh token ya revocado por el primero.
     *
     * @throws SesionExpiradaException si no hay sesión o el refresh ya no vale: el usuario tiene
     *   que volver a iniciar sesión, y ningún reintento automático lo va a arreglar.
     */
    @Synchronized
    fun accessTokenValido(): String {
        val refresh = prefs.getString(KEY_REFRESH, null)
            ?: throw SesionExpiradaException("No hay sesión iniciada")

        val access = prefs.getString(KEY_ACCESS, null)
        val expiraEn = prefs.getLong(KEY_EXPIRA_EN, 0L)
        if (access != null && System.currentTimeMillis() < expiraEn) return access

        val tokens = try {
            AuraApi.refresh(refresh, deviceId)
        } catch (e: AuraApiException) {
            // Un 401/403 acá significa refresh token revocado o vencido: la sesión está muerta.
            // Un fallo de red, en cambio, no dice nada sobre la validez del token, así que se
            // propaga tal cual para que el worker lo reintente más tarde.
            if (e.esNoAutorizado) {
                cerrarSesion()
                throw SesionExpiradaException("La sesión expiró, hay que iniciar sesión de nuevo")
            }
            throw e
        }

        guardar(tokens, prefs.getString(KEY_EMAIL, null))
        return tokens.accessToken
    }

    private fun guardar(tokens: AuthTokens, email: String?) {
        // Margen de 60 s para no mandar un token que vence en pleno vuelo de una subida larga.
        val expiraEn = System.currentTimeMillis() +
            (tokens.expiresInSeconds.coerceAtLeast(MARGEN_SEGUNDOS) - MARGEN_SEGUNDOS) * 1000L

        prefs.edit()
            .putString(KEY_ACCESS, tokens.accessToken)
            .putString(KEY_REFRESH, tokens.refreshToken)
            .putLong(KEY_EXPIRA_EN, expiraEn)
            .apply { if (email != null) putString(KEY_EMAIL, email) }
            .commit()
    }

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_EXPIRA_EN = "access_expira_en"
        const val KEY_EMAIL = "email"
        const val KEY_DEVICE_ID = "device_id"
        const val MARGEN_SEGUNDOS = 60L
    }
}

/** La sesión ya no sirve y no se puede recuperar sin que el usuario vuelva a iniciar sesión. */
class SesionExpiradaException(message: String) : Exception(message)
