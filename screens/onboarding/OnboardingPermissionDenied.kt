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
 * Onboarding · Permission denied
 * After the user refused a permission (and Android won't ask again):
 * says what still works, shows the steps in Settings, and offers a way
 * forward without the permission.
 *
 * Open Settings with
 *   Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
 * Defaults are for location; change icon and strings for other permissions.
 */

data class OnboardingPermissionDeniedState(
    val steps: List<String> = listOf("Tap Permissions", "Tap Location, then \"Allow while using\""),
)

sealed interface OnboardingPermissionDeniedEvent {
    data object OpenSettings : OnboardingPermissionDeniedEvent
    data object Alternative : OnboardingPermissionDeniedEvent
}

data class OnboardingPermissionDeniedStrings(
    val title: String = "Location is off",
    val body: String = "We use it only to show what's near you. Turn it on in Settings, or enter your area yourself.",
    val stepsHeading: String = "In Settings",
    val openSettings: String = "Open Settings",
    val alternative: String = "Enter my area instead",
)

@Composable
fun OnboardingPermissionDenied(
    state: OnboardingPermissionDeniedState,
    onEvent: (OnboardingPermissionDeniedEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingPermissionDeniedStrings = OnboardingPermissionDeniedStrings(),
    icon: ImageVector = LocationOffIcon,
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
            Modifier.size(96.dp).background(colors.errorContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = colors.onErrorContainer, modifier = Modifier.size(44.dp)) }
        Spacer(Modifier.height(28.dp))
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
            Text(strings.stepsHeading, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
            state.steps.forEachIndexed { i, step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(26.dp).background(colors.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = colors.onPrimaryContainer) }
                    Spacer(Modifier.width(12.dp))
                    Text(step, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(OnboardingPermissionDeniedEvent.OpenSettings) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(strings.openSettings) }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { onEvent(OnboardingPermissionDeniedEvent.Alternative) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(strings.alternative) }
    }
}

private val LocationOffIcon: ImageVector = ImageVector.Builder("LocationOff", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes("M12 6.5c1.38 0 2.5 1.12 2.5 2.5 0 .74-.33 1.39-.83 1.85l3.63 3.63c.98-1.86 1.7-3.8 1.7-5.48 0-3.87-3.13-7-7-7-1.98 0-3.76.83-5.04 2.15l3.19 3.19c.46-.52 1.11-.84 1.85-.84zm4.37 9.6l-4.63-4.63-.11-.11L3.27 3 2 4.27l3.18 3.18C5.07 7.95 5 8.47 5 9c0 5.25 7 13 7 13s1.67-1.85 3.38-4.35L18.73 21 20 19.73l-3.63-3.63z"),
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
internal fun OnboardingPermissionDeniedPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingPermissionDenied(state = OnboardingPermissionDeniedState(), onEvent = {})
    }
}
