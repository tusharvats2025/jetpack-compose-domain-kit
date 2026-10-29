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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Settings · Grouped list
 * The classic settings screen, driven entirely by data: sections of
 * toggle, link, slider and destructive rows. The same file covers the
 * "Save failed" state (saveError) and rows that are disabled with a
 * reason (lockedReason, with an optional action like Upgrade).
 *
 * Toggle optimistically in your ViewModel; if saving fails, flip the
 * value back and set saveError so the banner explains it.
 */

sealed interface SettingsGroupedRow {
    val id: String

    data class Toggle(
        override val id: String,
        val title: String,
        val subtitle: String? = null,
        val checked: Boolean,
        /** Shown under the row when the toggle can't be changed. */
        val lockedReason: String? = null,
        /** Optional action next to the reason, e.g. "Upgrade". */
        val lockedAction: String? = null,
    ) : SettingsGroupedRow

    data class Link(override val id: String, val title: String, val value: String? = null) : SettingsGroupedRow

    data class Slider(override val id: String, val title: String, val value: Float, val valueLabel: String) : SettingsGroupedRow

    data class Destructive(override val id: String, val title: String) : SettingsGroupedRow
}

data class SettingsGroupedSection(val title: String?, val rows: List<SettingsGroupedRow>)

data class SettingsGroupedListState(
    val title: String = "Settings",
    val sections: List<SettingsGroupedSection> = emptyList(),
    val saveError: String? = null,
    val footer: String? = null,
)

sealed interface SettingsGroupedListEvent {
    data object Back : SettingsGroupedListEvent
    data class Toggle(val id: String, val value: Boolean) : SettingsGroupedListEvent
    data class Open(val id: String) : SettingsGroupedListEvent
    data class SliderChanged(val id: String, val value: Float) : SettingsGroupedListEvent
    data class Destructive(val id: String) : SettingsGroupedListEvent
    data class LockedAction(val id: String) : SettingsGroupedListEvent
    data object RetrySave : SettingsGroupedListEvent
}

data class SettingsGroupedListStrings(
    val back: String = "Back",
    val retry: String = "Retry",
    val locked: String = "Locked",
)

@Composable
fun SettingsGroupedList(
    state: SettingsGroupedListState,
    onEvent: (SettingsGroupedListEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: SettingsGroupedListStrings = SettingsGroupedListStrings(),
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TopBar(state.title, strings.back, { onEvent(SettingsGroupedListEvent.Back) })
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        ) {
            state.sections.forEach { section ->
                if (section.title != null) SectionLabel(section.title) else Spacer(Modifier.height(8.dp))
                Group {
                    section.rows.forEachIndexed { i, row ->
                        if (i > 0) RowDivider()
                        SettingsRow(row, onEvent, strings)
                    }
                }
            }
            if (state.footer != null) {
                Text(
                    state.footer,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        // Save-failed banner sits above the bottom edge, like a persistent snackbar.
        if (state.saveError != null) {
            Surface(
                color = colors.inverseSurface,
                contentColor = colors.inverseOnSurface,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .semantics { liveRegion = LiveRegionMode.Assertive },
            ) {
                Row(Modifier.padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(WarningIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(state.saveError, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(vertical = 14.dp))
                    TextButton(
                        onClick = { onEvent(SettingsGroupedListEvent.RetrySave) },
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.inversePrimary),
                    ) { Text(strings.retry) }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(row: SettingsGroupedRow, onEvent: (SettingsGroupedListEvent) -> Unit, strings: SettingsGroupedListStrings) {
    val colors = MaterialTheme.colorScheme
    when (row) {
        is SettingsGroupedRow.Toggle -> Column {
            SwitchRow(
                title = row.title,
                subtitle = row.subtitle,
                checked = row.checked,
                enabled = row.lockedReason == null,
                onCheckedChange = { onEvent(SettingsGroupedListEvent.Toggle(row.id, it)) },
            )
            if (row.lockedReason != null) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(LockIcon, contentDescription = strings.locked, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(row.lockedReason, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                    if (row.lockedAction != null) {
                        TextButton(onClick = { onEvent(SettingsGroupedListEvent.LockedAction(row.id)) }) { Text(row.lockedAction) }
                    }
                }
            }
        }
        is SettingsGroupedRow.Link -> LinkRow(row.title, { onEvent(SettingsGroupedListEvent.Open(row.id)) }, value = row.value)
        is SettingsGroupedRow.Slider -> Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row {
                Text(row.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text(row.valueLabel, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Slider(
                value = row.value,
                onValueChange = { onEvent(SettingsGroupedListEvent.SliderChanged(row.id, it)) },
                modifier = Modifier.semantics { contentDescription = row.title },
            )
        }
        is SettingsGroupedRow.Destructive -> TextButton(
            onClick = { onEvent(SettingsGroupedListEvent.Destructive(row.id)) },
            colors = ButtonDefaults.textButtonColors(contentColor = colors.error),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        ) { Text(row.title, modifier = Modifier.fillMaxWidth()) }
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
private val WarningIcon = vectorIcon("Warning", "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z")
private val LockIcon = vectorIcon(
    "Lock",
    "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z",
)

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
internal fun SettingsGroupedListPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        SettingsGroupedList(
            state = SettingsGroupedListState(
                sections = listOf(
                    SettingsGroupedSection(
                        "General",
                        listOf(
                            SettingsGroupedRow.Toggle("notif", "Notifications", "Replies, reminders, updates", checked = true),
                            SettingsGroupedRow.Link("lang", "Language", "English"),
                            SettingsGroupedRow.Link("appearance", "Appearance", "System"),
                            SettingsGroupedRow.Slider("volume", "Sound volume", 0.6f, "60%"),
                        ),
                    ),
                    SettingsGroupedSection(
                        "Account",
                        listOf(
                            SettingsGroupedRow.Link("security", "Security"),
                            SettingsGroupedRow.Link("subscription", "Subscription", "Pro"),
                            SettingsGroupedRow.Toggle("backup", "Cloud backup", "Back up every night", checked = false, lockedReason = "Available on Pro.", lockedAction = "Upgrade"),
                            SettingsGroupedRow.Destructive("delete", "Delete account"),
                        ),
                    ),
                ),
                footer = "Version 1.0.0",
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Save failed")
@Composable
internal fun SettingsGroupedListSaveFailedPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        SettingsGroupedList(
            state = SettingsGroupedListState(
                title = "Privacy",
                sections = listOf(
                    SettingsGroupedSection(
                        null,
                        listOf(
                            SettingsGroupedRow.Toggle("private", "Private account", "Only followers see your posts", checked = false),
                            SettingsGroupedRow.Toggle("activity", "Show activity status", "Let others see when you're online", checked = true),
                        ),
                    ),
                ),
                saveError = "Couldn't turn on Private account. Check your connection.",
            ),
            onEvent = {},
        )
    }
}
