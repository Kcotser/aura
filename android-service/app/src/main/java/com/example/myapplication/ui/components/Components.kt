package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.ErrorColor
import com.example.myapplication.ui.theme.ErrorContainer
import com.example.myapplication.ui.theme.OnError
import com.example.myapplication.ui.theme.OnErrorContainer
import com.example.myapplication.ui.theme.Shapes
import com.example.myapplication.ui.theme.Sizes
import com.example.myapplication.ui.theme.Spacing

/* ---------------------------------------------------------------------------
 * Botones
 * ------------------------------------------------------------------------- */

/** Botón principal: violeta, ancho completo, 48dp. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = Shapes.button,
        modifier = modifier
            .fillMaxWidth()
            .height(Sizes.buttonHeight)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text("  ")
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

/** Botón crítico: rojo carmesí, reservado para SOS / acciones destructivas. */
@Composable
fun CriticalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        shape = Shapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = ErrorColor,
            contentColor = OnError
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(Sizes.buttonHeight)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Text("  ")
        }
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

/** Botón secundario con borde. */
@Composable
fun SecondaryOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    OutlinedButton(
        onClick = onClick,
        shape = Shapes.button,
        border = BorderStroke(1.5.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = contentColor),
        modifier = modifier
            .fillMaxWidth()
            .height(Sizes.buttonHeight)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

/* ---------------------------------------------------------------------------
 * Chips de estado
 * ------------------------------------------------------------------------- */

/** Chip pequeño (32dp) con fondo de color al 10-15% para estados. */
@Composable
fun StatusChip(
    text: String,
    contentColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    leadingDot: Boolean = false
) {
    Surface(
        color = containerColor,
        shape = Shapes.chip,
        modifier = modifier.height(Sizes.chipHeight)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = Spacing.sm)
        ) {
            if (leadingDot) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(contentColor)
                )
            }
            Text(
                text.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* ---------------------------------------------------------------------------
 * Tarjeta base (Nivel 1: blanco + sombra suave)
 * ------------------------------------------------------------------------- */

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    content: @Composable () -> Unit
) {
    Surface(
        color = color,
        shape = Shapes.card,
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        content()
    }
}

/* ---------------------------------------------------------------------------
 * Encabezado de sección (PREFERENCIAS, SEGURIDAD Y PRIVACIDAD, ...)
 * ------------------------------------------------------------------------- */

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(
            start = Spacing.base,
            top = Spacing.md,
            bottom = Spacing.xs
        )
    )
}

/* ---------------------------------------------------------------------------
 * Barra superior simple con back + título
 * ------------------------------------------------------------------------- */

@Composable
fun AuraTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = Spacing.xs)
    ) {
        if (onBack != null) {
            IconButtonSlot(icon = Icons.AutoMirrored.Filled.ArrowBack, onClick = onBack)
        } else {
            Box(Modifier.size(Spacing.xs))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .weight(1f)
                .padding(start = Spacing.xs)
        )
        if (actions != null) actions()
    }
}

/** Botón de icono con área táctil de 44dp. */
@Composable
fun IconButtonSlot(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    contentDescription: String? = null
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier.size(Sizes.touchTargetMin)
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint)
    }
}

/* ---------------------------------------------------------------------------
 * Círculo con icono (avatares de directorio, badges)
 * ------------------------------------------------------------------------- */

@Composable
fun CircleIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.primary,
    tint: Color = MaterialTheme.colorScheme.onPrimary,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

/** Marcador de avatar de persona (placeholder con inicial). */
@Composable
fun AvatarPlaceholder(
    initial: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    ringColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            initial.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

/* ---------------------------------------------------------------------------
 * Campo de texto (input de 52dp, borde redondeado)
 * ------------------------------------------------------------------------- */

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPhone: Boolean = false,
    isEmail: Boolean = false,
    isPassword: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1,
    enabled: Boolean = true,
    isError: Boolean = false
) {
    val keyboardType = when {
        isPhone -> KeyboardType.Phone
        isEmail -> KeyboardType.Email
        isPassword -> KeyboardType.Password
        else -> KeyboardType.Text
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        enabled = enabled,
        isError = isError,
        shape = Shapes.input,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedBorderColor = Color.Transparent
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/* ---------------------------------------------------------------------------
 * Fila de opción seleccionable (radio) dentro de una tarjeta con borde
 * ------------------------------------------------------------------------- */

@Composable
fun SelectableOptionCard(
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null
) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
        else MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = Shapes.card,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .clip(Shapes.card)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.md)
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    if (badge != null) {
                        Spacer(Modifier)
                        Box(Modifier.padding(start = Spacing.xs)) {
                            StatusChip(
                                text = badge,
                                contentColor = com.example.myapplication.ui.theme.OnSuccessContainer,
                                containerColor = com.example.myapplication.ui.theme.SuccessContainer,
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.size(Spacing.sm))
            Icon(
                if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
    }
}

/** Alias local para Spacer sin importar Column extra en cada archivo. */
@Composable
private fun Spacer(modifier: Modifier) = androidx.compose.foundation.layout.Spacer(modifier)

/* ---------------------------------------------------------------------------
 * Banner de advertencia (rojo, "no recuperable" / acciones destructivas)
 * ------------------------------------------------------------------------- */

@Composable
fun WarningBanner(
    title: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ErrorContainer,
        shape = Shapes.card,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(Spacing.md)) {
            Icon(Icons.Filled.Warning, null, tint = OnErrorContainer, modifier = Modifier.size(22.dp))
            Spacer(Modifier.size(Spacing.sm))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = OnErrorContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnErrorContainer
                )
            }
        }
    }
}

/* ---------------------------------------------------------------------------
 * Fila de ajuste con switch (para pantallas de settings)
 * ------------------------------------------------------------------------- */

@Composable
fun SwitchSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape = Shapes.card,
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(Spacing.md)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/* ---------------------------------------------------------------------------
 * PIN: indicador de dígitos + teclado numérico (compartido por CrearPin,
 * Lock y "cambiar PIN" en Ajustes).
 * ------------------------------------------------------------------------- */

@Composable
fun PinDots(
    length: Int,
    modifier: Modifier = Modifier,
    maxLength: Int = 4,
    error: Boolean = false
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = modifier) {
        repeat(maxLength) { i ->
            val filled = i < length
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            error -> ErrorColor
                            filled -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                    )
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .then(
                                if (key.isNotEmpty()) {
                                    Modifier
                                        .clip(CircleShape)
                                        .clickable { if (key == "⌫") onBackspace() else onDigit(key) }
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        if (key.isNotEmpty()) {
                            Text(
                                key,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

