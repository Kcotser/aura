package com.example.myapplication.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Espaciado y formas del DESIGN.md (grid de 4/8pt).
 */
object Spacing {
    val base = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp   // margen horizontal seguro
    val lg = 24.dp
    val xl = 32.dp
}

object Sizes {
    val touchTargetMin = 44.dp
    val buttonHeight = 48.dp
    val inputHeight = 52.dp
    val listItemHeight = 64.dp
    val chipHeight = 32.dp
    val bottomBarHeight = 72.dp
}

/** Radios: 16dp para tarjetas, 8dp para botones/inputs. */
object Shapes {
    val card = RoundedCornerShape(16.dp)
    val button = RoundedCornerShape(8.dp)
    val input = RoundedCornerShape(8.dp)
    val chip = RoundedCornerShape(9999.dp)
    val bottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
}
