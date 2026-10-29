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
 * Blocking · Maintenance
 * Planned downtime. States when it's expected back, reassures that saved
 * data is fine, and links to your status page.
 *
 * Drive it from remote config or your API so you can turn it on without a
 * release. Format expectedBack in the user's time zone.
 */

data class BlockingMaintenanceState(
    val expectedBack: String? = "Today, 4:30 pm IST",
    val isChecking: Boolean = false,
)

sealed interface BlockingMaintenanceEvent {
    data object CheckAgain : BlockingMaintenanceEvent
    data object StatusPage : BlockingMaintenanceEvent
}

data class BlockingMaintenanceStrings(
    val heading: String = "We'll be right back",
    val body: String = "[App name] is down for planned maintenance. Nothing you saved is affected.",
    val expected: String = "Expected back",
    val check: String = "Check again",
    val status: String = "Status page",
)

@Composable
fun BlockingMaintenance(
    state: BlockingMaintenanceState,
    onEvent: (BlockingMaintenanceEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: BlockingMaintenanceStrings = BlockingMaintenanceStrings(),
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
        Box(Modifier.size(104.dp).background(colors.tertiaryContainer, CircleShape), contentAlignment = Alignment.Center) {
            Icon(BuildIcon, contentDescription = null, tint = colors.onTertiaryContainer, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(strings.heading, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        if (state.expectedBack != null) {
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth().background(colors.surfaceContainerLow, MaterialTheme.shapes.large).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(ClockIcon, contentDescription = null, tint = colors.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(strings.expected, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    Text(state.expectedBack, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        OutlinedButton(onClick = { onEvent(BlockingMaintenanceEvent.CheckAgain) }, enabled = !state.isChecking, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            if (state.isChecking) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text(strings.check)
        }
        TextButton(onClick = { onEvent(BlockingMaintenanceEvent.StatusPage) }, modifier = Modifier.padding(top = 4.dp)) { Text(strings.status) }
    }
}

private fun vectorIcon(name: String, path: String): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BuildIcon = vectorIcon(
    "Build",
    "M22.7 19l-9.1-9.1c.9-2.3.4-5-1.5-6.9-2-2-5-2.4-7.4-1.3L9 6 6 9 1.6 4.7C.4 7.1.9 10.1 2.9 12.1c1.9 1.9 4.6 2.4 6.9 1.5l9.1 9.1c.4.4 1 .4 1.4 0l2.3-2.3c.5-.4.5-1.1.1-1.4z",
)
private val ClockIcon = vectorIcon(
    "Clock",
    "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z",
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
internal fun BlockingMaintenancePreview() {
    MaterialTheme(colorScheme = PreviewColors) { BlockingMaintenance(state = BlockingMaintenanceState(), onEvent = {}) }
}
