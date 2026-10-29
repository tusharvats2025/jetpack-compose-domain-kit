package com.yourapp.ui.screens.feedback

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
 * Blocking · Force update
 * Shown when the installed version is no longer supported. No way past it
 * except updating; back is swallowed. Reassures that data is kept.
 *
 * Prefer Play's in-app updates (AppUpdateManager, IMMEDIATE) when you can;
 * use this screen as the fallback or when updates come from elsewhere.
 * Needs androidx.activity:activity-compose for BackHandler.
 */

data class BlockingForceUpdateState(
    val currentVersion: String = "1.4.2",
    val latestVersion: String = "2.0.0",
)

sealed interface BlockingForceUpdateEvent {
    data object Update : BlockingForceUpdateEvent
}

data class BlockingForceUpdateStrings(
    val heading: String = "Time to update",
    val body: String = "This version of [App name] is no longer supported. Update to keep using it. Your data stays as it is.",
    val current: (String) -> String = { "You have $it" },
    val latest: (String) -> String = { "Latest $it" },
    val update: String = "Update now",
    val store: String = "Opens the Play Store",
)

@Composable
fun BlockingForceUpdate(
    state: BlockingForceUpdateState,
    onEvent: (BlockingForceUpdateEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: BlockingForceUpdateStrings = BlockingForceUpdateStrings(),
) {
    val colors = MaterialTheme.colorScheme
    BackHandler { /* Blocking: nothing behind this screen is usable. */ }

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(104.dp).background(colors.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
            Icon(UpdateIcon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(strings.heading, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, enabled = false, label = { Text(strings.current(state.currentVersion)) })
            AssistChip(onClick = {}, enabled = false, label = { Text(strings.latest(state.latestVersion)) })
        }
        Spacer(Modifier.height(28.dp))
        Button(onClick = { onEvent(BlockingForceUpdateEvent.Update) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text(strings.update)
        }
        Spacer(Modifier.height(8.dp))
        Text(strings.store, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

private val UpdateIcon: ImageVector = ImageVector.Builder("Update", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes("M21 10.12h-6.78l2.74-2.82c-2.73-2.7-7.15-2.8-9.88-.1-2.73 2.71-2.73 7.08 0 9.79s7.15 2.71 9.88 0C18.32 15.65 19 14.08 19 12.1h2c0 1.98-.88 4.55-2.64 6.29-3.51 3.48-9.21 3.48-12.72 0-3.5-3.47-3.53-9.11-.02-12.58s9.14-3.47 12.65 0L21 3v7.12zM12.5 8v4.25l3.5 2.08-.72 1.21L11 13V8h1.5z"),
        fill = SolidColor(Color.Black),
    )
    .build()

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
internal fun BlockingForceUpdatePreview() {
    MaterialTheme(colorScheme = PreviewColors) { BlockingForceUpdate(state = BlockingForceUpdateState(), onEvent = {}) }
}
