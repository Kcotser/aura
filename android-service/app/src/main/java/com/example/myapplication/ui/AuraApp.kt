package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.myapplication.AuraApplication
import com.example.myapplication.data.SessionState
import com.example.myapplication.gesture.AppForegroundState
import com.example.myapplication.gesture.SosTrigger
import com.example.myapplication.ui.nav.Nav
import com.example.myapplication.ui.nav.Screen
import com.example.myapplication.ui.nav.Sheet
import com.example.myapplication.ui.nav.rememberNav
import com.example.myapplication.ui.screens.AccesoBiometricoScreen
import com.example.myapplication.ui.screens.AjustesBiometricoScreen
import com.example.myapplication.ui.screens.AjustesCamuflajeScreen
import com.example.myapplication.ui.screens.AjustesScreen
import com.example.myapplication.ui.screens.CalibracionAVScreen
import com.example.myapplication.ui.screens.CalibracionGestoRapidoScreen
import com.example.myapplication.ui.screens.CamuflajeBloqueoScreen
import com.example.myapplication.ui.screens.CamuflajeCalculadoraScreen
import com.example.myapplication.ui.screens.CamuflajeMusicaScreen
import com.example.myapplication.ui.screens.ConfirmandoSOSScreen
import com.example.myapplication.ui.screens.CrearPinScreen
import com.example.myapplication.ui.screens.DetalleDeCasoScreen
import com.example.myapplication.ui.screens.EditarIncidenteScreen
import com.example.myapplication.ui.screens.FichaIncidenteScreen
import com.example.myapplication.ui.screens.HistorialScreen
import com.example.myapplication.ui.screens.InicioScreen
import com.example.myapplication.ui.screens.LockScreen
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.OnboardingCompletadoScreen
import com.example.myapplication.ui.screens.PermisosEsencialesScreen
import com.example.myapplication.ui.screens.EnvioEvidenciaScreen
import com.example.myapplication.ui.screens.PoliticasAutodestruccionScreen
import com.example.myapplication.ui.screens.RedApoyoScreen
import com.example.myapplication.ui.screens.SelectorCamuflajeScreen
import com.example.myapplication.ui.screens.SplashScreen
import com.example.myapplication.ui.screens.TransicionActivandoScreen
import com.example.myapplication.ui.screens.VisorMultimediaScreen
import com.example.myapplication.ui.sheets.AgregarContactoSheetContent
import com.example.myapplication.ui.sheets.ConfigurarAtajoSheetContent
import com.example.myapplication.ui.sheets.ConfirmacionAprobacionSheetContent
import com.example.myapplication.ui.sheets.EditarContactoSheetContent
import com.example.myapplication.ui.sheets.EliminarArchivosSheetContent
import com.example.myapplication.ui.sheets.EnviarAContactoSheetContent
import com.example.myapplication.ui.sheets.ExportarReporteSheetContent
import com.example.myapplication.ui.sheets.GestionCuentaSheetContent
import com.example.myapplication.ui.theme.Sizes
import kotlinx.coroutines.flow.first

/** Pestañas de la barra inferior. */
enum class Tab(val label: String, val icon: ImageVector) {
    Inicio("Inicio", Icons.Filled.Home),
    Historial("Historial", Icons.Filled.History),
    RedApoyo("Red de Apoyo", Icons.Filled.PersonAddAlt1),
    Ajustes("Ajustes", Icons.Filled.Settings)
}

/**
 * Punto de entrada de la app. Toda la navegación se resuelve con [Nav]:
 * una pila de [Screen] para pantallas completas y un [Sheet] opcional para
 * modales deslizables.
 */
/** Pantallas del flujo de onboarding/acceso: mientras estemos aquí, no se dispara el relock. */
private val onboardingFlowScreens = setOf(
    Screen.Splash,
    Screen.Login,
    Screen.PermisosEsenciales,
    Screen.AccesoBiometrico,
    Screen.CrearPin,
    Screen.CalibracionGesto,
    Screen.OnboardingCompletado
)

/** Pantallas del flujo de SOS: si el gesto de volumen se detecta mientras ya estamos acá, no se re-empuja. */
private val sosFlowScreens = setOf(
    Screen.TransicionActivando,
    Screen.ConfirmandoSOS,
    Screen.EnvioEvidencia
)

private const val RELOCK_THRESHOLD_MS = 2 * 60 * 1000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraApp() {
    val context = LocalContext.current
    val app = context.applicationContext as AuraApplication
    val nav = rememberNav(Screen.Splash)
    var rutaResuelta by remember { mutableStateOf(false) }
    var onboardingCompletado by remember { mutableStateOf(false) }

    // Decide el punto de entrada real al arrancar en frío: onboarding, alerta SOS (si el gesto de
    // volumen se disparó estando la app cerrada/bloqueada) o directo a Main. Inicio (el botón de
    // SOS) siempre se ve sin PIN — el candado solo aparece al tocar otra pestaña (ver MainShell).
    // Esto se resuelve en un único efecto secuencial a propósito: separarlo en dos efectos
    // independientes (uno para el perfil, otro para el gesto) causaba una carrera de navegación.
    LaunchedEffect(Unit) {
        val perfil = app.profileRepository.profile.first()
        onboardingCompletado = perfil.onboardingCompletado
        when {
            SosTrigger.pending -> {
                SosTrigger.pending = false
                nav.replace(Screen.TransicionActivando)
            }
            perfil.onboardingCompletado && app.authRepository.estaLogueado -> nav.replace(Screen.Main)
            // Onboarding hecho pero sin sesión (expiró o se cerró): se pide login de nuevo sin
            // hacerle repetir todo el alta.
            perfil.onboardingCompletado -> nav.replace(Screen.Login)
        }
        rutaResuelta = true
    }

    // Ciclo de vida del proceso: marca AppForegroundState (para que VolumeGestureAccessibilityService
    // sepa cuándo escuchar), hace relock tras estar en background más de RELOCK_THRESHOLD_MS
    // (vuelve a Main con las pestañas bloqueadas, no a una pantalla de PIN aparte), y atiende
    // cualquier gesto SOS que se dispare mientras la app ya está corriendo (mismo criterio: el
    // gesto gana sobre el relock). No actúa hasta que la resolución inicial en frío (de arriba)
    // haya terminado, para no pisarle la decisión.
    DisposableEffect(Unit) {
        var backgroundedAt = 0L
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    backgroundedAt = System.currentTimeMillis()
                    AppForegroundState.isForeground = false
                }
                Lifecycle.Event.ON_START -> {
                    AppForegroundState.isForeground = true
                    if (rutaResuelta) {
                        if (SosTrigger.pending) {
                            SosTrigger.pending = false
                            if (nav.current !in sosFlowScreens) nav.push(Screen.TransicionActivando)
                        } else if (backgroundedAt != 0L) {
                            val elapsed = System.currentTimeMillis() - backgroundedAt
                            if (elapsed > RELOCK_THRESHOLD_MS && nav.current !in onboardingFlowScreens) {
                                SessionState.tabsUnlocked = false
                                if (nav.current != Screen.Main) nav.popToMain()
                            }
                        }
                    }
                    backgroundedAt = 0L
                }
                else -> {}
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
        onDispose { ProcessLifecycleOwner.get().lifecycle.removeObserver(observer) }
    }

    if (!rutaResuelta) return

    Box(Modifier.fillMaxSize()) {
        when (val screen = nav.current) {
            Screen.Splash -> SplashScreen(onComenzar = { nav.push(Screen.Login) })
            Screen.Login -> LoginScreen(
                onListo = {
                    if (onboardingCompletado) nav.popToMain() else nav.replace(Screen.PermisosEsenciales)
                }
            )
            Screen.CrearPin -> CrearPinScreen(nav)
            Screen.PermisosEsenciales -> PermisosEsencialesScreen(nav)
            Screen.AccesoBiometrico -> AccesoBiometricoScreen(nav)
            Screen.CalibracionGesto -> CalibracionGestoRapidoScreen(nav)
            Screen.OnboardingCompletado -> OnboardingCompletadoScreen(nav)
            Screen.Main -> MainShell(nav)
            Screen.TransicionActivando -> TransicionActivandoScreen(nav)
            Screen.ConfirmandoSOS -> ConfirmandoSOSScreen(nav)
            Screen.EnvioEvidencia -> EnvioEvidenciaScreen(nav)
            Screen.DetalleDeCaso -> DetalleDeCasoScreen(nav)
            Screen.FichaIncidente -> FichaIncidenteScreen(nav)
            Screen.EditarIncidente -> EditarIncidenteScreen(nav)
            Screen.VisorMultimedia -> VisorMultimediaScreen(nav)
            Screen.AjustesBiometrico -> AjustesBiometricoScreen(nav)
            Screen.AjustesCamuflaje -> AjustesCamuflajeScreen(nav)
            Screen.SelectorCamuflaje -> SelectorCamuflajeScreen(nav)
            Screen.CalibracionAV -> CalibracionAVScreen(nav)
            Screen.PoliticasAutodestruccion -> PoliticasAutodestruccionScreen(nav)
            Screen.CamuflajeCalculadora -> CamuflajeCalculadoraScreen(nav)
            Screen.CamuflajeMusica -> CamuflajeMusicaScreen(nav)
            Screen.CamuflajeBloqueo -> CamuflajeBloqueoScreen(nav)
        }

        val sheet = nav.sheet
        if (sheet != null) {
            ModalBottomSheet(
                onDismissRequest = { nav.hideSheet() },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                when (sheet) {
                    Sheet.AgregarContacto -> AgregarContactoSheetContent(nav)
                    is Sheet.EditarContacto -> EditarContactoSheetContent(nav, sheet.contactoId)
                    Sheet.EnviarAContacto -> EnviarAContactoSheetContent(nav)
                    Sheet.ExportarReporte -> ExportarReporteSheetContent(nav)
                    Sheet.EliminarArchivos -> EliminarArchivosSheetContent(nav)
                    Sheet.GestionCuenta -> GestionCuentaSheetContent(nav)
                    Sheet.ConfigurarAtajo -> ConfigurarAtajoSheetContent(nav)
                    Sheet.ConfirmacionAprobacion -> ConfirmacionAprobacionSheetContent(nav)
                }
            }
        }
    }
}

/**
 * Shell principal con las 4 pestañas inferiores. Vive como [Screen.Main] en la pila.
 *
 * Inicio (el botón de SOS) es siempre visible sin PIN. Tocar cualquier otra pestaña, si la
 * sesión no está desbloqueada ([SessionState.tabsUnlocked]), muestra el candado ([LockScreen])
 * en el lugar de esa pestaña; recién al autenticarse se cambia de verdad a ella.
 */
@Composable
private fun MainShell(nav: Nav) {
    var currentTab by remember { mutableStateOf(Tab.Inicio) }
    var tabPendiente by remember { mutableStateOf<Tab?>(null) }

    // Si se relockea (o se cierra sesión) mientras se estaba viendo una pestaña protegida,
    // hay que volver a Inicio — seguir mostrando su contenido sin PIN sería la fuga obvia.
    LaunchedEffect(SessionState.tabsUnlocked) {
        if (!SessionState.tabsUnlocked) {
            currentTab = Tab.Inicio
            tabPendiente = null
        }
    }

    // Alguien pidió abrir el shell en una pestaña concreta (por ejemplo, "Continuar" al cerrar
    // una alerta manda a Historial). Pasa por el mismo filtro que tocarla a mano: si está
    // bloqueada, se muestra el candado en vez de saltárselo.
    LaunchedEffect(nav.tabSolicitada) {
        val pedida = nav.tabSolicitada ?: return@LaunchedEffect
        nav.tabSolicitada = null
        if (pedida == Tab.Inicio || SessionState.tabsUnlocked) {
            currentTab = pedida
            tabPendiente = null
        } else {
            tabPendiente = pedida
        }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(Modifier.weight(1f)) {
            val pendiente = tabPendiente
            if (pendiente != null) {
                LockScreen(
                    onUnlocked = {
                        SessionState.tabsUnlocked = true
                        currentTab = pendiente
                        tabPendiente = null
                    }
                )
            } else {
                when (currentTab) {
                    Tab.Inicio -> InicioScreen(nav)
                    Tab.Historial -> HistorialScreen(nav)
                    Tab.RedApoyo -> RedApoyoScreen(nav)
                    Tab.Ajustes -> AjustesScreen(nav)
                }
            }
        }
        AuraBottomBar(
            current = tabPendiente ?: currentTab,
            onSelect = { tab ->
                when {
                    tab == Tab.Inicio -> {
                        tabPendiente = null
                        currentTab = Tab.Inicio
                    }
                    SessionState.tabsUnlocked -> {
                        tabPendiente = null
                        currentTab = tab
                    }
                    else -> tabPendiente = tab
                }
            }
        )
    }
}

@Composable
fun AuraBottomBar(
    current: Tab,
    onSelect: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(Sizes.bottomBarHeight)
        ) {
            Tab.entries.forEach { tab ->
                BottomBarItem(
                    tab = tab,
                    selected = tab == current,
                    onClick = { onSelect(tab) }
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    tab: Tab,
    selected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        // "Píldora" violeta detrás del icono activo
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(50))
                .then(
                    if (selected) Modifier.background(activeColor) else Modifier
                )
                .padding(horizontal = 18.dp)
        ) {
            Icon(
                tab.icon,
                contentDescription = tab.label,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else inactiveColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            tab.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) activeColor else inactiveColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
