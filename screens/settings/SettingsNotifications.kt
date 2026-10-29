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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Settings · Notifications
 * A master switch, then a channel grid: each kind of notification can go
 * to push, email, both or neither. Safety-critical kinds can be locked on.
 * Quiet hours at the bottom.
 *
 * Mirror each kind as a NotificationChannel on Android 8+ so the system
 * settings match. Picking times is yours (TimePicker in a dialog).
 */

data class SettingsNotificationKind(
    val id: String,
    val title: String,
    val subtitle: String,
    val push: Boolean,
    val email: Boolean,
    /** Can't be turned off, e.g. security alerts. */
    val locked: Boolean = false,
)

data class SettingsNotificationsState(
    val enabled: Boolean = true,
    val kinds: List<SettingsNotificationKind> = emptyList(),
    val quietHours: Boolean = false,
    val quietFrom: String = "10:00 pm",
    val quietUntil: String = "7:00 am",
)

sealed interface SettingsNotificationsEvent {
    data object Back : SettingsNotificationsEvent
    data class SetEnabled(val value: Boolean) : SettingsNotificationsEvent
    data class SetPush(val id: String, val value: Boolean) : SettingsNotificationsEvent
    data class SetEmail(val id: String, val value: Boolean) : SettingsNotificationsEvent
    data class SetQuietHours(val value: Boolean) : SettingsNotificationsEvent
    data object PickQuietFrom : SettingsNotificationsEvent
    data object PickQuietUntil : SettingsNotificationsEvent
}

data class SettingsNotificationsStrings(
    val back: String = "Back",
    val title: String = "Notifications",
    val allow: String = "Allow notifications",
    val what: String = "What you hear about",
    val push: String = "Push",
    val email: String = "Email",
    val channel: (kind: String, channel: String) -> String = { k, c -> "$k, $c" },
    val alwaysOn: String = "always on",
    val quietHours: String = "Quiet hours",
    val pause: String = "Pause notifications",
    val everyDay: String = "Every day",
    val from: String = "From",
    val until: String = "Until",
)

@Composable
fun SettingsNotifications(
    state: SettingsNotificationsState,
    onEvent: (SettingsNotificationsEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: SettingsNotificationsStrings = SettingsNotificationsStrings(),
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(SettingsNotificationsEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Group {
                SwitchRow(strings.allow, null, state.enabled, { onEvent(SettingsNotificationsEvent.SetEnabled(it)) })
            }
            Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 24.dp, bottom = 8.dp), verticalAlignment = Alignment.Bottom) {
                Text(
                    strings.what,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                Text(strings.push, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.width(56.dp))
                Text(strings.email, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.width(56.dp))
            }
            Group {
                state.kinds.forEachIndexed { i, kind ->
                    if (i > 0) RowDivider()
                    Row(Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(kind.title, style = MaterialTheme.typography.bodyLarge)
                            Text(kind.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        val enabled = state.enabled && !kind.locked
                        val suffix = if (kind.locked) ", ${strings.alwaysOn}" else ""
                        Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                            Checkbox(
                                checked = kind.push,
                                onCheckedChange = { onEvent(SettingsNotificationsEvent.SetPush(kind.id, it)) },
                                enabled = enabled,
                                modifier = Modifier.semantics { contentDescription = strings.channel(kind.title, strings.push) + suffix },
                            )
                        }
                        Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                            Checkbox(
                                checked = kind.email,
                                onCheckedChange = { onEvent(SettingsNotificationsEvent.SetEmail(kind.id, it)) },
                                enabled = enabled,
                                modifier = Modifier.semantics { contentDescription = strings.channel(kind.title, strings.email) + suffix },
                            )
                        }
                    }
                }
            }
            SectionLabel(strings.quietHours)
            Group {
                SwitchRow(strings.pause, strings.everyDay, state.quietHours, { onEvent(SettingsNotificationsEvent.SetQuietHours(it)) }, enabled = state.enabled)
                if (state.quietHours) {
                    RowDivider()
                    LinkRow(strings.from, { onEvent(SettingsNotificationsEvent.PickQuietFrom) }, value = state.quietFrom)
                    RowDivider()
                    LinkRow(strings.until, { onEvent(SettingsNotificationsEvent.PickQuietUntil) }, value = state.quietUntil)
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
internal fun SettingsNotificationsPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        SettingsNotifications(
            state = SettingsNotificationsState(
                kinds = listOf(
                    SettingsNotificationKind("replies", "Replies and mentions", "When someone responds to you", push = true, email = false),
                    SettingsNotificationKind("reminders", "Reminders", "Ones you set yourself", push = true, email = true),
                    SettingsNotificationKind("security", "Account and security", "Always on for your safety", push = true, email = true, locked = true),
                    SettingsNotificationKind("news", "News and offers", "Off unless you choose", push = false, email = false),
                ),
                quietHours = true,
            ),
            onEvent = {},
        )
    }
}
