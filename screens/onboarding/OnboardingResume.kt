package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Onboarding · Resume setup
 * For users who left onboarding part-way: shows the steps already done
 * and picks up at the next one. Skipping goes straight to the app.
 */

enum class OnboardingStepStatus { Done, Next, Todo }

data class OnboardingResumeStep(val label: String, val status: OnboardingStepStatus)

data class OnboardingResumeState(
    val steps: List<OnboardingResumeStep> = emptyList(),
)

sealed interface OnboardingResumeEvent {
    data object Continue : OnboardingResumeEvent
    data object SkipToApp : OnboardingResumeEvent
}

data class OnboardingResumeStrings(
    val eyebrow: String = "Welcome back",
    val title: (remaining: Int) -> String = { if (it == 1) "You're 1 step from\nfinishing setup" else "You're $it steps from\nfinishing setup" },
    val body: String = "We saved where you left off.",
    val done: String = "Done",
    val next: String = "Next",
    val todo: String = "To do",
    val submit: String = "Continue setup",
    val skip: String = "Skip, go to the app",
)

@Composable
fun OnboardingResume(
    state: OnboardingResumeState,
    onEvent: (OnboardingResumeEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingResumeStrings = OnboardingResumeStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val remaining = state.steps.count { it.status != OnboardingStepStatus.Done }

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 56.dp, bottom = 24.dp),
    ) {
        Text(strings.eyebrow, style = MaterialTheme.typography.labelLarge, color = colors.primary)
        Spacer(Modifier.height(10.dp))
        Text(strings.title(remaining), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))

        Column(Modifier.fillMaxWidth().background(colors.surfaceContainerLow, MaterialTheme.shapes.large)) {
            state.steps.forEachIndexed { i, step ->
                if (i > 0) HorizontalDivider(Modifier.padding(start = 60.dp), color = colors.outlineVariant)
                val statusLabel = when (step.status) {
                    OnboardingStepStatus.Done -> strings.done
                    OnboardingStepStatus.Next -> strings.next
                    OnboardingStepStatus.Todo -> strings.todo
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .semantics(mergeDescendants = true) { stateDescription = statusLabel },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .then(
                                when (step.status) {
                                    OnboardingStepStatus.Done -> Modifier.background(colors.primary, CircleShape)
                                    OnboardingStepStatus.Next -> Modifier.border(2.dp, colors.primary, CircleShape)
                                    OnboardingStepStatus.Todo -> Modifier.border(1.5.dp, colors.outlineVariant, CircleShape)
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (step.status == OnboardingStepStatus.Done) {
                            Icon(CheckIcon, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(16.dp))
                        } else {
                            Text(
                                "${i + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (step.status == OnboardingStepStatus.Next) colors.primary else colors.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Text(
                        step.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (step.status == OnboardingStepStatus.Next) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (step.status == OnboardingStepStatus.Todo) colors.onSurfaceVariant else colors.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    if (step.status != OnboardingStepStatus.Todo) {
                        Text(
                            statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (step.status == OnboardingStepStatus.Next) colors.primary else colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(OnboardingResumeEvent.Continue) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(strings.submit) }
        TextButton(
            onClick = { onEvent(OnboardingResumeEvent.SkipToApp) },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(48.dp),
        ) { Text(strings.skip) }
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
internal fun OnboardingResumePreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingResume(
            state = OnboardingResumeState(
                steps = listOf(
                    OnboardingResumeStep("Create account", OnboardingStepStatus.Done),
                    OnboardingResumeStep("Pick interests", OnboardingStepStatus.Done),
                    OnboardingResumeStep("Set up your profile", OnboardingStepStatus.Next),
                    OnboardingResumeStep("Turn on notifications", OnboardingStepStatus.Todo),
                ),
            ),
            onEvent = {},
        )
    }
}
