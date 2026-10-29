package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/*
 * Onboarding · Goal and body (fitness)
 * Main goal, height and weight with unit toggles, and activity level on a
 * five-step slider. Values stay as text so users can type freely; parse
 * and validate them in your ViewModel.
 *
 * The design uses Sora; set it in your Typography to match.
 */

enum class OnboardingFitnessGoal { LoseWeight, BuildMuscle, StayHealthy }
enum class OnboardingHeightUnit { Cm, FtIn }
enum class OnboardingWeightUnit { Kg, Lb }

data class OnboardingFitnessGoalsState(
    val step: Int = 2,
    val totalSteps: Int = 5,
    val goal: OnboardingFitnessGoal? = null,
    val height: String = "",
    val heightUnit: OnboardingHeightUnit = OnboardingHeightUnit.Cm,
    val weight: String = "",
    val weightUnit: OnboardingWeightUnit = OnboardingWeightUnit.Kg,
    val activity: Int = 3, // 1..5
)

sealed interface OnboardingFitnessGoalsEvent {
    data object Back : OnboardingFitnessGoalsEvent
    data class PickGoal(val goal: OnboardingFitnessGoal) : OnboardingFitnessGoalsEvent
    data class HeightChanged(val value: String) : OnboardingFitnessGoalsEvent
    data class HeightUnitChanged(val unit: OnboardingHeightUnit) : OnboardingFitnessGoalsEvent
    data class WeightChanged(val value: String) : OnboardingFitnessGoalsEvent
    data class WeightUnitChanged(val unit: OnboardingWeightUnit) : OnboardingFitnessGoalsEvent
    data class ActivityChanged(val level: Int) : OnboardingFitnessGoalsEvent
    data object Continue : OnboardingFitnessGoalsEvent
}

data class OnboardingFitnessGoalsStrings(
    val back: String = "Back",
    val title: String = "Your goal and body",
    val body: String = "We use this to set your starting plan.",
    val goals: Map<OnboardingFitnessGoal, String> = mapOf(
        OnboardingFitnessGoal.LoseWeight to "Lose weight",
        OnboardingFitnessGoal.BuildMuscle to "Build muscle",
        OnboardingFitnessGoal.StayHealthy to "Stay healthy",
    ),
    val height: String = "Height",
    val weight: String = "Weight",
    val cm: String = "cm",
    val ftIn: String = "ft/in",
    val kg: String = "kg",
    val lb: String = "lb",
    val activity: String = "How active are you?",
    val activityLevels: List<String> = listOf("Mostly sitting", "Lightly", "Moderately", "Very", "Very active"),
    val privacy: String = "Stays on your device unless you turn on sync.",
    val submit: String = "Continue",
)

@OptIn(ExperimentalMaterial3Api::class) // SegmentedButton on older Material 3 versions
@Composable
fun OnboardingFitnessGoals(
    state: OnboardingFitnessGoalsState,
    onEvent: (OnboardingFitnessGoalsEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingFitnessGoalsStrings = OnboardingFitnessGoalsStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val colors = MaterialTheme.colorScheme
    val canContinue = state.goal != null && state.height.isNotBlank() && state.weight.isNotBlank()
    val levelLabel = strings.activityLevels.getOrElse(state.activity - 1) { "" }

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(OnboardingFitnessGoalsEvent.Back) }, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(BackIcon, contentDescription = strings.back)
            }
            LinearProgressIndicator(
                progress = { state.step / state.totalSteps.coerceAtLeast(1).toFloat() },
                modifier = Modifier.weight(1f).height(6.dp),
                color = accent,
                strokeCap = StrokeCap.Round,
            )
            Spacer(Modifier.width(12.dp))
            Text("${state.step}/${state.totalSteps}", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
        }
        Spacer(Modifier.height(24.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))

        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OnboardingFitnessGoal.entries.forEach { goal ->
                val selected = goal == state.goal
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) accent else colors.surfaceContainerLow)
                        .border(1.dp, if (selected) accent else colors.outlineVariant, RoundedCornerShape(16.dp))
                        .selectable(selected = selected, role = Role.RadioButton) { onEvent(OnboardingFitnessGoalsEvent.PickGoal(goal)) }
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        when (goal) {
                            OnboardingFitnessGoal.LoseWeight -> TrendDownIcon
                            OnboardingFitnessGoal.BuildMuscle -> DumbbellIcon
                            OnboardingFitnessGoal.StayHealthy -> HeartIcon
                        },
                        contentDescription = null,
                        tint = if (selected) onAccent else accent,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        strings.goals[goal].orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) onAccent else colors.onSurface,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        MeasureField(
            label = strings.height,
            value = state.height,
            onValueChange = { onEvent(OnboardingFitnessGoalsEvent.HeightChanged(it)) },
            suffix = if (state.heightUnit == OnboardingHeightUnit.Cm) strings.cm else strings.ftIn,
            keyboardType = KeyboardType.Number,
            accent = accent,
        ) {
            SingleChoiceSegmentedButtonRow {
                OnboardingHeightUnit.entries.forEachIndexed { i, unit ->
                    SegmentedButton(
                        selected = unit == state.heightUnit,
                        onClick = { onEvent(OnboardingFitnessGoalsEvent.HeightUnitChanged(unit)) },
                        shape = SegmentedButtonDefaults.itemShape(i, OnboardingHeightUnit.entries.size),
                        icon = {},
                    ) { Text(if (unit == OnboardingHeightUnit.Cm) strings.cm else strings.ftIn) }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        MeasureField(
            label = strings.weight,
            value = state.weight,
            onValueChange = { onEvent(OnboardingFitnessGoalsEvent.WeightChanged(it)) },
            suffix = if (state.weightUnit == OnboardingWeightUnit.Kg) strings.kg else strings.lb,
            keyboardType = KeyboardType.Decimal,
            accent = accent,
        ) {
            SingleChoiceSegmentedButtonRow {
                OnboardingWeightUnit.entries.forEachIndexed { i, unit ->
                    SegmentedButton(
                        selected = unit == state.weightUnit,
                        onClick = { onEvent(OnboardingFitnessGoalsEvent.WeightUnitChanged(unit)) },
                        shape = SegmentedButtonDefaults.itemShape(i, OnboardingWeightUnit.entries.size),
                        icon = {},
                    ) { Text(if (unit == OnboardingWeightUnit.Kg) strings.kg else strings.lb) }
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        Row(Modifier.fillMaxWidth()) {
            Text(strings.activity, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text(levelLabel, style = MaterialTheme.typography.titleSmall, color = accent)
        }
        Slider(
            value = state.activity.toFloat(),
            onValueChange = { onEvent(OnboardingFitnessGoalsEvent.ActivityChanged(it.roundToInt())) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
            modifier = Modifier.semantics {
                contentDescription = strings.activity
                stateDescription = levelLabel
            },
        )
        Row(Modifier.fillMaxWidth()) {
            Text(strings.activityLevels.first(), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(strings.activityLevels.last(), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(LockIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(strings.privacy, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onEvent(OnboardingFitnessGoalsEvent.Continue) },
            enabled = canContinue,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(strings.submit, fontWeight = FontWeight.SemiBold) }
    }
}

/** A number field with its unit, and the unit toggle beside it. */
@Composable
private fun MeasureField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
    keyboardType: KeyboardType,
    accent: Color,
    toggle: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = { v -> onValueChange(v.filter { it.isDigit() || it == '.' || it == '\'' || it == ' ' }) },
            label = { Text(label) },
            suffix = { Text(suffix) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, focusedLabelColor = accent, cursorColor = accent),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
            modifier = Modifier.keepAboveKeyboard().weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        Box(Modifier.width(132.dp)) { toggle() }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val TrendDownIcon = vectorIcon("TrendDown", "M16 18l2.29-2.29-4.88-4.88-4 4L2 7.41 3.41 6l6 6 4-4 6.3 6.29L22 12v6z")
private val DumbbellIcon = vectorIcon(
    "Dumbbell",
    "M20.57 14.86L22 13.43 20.57 12 17 15.57 8.43 7 12 3.43 10.57 2 9.14 3.43 7.71 2 5.57 4.14 4.14 2.71 2.71 4.14l1.43 1.43L2 7.71l1.43 1.43L2 10.57 3.43 12 7 8.43 15.57 17 12 20.57 13.43 22l1.43-1.43L16.29 22l2.14-2.14 1.43 1.43 1.43-1.43-1.43-1.43L22 16.29z",
)
private val HeartIcon = vectorIcon(
    "Heart",
    "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z",
)
private val LockIcon = vectorIcon(
    "Lock",
    "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z",
)

/**
 * Keeps a focused field visible above the keyboard. It scrolls the field into view
 * when it gains focus, and again as the keyboard opens and the screen shrinks.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.keepAboveKeyboard(): Modifier = composed {
    val requester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    val keyboardHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(focused, keyboardHeight) {
        if (focused) requester.bringIntoView()
    }
    bringIntoViewRequester(requester).onFocusChanged { focused = it.hasFocus }
}

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
internal fun OnboardingFitnessGoalsPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingFitnessGoals(
            state = OnboardingFitnessGoalsState(goal = OnboardingFitnessGoal.BuildMuscle, height = "172", weight = "68"),
            onEvent = {},
            accent = Color(0xFF2F6B3F),
            onAccent = Color.White,
        )
    }
}
