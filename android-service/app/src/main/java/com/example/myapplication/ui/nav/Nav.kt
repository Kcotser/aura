package com.example.myapplication.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList

/**
 * Destinos de pantalla completa (participan en el back stack).
 * Es una maqueta visual: no hay datos reales detrás, solo navegación.
 */
sealed class Screen {
    object Splash : Screen()

    // Perfil local / acceso
    object NombrePerfil : Screen()
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
    object ProcesandoIncidente : Screen()

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
    object EditarContacto : Sheet()
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
    private val sheetState: MutableState<Sheet?>
) {
    val current: Screen get() = backStack.last()
    val sheet: Sheet? get() = sheetState.value

    fun push(screen: Screen) {
        backStack.add(screen)
    }

    fun pop() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun replace(screen: Screen) {
        backStack[backStack.lastIndex] = screen
    }

    /** Vuelve a la raíz (Main) descartando toda la pila intermedia. */
    fun popToMain() {
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
    return remember { Nav(backStack, sheetState) }
}
