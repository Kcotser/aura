package com.example.myapplication.network

import android.content.Context
import android.net.Uri
import com.example.myapplication.BuildConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.BufferedSink
import okio.source
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP contra el backend de AURA.
 *
 * Todas las llamadas son **bloqueantes**: están pensadas para correr dentro de un worker o de
 * un `Dispatchers.IO`, no en el hilo principal. Se eligió OkHttp pelado en vez de Retrofit
 * porque son cinco endpoints y la subida de evidencia necesita control fino del multipart.
 *
 * El backend envuelve todo en `ApiResponse`:
 * ```
 * { "success": true, "data": { ... }, "message": null, "timestamp": "..." }
 * ```
 * por eso [dataObject] desenvuelve `data` y [fallar] saca el `message` cuando algo sale mal.
 */
object AuraApi {

    /** Coordenadas usadas al abrir el incidente mientras el GPS real no esté conectado. */
    const val LAT_PLACEHOLDER = 0.0
    const val LNG_PLACEHOLDER = 0.0

    private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

    private val baseUrl: HttpUrl by lazy {
        BuildConfig.AURA_BASE_URL.trimEnd('/').toHttpUrlOrNull()
            ?: throw IllegalStateException(
                "AURA_BASE_URL inválida: '${BuildConfig.AURA_BASE_URL}'. " +
                    "Configúrala en app/build.gradle.kts o con -PauraBaseUrl=https://..."
            )
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            // El plan free de Render apaga el servicio por inactividad: la primera request
            // después de un rato se queda esperando ~50 s mientras el contenedor arranca. Con
            // los 10 s habituales, ese arranque en frío se vería como "no hay conexión".
            .readTimeout(120, TimeUnit.SECONDS)
            // Subir dos videos por una red móvil puede tardar bastante más que una request normal.
            .writeTimeout(10, TimeUnit.MINUTES)
            .callTimeout(15, TimeUnit.MINUTES)
            .apply {
                if (BuildConfig.DEBUG) {
                    // BASIC a propósito: HEADERS o BODY volcarían el header Authorization y las
                    // contraseñas de login al logcat.
                    addInterceptor(
                        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC)
                    )
                }
            }
            .build()
    }

    /* ------------------------------------------------------------------ auth */

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String?
    ) {
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
            .put("firstName", firstName)
            .put("lastName", lastName)
            .put("phoneNumber", phoneNumber ?: JSONObject.NULL)

        ejecutar(postJson("api/v1/auth/register", body)).use { verificar(it) }
    }

    fun login(email: String, password: String, deviceId: String): AuthTokens {
        val body = JSONObject()
            .put("email", email)
            .put("password", password)
            .put("deviceId", deviceId)

        return ejecutar(postJson("api/v1/auth/login", body)).use { AuthTokens.desde(dataObject(it)) }
    }

    fun refresh(refreshToken: String, deviceId: String): AuthTokens {
        val body = JSONObject()
            .put("refreshToken", refreshToken)
            .put("deviceId", deviceId)

        return ejecutar(postJson("api/v1/auth/refresh", body)).use { AuthTokens.desde(dataObject(it)) }
    }

    /** Perfil de la cuenta autenticada (`GET /users/me`). */
    fun perfil(accessToken: String): PerfilUsuario {
        val request = Request.Builder()
            .url(baseUrl.newBuilder().addPathSegments("api/v1/users/me").build())
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        return ejecutar(request).use { PerfilUsuario.desde(dataObject(it)) }
    }

    /* -------------------------------------------------------------- incidentes */

    /**
     * Abre un incidente y devuelve su id, que es lo que necesita [subirEvidencia].
     *
     * Las coordenadas son un placeholder mientras el GPS no esté enganchado: el backend exige
     * latitud y longitud para activar, así que no hay forma de crear un incidente sin mandarlas.
     */
    fun activarIncidente(
        accessToken: String,
        latitud: Double = LAT_PLACEHOLDER,
        longitud: Double = LNG_PLACEHOLDER
    ): String {
        val body = JSONObject()
            .put("latitude", latitud)
            .put("longitude", longitud)

        val request = postJson("api/v1/incidents/activate", body)
            .newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return ejecutar(request).use { respuesta ->
            dataObject(respuesta).optString("id").takeIf { it.isNotBlank() }
                ?: throw AuraApiException(respuesta.code, "El backend no devolvió el id del incidente")
        }
    }

    /* ---------------------------------------------------------------- evidencia */

    /**
     * Sube los archivos capturados al `EvidenceController` en un único multipart.
     *
     * Los nombres de las partes (`frontCamera`, `backCamera`, `ambientAudio`) tienen que coincidir
     * exactamente con los `@RequestPart` del backend. Cualquiera puede venir en `null` — el
     * backend solo exige que llegue al menos uno — pero recién cuando llegan las tres emite
     * `AllEvidenceUploadedEvent`.
     */
    fun subirEvidencia(
        context: Context,
        accessToken: String,
        incidentId: String,
        camaraFrontal: Uri?,
        camaraTrasera: Uri?,
        audioAmbiente: Uri?
    ) {
        val multipart = MultipartBody.Builder().setType(MultipartBody.FORM)
        var partes = 0

        fun agregar(nombreParte: String, uri: Uri?, mime: String, extension: String) {
            if (uri == null) return
            multipart.addFormDataPart(
                nombreParte,
                "${incidentId}_$nombreParte.$extension",
                UriRequestBody(context, uri, mime)
            )
            partes++
        }

        agregar("frontCamera", camaraFrontal, "video/mp4", "mp4")
        agregar("backCamera", camaraTrasera, "video/mp4", "mp4")
        agregar("ambientAudio", audioAmbiente, "audio/mp4", "m4a")

        require(partes > 0) { "No hay ningún archivo de evidencia para subir" }

        val request = Request.Builder()
            .url(baseUrl.newBuilder().addPathSegments("api/v1/incidents/$incidentId/evidence").build())
            .header("Authorization", "Bearer $accessToken")
            .post(multipart.build())
            .build()

        ejecutar(request).use { verificar(it) }
    }

    /* ------------------------------------------------------------------ plomería */

    private fun postJson(ruta: String, body: JSONObject): Request = Request.Builder()
        .url(baseUrl.newBuilder().addPathSegments(ruta).build())
        .post(body.toString().toRequestBody(JSON))
        .build()

    private fun ejecutar(request: Request): Response = try {
        client.newCall(request).execute()
    } catch (e: IOException) {
        throw AuraApiException(0, "No se pudo contactar al servidor: ${e.message}", e)
    }

    /** Lanza [AuraApiException] si la respuesta no fue 2xx. */
    private fun verificar(respuesta: Response) {
        if (respuesta.isSuccessful) return

        // El cuerpo de error también viene envuelto en ApiResponse, pero puede ser HTML o vacío
        // (401 del entry point de Spring Security, por ejemplo), así que se lee a la defensiva.
        val detalle = runCatching {
            val texto = respuesta.body?.string().orEmpty()
            JSONObject(texto).optString("message").takeIf { it.isNotBlank() }
        }.getOrNull()

        throw AuraApiException(respuesta.code, detalle ?: "HTTP ${respuesta.code} ${respuesta.message}")
    }

    /** Verifica la respuesta y devuelve el `data` de la envoltura `ApiResponse`. */
    private fun dataObject(respuesta: Response): JSONObject {
        verificar(respuesta)
        val texto = respuesta.body?.string().orEmpty()
        val sobre = runCatching { JSONObject(texto) }.getOrElse {
            throw AuraApiException(respuesta.code, "Respuesta del servidor ilegible")
        }
        return sobre.optJSONObject("data")
            ?: throw AuraApiException(respuesta.code, sobre.optString("message", "El servidor no devolvió datos"))
    }
}

/** Datos de la cuenta que devuelve `/users/me`. */
data class PerfilUsuario(
    val email: String,
    val nombre: String,
    val apellido: String
) {
    companion object {
        fun desde(json: JSONObject) = PerfilUsuario(
            email = json.optString("email"),
            nombre = json.optString("firstName"),
            apellido = json.optString("lastName")
        )
    }
}

/** Tokens devueltos por `/auth/login` y `/auth/refresh`. */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long
) {
    companion object {
        fun desde(json: JSONObject) = AuthTokens(
            accessToken = json.getString("accessToken"),
            refreshToken = json.getString("refreshToken"),
            expiresInSeconds = json.optLong("expiresInSeconds", 0L)
        )
    }
}

/**
 * Fallo de una llamada al backend.
 *
 * @param code código HTTP, o `0` si ni siquiera se llegó a hablar con el servidor (sin red,
 *   DNS caído, URL mal configurada). Esa distinción es la que decide si vale la pena reintentar.
 */
class AuraApiException(
    val code: Int,
    message: String,
    cause: Throwable? = null
) : IOException(message, cause) {

    /** true si reintentar más tarde tiene sentido: sin red o error del servidor. */
    val esTransitorio: Boolean get() = code == 0 || code >= 500

    /** true si el token no sirve y hay que volver a autenticarse. */
    val esNoAutorizado: Boolean get() = code == 401 || code == 403
}

/**
 * Cuerpo multipart que lee directo de un `content://` de MediaStore.
 *
 * Va en streaming a propósito: cargar dos videos en memoria para armar un `ByteArray` es la
 * forma más fácil de comerse un OutOfMemory justo cuando hay que subir la evidencia.
 */
private class UriRequestBody(
    private val context: Context,
    private val uri: Uri,
    private val mime: String
) : RequestBody() {

    override fun contentType(): MediaType? = mime.toMediaTypeOrNull()

    override fun contentLength(): Long = runCatching {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
    }.getOrNull()?.takeIf { it >= 0 } ?: -1L

    override fun writeTo(sink: BufferedSink) {
        val entrada = context.contentResolver.openInputStream(uri)
            ?: throw IOException("No se pudo abrir el archivo de evidencia: $uri")
        entrada.source().use { sink.writeAll(it) }
    }
}
