# AURA — mapa del proyecto (para IA)

> App Android (Jetpack Compose + Material3) de seguridad personal. Arrancó como
> maqueta visual (33 pantallas del kit de Stitch) y hoy ya tiene un primer
> bloque real: **perfil local, acceso por PIN/biometría, y un atajo físico
> (botones de volumen) que dispara la alerta SOS aunque la app esté cerrada o
> el teléfono bloqueado**. El resto (Red de Apoyo, Historial, captura real del
> SOS, Camuflaje, backend) sigue siendo maqueta — se va conectando pantalla
> por pantalla.

## ⚠️ Gotchas antes de tocar código

- **El paquete sigue siendo `com.example.myapplication`** aunque la app se
  llama "AURA" (`strings.xml: app_name`). No se ha renombrado el paquete.
- **JAVA_HOME no está en el PATH del sistema.** Para compilar por CLI:
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
  .\gradlew.bat :app:assembleDebug
  ```
- `androidx.biometric:1.1.0` arrastra transitivamente `androidx.fragment:1.2.5`
  (versión de 2020), incompatible con el resto de las libs modernas del
  proyecto y causa crashes en runtime al pedir permisos/biometría. Por eso
  `libs.versions.toml` fija `androidx.fragment:fragment-ktx` a una versión
  moderna explícita — **no borrar esa dependencia** aunque parezca redundante.
- El origen de diseño está en `stitch_sororia_safety_tech_ui_kit/<pantalla>/`
  (cada carpeta tiene `code.html` + `screen.png`) y el sistema de diseño
  completo (colores, tipografía, spacing, componentes) en
  `stitch_sororia_safety_tech_ui_kit/sororia/DESIGN.md`. Si hay que
  verificar un valor exacto (hex, radio, tamaño), la fuente de verdad es ese
  DESIGN.md, no lo que haya en `Color.kt`/`Dimens.kt` (que es una
  traducción manual a Kotlin).
- El `AccessibilityService` que detecta el gesto de volumen **no se puede
  activar desde la app** — Android obliga a habilitarlo a mano en Ajustes del
  sistema > Accesibilidad. Si el usuario hace "Forzar detención" de la app
  desde Ajustes, ese permiso se desactiva solo (medida de seguridad del SO) y
  hay que volver a activarlo a mano.
- No hay login ni backend: el "perfil" es local a la instalación (un solo
  perfil por dispositivo, sin multi-usuario).

## Stack

- Kotlin 2.2.10, AGP 9.3.1, Compose BOM 2026.02.01, Material3, `compileSdk 36`, `minSdk 24`.
- Sin Navigation-Compose ni Hilt: navegación casera (ver abajo), DI manual vía `AuraApplication`.
- Persistencia: `androidx.datastore:datastore-preferences` (perfil) + `androidx.security:security-crypto` (PIN, como hash+salt, nunca en texto plano).
- Acceso: `androidx.biometric` (BiometricPrompt) + PIN propio.
- Gesto fuera de la app: `AccessibilityService` + `androidx.lifecycle:lifecycle-process` (ProcessLifecycleOwner).
- Iconos: `material-icons-extended`.

## Arquitectura de navegación

Todo vive en `ui/nav/Nav.kt`:

- `sealed class Screen` — pantallas de pila completa (incluye `Screen.Main`, que es el shell con bottom nav).
- `sealed class Sheet` — bottom sheets modales (se muestran superpuestos, no en la pila).
- `class Nav(backStack, sheetState)` — controlador: `push`, `pop`, `replace`, `popToMain`, `showSheet`, `hideSheet`.
- `rememberNav(start = Screen.Splash)` crea la instancia.

`ui/AuraApp.kt` es el único punto que hace `when (nav.current)` y `when (nav.sheet)` — ahí se registra cada pantalla/sheet nuevo. También resuelve, en un único `LaunchedEffect` secuencial, el punto de entrada real al arrancar (onboarding vs. alerta SOS vs. `Screen.Main` directo — ver más abajo por qué está todo en un solo efecto y no separado).

`MainShell` (privado, mismo archivo) resuelve las 4 pestañas del bottom nav (`Tab.Inicio/Historial/RedApoyo/Ajustes`) con estado local, **no** pasan por `Nav`. Desde acá también se resuelve el candado por pestaña (ver "Perfil local y acceso").

Para agregar una pantalla nueva: (1) añadir el `object` a `Screen` o `Sheet`, (2) crear el composable `fun XScreen(nav: Nav)` en `ui/screens/`, (3) registrar la rama en `AuraApp.kt`.

## Perfil local y acceso (`data/`, `AuraApplication.kt`)

- **`ProfileRepository`** (DataStore) — nombre, `onboardingCompletado`, `permisosSolicitados`, `metodoAcceso` (`PIN` | `BIOMETRICO`).
- **`PinRepository`** (EncryptedSharedPreferences) — PIN como hash SHA-256 + salt, nunca en texto plano. `setPin()` usa `commit()` (no `apply()`) a propósito, para garantizar que quedó en disco antes de navegar.
- **`SessionState`** (objeto en memoria, `mutableStateOf`) — `tabsUnlocked`: si ya se autenticó para ver más allá de Inicio, en esta sesión. Se resetea a `false` al relockear (2 min en background) o al "Cerrar sesión".
- **`AuthRepository`** (EncryptedSharedPreferences) — la única cuenta **no** local: sesión contra el backend (`/api/v1/auth`). Guarda access + refresh token y renueva solo cuando hace falta. El backend **rota** el refresh token en cada `/auth/refresh`, así que hay que guardar el par nuevo cada vez. `accessTokenValido()` es bloqueante y está sincronizado: la subida de evidencia corre en un worker en paralelo a la UI.
- **`AuraApplication`** expone `profileRepository`, `pinRepository` y `authRepository` como singletons; se obtienen vía `LocalContext.current.applicationContext as AuraApplication`.

**Flujo de onboarding** (`NombrePerfilScreen` → `PermisosEsencialesScreen` → `AccesoBiometricoScreen` → `CrearPinScreen` → `CalibracionGestoRapidoScreen` → `OnboardingCompletadoScreen`): el PIN se configura **siempre**, sea o no el método principal — sirve de respaldo si falla la biometría. Recién en `OnboardingCompletadoScreen` se marca `onboardingCompletado = true`.

**Reingreso**: Inicio (el botón de SOS) es siempre visible sin PIN. Tocar cualquier otra pestaña sin `SessionState.tabsUnlocked` muestra `LockScreen` (composable reutilizable, recibe `onUnlocked: () -> Unit`, ya no es un `Screen` de la pila) en el lugar de esa pestaña. `AuraApp.kt` también relockea (pone `tabsUnlocked = false` y hace `popToMain()`) si la app estuvo 2+ min en segundo plano.

## Gesto de activación fuera de la app (`gesture/`)

Presionar subir y bajar volumen, 3 veces cada uno, dispara la alerta SOS **sin abrir la app**, incluso con la pantalla bloqueada:

- **`VolumeGestureAccessibilityService`** — `AccessibilityService` con `canRequestFilterKeyEvents`, corre como *foreground service* (más resistente a que el sistema lo mate). Solo cuenta pulsaciones cuando `AppForegroundState.isForeground == false` (si la app está abierta, la calibración la escucha directo `MainActivity.dispatchKeyEvent`, para no contar el gesto dos veces).
- **`SosTrigger`** — flag compartido (`mutableStateOf`) que el servicio enciende y `AuraApp.kt` consume para saltar directo a `Screen.TransicionActivando`, sin pedir PIN (el gesto ya es la confirmación).
- **`MainActivity`** se muestra sobre la pantalla de bloqueo (`setShowWhenLocked`/`setTurnScreenOn`) solo cuando `SosTrigger.pending` está activo al crearse/recibir el intent; limpia esos flags en `onPause` para que abrir la app normalmente siga pidiendo PIN.
- **`DebugNotifier`** — notificaciones de debug (visibles en pantalla de bloqueo) con el conteo en vivo del gesto y aviso cuando se dispara. Pensado para diagnosticar, no para producción.
- Config: `res/xml/accessibility_service_config.xml`, registrado en el Manifest con `foregroundServiceType="specialUse"`. Se activa/gestiona desde el sheet **Configurar Atajo** (`ConfigurarAtajoSheetContent`), que linkea a Ajustes de Accesibilidad del sistema.

## Captura y envío de evidencia (`capture/`, `network/`)

Lo que pasa entre que arranca la alerta y que la evidencia queda en el backend:

- **`SosCaptureService`** — foreground service (`camera|microphone`) que graba mientras dura la alerta. Modo simple (trasera + audio) o dual (frontal con audio + trasera), según la preferencia `grabacionDual`; el dual depende del hardware, ver `DualCameraSupport`. Los archivos van a `Movies/AURA/` vía MediaStore.
- **`AudioExtractor`** — saca la pista de audio del video a un `.m4a` aparte (remux, sin recodificar), porque el backend espera el audio como una parte propia.
- **`EvidenceUploadWorker`** — sube la evidencia con WorkManager, **no** dentro del servicio: el servicio muere apenas cierra los archivos, y subir dos videos puede tardar minutos y fallar. Abre el incidente (`/incidents/activate`) y sube el multipart (`/incidents/{id}/evidence`). El `incidentId` se memoriza en disco entre reintentos: sin eso cada reintento dejaría un incidente huérfano.
- **`EstadoSubida`** — objeto en memoria con el id del trabajo encolado, para que `EnvioEvidenciaScreen` muestre el estado real. Si el proceso muere se pierde el progreso en pantalla, no la subida.
- **`network/AuraApi`** — cliente OkHttp. El multipart va en streaming desde el `content://`; cargar los videos en memoria sería un OOM justo al subir la evidencia. La URL base es `BuildConfig.AURA_BASE_URL`, configurable con `-PauraBaseUrl=` o `gradle.properties`.

Mapeo a las partes que espera el backend: dual → `frontCamera` + `backCamera` + `ambientAudio`; simple → `backCamera` + `ambientAudio`. El backend solo emite `AllEvidenceUploadedEvent` con las tres, así que en modo simple el incidente no avanza de estado solo.

## Sistema de diseño (`ui/theme/`)

| Archivo | Contenido |
|---|---|
| `Color.kt` | Paleta completa: primary (violeta `#3F0075`), secondary (azul), tertiary (verde), error (rojo emergencia), superficies, + extras `Success/Warning` y paleta `Camo*` para el modo camuflaje. |
| `Type.kt` | Escala tipográfica mapeada a slots Material3 (`headlineMedium`=headline-lg, `titleLarge`=headline-md, `titleMedium`=critical-data, etc). Usa `FontFamily.Default`; **falta** cargar la fuente Inter real (hay un comentario marcando dónde). |
| `Dimens.kt` | `Spacing` (4/8pt grid), `Sizes` (alturas: botón 48dp, input 52dp, lista 64dp, touch-min 44dp), `Shapes` (card 16dp, button/input 8dp, chip full, bottomSheet solo esquinas superiores). |
| `Theme.kt` | `AuraTheme{}` — color scheme fijo de marca, **sin** dynamic color ni dark theme del sistema (el "modo oscuro" real de la app es el Modo Camuflaje). |

## Componentes reutilizables (`ui/components/Components.kt`)

`PrimaryButton`, `CriticalButton` (rojo, para SOS/logout), `SecondaryOutlineButton`, `StatusChip`, `AppCard`, `SectionHeader`, `AuraTopBar` (con back opcional), `IconButtonSlot`, `CircleIcon`, `AvatarPlaceholder`, `AppTextField`, `SelectableOptionCard`, `WarningBanner`, `SwitchSettingRow`, `PinDots`/`PinKeypad` (teclado numérico, compartido por `CrearPinScreen`, `LockScreen` y "cambiar PIN" en Ajustes).

## Inventario de pantallas

Shell principal (`Screen.Main`, 4 tabs — bottom nav propio, no pasa por `Nav`):

| Tab | Archivo | Función |
|---|---|---|
| Inicio | `InicioScreen.kt` | Botón circular de activación (long-press → `TransicionActivando`), tarjeta Red de Apoyo. Único tab sin candado. |
| Historial | `HistorialScreen.kt` | Lista de casos con filtros y búsqueda (visual, maqueta) |
| Red de Apoyo | `RedApoyoScreen.kt` | Contactos de confianza + directorio institucional (maqueta, no persiste) |
| Ajustes | `AjustesScreen.kt` | Perfil, estado del sistema, links a ajustes avanzados, cerrar sesión |

Pantallas de pila completa (`Screen`), por archivo:

| Archivo | Screens que contiene |
|---|---|
| `SplashScreen.kt` | `Splash` (un solo CTA "Comenzar" → `Login`) |
| `LoginScreen.kt` | `Login` (real) — login y registro contra el backend en el mismo formulario |
| `NombrePerfilScreen.kt` | `NombrePerfil` (pide el nombre, real) |
| `CrearPinScreen.kt` | `CrearPin` (real) |
| `LockScreen.kt` | Candado reutilizable (real) — ya **no** es un `Screen` de la pila, ver "Perfil local y acceso" |
| `OnboardingScreens.kt` | `PermisosEsenciales` (permisos reales), `AccesoBiometrico` (real), `CalibracionGesto` (gesto real de volumen), `OnboardingCompletado` |
| `SosFlowScreens.kt` | `TransicionActivando` (arranca la captura real), `ConfirmandoSOS`, `EnvioEvidencia` (real: corta la grabación y muestra el estado del trabajo de subida; "Continuar" va a Historial) |
| `IncidenteScreens.kt` | `DetalleDeCaso`, `FichaIncidente`, `EditarIncidente`, `VisorMultimedia` (maqueta) |
| `AjustesAvanzadosScreens.kt` | `AjustesBiometrico` (real: cambiar método/PIN), `CalibracionAV`, `PoliticasAutodestruccion` (maqueta) |
| `CamuflajeScreens.kt` | `SelectorCamuflaje`, `AjustesCamuflaje` (maqueta) |
| `CamuflajeDecoyScreens.kt` | `CamuflajeCalculadora`, `CamuflajeMusica`, `CamuflajeBloqueo` (pantallas señuelo, maqueta, look no-Material a propósito) |

Bottom sheets (`ui/sheets/BottomSheets.kt`, todas `Sheet`): `AgregarContacto`, `EditarContacto`, `EnviarAContacto`, `ExportarReporte`, `EliminarArchivos`, `GestionCuenta` (incluye "cerrar sesión", que resetea `SessionState.tabsUnlocked`), `ConfigurarAtajo` (real: estado del `AccessibilityService` + link a Ajustes del sistema), `ConfirmacionAprobacion`.

**Cobertura vs. kit de Stitch**: las 33 pantallas de `stitch_sororia_safety_tech_ui_kit/` (excluyendo la carpeta `sororia/` que solo tiene el DESIGN.md) están representadas, más las nuevas de perfil/acceso que no estaban en el kit original. Cada composable tiene un `@Preview(widthDp = 390, heightDp = 844)` al final del archivo para verlo sin correr la app.

## Estado / próximos pasos típicos

**Ya real**: perfil local (nombre, onboarding), permisos del sistema (cámara/audio/ubicación/notificaciones pedidos una sola vez), método de acceso PIN/biometría con PIN siempre como respaldo, candado por pestaña con relock por tiempo, gesto físico de volumen que dispara la alerta SOS fuera de la app y con pantalla bloqueada (vía `AccessibilityService` + foreground service), notificaciones de debug del gesto.

**Sigue siendo maqueta** (sin ViewModel, sin persistencia, sin validación real):
- Red de Apoyo: contactos hardcodeados, no hay CRUD real ni llamadas reales.
- Historial / Incidentes: datos de ejemplo, sin persistencia.
- SOS: el trigger, la captura (cámara + audio) y la subida al backend son reales. Siguen siendo visuales el envío a contactos y el cifrado.
- GPS: el incidente se abre con coordenadas placeholder (`AuraApi.LAT_PLACEHOLDER`/`LNG_PLACEHOLDER`); el backend exige lat/lng para activar, pero todavía no se manda la ubicación real.
- Camuflaje: no cambia el ícono/nombre real de la app.
- Falta cargar la fuente Inter real (actualmente usa la fuente del sistema).
- El paquete/applicationId no se ha migrado de `com.example.myapplication` a algo con "aura".
- Backend: solo está conectado el flujo de auth + subida de evidencia. Historial, Red de Apoyo y el resto siguen viviendo solo en el dispositivo (DataStore + EncryptedSharedPreferences).
