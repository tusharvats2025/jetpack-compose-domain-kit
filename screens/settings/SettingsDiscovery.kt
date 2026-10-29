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
import kotlin.math.roundToInt

/*
 * Settings · Discovery (dating)
 * Who to show, age range on a two-thumb slider, maximum distance, and
 * privacy switches like verified-only and incognito.
 *
 * The design uses DM Sans; set it in your Typography to match.
 */

data class SettingsDiscoveryState(
    val showMe: List<String> = listOf("Women", "Men", "Everyone"),
    val selectedShowMe: Int = 2,
    val ageMin: Int = 24,
    val ageMax: Int = 32,
    val ageRange: IntRange = 18..60,
    val distanceKm: Int = 15,
    val verifiedOnly: Boolean = true,
    val incognito: Boolean = false,
)

sealed interface SettingsDiscoveryEvent {
    data object Back : SettingsDiscoveryEvent
    data class SetShowMe(val index: Int) : SettingsDiscoveryEvent
    data class SetAgeRange(val min: Int, val max: Int) : SettingsDiscoveryEvent
    data class SetDistance(val km: Int) : SettingsDiscoveryEvent
    data class SetVerifiedOnly(val value: Boolean) : SettingsDiscoveryEvent
    data class SetIncognito(val value: Boolean) : SettingsDiscoveryEvent
    data object PauseProfile : SettingsDiscoveryEvent
}

data class SettingsDiscoveryStrings(
    val back: String = "Back",
    val title: String = "Discovery",
    val showMe: String = "Show me",
    val age: String = "Age range",
    val ageValue: (Int, Int, Boolean) -> String = { a, b, open -> "$a – $b" + if (open) "+" else "" },
    val distance: String = "Maximum distance",
    val km: (Int) -> String = { "$it km" },
    val verified: String = "Only verified profiles",
    val verifiedSub: String = "People who confirmed it's really them",
    val incognito: String = "Incognito",
    val incognitoSub: String = "Only people you like can see you",
    val pause: String = "Pause my profile",
)

@OptIn(ExperimentalMaterial3Api::class) // SegmentedButton on older Material 3 versions
@Composable
fun SettingsDiscovery(
    state: SettingsDiscoveryState,
    onEvent: (SettingsDiscoveryEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: SettingsDiscoveryStrings = SettingsDiscoveryStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    val colors = MaterialTheme.colorScheme
    val sliderColors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent)
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(SettingsDiscoveryEvent.Back) })
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Group {
                Column(Modifier.padding(16.dp)) {
                    Text(strings.showMe, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(10.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        state.showMe.forEachIndexed { i, label ->
                            SegmentedButton(
                                selected = i == state.selectedShowMe,
                                onClick = { onEvent(SettingsDiscoveryEvent.SetShowMe(i)) },
                                shape = SegmentedButtonDefaults.itemShape(i, state.showMe.size),
                                colors = SegmentedButtonDefaults.colors(activeContainerColor = accent.copy(alpha = 0.16f)),
                            ) { Text(label) }
                        }
                    }
                }
            }
            Group {
                Column(Modifier.padding(16.dp)) {
                    Row {
                        Text(strings.age, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(
                            strings.ageValue(state.ageMin, state.ageMax, state.ageMax >= state.ageRange.last),
                            style = MaterialTheme.typography.titleSmall,
                            color = accent,
                        )
                    }
                    RangeSlider(
                        value = state.ageMin.toFloat()..state.ageMax.toFloat(),
                        onValueChange = { r -> onEvent(SettingsDiscoveryEvent.SetAgeRange(r.start.roundToInt(), r.endInclusive.roundToInt())) },
                        valueRange = state.ageRange.first.toFloat()..state.ageRange.last.toFloat(),
                        colors = sliderColors,
                        modifier = Modifier.semantics { contentDescription = strings.age },
                    )
                    Row {
                        Text("${state.ageRange.first}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                        Text("${state.ageRange.last}+", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
            }
            Group {
                Column(Modifier.padding(16.dp)) {
                    Row {
                        Text(strings.distance, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(strings.km(state.distanceKm), style = MaterialTheme.typography.titleSmall, color = accent)
                    }
                    Slider(
                        value = state.distanceKm.toFloat(),
                        onValueChange = { onEvent(SettingsDiscoveryEvent.SetDistance(it.roundToInt())) },
                        valueRange = 1f..100f,
                        colors = sliderColors,
                        modifier = Modifier.semantics {
                            contentDescription = strings.distance
                            stateDescription = strings.km(state.distanceKm)
                        },
                    )
                }
            }
            Group {
                SwitchRow(strings.verified, strings.verifiedSub, state.verifiedOnly, { onEvent(SettingsDiscoveryEvent.SetVerifiedOnly(it)) }, accent = accent)
                RowDivider()
                SwitchRow(strings.incognito, strings.incognitoSub, state.incognito, { onEvent(SettingsDiscoveryEvent.SetIncognito(it)) }, accent = accent)
                RowDivider()
                LinkRow(strings.pause, { onEvent(SettingsDiscoveryEvent.PauseProfile) })
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
internal fun SettingsDiscoveryPreview() {
    MaterialTheme(colorScheme = PreviewColors) { SettingsDiscovery(state = SettingsDiscoveryState(), onEvent = {}, accent = Color(0xFFA21C46)) }
}
