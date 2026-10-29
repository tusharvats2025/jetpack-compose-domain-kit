package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Onboarding · Interest picker
 * A chip grid with a minimum selection and a live count. Continue unlocks
 * once the minimum is reached.
 *
 * Selection lives in your state as a set of ids, so it survives rotation
 * and process death the same way the rest of your state does.
 */

data class OnboardingInterest(val id: String, val label: String)

data class OnboardingInterestPickerState(
    val step: Int = 2,
    val totalSteps: Int = 4,
    val interests: List<OnboardingInterest> = emptyList(),
    val selected: Set<String> = emptySet(),
    val minimum: Int = 3,
)

sealed interface OnboardingInterestPickerEvent {
    data object Back : OnboardingInterestPickerEvent
    data class Toggle(val id: String) : OnboardingInterestPickerEvent
    data object Continue : OnboardingInterestPickerEvent
}

data class OnboardingInterestPickerStrings(
    val back: String = "Back",
    val title: String = "What are you into?",
    val subtitle: (minimum: Int) -> String = { "Pick at least $it. You can change these later." },
    val selectedCount: (count: Int) -> String = { "$it selected" },
    val submit: String = "Continue",
)

@OptIn(ExperimentalLayoutApi::class) // FlowRow on older Compose versions
@Composable
fun OnboardingInterestPicker(
    state: OnboardingInterestPickerState,
    onEvent: (OnboardingInterestPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingInterestPickerStrings = OnboardingInterestPickerStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val count = state.selected.size

    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
        ) {
            StepHeader(state.step, state.totalSteps, strings.back) { onEvent(OnboardingInterestPickerEvent.Back) }
            Spacer(Modifier.height(24.dp))
            Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(strings.subtitle(state.minimum), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                state.interests.forEach { interest ->
                    val isSelected = interest.id in state.selected
                    FilterChip(
                        selected = isSelected,
                        onClick = { onEvent(OnboardingInterestPickerEvent.Toggle(interest.id)) },
                        label = { Text(interest.label) },
                        leadingIcon = if (isSelected) {
                            { Icon(CheckIcon, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                        } else null,
                        modifier = Modifier.heightIn(min = 40.dp),
                    )
                }
            }
        }

        // Sticky footer: count and Continue stay visible while the grid scrolls.
        HorizontalDivider()
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                strings.selectedCount(count),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f).semantics { liveRegion = LiveRegionMode.Polite },
            )
            Button(
                onClick = { onEvent(OnboardingInterestPickerEvent.Continue) },
                enabled = count >= state.minimum,
                modifier = Modifier.height(52.dp),
            ) { Text(strings.submit) }
        }
    }
}

@Composable
private fun StepHeader(step: Int, total: Int, backLabel: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
            Icon(BackIcon, contentDescription = backLabel)
        }
        LinearProgressIndicator(
            progress = { if (total > 0) step / total.toFloat() else 0f },
            modifier = Modifier.weight(1f).height(6.dp),
            strokeCap = StrokeCap.Round,
        )
        Spacer(Modifier.width(12.dp))
        Text("$step/$total", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
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
internal fun OnboardingInterestPickerPreview() {
    val all = listOf("Design", "Travel", "Technology", "Food", "Music", "Photography", "Fitness", "Finance", "Books", "Gaming", "Science", "Movies")
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingInterestPicker(
            state = OnboardingInterestPickerState(
                interests = all.map { OnboardingInterest(it.lowercase(), it) },
                selected = setOf("design", "technology", "photography", "books"),
            ),
            onEvent = {},
        )
    }
}
