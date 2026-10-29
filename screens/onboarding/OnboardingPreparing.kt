package com.yourapp.ui.screens.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Onboarding · Preparing
 * A progress ring and a short checklist while the app personalises.
 * Use it only when setup really takes a few seconds; if it's instant,
 * skip this screen.
 *
 * Drive progress (0f to 1f) and each item's done flag from real work.
 * Navigate on your own when everything is done.
 */

data class OnboardingPrepItem(val label: String, val done: Boolean)

data class OnboardingPreparingState(
    val progress: Float = 0f,
    val items: List<OnboardingPrepItem> = emptyList(),
)

data class OnboardingPreparingStrings(
    val title: String = "Setting things up for you",
    val body: String = "This only takes a moment.",
    val percent: (Int) -> String = { "$it%" },
    val progressDescription: (Int) -> String = { "Setting up, $it percent" },
)

@Composable
fun OnboardingPreparing(
    state: OnboardingPreparingState,
    modifier: Modifier = Modifier,
    strings: OnboardingPreparingStrings = OnboardingPreparingStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val animated by animateFloatAsState(state.progress.coerceIn(0f, 1f), label = "progress")
    val percent = (animated * 100).toInt()

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(140.dp)
                .clearAndSetSemantics { contentDescription = strings.progressDescription(percent) },
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                progress = { animated },
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 10.dp,
                trackColor = colors.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
            )
            Text(strings.percent(percent), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(32.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        Column(
            Modifier.widthIn(max = 320.dp).semantics { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            state.items.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(24.dp)
                            .then(
                                if (item.done) Modifier.background(colors.primary, CircleShape)
                                else Modifier.border(2.dp, colors.outlineVariant, CircleShape),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (item.done) Icon(CheckIcon, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        item.label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (item.done) colors.onSurface else colors.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private val CheckIcon: ImageVector = ImageVector.Builder("Check", 24.dp, 24.dp, 24f, 24f)
    .addPath(addPathNodes("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"), fill = SolidColor(Color.Black))
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
internal fun OnboardingPreparingPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingPreparing(
            state = OnboardingPreparingState(
                progress = 0.66f,
                items = listOf(
                    OnboardingPrepItem("Saving your interests", true),
                    OnboardingPrepItem("Creating your profile", true),
                    OnboardingPrepItem("Finding things you'll like", false),
                    OnboardingPrepItem("Preparing your home screen", false),
                ),
            ),
        )
    }
}
