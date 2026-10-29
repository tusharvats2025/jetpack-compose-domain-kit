package com.yourapp.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
 * Settings · Security
 * A status summary, sign-in settings, and every device the account is
 * signed in on, with per-device and all-other-devices sign-out.
 *
 * Confirm "sign out of all" in your own dialog before acting.
 */

enum class SettingsDeviceKind { Phone, Computer, Tablet }

data class SettingsSignedInDevice(
    val id: String,
    val name: String,
    val detail: String,
    val kind: SettingsDeviceKind,
    val isThisDevice: Boolean = false,
)

data class SettingsSecurityState(
    val isWellProtected: Boolean = true,
    val passwordChanged: String = "Changed 3 months ago",
    val twoStep: Boolean = true,
    val twoStepMethod: String = "Code by authenticator app",
    val devices: List<SettingsSignedInDevice> = emptyList(),
)

sealed interface SettingsSecurityEvent {
    data object Back : SettingsSecurityEvent
    data object ChangePassword : SettingsSecurityEvent
    data class SetTwoStep(val value: Boolean) : SettingsSecurityEvent
    data class SignOutDevice(val id: String) : SettingsSecurityEvent
    data object SignOutOthers : SettingsSecurityEvent
}

data class SettingsSecurityStrings(
    val back: String = "Back",
    val title: String = "Security",
    val protected: String = "Your account is well protected",
    val atRisk: String = "Turn on two-step verification to protect your account",
    val signIn: String = "Sign-in",
    val password: String = "Password",
    val twoStep: String = "Two-step verification",
    val devices: String = "Where you're signed in",
    val thisDevice: String = "This device",
    val signOut: String = "Sign out",
    val signOutOthers: String = "Sign out of all other devices",
)

@Composable
fun SettingsSecurity(
    state: SettingsSecurityState,
    onEvent: (SettingsSecurityEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: SettingsSecurityStrings = SettingsSecurityStrings(),
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(SettingsSecurityEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            val ok = state.isWellProtected
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .background(if (ok) colors.primaryContainer else colors.errorContainer)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(if (ok) ShieldIcon else WarningIcon, contentDescription = null, tint = if (ok) colors.onPrimaryContainer else colors.onErrorContainer)
                Spacer(Modifier.width(12.dp))
                Text(
                    if (ok) strings.protected else strings.atRisk,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (ok) colors.onPrimaryContainer else colors.onErrorContainer,
                )
            }
            SectionLabel(strings.signIn)
            Group {
                LinkRow(strings.password, { onEvent(SettingsSecurityEvent.ChangePassword) }, subtitle = state.passwordChanged)
                RowDivider()
                SwitchRow(strings.twoStep, state.twoStepMethod, state.twoStep, { onEvent(SettingsSecurityEvent.SetTwoStep(it)) })
            }
            SectionLabel(strings.devices)
            Group {
                state.devices.forEachIndexed { i, d ->
                    if (i > 0) RowDivider()
                    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(colors.surfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(
                                when (d.kind) {
                                    SettingsDeviceKind.Phone -> PhoneIcon
                                    SettingsDeviceKind.Computer -> LaptopIcon
                                    SettingsDeviceKind.Tablet -> TabletIcon
                                },
                                contentDescription = null,
                                tint = colors.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(d.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                if (d.isThisDevice) strings.thisDevice else d.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (d.isThisDevice) colors.primary else colors.onSurfaceVariant,
                            )
                        }
                        if (!d.isThisDevice) {
                            TextButton(onClick = { onEvent(SettingsSecurityEvent.SignOutDevice(d.id)) }) { Text(strings.signOut) }
                        }
                    }
                }
            }
            if (state.devices.count { !it.isThisDevice } > 0) {
                OutlinedButton(
                    onClick = { onEvent(SettingsSecurityEvent.SignOutOthers) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                    border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(48.dp),
                ) { Text(strings.signOutOthers) }
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
private val WarningIcon = vectorIcon("Warning", "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z")
private val ShieldIcon = vectorIcon(
    "Shield",
    "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z",
)
private val PhoneIcon = vectorIcon(
    "Phone",
    "M16 1H8C6.34 1 5 2.34 5 4v16c0 1.66 1.34 3 3 3h8c1.66 0 3-1.34 3-3V4c0-1.66-1.34-3-3-3zm-2 20h-4v-1h4v1zm3.25-3H6.75V4h10.5v14z",
)
private val LaptopIcon = vectorIcon("Laptop", "M20 18c1.1 0 1.99-.9 1.99-2L22 6c0-1.1-.9-2-2-2H4c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2H0v2h24v-2h-4zM4 6h16v10H4V6z")
private val TabletIcon = vectorIcon("Tablet", "M21 4H3c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h18c1.1 0 1.99-.9 1.99-2L23 6c0-1.1-.9-2-2-2zm-2 14H5V6h14v12z")

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
internal fun SettingsSecurityPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        SettingsSecurity(
            state = SettingsSecurityState(
                devices = listOf(
                    SettingsSignedInDevice("1", "Pixel 9", "", SettingsDeviceKind.Phone, isThisDevice = true),
                    SettingsSignedInDevice("2", "Chrome on Windows", "Pune · 2 days ago", SettingsDeviceKind.Computer),
                    SettingsSignedInDevice("3", "Galaxy Tab S9", "Mumbai · 3 weeks ago", SettingsDeviceKind.Tablet),
                ),
            ),
            onEvent = {},
        )
    }
}
