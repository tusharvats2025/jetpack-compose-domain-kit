package com.yourapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/*
 * Settings · Security and limits (fintech)
 * App lock, biometric unlock and PIN, then transaction limits on sliders
 * with the cooling-off rule spelled out.
 *
 * Limits are in whole rupees (or your currency's minor units, scaled).
 * Enforce limits and the cooling-off period on your server, not here.
 * The design uses Manrope; set it in your Typography to match.
 */

data class SettingsFintechSecurityState(
    val appLock: Boolean = true,
    val lockAfter: String = "1 minute",
    val biometric: Boolean = true,
    val dailyLimit: Int = 25_000,
    val dailyRange: IntRange = 1_000..1_00_000,
    val dailyStep: Int = 1_000,
    val perTxnLimit: Int = 10_000,
    val perTxnRange: IntRange = 500..25_000,
    val perTxnStep: Int = 500,
    val coolingOff: String = "24 hours",
)

sealed interface SettingsFintechSecurityEvent {
    data object Back : SettingsFintechSecurityEvent
    data class SetAppLock(val value: Boolean) : SettingsFintechSecurityEvent
    data object PickLockAfter : SettingsFintechSecurityEvent
    data class SetBiometric(val value: Boolean) : SettingsFintechSecurityEvent
    data object ChangePin : SettingsFintechSecurityEvent
    data class SetDailyLimit(val amount: Int) : SettingsFintechSecurityEvent
    data class SetPerTxnLimit(val amount: Int) : SettingsFintechSecurityEvent
}

data class SettingsFintechSecurityStrings(
    val back: String = "Back",
    val title: String = "Security and limits",
    val appLockHeading: String = "APP LOCK",
    val lockApp: String = "Lock the app",
    val lockAppSub: String = "Ask for PIN or fingerprint on open",
    val lockAfter: String = "Lock after",
    val biometric: String = "Use fingerprint",
    val changePin: String = "Change PIN",
    val limitsHeading: String = "TRANSACTION LIMITS",
    val daily: String = "Daily UPI limit",
    val perTxn: String = "Per transaction",
    val coolingOff: (String) -> String = { "Raising a limit takes effect after $it for your safety." },
    /** Indian grouping by default (1,00,000). Swap for your locale's formatter. */
    val money: (Int) -> String = { "₹" + indianGrouping(it) },
)

@Composable
fun SettingsFintechSecurity(
    state: SettingsFintechSecurityState,
    onEvent: (SettingsFintechSecurityEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: SettingsFintechSecurityStrings = SettingsFintechSecurityStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surfaceContainerLowest).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(SettingsFintechSecurityEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Label(strings.appLockHeading)
            Group {
                SwitchRow(strings.lockApp, strings.lockAppSub, state.appLock, { onEvent(SettingsFintechSecurityEvent.SetAppLock(it)) }, accent = accent)
                if (state.appLock) {
                    RowDivider()
                    LinkRow(strings.lockAfter, { onEvent(SettingsFintechSecurityEvent.PickLockAfter) }, value = state.lockAfter)
                    RowDivider()
                    SwitchRow(strings.biometric, null, state.biometric, { onEvent(SettingsFintechSecurityEvent.SetBiometric(it)) }, accent = accent)
                }
                RowDivider()
                LinkRow(strings.changePin, { onEvent(SettingsFintechSecurityEvent.ChangePin) })
            }
            Label(strings.limitsHeading)
            Group {
                LimitSlider(
                    label = strings.daily,
                    value = state.dailyLimit,
                    range = state.dailyRange,
                    step = state.dailyStep,
                    format = strings.money,
                    accent = accent,
                    showEnds = true,
                    onChange = { onEvent(SettingsFintechSecurityEvent.SetDailyLimit(it)) },
                )
                RowDivider()
                LimitSlider(
                    label = strings.perTxn,
                    value = state.perTxnLimit,
                    range = state.perTxnRange,
                    step = state.perTxnStep,
                    format = strings.money,
                    accent = accent,
                    showEnds = false,
                    onChange = { onEvent(SettingsFintechSecurityEvent.SetPerTxnLimit(it)) },
                )
                Row(
                    Modifier.fillMaxWidth().padding(16.dp).clip(MaterialTheme.shapes.medium).background(accent.copy(alpha = 0.08f)).padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(InfoIcon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(strings.coolingOff(state.coolingOff), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp).semantics { heading() },
    )
}

@Composable
private fun LimitSlider(
    label: String,
    value: Int,
    range: IntRange,
    step: Int,
    format: (Int) -> String,
    accent: Color,
    showEnds: Boolean,
    onChange: (Int) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val steps = ((range.last - range.first) / step - 1).coerceAtLeast(0)
    Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(format(value), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(((it / step).roundToInt() * step).coerceIn(range.first, range.last)) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = steps,
            colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
            modifier = Modifier.semantics {
                contentDescription = label
                stateDescription = format(value)
            },
        )
        if (showEnds) {
            Row {
                Text(format(range.first), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text(format(range.last), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

/** 100000 -> "1,00,000". */
private fun indianGrouping(amount: Int): String {
    val s = amount.toString()
    if (s.length <= 3) return s
    val last3 = s.takeLast(3)
    val rest = s.dropLast(3).reversed().chunked(2).joinToString(",").reversed()
    return "$rest,$last3"
}

@Composable
private fun TopBar(title: String, backLabel: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) { Icon(BackIcon, contentDescription = backLabel) }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        actions()
    }
}

/** Rounded card that holds a run of rows. */
@Composable
private fun Group(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        content = content,
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

/** Whole row toggles, so the touch target is full width. */
@Composable
private fun SwitchRow(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.38f))
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = SwitchDefaults.colors(checkedTrackColor = accent, checkedBorderColor = accent),
        )
    }
}

@Composable
private fun LinkRow(title: String, onClick: () -> Unit, value: String? = null, subtitle: String? = null) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp))
        }
        Icon(ChevronIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp).size(20.dp))
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val ChevronIcon = vectorIcon("Chevron", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z", autoMirror = true)
private val InfoIcon = vectorIcon("Info", "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z")

/**
 * Preview-only colours, matching the screens on the website. Without them,
 * a bare MaterialTheme shows Material's default purple. Your app's own
 * MaterialTheme decides the real colours.
 */
private val PreviewColors = lightColorScheme(
    primary = Color(0xFF3949AB),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8EAF8),
    onPrimaryContainer = Color(0xFF283593),
    secondaryContainer = Color(0xFFE8EAF8),
    onSecondaryContainer = Color(0xFF283593),
    background = Color(0xFFFBFAFF),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFBFAFF),
    onSurface = Color(0xFF1B1B21),
    onSurfaceVariant = Color(0xFF55565F),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF4F3F9),
    surfaceContainer = Color(0xFFECEBF2),
    surfaceContainerHigh = Color(0xFFE3E3EC),
    surfaceContainerHighest = Color(0xFFE3E3EC),
    outline = Color(0xFF777882),
    outlineVariant = Color(0xFFC7C6D0),
    error = Color(0xFFB3261E),
)

@Preview(showBackground = true)
@Composable
internal fun SettingsFintechSecurityPreview() {
    MaterialTheme(colorScheme = PreviewColors) { SettingsFintechSecurity(state = SettingsFintechSecurityState(), onEvent = {}, accent = Color(0xFF1F4FBF)) }
}
