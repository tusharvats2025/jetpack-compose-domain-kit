package com.yourapp.ui.screens.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Empty · All caught up
 * A positive empty state: the inbox is clear. Calm, no call to action
 * beyond a quiet link to settings. Tabs stay so the user can switch view.
 */

data class EmptyAllCaughtUpState(
    val tabs: List<String> = listOf("All", "Mentions"),
    val selectedTab: Int = 0,
)

sealed interface EmptyAllCaughtUpEvent {
    data object Back : EmptyAllCaughtUpEvent
    data class SelectTab(val index: Int) : EmptyAllCaughtUpEvent
    data object OpenSettings : EmptyAllCaughtUpEvent
}

data class EmptyAllCaughtUpStrings(
    val back: String = "Back",
    val title: String = "Notifications",
    val heading: String = "You're all caught up",
    val body: String = "New notifications will show up here.",
    val settings: String = "Notification settings",
)

@OptIn(ExperimentalMaterial3Api::class) // PrimaryTabRow on older Material 3 versions
@Composable
fun EmptyAllCaughtUp(
    state: EmptyAllCaughtUpState,
    onEvent: (EmptyAllCaughtUpEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: EmptyAllCaughtUpStrings = EmptyAllCaughtUpStrings(),
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(EmptyAllCaughtUpEvent.Back) }) { Icon(BackIcon, contentDescription = strings.back) }
            Text(strings.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        PrimaryTabRow(selectedTabIndex = state.selectedTab, containerColor = colors.surface) {
            state.tabs.forEachIndexed { i, label ->
                Tab(selected = i == state.selectedTab, onClick = { onEvent(EmptyAllCaughtUpEvent.SelectTab(i)) }, text = { Text(label) })
            }
        }
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(Modifier.size(104.dp).background(colors.tertiaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Icon(DoneAllIcon, contentDescription = null, tint = colors.onTertiaryContainer, modifier = Modifier.size(48.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(strings.heading, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { onEvent(EmptyAllCaughtUpEvent.OpenSettings) }) { Text(strings.settings) }
            Spacer(Modifier.height(64.dp))
        }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val DoneAllIcon = vectorIcon("DoneAll", "M18 7l-1.41-1.41-6.34 6.34 1.41 1.41L18 7zm4.24-1.41L11.66 16.17 7.48 12l-1.41 1.41L11.66 19l12-12-1.42-1.41zM.41 13.41L6 19l1.41-1.41L1.83 12 .41 13.41z")

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
internal fun EmptyAllCaughtUpPreview() {
    MaterialTheme(colorScheme = PreviewColors) { EmptyAllCaughtUp(state = EmptyAllCaughtUpState(), onEvent = {}) }
}
