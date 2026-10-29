package com.yourapp.ui.screens.profile

import androidx.compose.foundation.background
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
 * Profile · Minimal card
 * A plain account page: avatar, name, a few editable fields with inline
 * Change actions, and sign out. Good for utility apps.
 */

data class ProfileMinimalField(val id: String, val label: String, val value: String, val action: String)

data class ProfileMinimalCardState(
    val name: String = "[Full name]",
    val initials: String = "AB",
    val memberSince: String = "Member since 2024",
    val fields: List<ProfileMinimalField> = emptyList(),
)

sealed interface ProfileMinimalCardEvent {
    data object Back : ProfileMinimalCardEvent
    data class FieldAction(val id: String) : ProfileMinimalCardEvent
    data object SignOut : ProfileMinimalCardEvent
}

data class ProfileMinimalCardStrings(
    val back: String = "Back",
    val title: String = "Account",
    val signOut: String = "Sign out",
)

@Composable
fun ProfileMinimalCard(
    state: ProfileMinimalCardState,
    onEvent: (ProfileMinimalCardEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileMinimalCardStrings = ProfileMinimalCardStrings(),
    avatar: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(ProfileMinimalCardEvent.Back) })
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            Avatar(state.initials, avatar, 72.dp, colors.primaryContainer, colors.onPrimaryContainer)
            Spacer(Modifier.height(12.dp))
            Text(state.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(state.memberSince, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Group {
                state.fields.forEachIndexed { i, f ->
                    if (i > 0) RowDivider()
                    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(f.label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                            Text(f.value, style = MaterialTheme.typography.bodyLarge)
                        }
                        TextButton(onClick = { onEvent(ProfileMinimalCardEvent.FieldAction(f.id)) }) { Text(f.action) }
                    }
                }
            }
        }
        TextButton(
            onClick = { onEvent(ProfileMinimalCardEvent.SignOut) },
            colors = ButtonDefaults.textButtonColors(contentColor = colors.error),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) { Text(strings.signOut) }
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
internal fun ProfileMinimalCardPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileMinimalCard(
            state = ProfileMinimalCardState(
                fields = listOf(
                    ProfileMinimalField("email", "Email", "name@example.com", "Change"),
                    ProfileMinimalField("phone", "Phone", "+91 00000 00000", "Change"),
                    ProfileMinimalField("plan", "Plan", "Free", "Upgrade"),
                ),
            ),
            onEvent = {},
        )
    }
}
