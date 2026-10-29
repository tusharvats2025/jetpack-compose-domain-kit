package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Onboarding · Consent and basics (healthcare)
 * A few optional medical basics, then explicit consent before the first
 * consultation: one required checkbox, one optional, never pre-ticked by
 * default. Continue unlocks only with the required consent.
 *
 * Record what was agreed and when on your server. The design uses
 * Public Sans; set it in your Typography to match.
 */

data class OnboardingHealthConsentState(
    val dateOfBirth: String = "",
    val allergies: List<String> = emptyList(),
    val noAllergies: Boolean = false,
    val shareWithDoctors: Boolean = false,
    val improveRecommendations: Boolean = false,
    val isSaving: Boolean = false,
)

sealed interface OnboardingHealthConsentEvent {
    data object Back : OnboardingHealthConsentEvent
    data class DateOfBirthChanged(val value: String) : OnboardingHealthConsentEvent
    data class NoAllergiesChanged(val value: Boolean) : OnboardingHealthConsentEvent
    data object AddAllergy : OnboardingHealthConsentEvent
    data class RemoveAllergy(val value: String) : OnboardingHealthConsentEvent
    data class ShareWithDoctorsChanged(val value: Boolean) : OnboardingHealthConsentEvent
    data class ImproveRecommendationsChanged(val value: Boolean) : OnboardingHealthConsentEvent
    data object HowWeProtect : OnboardingHealthConsentEvent
    data object Agree : OnboardingHealthConsentEvent
}

data class OnboardingHealthConsentStrings(
    val back: String = "Back",
    val title: String = "Before your first consultation",
    val body: String = "A few basics help your doctor. You can skip any you'd rather not share.",
    val dob: String = "Date of birth",
    val dobPlaceholder: String = "DD / MM / YYYY",
    val allergies: String = "Known allergies",
    val none: String = "None",
    val addAllergy: String = "+ Add allergy",
    val removeAllergy: (name: String) -> String = { "Remove $it" },
    val consentHeading: String = "Your consent",
    val shareStrong: String = "Share my health details with doctors I consult.",
    val shareRequired: String = " Required to book.",
    val improve: String = "Use my data to improve recommendations. Optional.",
    val protect: String = "How we protect your health data",
    val submit: String = "Agree and continue",
)

@OptIn(ExperimentalLayoutApi::class) // FlowRow on older Compose versions
@Composable
fun OnboardingHealthConsent(
    state: OnboardingHealthConsentState,
    onEvent: (OnboardingHealthConsentEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingHealthConsentStrings = OnboardingHealthConsentStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val colors = MaterialTheme.colorScheme
    val checkboxColors = CheckboxDefaults.colors(checkedColor = accent, checkmarkColor = onAccent)

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surfaceContainerLow)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
    ) {
        IconButton(onClick = { onEvent(OnboardingHealthConsentEvent.Back) }, modifier = Modifier.offset(x = (-12).dp)) {
            Icon(BackIcon, contentDescription = strings.back)
        }
        Spacer(Modifier.height(12.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))

        Column(
            Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(16.dp)).padding(16.dp),
        ) {
            OutlinedTextField(
                value = state.dateOfBirth,
                onValueChange = { v -> onEvent(OnboardingHealthConsentEvent.DateOfBirthChanged(v.filter { it.isDigit() || it == '/' || it == ' ' })) },
                label = { Text(strings.dob) },
                placeholder = { Text(strings.dobPlaceholder) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, focusedLabelColor = accent, cursorColor = accent),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.BirthDateFull },
            )
            Spacer(Modifier.height(16.dp))
            Text(strings.allergies, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.noAllergies,
                    onClick = { onEvent(OnboardingHealthConsentEvent.NoAllergiesChanged(!state.noAllergies)) },
                    label = { Text(strings.none) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent.copy(alpha = 0.14f)),
                )
                state.allergies.forEach { allergy ->
                    InputChip(
                        selected = false,
                        onClick = { onEvent(OnboardingHealthConsentEvent.RemoveAllergy(allergy)) },
                        label = { Text(allergy) },
                        trailingIcon = { Icon(CloseIcon, contentDescription = strings.removeAllergy(allergy), modifier = Modifier.size(16.dp)) },
                    )
                }
                AssistChip(
                    onClick = { onEvent(OnboardingHealthConsentEvent.AddAllergy) },
                    label = { Text(strings.addAllergy) },
                    enabled = !state.noAllergies,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            strings.consentHeading.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.2.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(16.dp)).padding(vertical = 4.dp)) {
            ConsentRow(
                checked = state.shareWithDoctors,
                onCheckedChange = { onEvent(OnboardingHealthConsentEvent.ShareWithDoctorsChanged(it)) },
                colors = checkboxColors,
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(strings.shareStrong) }
                        append(strings.shareRequired)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            HorizontalDivider(Modifier.padding(start = 60.dp), color = colors.outlineVariant)
            ConsentRow(
                checked = state.improveRecommendations,
                onCheckedChange = { onEvent(OnboardingHealthConsentEvent.ImproveRecommendationsChanged(it)) },
                colors = checkboxColors,
            ) { Text(strings.improve, style = MaterialTheme.typography.bodyMedium) }
        }
        TextButton(
            onClick = { onEvent(OnboardingHealthConsentEvent.HowWeProtect) },
            colors = ButtonDefaults.textButtonColors(contentColor = accent),
            contentPadding = PaddingValues(horizontal = 0.dp),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Icon(LockIcon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(strings.protect)
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onEvent(OnboardingHealthConsentEvent.Agree) },
            enabled = state.shareWithDoctors && !state.isSaving,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            if (state.isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
            else Text(strings.submit, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Whole row toggles the checkbox, so the touch target is the full width. */
@Composable
private fun ConsentRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: CheckboxColors,
    text: @Composable () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(checked = checked, onCheckedChange = null, colors = colors)
        Spacer(Modifier.width(16.dp))
        Box(Modifier.weight(1f)) { text() }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val CloseIcon = vectorIcon("Close", "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z")
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
internal fun OnboardingHealthConsentPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingHealthConsent(
            state = OnboardingHealthConsentState(noAllergies = true, shareWithDoctors = true),
            onEvent = {},
            accent = Color(0xFF0D5F78),
        )
    }
}
