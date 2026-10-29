package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Onboarding · Feature list
 * A single screen listing up to four benefits with icons, and one
 * Continue. Faster than a carousel when the benefits are simple.
 *
 * Bring your own icons per feature, or use the four defaults below.
 */

data class OnboardingFeature(
    val title: String,
    val body: String,
    val icon: ImageVector,
)

/** Four placeholder features with icons drawn in this file. */
fun onboardingDefaultFeatures(): List<OnboardingFeature> = listOf(
    OnboardingFeature("Fast to start", "One line on the first benefit.", BoltIcon),
    OnboardingFeature("Everything in one place", "One line on the second benefit.", FolderIcon),
    OnboardingFeature("Private by default", "One line on the third benefit.", ShieldIcon),
    OnboardingFeature("Works offline", "One line on the fourth benefit.", CloudOffIcon),
)

data class OnboardingFeatureListState(
    val features: List<OnboardingFeature> = onboardingDefaultFeatures(),
)

sealed interface OnboardingFeatureListEvent {
    data object Continue : OnboardingFeatureListEvent
}

data class OnboardingFeatureListStrings(
    val eyebrow: String = "Welcome to [App name]",
    val title: String = "Here's what you\ncan do",
    val submit: String = "Continue",
)

@Composable
fun OnboardingFeatureList(
    state: OnboardingFeatureListState,
    onEvent: (OnboardingFeatureListEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingFeatureListStrings = OnboardingFeatureListStrings(),
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 24.dp),
    ) {
        Text(strings.eyebrow, style = MaterialTheme.typography.labelLarge, color = colors.primary)
        Spacer(Modifier.height(12.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(36.dp))
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            state.features.forEach { feature ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.size(48.dp).background(colors.primaryContainer, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(feature.icon, contentDescription = null, tint = colors.onPrimaryContainer) }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f).padding(top = 2.dp)) {
                        Text(feature.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(2.dp))
                        Text(feature.body, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(OnboardingFeatureListEvent.Continue) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(strings.submit) }
    }
}

private fun vectorIcon(name: String, path: String): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BoltIcon = vectorIcon("Bolt", "M11 21h-1l1-7H7.5c-.88 0-.33-.75-.31-.78C8.48 10.94 10.42 7.54 13.01 3h1l-1 7h3.51c.4 0 .62.19.4.66C12.97 17.55 11 21 11 21z")
private val FolderIcon = vectorIcon("Folder", "M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z")
private val ShieldIcon = vectorIcon("Shield", "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z")
private val CloudOffIcon = vectorIcon(
    "CloudOff",
    "M19.35 10.04C18.67 6.59 15.64 4 12 4c-1.48 0-2.85.43-4.01 1.17l1.46 1.46C10.21 6.23 11.08 6 12 6c3.04 0 5.5 2.46 5.5 5.5v.5H19c1.66 0 3 1.34 3 3 0 1.13-.64 2.11-1.56 2.62l1.45 1.45C23.16 18.16 24 16.68 24 15c0-2.64-2.05-4.78-4.65-4.96zM3 5.27l2.75 2.74C2.56 8.15 0 10.77 0 14c0 3.31 2.69 6 6 6h11.73l2 2L21 20.73 4.27 4 3 5.27zM7.73 10l8 8H6c-2.21 0-4-1.79-4-4s1.79-4 4-4h1.73z",
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
internal fun OnboardingFeatureListPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingFeatureList(state = OnboardingFeatureListState(), onEvent = {})
    }
}
