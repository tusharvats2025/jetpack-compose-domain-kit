package com.yourapp.ui.screens.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Profile · Who's watching
 * Pick a profile on a shared account (streaming, family plans). Big tiles,
 * a kids badge, and add/manage. Dark by default; the tiles use each
 * profile's own colour.
 */

data class ProfilePickerProfile(
    val id: String,
    val name: String,
    val initial: String,
    val color: Color,
    val isKids: Boolean = false,
)

data class ProfilePickerState(
    val profiles: List<ProfilePickerProfile> = emptyList(),
    val canAdd: Boolean = true,
)

sealed interface ProfilePickerEvent {
    data class Pick(val id: String) : ProfilePickerEvent
    data object Add : ProfilePickerEvent
    data object Manage : ProfilePickerEvent
}

data class ProfilePickerStrings(
    val title: String = "Who's watching?",
    val kids: String = "KIDS",
    val add: String = "Add profile",
    val manage: String = "Manage profiles",
)

data class ProfilePickerColors(
    val background: Color = Color(0xFF14141A),
    val text: Color = Color(0xFFF2F1F8),
    val muted: Color = Color(0xFFA9A9B4),
    val outline: Color = Color(0xFF3A3A44),
)

@Composable
fun ProfilePicker(
    state: ProfilePickerState,
    onEvent: (ProfilePickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfilePickerStrings = ProfilePickerStrings(),
    colors: ProfilePickerColors = ProfilePickerColors(),
) {
    Column(
        modifier.fillMaxSize().background(colors.background).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(48.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, color = colors.text)
        Spacer(Modifier.height(32.dp))
        val tiles: List<ProfilePickerProfile?> = state.profiles + if (state.canAdd) listOf(null) else emptyList()
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            tiles.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    row.forEach { p ->
                        Column(
                            Modifier
                                .width(104.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable(role = Role.Button) { if (p == null) onEvent(ProfilePickerEvent.Add) else onEvent(ProfilePickerEvent.Pick(p.id)) }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                Modifier
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .then(if (p == null) Modifier.border(2.dp, colors.outline, RoundedCornerShape(16.dp)) else Modifier.background(p.color)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (p == null) {
                                    Icon(AddIcon, contentDescription = null, tint = colors.muted, modifier = Modifier.size(36.dp))
                                } else {
                                    Text(p.initial, color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                                    if (p.isKids) {
                                        Text(
                                            strings.kids,
                                            color = p.color,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp).background(Color.White, CircleShape).padding(horizontal = 6.dp, vertical = 1.dp),
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(p?.name ?: strings.add, style = MaterialTheme.typography.bodyMedium, color = if (p == null) colors.muted else colors.text)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(40.dp))
        OutlinedButton(
            onClick = { onEvent(ProfilePickerEvent.Manage) },
            border = BorderStroke(1.dp, colors.outline),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.text),
        ) {
            Icon(EditIcon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(strings.manage)
        }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val EditIcon = vectorIcon(
    "Edit",
    "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
)
private val AddIcon = vectorIcon("Add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")

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

@Preview
@Composable
internal fun ProfilePickerPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfilePicker(
            state = ProfilePickerState(
                profiles = listOf(
                    ProfilePickerProfile("a", "Asha", "A", Color(0xFF3949AB)),
                    ProfilePickerProfile("r", "Rohan", "R", Color(0xFF00695C)),
                    ProfilePickerProfile("k", "Kids", "K", Color(0xFFE65100), isKids = true),
                ),
            ),
            onEvent = {},
        )
    }
}
