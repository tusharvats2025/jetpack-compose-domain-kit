package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Onboarding · Quiz step
 * One question per screen with large option cards and a progress bar.
 * Reuse it for every question: change the state, keep the screen.
 */

data class OnboardingQuizOption(val id: String, val title: String, val subtitle: String? = null)

data class OnboardingQuizStepState(
    val step: Int = 2,
    val totalSteps: Int = 5,
    val section: String = "Your routine",
    val question: String = "How often do you want to practise?",
    val options: List<OnboardingQuizOption> = emptyList(),
    val selectedId: String? = null,
)

sealed interface OnboardingQuizStepEvent {
    data object Back : OnboardingQuizStepEvent
    data class Select(val id: String) : OnboardingQuizStepEvent
    data object Continue : OnboardingQuizStepEvent
}

data class OnboardingQuizStepStrings(
    val back: String = "Back",
    val submit: String = "Continue",
)

@Composable
fun OnboardingQuizStep(
    state: OnboardingQuizStepState,
    onEvent: (OnboardingQuizStepEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingQuizStepStrings = OnboardingQuizStepStrings(),
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
    ) {
        StepHeader(state.step, state.totalSteps, strings.back) { onEvent(OnboardingQuizStepEvent.Back) }
        Spacer(Modifier.height(28.dp))
        Text(state.section, style = MaterialTheme.typography.labelLarge, color = colors.primary)
        Spacer(Modifier.height(8.dp))
        Text(state.question, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(28.dp))

        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.options.forEach { option ->
                val selected = option.id == state.selectedId
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(if (selected) colors.primaryContainer.copy(alpha = 0.5f) else colors.surfaceContainerLow)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) colors.primary else colors.outlineVariant,
                            shape = MaterialTheme.shapes.large,
                        )
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onEvent(OnboardingQuizStepEvent.Select(option.id)) },
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected, onClick = null)
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(option.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (option.subtitle != null) {
                            Text(option.subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(OnboardingQuizStepEvent.Continue) },
            enabled = state.selectedId != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(strings.submit) }
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

private val BackIcon: ImageVector = ImageVector.Builder("Back", 24.dp, 24.dp, 24f, 24f, autoMirror = true)
    .addPath(addPathNodes("M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"), fill = SolidColor(Color.Black))
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
internal fun OnboardingQuizStepPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingQuizStep(
            state = OnboardingQuizStepState(
                options = listOf(
                    OnboardingQuizOption("casual", "Casual", "5 minutes a day"),
                    OnboardingQuizOption("regular", "Regular", "15 minutes a day"),
                    OnboardingQuizOption("serious", "Serious", "30 minutes a day"),
                    OnboardingQuizOption("unsure", "Not sure yet", "We'll suggest something"),
                ),
                selectedId = "regular",
            ),
            onEvent = {},
        )
    }
}
