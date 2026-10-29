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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Error · Something went wrong
 * A server error inside a screen. Says whose problem it is, offers retry,
 * and shows a reference code support can look up (with a copy button).
 *
 * Copying is yours: LocalClipboard / ClipboardManager on CopyReference.
 */

data class ErrorServerState(
    val title: String = "Orders",
    val reference: String? = null,
    val isRetrying: Boolean = false,
)

sealed interface ErrorServerEvent {
    data object Back : ErrorServerEvent
    data object Retry : ErrorServerEvent
    data object CopyReference : ErrorServerEvent
    data object ContactSupport : ErrorServerEvent
}

data class ErrorServerStrings(
    val back: String = "Back",
    val heading: String = "We couldn't load your orders",
    val body: String = "It's a problem on our side, not your connection. Try again in a moment.",
    val retry: String = "Try again",
    val reference: String = "Reference",
    val copy: String = "Copy reference code",
    val support: String = "Contact support",
)

@Composable
fun ErrorServer(
    state: ErrorServerState,
    onEvent: (ErrorServerEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ErrorServerStrings = ErrorServerStrings(),
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(ErrorServerEvent.Back) }) { Icon(BackIcon, contentDescription = strings.back) }
            Text(state.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(104.dp).background(colors.errorContainer, CircleShape), contentAlignment = Alignment.Center) {
                Icon(CloudErrorIcon, contentDescription = null, tint = colors.onErrorContainer, modifier = Modifier.size(48.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(strings.heading, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Button(onClick = { onEvent(ErrorServerEvent.Retry) }, enabled = !state.isRetrying, modifier = Modifier.height(48.dp)) {
                if (state.isRetrying) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                else Text(strings.retry)
            }
            if (state.reference != null) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${strings.reference} ", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    Text(state.reference, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                    IconButton(onClick = { onEvent(ErrorServerEvent.CopyReference) }) {
                        Icon(CopyIcon, contentDescription = strings.copy, modifier = Modifier.size(18.dp))
                    }
                }
            }
            TextButton(onClick = { onEvent(ErrorServerEvent.ContactSupport) }) { Text(strings.support) }
        }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val CloudErrorIcon = vectorIcon(
    "CloudError",
    "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM13 17h-2v-2h2v2zm0-4h-2V8h2v5z",
)
private val CopyIcon = vectorIcon(
    "Copy",
    "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z",
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
internal fun ErrorServerPreview() {
    MaterialTheme(colorScheme = PreviewColors) { ErrorServer(state = ErrorServerState(reference = "ERR-7F3A"), onEvent = {}) }
}
