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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Error · Permission needed
 * A feature that can't work without a permission the user turned off.
 * Says why it's needed, how to turn it back on, and offers an alternative.
 *
 * Defaults are for the camera; change icon and strings for others.
 */

data class ErrorPermissionState(
    val title: String = "Scan document",
    val steps: List<String> = listOf("Open Settings, then Permissions", "Set Camera to \"Allow while using\""),
)

sealed interface ErrorPermissionEvent {
    data object Back : ErrorPermissionEvent
    data object OpenSettings : ErrorPermissionEvent
    data object Alternative : ErrorPermissionEvent
}

data class ErrorPermissionStrings(
    val back: String = "Back",
    val heading: String = "Camera access is off",
    val body: String = "[App name] needs the camera to scan documents. Nothing is recorded or stored without you saving it.",
    val openSettings: String = "Open Settings",
    val alternative: String = "Upload a photo instead",
)

@Composable
fun ErrorPermission(
    state: ErrorPermissionState,
    onEvent: (ErrorPermissionEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ErrorPermissionStrings = ErrorPermissionStrings(),
    icon: ImageVector = CameraOffIcon,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(ErrorPermissionEvent.Back) }) { Icon(BackIcon, contentDescription = strings.back) }
            Text(state.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(104.dp).background(colors.errorContainer, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = colors.onErrorContainer, modifier = Modifier.size(48.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(strings.heading, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Column(
                Modifier.fillMaxWidth().background(colors.surfaceContainerLow, MaterialTheme.shapes.large).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.steps.forEachIndexed { i, step ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(26.dp).background(colors.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = colors.onPrimaryContainer)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(step, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = { onEvent(ErrorPermissionEvent.OpenSettings) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(strings.openSettings)
            }
            TextButton(onClick = { onEvent(ErrorPermissionEvent.Alternative) }, modifier = Modifier.padding(top = 4.dp)) {
                Text(strings.alternative)
            }
        }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val CameraOffIcon = vectorIcon(
    "CameraOff",
    "M21 6.5l-4 4V7c0-.55-.45-1-1-1H9.82L21 17.18V6.5zM3.27 2L2 3.27 4.73 6H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.21 0 .39-.08.54-.18L19.73 21 21 19.73 3.27 2z",
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
internal fun ErrorPermissionPreview() {
    MaterialTheme(colorScheme = PreviewColors) { ErrorPermission(state = ErrorPermissionState(), onEvent = {}) }
}
