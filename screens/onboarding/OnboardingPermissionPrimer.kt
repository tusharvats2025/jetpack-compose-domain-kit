package com.yourapp.ui.screens.onboarding

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
 * Onboarding · Permission primer
 * Explains a permission in your words before the system dialog asks.
 * Users who understand why say yes more often, and a "Not now" here
 * doesn't burn the system prompt.
 *
 * Allow is where you launch the real request, e.g. with
 * rememberLauncherForActivityResult(RequestPermission()). Defaults are for
 * notifications; change icon and strings for camera, location and so on.
 */

data class OnboardingPermissionPrimerState(
    val points: List<String> = listOf(
        "Reminders you set yourself",
        "Replies and mentions",
        "Never marketing, unless you opt in",
    ),
)

sealed interface OnboardingPermissionPrimerEvent {
    data object Allow : OnboardingPermissionPrimerEvent
    data object NotNow : OnboardingPermissionPrimerEvent
}

data class OnboardingPermissionPrimerStrings(
    val title: String = "Stay on top of updates",
    val body: String = "Turn on notifications and we'll only tell you about things that need you.",
    val allow: String = "Turn on notifications",
    val notNow: String = "Not now",
    val footnote: String = "You can change this anytime in Settings.",
)

@Composable
fun OnboardingPermissionPrimer(
    state: OnboardingPermissionPrimerState,
    onEvent: (OnboardingPermissionPrimerEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingPermissionPrimerStrings = OnboardingPermissionPrimerStrings(),
    icon: ImageVector = BellIcon,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.size(132.dp).background(colors.primaryContainer.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.size(88.dp).background(colors.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(40.dp)) }
        }
        Spacer(Modifier.height(32.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerLow, MaterialTheme.shapes.large)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.points.forEach { point ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(CheckIcon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(point, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(OnboardingPermissionPrimerEvent.Allow) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(strings.allow) }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = { onEvent(OnboardingPermissionPrimerEvent.NotNow) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { Text(strings.notNow) }
        Spacer(Modifier.height(4.dp))
        Text(strings.footnote, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

private fun vectorIcon(name: String, path: String): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BellIcon = vectorIcon(
    "Bell",
    "M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z",
)
private val CheckIcon = vectorIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")

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
internal fun OnboardingPermissionPrimerPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingPermissionPrimer(state = OnboardingPermissionPrimerState(), onEvent = {})
    }
}
