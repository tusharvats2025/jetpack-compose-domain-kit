package com.yourapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
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
 * Settings · Appearance
 * Theme (light, dark, system), text size and accent colour, with a live
 * preview card at the top that reflects the choices.
 *
 * Apply the choices in your app's theme: pick light/dark from theme,
 * scale your Typography by textScale, and use accent (or dynamic colour
 * when useWallpaperColours is on) as the primary seed.
 */

enum class SettingsThemeMode { Light, Dark, System }

data class SettingsAppearanceState(
    val theme: SettingsThemeMode = SettingsThemeMode.System,
    /** 1 to 5, where 3 is the default size. */
    val textSize: Int = 3,
    val accents: List<Color> = listOf(Color(0xFF3949AB), Color(0xFF00695C), Color(0xFFAD1457), Color(0xFF5D4037)),
    val selectedAccent: Int = 0,
    /** Offer Android 12+ dynamic colour as the last option. */
    val supportsWallpaperColours: Boolean = true,
    val useWallpaperColours: Boolean = false,
)

sealed interface SettingsAppearanceEvent {
    data object Back : SettingsAppearanceEvent
    data class SetTheme(val mode: SettingsThemeMode) : SettingsAppearanceEvent
    data class SetTextSize(val size: Int) : SettingsAppearanceEvent
    data class SetAccent(val index: Int) : SettingsAppearanceEvent
    data object UseWallpaperColours : SettingsAppearanceEvent
}

data class SettingsAppearanceStrings(
    val back: String = "Back",
    val title: String = "Appearance",
    val preview: String = "PREVIEW",
    val previewText: String = "This is how text will look.",
    val previewButton: String = "Button",
    val theme: String = "Theme",
    val modes: Map<SettingsThemeMode, String> = mapOf(
        SettingsThemeMode.Light to "Light",
        SettingsThemeMode.Dark to "Dark",
        SettingsThemeMode.System to "System",
    ),
    val textSize: String = "Text size",
    val sizes: List<String> = listOf("Smallest", "Small", "Default", "Large", "Largest"),
    val accent: String = "Accent colour",
    val accentNames: List<String> = listOf("Indigo", "Teal", "Pink", "Brown"),
    val wallpaper: String = "Use wallpaper colours",
)

@Composable
fun SettingsAppearance(
    state: SettingsAppearanceState,
    onEvent: (SettingsAppearanceEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: SettingsAppearanceStrings = SettingsAppearanceStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val accent = state.accents.getOrElse(state.selectedAccent) { colors.primary }
    val previewDark = state.theme == SettingsThemeMode.Dark
    val scale = 0.85f + (state.textSize.coerceIn(1, 5) - 1) * 0.1f

    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(SettingsAppearanceEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            // Live preview of the current choices.
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(if (previewDark) Color(0xFF1B1B21) else Color(0xFFF4F3F9))
                    .padding(16.dp)
                    .clearAndSetSemantics { },
            ) {
                Text(strings.preview, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp, color = if (previewDark) Color(0xFFC7C6D0) else Color(0xFF55565F))
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(28.dp).background(accent, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Box(Modifier.height(10.dp).width(120.dp).background(if (previewDark) Color(0xFF46464F) else Color(0xFFDCDBE6), CircleShape))
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    strings.previewText,
                    fontSize = (15 * scale).sp,
                    color = if (previewDark) Color(0xFFE4E1E9) else Color(0xFF1B1B21),
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    strings.previewButton,
                    fontSize = (13 * scale).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.background(accent, CircleShape).padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }

            SectionLabel(strings.theme)
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SettingsThemeMode.entries.forEach { mode ->
                    val selected = mode == state.theme
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, MaterialTheme.shapes.medium)
                            .selectable(selected = selected, role = Role.RadioButton) { onEvent(SettingsAppearanceEvent.SetTheme(mode)) }
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(
                                    when (mode) {
                                        SettingsThemeMode.Light -> SolidColor(Color(0xFFF4F3F9))
                                        SettingsThemeMode.Dark -> SolidColor(Color(0xFF1B1B21))
                                        SettingsThemeMode.System -> Brush.horizontalGradient(0.5f to Color(0xFFF4F3F9), 0.5f to Color(0xFF1B1B21))
                                    },
                                ),
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(strings.modes[mode].orEmpty(), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            SectionLabel(strings.textSize)
            Group {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row {
                        Text(strings.textSize, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(strings.sizes.getOrElse(state.textSize - 1) { "" }, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("A", fontSize = 13.sp, color = colors.onSurfaceVariant)
                        Slider(
                            value = state.textSize.toFloat(),
                            onValueChange = { onEvent(SettingsAppearanceEvent.SetTextSize(it.roundToInt())) },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp).semantics {
                                contentDescription = strings.textSize
                                stateDescription = strings.sizes.getOrElse(state.textSize - 1) { "" }
                            },
                        )
                        Text("A", fontSize = 20.sp, color = colors.onSurfaceVariant)
                    }
                }
            }

            SectionLabel(strings.accent)
            Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                state.accents.forEachIndexed { i, c ->
                    val selected = !state.useWallpaperColours && i == state.selectedAccent
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(if (selected) 3.dp else 0.dp, if (selected) colors.onSurface else Color.Transparent, CircleShape)
                            .selectable(selected = selected, role = Role.RadioButton) { onEvent(SettingsAppearanceEvent.SetAccent(i)) }
                            .semantics { contentDescription = strings.accentNames.getOrElse(i) { "#${i + 1}" } },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (selected) Icon(CheckIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                if (state.supportsWallpaperColours) {
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Brush.sweepGradient(listOf(Color(0xFFE57373), Color(0xFFFFD54F), Color(0xFF81C784), Color(0xFF64B5F6), Color(0xFFE57373))))
                            .border(if (state.useWallpaperColours) 3.dp else 0.dp, if (state.useWallpaperColours) colors.onSurface else Color.Transparent, CircleShape)
                            .selectable(selected = state.useWallpaperColours, role = Role.RadioButton) { onEvent(SettingsAppearanceEvent.UseWallpaperColours) }
                            .semantics { contentDescription = strings.wallpaper },
                    )
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

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val CheckIcon = vectorIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")

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
internal fun SettingsAppearancePreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        SettingsAppearance(state = SettingsAppearanceState(theme = SettingsThemeMode.Dark, textSize = 4), onEvent = {})
    }
}
