package com.yourapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Profile · Directory profile (enterprise)
 * A colleague in the company directory: presence, title and team, local
 * time, quick actions, manager, contact details and direct reports.
 *
 * Square-ish corners and a quiet palette suit B2B. The design uses IBM
 * Plex Sans; set it in your Typography to match.
 */

enum class ProfilePresence { Available, Busy, Away, Offline }

data class ProfileContactField(val label: String, val value: String)
data class ProfilePerson(val id: String, val name: String, val initials: String)

data class ProfileDirectoryState(
    val name: String = "[Full name]",
    val initials: String = "CD",
    val title: String = "[Job title] · [Team]",
    val presence: ProfilePresence = ProfilePresence.Available,
    val locationTime: String = "[City] · 4:32 pm local time",
    val manager: ProfilePerson? = null,
    val fields: List<ProfileContactField> = emptyList(),
    val reports: List<ProfilePerson> = emptyList(),
)

sealed interface ProfileDirectoryEvent {
    data object Back : ProfileDirectoryEvent
    data object Message : ProfileDirectoryEvent
    data object Call : ProfileDirectoryEvent
    data object Email : ProfileDirectoryEvent
    data class OpenPerson(val id: String) : ProfileDirectoryEvent
    data object AllReports : ProfileDirectoryEvent
}

data class ProfileDirectoryStrings(
    val back: String = "Back",
    val title: String = "Directory",
    val presence: Map<ProfilePresence, String> = mapOf(
        ProfilePresence.Available to "Available",
        ProfilePresence.Busy to "Busy",
        ProfilePresence.Away to "Away",
        ProfilePresence.Offline to "Offline",
    ),
    val message: String = "Message",
    val call: String = "Call",
    val email: String = "Email",
    val reportsTo: String = "Reports to",
    val directReports: (Int) -> String = { "Direct reports · $it" },
)

@Composable
fun ProfileDirectory(
    state: ProfileDirectoryState,
    onEvent: (ProfileDirectoryEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileDirectoryStrings = ProfileDirectoryStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    avatar: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(10.dp)
    val presenceColor = when (state.presence) {
        ProfilePresence.Available -> Color(0xFF2E7D32)
        ProfilePresence.Busy -> Color(0xFFC62828)
        ProfilePresence.Away -> Color(0xFFF9A825)
        ProfilePresence.Offline -> colors.outline
    }
    Column(modifier.fillMaxSize().background(colors.surfaceContainerLow).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(ProfileDirectoryEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(
                Modifier.fillMaxWidth().clip(shape).background(colors.surface).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box {
                    Avatar(state.initials, avatar, 80.dp, accent, onAccent)
                    Box(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .size(20.dp)
                            .border(3.dp, colors.surface, CircleShape)
                            .background(presenceColor, CircleShape)
                            .semantics { contentDescription = strings.presence[state.presence].orEmpty() },
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(state.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(state.title, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                Text(state.locationTime, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple(ChatIcon, strings.message, ProfileDirectoryEvent.Message),
                        Triple(PhoneIcon, strings.call, ProfileDirectoryEvent.Call),
                        Triple(MailIcon, strings.email, ProfileDirectoryEvent.Email),
                    ).forEach { (icon, label, event) ->
                        OutlinedButton(onClick = { onEvent(event) }, shape = RoundedCornerShape(6.dp), contentPadding = PaddingValues(horizontal = 12.dp), modifier = Modifier.weight(1f)) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = accent)
                            Spacer(Modifier.width(6.dp))
                            Text(label, maxLines = 1)
                        }
                    }
                }
            }
            Column(Modifier.fillMaxWidth().clip(shape).background(colors.surface)) {
                state.manager?.let { m ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onEvent(ProfileDirectoryEvent.OpenPerson(m.id)) }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Avatar(m.initials, null, 36.dp, colors.secondaryContainer, colors.onSecondaryContainer)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(strings.reportsTo, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                            Text(m.name, style = MaterialTheme.typography.bodyLarge)
                        }
                        Icon(ChevronIcon, contentDescription = null, tint = colors.onSurfaceVariant)
                    }
                    HorizontalDivider(color = colors.outlineVariant)
                }
                state.fields.forEachIndexed { i, f ->
                    if (i > 0) HorizontalDivider(Modifier.padding(start = 16.dp), color = colors.outlineVariant)
                    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(f.label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                        Text(f.value, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            if (state.reports.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().clip(shape).background(colors.surface).clickable { onEvent(ProfileDirectoryEvent.AllReports) }.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(strings.directReports(state.reports.size), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                        state.reports.take(4).forEach { p ->
                            Box(Modifier.border(2.dp, colors.surface, CircleShape)) {
                                Avatar(p.initials, null, 32.dp, colors.tertiaryContainer, colors.onTertiaryContainer)
                            }
                        }
                    }
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

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val ChevronIcon = vectorIcon("Chevron", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z", autoMirror = true)
private val PhoneIcon = vectorIcon(
    "Phone",
    "M16 1H8C6.34 1 5 2.34 5 4v16c0 1.66 1.34 3 3 3h8c1.66 0 3-1.34 3-3V4c0-1.66-1.34-3-3-3zm-2 20h-4v-1h4v1zm3.25-3H6.75V4h10.5v14z",
)
private val MailIcon = vectorIcon("Mail", "M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z")
private val ChatIcon = vectorIcon("Chat", "M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 9h12v2H6V9zm8 5H6v-2h8v2zm4-6H6V6h12v2z")

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
internal fun ProfileDirectoryPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileDirectory(
            state = ProfileDirectoryState(
                manager = ProfilePerson("m", "[Manager name]", "EF"),
                fields = listOf(
                    ProfileContactField("Email", "name@company.com"),
                    ProfileContactField("Phone", "+91 00000 00000"),
                    ProfileContactField("Working hours", "10:00 am – 7:00 pm, Mon–Fri"),
                ),
                reports = listOf(ProfilePerson("1", "A", "GH"), ProfilePerson("2", "B", "IJ"), ProfilePerson("3", "C", "KL")),
            ),
            onEvent = {},
            accent = Color(0xFF2B45C4),
        )
    }
}
