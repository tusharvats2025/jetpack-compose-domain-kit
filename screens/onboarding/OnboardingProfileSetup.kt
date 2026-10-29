package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Onboarding · Profile setup
 * Photo, display name and a username with a live availability check.
 *
 * The check is yours: debounce UsernameChanged in your ViewModel, call
 * your API, then set usernameStatus. Picking the photo is yours too
 * (PickVisualMedia); pass the loaded image in the photo slot.
 */

enum class OnboardingUsernameStatus { Idle, Checking, Available, Taken, Invalid }

data class OnboardingProfileSetupState(
    val step: Int = 3,
    val totalSteps: Int = 4,
    val name: String = "",
    val username: String = "",
    val usernameStatus: OnboardingUsernameStatus = OnboardingUsernameStatus.Idle,
    val hasPhoto: Boolean = false,
    val isSaving: Boolean = false,
)

sealed interface OnboardingProfileSetupEvent {
    data object Back : OnboardingProfileSetupEvent
    data object PickPhoto : OnboardingProfileSetupEvent
    data class NameChanged(val value: String) : OnboardingProfileSetupEvent
    data class UsernameChanged(val value: String) : OnboardingProfileSetupEvent
    data object Continue : OnboardingProfileSetupEvent
    data object Skip : OnboardingProfileSetupEvent
}

data class OnboardingProfileSetupStrings(
    val back: String = "Back",
    val title: String = "Set up your profile",
    val subtitle: String = "This is how others will see you.",
    val addPhoto: String = "Add photo",
    val changePhoto: String = "Change photo",
    val name: String = "Name",
    val username: String = "Username",
    val checking: String = "Checking…",
    val available: String = "Available",
    val taken: String = "Already taken. Try another.",
    val invalid: String = "Use letters, numbers and underscores.",
    val submit: String = "Continue",
    val skip: String = "Skip for now",
)

@Composable
fun OnboardingProfileSetup(
    state: OnboardingProfileSetupState,
    onEvent: (OnboardingProfileSetupEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingProfileSetupStrings = OnboardingProfileSetupStrings(),
    photo: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val status = state.usernameStatus
    val isBad = status == OnboardingUsernameStatus.Taken || status == OnboardingUsernameStatus.Invalid
    val canContinue = state.name.isNotBlank() && status == OnboardingUsernameStatus.Available && !state.isSaving

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
    ) {
        StepHeader(state.step, state.totalSteps, strings.back) { onEvent(OnboardingProfileSetupEvent.Back) }
        Spacer(Modifier.height(24.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(strings.subtitle, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))

        Box(Modifier.align(Alignment.CenterHorizontally)) {
            Box(
                Modifier
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                if (state.hasPhoto && photo != null) photo()
                else Icon(PersonIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(56.dp))
            }
            FilledIconButton(
                onClick = { onEvent(OnboardingProfileSetupEvent.PickPhoto) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp)
                    .border(3.dp, colors.surface, CircleShape),
            ) {
                Icon(CameraIcon, contentDescription = if (state.hasPhoto) strings.changePhoto else strings.addPhoto, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { onEvent(OnboardingProfileSetupEvent.NameChanged(it)) },
            label = { Text(strings.name) },
            singleLine = true,
            enabled = !state.isSaving,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.PersonFullName },
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = state.username,
            onValueChange = { onEvent(OnboardingProfileSetupEvent.UsernameChanged(it.trim().lowercase())) },
            label = { Text(strings.username) },
            prefix = { Text("@") },
            singleLine = true,
            enabled = !state.isSaving,
            isError = isBad,
            trailingIcon = when (status) {
                OnboardingUsernameStatus.Checking -> { { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) } }
                OnboardingUsernameStatus.Available -> { { Icon(CheckIcon, contentDescription = null, tint = colors.primary) } }
                else -> null
            },
            supportingText = when (status) {
                OnboardingUsernameStatus.Checking -> { { Text(strings.checking) } }
                OnboardingUsernameStatus.Available -> { { Text(strings.available, color = colors.primary) } }
                OnboardingUsernameStatus.Taken -> { { Text(strings.taken) } }
                OnboardingUsernameStatus.Invalid -> { { Text(strings.invalid) } }
                OnboardingUsernameStatus.Idle -> null
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.NewUsername },
        )

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(OnboardingProfileSetupEvent.Continue) },
            enabled = canContinue,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            if (state.isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
            else Text(strings.submit)
        }
        TextButton(
            onClick = { onEvent(OnboardingProfileSetupEvent.Skip) },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(48.dp),
        ) { Text(strings.skip) }
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
private val PersonIcon = vectorIcon("Person", "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z")
private val CameraIcon = vectorIcon(
    "Camera",
    "M12 15.2a3.2 3.2 0 1 0 0-6.4 3.2 3.2 0 0 0 0 6.4zM9 2L7.17 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2h-3.17L15 2H9zm3 15c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z",
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
internal fun OnboardingProfileSetupPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingProfileSetup(
            state = OnboardingProfileSetupState(name = "Asha Rao", username = "asha", usernameStatus = OnboardingUsernameStatus.Available),
            onEvent = {},
        )
    }
}
