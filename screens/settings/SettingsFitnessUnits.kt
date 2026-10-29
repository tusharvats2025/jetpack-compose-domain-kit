package com.yourapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Settings · Units and health data (fitness)
 * Metric or imperial in one tap (with per-measure detail), which Health
 * Connect data types to sync, and the daily workout reminder.
 *
 * Health Connect permissions are per data type; request them from your
 * ViewModel when a switch turns on. The design uses Sora.
 */

enum class SettingsUnitSystem { Metric, Imperial }

data class SettingsHealthDataType(val id: String, val label: String, val enabled: Boolean)

data class SettingsFitnessUnitsState(
    val system: SettingsUnitSystem = SettingsUnitSystem.Metric,
    val healthConnected: Boolean = true,
    val dataTypes: List<SettingsHealthDataType> = emptyList(),
    val reminder: Boolean = true,
    val reminderTime: String = "7:00 am",
)

sealed interface SettingsFitnessUnitsEvent {
    data object Back : SettingsFitnessUnitsEvent
    data class SetSystem(val system: SettingsUnitSystem) : SettingsFitnessUnitsEvent
    data object ManageHealthConnect : SettingsFitnessUnitsEvent
    data class SetDataType(val id: String, val value: Boolean) : SettingsFitnessUnitsEvent
    data class SetReminder(val value: Boolean) : SettingsFitnessUnitsEvent
    data object PickReminderTime : SettingsFitnessUnitsEvent
}

data class SettingsFitnessUnitsStrings(
    val back: String = "Back",
    val title: String = "Units and health data",
    val units: String = "Units",
    val metric: String = "Metric",
    val imperial: String = "Imperial",
    val weight: String = "Weight",
    val distance: String = "Distance",
    val energy: String = "Energy",
    val healthConnect: String = "Health Connect",
    val connected: String = "Connected",
    val notConnected: String = "Not connected",
    val manage: String = "Manage",
    val reminders: String = "Reminders",
    val reminder: String = "Daily workout reminder",
)

@OptIn(ExperimentalMaterial3Api::class) // SegmentedButton on older Material 3 versions
@Composable
fun SettingsFitnessUnits(
    state: SettingsFitnessUnitsState,
    onEvent: (SettingsFitnessUnitsEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: SettingsFitnessUnitsStrings = SettingsFitnessUnitsStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    val colors = MaterialTheme.colorScheme
    val metric = state.system == SettingsUnitSystem.Metric
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(SettingsFitnessUnitsEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            SectionLabel(strings.units, accent)
            Group {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(16.dp)) {
                    SettingsUnitSystem.entries.forEachIndexed { i, s ->
                        SegmentedButton(
                            selected = s == state.system,
                            onClick = { onEvent(SettingsFitnessUnitsEvent.SetSystem(s)) },
                            shape = SegmentedButtonDefaults.itemShape(i, SettingsUnitSystem.entries.size),
                            colors = SegmentedButtonDefaults.colors(activeContainerColor = accent.copy(alpha = 0.16f)),
                        ) { Text(if (s == SettingsUnitSystem.Metric) strings.metric else strings.imperial) }
                    }
                }
                listOf(
                    strings.weight to if (metric) "kg" else "lb",
                    strings.distance to if (metric) "km" else "mi",
                    strings.energy to "kcal",
                ).forEach { (label, unit) ->
                    RowDivider()
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(unit, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                }
            }
            SectionLabel(strings.healthConnect, accent)
            Group {
                Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(if (state.healthConnected) accent else colors.outline, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (state.healthConnected) strings.connected else strings.notConnected,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { onEvent(SettingsFitnessUnitsEvent.ManageHealthConnect) }) { Text(strings.manage, color = accent) }
                }
                state.dataTypes.forEach { t ->
                    RowDivider()
                    SwitchRow(t.label, null, t.enabled, { onEvent(SettingsFitnessUnitsEvent.SetDataType(t.id, it)) }, enabled = state.healthConnected, accent = accent)
                }
            }
            SectionLabel(strings.reminders, accent)
            Group {
                SwitchRow(strings.reminder, state.reminderTime, state.reminder, { onEvent(SettingsFitnessUnitsEvent.SetReminder(it)) }, accent = accent)
                if (state.reminder) {
                    RowDivider()
                    LinkRow(state.reminderTime, { onEvent(SettingsFitnessUnitsEvent.PickReminderTime) })
                }
            }
        }
    }
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

@Composable
private fun SectionLabel(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp).semantics { heading() },
    )
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
internal fun SettingsFitnessUnitsPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        SettingsFitnessUnits(
            state = SettingsFitnessUnitsState(
                dataTypes = listOf(
                    SettingsHealthDataType("steps", "Steps", true),
                    SettingsHealthDataType("heart", "Heart rate", true),
                    SettingsHealthDataType("sleep", "Sleep", false),
                    SettingsHealthDataType("workouts", "Workouts", true),
                ),
            ),
            onEvent = {},
            accent = Color(0xFF2F6B3F),
        )
    }
}
