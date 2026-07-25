package com.example.myapplication.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.example.myapplication.ui.Tab

/**
 * Destinos de pantalla completa (participan en el back stack).
 * Es una maqueta visual: no hay datos reales detrás, solo navegación.
 */
sealed class Screen {
    object Splash : Screen()

    /** Cuenta del backend (la única que no es local): sin ella no se puede subir evidencia. */
    object Login : Screen()

    // Perfil local / acceso
    object CrearPin : Screen()
    // El candado de reingreso ya no es una pantalla completa de la pila — ver
    // MainShell (AuraApp.kt), que lo muestra inline al tocar una pestaña bloqueada.

    // Onboarding
    object PermisosEsenciales : Screen()
    object AccesoBiometrico : Screen()
    object CalibracionGesto : Screen()
    object OnboardingCompletado : Screen()

    // Shell principal (bottom nav de 4 pestañas)
    object Main : Screen()

    // Activación SOS
    object TransicionActivando : Screen()
    object ConfirmandoSOS : Screen()

    /** Cierre de la alerta: muestra cómo va el envío de la evidencia al backend. */
    object EnvioEvidencia : Screen()

    // Historial / incidentes
    object DetalleDeCaso : Screen()
    object FichaIncidente : Screen()
    object EditarIncidente : Screen()
    object VisorMultimedia : Screen()

    // Ajustes avanzados
    object AjustesBiometrico : Screen()
    object AjustesCamuflaje : Screen()
    object SelectorCamuflaje : Screen()
    object CalibracionAV : Screen()
    object PoliticasAutodestruccion : Screen()

    // Camuflaje (pantallas señuelo)
    object CamuflajeCalculadora : Screen()
    object CamuflajeMusica : Screen()
    object CamuflajeBloqueo : Screen()
}

/** Bottom sheets modales (se superponen a la pantalla actual). */
sealed class Sheet {
    object AgregarContacto : Sheet()

    /** Edita un contacto concreto de la Red de Apoyo. */
    data class EditarContacto(val contactoId: String) : Sheet()
    object EnviarAContacto : Sheet()
    object ExportarReporte : Sheet()
    object EliminarArchivos : Sheet()
    object GestionCuenta : Sheet()
    object ConfigurarAtajo : Sheet()
    object ConfirmacionAprobacion : Sheet()
}

/** Controlador de navegación simple: pila de pantallas + sheet modal activo. */
class Nav(
    val backStack: SnapshotStateList<Screen>,
    private val sheetState: MutableState<Sheet?>,
    private val tabSolicitadaState: MutableState<Tab?>
) {
    val current: Screen get() = backStack.last()
    val sheet: Sheet? get() = sheetState.value

    /**
     * Pestaña con la que debe abrirse el shell principal. La consume [Screen.Main] al mostrarse
     * y vuelve a null: es una orden de una sola vez, no el estado de la pestaña actual.
     */
    var tabSolicitada: Tab?
        get() = tabSolicitadaState.value
        set(value) {
            tabSolicitadaState.value = value
        }

    fun push(screen: Screen) {
        backStack.add(screen)
    }

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun replace(screen: Screen) {
        backStack[backStack.lastIndex] = screen
    }

    /**
     * Vuelve a la raíz (Main) descartando toda la pila intermedia.
     *
     * @param tab pestaña con la que abrir el shell. Si está bloqueada, el candado se muestra
     *   igual que al tocarla a mano; no salta la protección.
     */
    fun popToMain(tab: Tab? = null) {
        tabSolicitada = tab
        backStack.clear()
        backStack.add(Screen.Main)
    }

    fun showSheet(sheet: Sheet) {
        sheetState.value = sheet
    }

    fun hideSheet() {
        sheetState.value = null
    }
}

@Composable
fun rememberNav(start: Screen = Screen.Splash): Nav {
    val backStack = remember { mutableListOf(start).toMutableStateList() }
    val sheetState = remember { mutableStateOf<Sheet?>(null) }
    val tabSolicitadaState = remember { mutableStateOf<Tab?>(null) }
    return remember { Nav(backStack, sheetState, tabSolicitadaState) }
}
