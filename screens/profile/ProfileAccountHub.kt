package com.yourapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Profile · Account hub
 * The "You" tab of a shopping or service app: a header card with Edit,
 * grouped shortcuts, and log out. Keeps your bottom navigation.
 *
 * Menu items are data, so you can add, remove or reorder them.
 */

enum class ProfileHubIcon { Orders, Saved, Payments, Addresses, Settings, Help }

data class ProfileHubItem(val id: String, val label: String, val icon: ProfileHubIcon, val badge: String? = null)

data class ProfileAccountHubState(
    val name: String = "[Full name]",
    val email: String = "name@example.com",
    val initials: String = "AB",
    val primaryItems: List<ProfileHubItem> = emptyList(),
    val secondaryItems: List<ProfileHubItem> = emptyList(),
)

sealed interface ProfileAccountHubEvent {
    data object EditProfile : ProfileAccountHubEvent
    data class Open(val id: String) : ProfileAccountHubEvent
    data object LogOut : ProfileAccountHubEvent
}

data class ProfileAccountHubStrings(
    val title: String = "Profile",
    val edit: String = "Edit",
    val logOut: String = "Log out",
)

@Composable
fun ProfileAccountHub(
    state: ProfileAccountHubState,
    onEvent: (ProfileAccountHubEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileAccountHubStrings = ProfileAccountHubStrings(),
    avatar: (@Composable () -> Unit)? = null,
    bottomBar: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Scaffold(modifier = modifier, bottomBar = bottomBar, containerColor = colors.surfaceContainerLowest) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
            Row(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(state.initials, avatar, 56.dp, colors.primary, colors.onPrimary)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(state.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(state.email, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(onClick = { onEvent(ProfileAccountHubEvent.EditProfile) }) { Text(strings.edit) }
            }
            MenuCard(state.primaryItems, onEvent)
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow)) {
                state.secondaryItems.forEach { item ->
                    MenuRow(item, onEvent)
                    HorizontalDivider(Modifier.padding(start = 56.dp), color = colors.outlineVariant)
                }
                Row(
                    Modifier.fillMaxWidth().clickable(role = Role.Button) { onEvent(ProfileAccountHubEvent.LogOut) }.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(LogoutIcon, contentDescription = null, tint = colors.error, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(18.dp))
                    Text(strings.logOut, style = MaterialTheme.typography.bodyLarge, color = colors.error)
                }
            }
        }
    }
}

@Composable
private fun MenuCard(items: List<ProfileHubItem>, onEvent: (ProfileAccountHubEvent) -> Unit) {
    if (items.isEmpty()) return
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow)) {
        items.forEachIndexed { i, item ->
            if (i > 0) HorizontalDivider(Modifier.padding(start = 56.dp), color = colors.outlineVariant)
            MenuRow(item, onEvent)
        }
    }
}

@Composable
private fun MenuRow(item: ProfileHubItem, onEvent: (ProfileAccountHubEvent) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button) { onEvent(ProfileAccountHubEvent.Open(item.id)) }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            when (item.icon) {
                ProfileHubIcon.Orders -> BagIcon
                ProfileHubIcon.Saved -> HeartIcon
                ProfileHubIcon.Payments -> CardIcon
                ProfileHubIcon.Addresses -> PinIcon
                ProfileHubIcon.Settings -> SettingsIcon
                ProfileHubIcon.Help -> HelpIcon
            },
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(18.dp))
        Text(item.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (item.badge != null) Badge { Text(item.badge) }
        Icon(ChevronIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.padding(start = 8.dp).size(20.dp))
    }
}

/** Your picture, or initials on a tinted circle. */
@Composable
private fun Avatar(initials: String, image: (@Composable () -> Unit)?, size: Dp, container: Color, content: Color) {
    Box(Modifier.size(size).clip(CircleShape).background(container), contentAlignment = Alignment.Center) {
        if (image != null) image()
        else Text(initials, color = content, fontWeight = FontWeight.SemiBold, fontSize = (size.value * 0.36f).sp)
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val ChevronIcon = vectorIcon("Chevron", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z", autoMirror = true)
private val SettingsIcon = vectorIcon(
    "Settings",
    "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z",
)
private val HelpIcon = vectorIcon(
    "Help",
    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 17h-2v-2h2v2zm2.07-7.75l-.9.92C13.45 12.9 13 13.5 13 15h-2v-.5c0-1.1.45-2.1 1.17-2.83l1.24-1.26c.37-.36.59-.86.59-1.41 0-1.1-.9-2-2-2s-2 .9-2 2H8c0-2.21 1.79-4 4-4s4 1.79 4 4c0 .88-.36 1.68-.93 2.25z",
)
private val LogoutIcon = vectorIcon(
    "Logout",
    "M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z", autoMirror = true,
)
private val BagIcon = vectorIcon(
    "Bag",
    "M18 6h-2c0-2.21-1.79-4-4-4S8 3.79 8 6H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-6-2c1.1 0 2 .9 2 2h-4c0-1.1.9-2 2-2zm6 16H6V8h2v2c0 .55.45 1 1 1s1-.45 1-1V8h4v2c0 .55.45 1 1 1s1-.45 1-1V8h2v12z",
)
private val HeartIcon = vectorIcon(
    "Heart",
    "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z",
)
private val CardIcon = vectorIcon(
    "Card",
    "M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z",
)
private val PinIcon = vectorIcon(
    "Pin",
    "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z",
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
internal fun ProfileAccountHubPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileAccountHub(
            state = ProfileAccountHubState(
                primaryItems = listOf(
                    ProfileHubItem("orders", "My orders", ProfileHubIcon.Orders, badge = "2"),
                    ProfileHubItem("saved", "Saved", ProfileHubIcon.Saved),
                    ProfileHubItem("payments", "Payment methods", ProfileHubIcon.Payments),
                    ProfileHubItem("addresses", "Addresses", ProfileHubIcon.Addresses),
                ),
                secondaryItems = listOf(
                    ProfileHubItem("settings", "Settings", ProfileHubIcon.Settings),
                    ProfileHubItem("help", "Help and support", ProfileHubIcon.Help),
                ),
            ),
            onEvent = {},
        )
    }
}
