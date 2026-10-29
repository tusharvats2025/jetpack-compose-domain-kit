package com.yourapp.ui.screens.feedback

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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Error · Offline
 * Full-screen offline state for content that can't be shown without a
 * connection. Says it will recover by itself, offers a manual retry and,
 * if you keep anything offline, a way to it.
 *
 * Watch connectivity with ConnectivityManager.registerDefaultNetworkCallback
 * and retry automatically when it comes back.
 */

data class ErrorOfflineState(
    val isRetrying: Boolean = false,
    val hasOfflineContent: Boolean = true,
)

sealed interface ErrorOfflineEvent {
    data object Retry : ErrorOfflineEvent
    data object OpenSaved : ErrorOfflineEvent
}

data class ErrorOfflineStrings(
    val heading: String = "You're offline",
    val body: String = "Check your Wi-Fi or mobile data. This page reloads by itself when you're back online.",
    val retry: String = "Try again",
    val waiting: String = "Waiting for a connection",
    val saved: String = "See what's saved offline",
)

@Composable
fun ErrorOffline(
    state: ErrorOfflineState,
    onEvent: (ErrorOfflineEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ErrorOfflineStrings = ErrorOfflineStrings(),
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(104.dp).background(colors.surfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) {
            Icon(WifiOffIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(strings.heading, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { onEvent(ErrorOfflineEvent.Retry) }, enabled = !state.isRetrying, modifier = Modifier.height(48.dp)) {
            Text(strings.retry)
        }
        Spacer(Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = colors.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Text(strings.waiting, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        if (state.hasOfflineContent) {
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { onEvent(ErrorOfflineEvent.OpenSaved) }) { Text(strings.saved) }
        }
    }
}

private val WifiOffIcon: ImageVector = ImageVector.Builder("WifiOff", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes("M23.64 7c-.45-.34-4.93-4-11.64-4-1.5 0-2.89.19-4.15.48L18.18 13.8 23.64 7zm-6.6 8.22L3.27 1.44 2 2.72l2.05 2.06C1.91 5.76.59 6.82.36 7l11.63 14.49.01.01.01-.01 3.9-4.86 3.32 3.32 1.27-1.27-3.46-3.46z"),
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
internal fun ErrorOfflinePreview() {
    MaterialTheme(colorScheme = PreviewColors) { ErrorOffline(state = ErrorOfflineState(), onEvent = {}) }
}
