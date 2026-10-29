package com.yourapp.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Login · PIN unlock (fintech)
 * For returning users: avatar, greeting, PIN dots and a custom keypad
 * with fingerprint and delete. The screen never shows the digits.
 *
 * Keep the PIN out of logs and saved state; hold it only as long as you
 * need it to verify. Starting the BiometricPrompt is yours: handle the
 * Biometric event. The design uses Manrope; set it in your Typography.
 */

data class LoginPinUnlockState(
    val initials: String = "AB",
    val name: String = "[Name]",
    val enteredCount: Int = 0,
    val pinLength: Int = 4,
    val showBiometric: Boolean = true,
    val isVerifying: Boolean = false,
    val error: String? = null,
)

sealed interface LoginPinUnlockEvent {
    data class Digit(val value: Int) : LoginPinUnlockEvent
    data object Delete : LoginPinUnlockEvent
    data object Biometric : LoginPinUnlockEvent
    data object ForgotPin : LoginPinUnlockEvent
    data object SwitchAccount : LoginPinUnlockEvent
}

data class LoginPinUnlockStrings(
    val greeting: (name: String) -> String = { "Welcome back, $it" },
    val prompt: (length: Int) -> String = { "Enter your $it-digit PIN" },
    val progress: (entered: Int, length: Int) -> String = { e, l -> "$e of $l digits entered" },
    val biometric: String = "Unlock with fingerprint",
    val delete: String = "Delete last digit",
    val forgot: String = "Forgot PIN?",
    val switchAccount: String = "Not you? Switch account",
)

data class LoginPinUnlockColors(
    val background: Color = Color(0xFF0A1A2F),
    val key: Color = Color(0xFF15294A),
    val keyBorder: Color = Color(0xFF2C456F),
    val text: Color = Color(0xFFFFFFFF),
    val muted: Color = Color(0xFFA8B6CC),
    val dotEmpty: Color = Color(0xFF5C7299),
    val accent: Color = Color(0xFF5EE0B5),
    val error: Color = Color(0xFFFFB4AB),
)

@Composable
fun LoginPinUnlock(
    state: LoginPinUnlockState,
    onEvent: (LoginPinUnlockEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginPinUnlockStrings = LoginPinUnlockStrings(),
    colors: LoginPinUnlockColors = LoginPinUnlockColors(),
    avatar: (@Composable () -> Unit)? = null,
) {
    val canType = !state.isVerifying && state.enteredCount < state.pinLength

    Column(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 32.dp, end = 32.dp, top = 40.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(60.dp)
                .background(colors.key, CircleShape)
                .border(1.5.dp, colors.keyBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (avatar != null) avatar()
            else Text(state.initials, color = colors.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        Text(strings.greeting(state.name), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = colors.text)
        Spacer(Modifier.height(4.dp))
        Text(
            state.error ?: strings.prompt(state.pinLength),
            style = MaterialTheme.typography.bodyLarge,
            color = if (state.error != null) colors.error else colors.muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = state.error ?: strings.prompt(state.pinLength)
                liveRegion = LiveRegionMode.Polite
            },
        )
        Spacer(Modifier.height(28.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = strings.progress(state.enteredCount, state.pinLength)
            },
        ) {
            repeat(state.pinLength) { i ->
                val filled = i < state.enteredCount
                Box(
                    Modifier
                        .size(16.dp)
                        .then(
                            if (filled) Modifier.background(if (state.error != null) colors.error else colors.accent, CircleShape)
                            else Modifier.border(2.dp, if (state.error != null) colors.error else colors.dotEmpty, CircleShape),
                        ),
                )
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        if (state.isVerifying) {
            CircularProgressIndicator(color = colors.accent, modifier = Modifier.padding(bottom = 24.dp))
        }

        val keyColors = ButtonDefaults.buttonColors(
            containerColor = colors.key,
            contentColor = colors.text,
            disabledContainerColor = colors.key.copy(alpha = 0.5f),
            disabledContentColor = colors.text.copy(alpha = 0.5f),
        )
        val rows = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { d -> DigitKey(d, canType, keyColors) { onEvent(LoginPinUnlockEvent.Digit(d)) } }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(76.dp), contentAlignment = Alignment.Center) {
                    if (state.showBiometric) {
                        IconButton(
                            onClick = { onEvent(LoginPinUnlockEvent.Biometric) },
                            enabled = !state.isVerifying,
                            modifier = Modifier.size(76.dp),
                        ) { Icon(FingerprintIcon, contentDescription = strings.biometric, tint = colors.accent, modifier = Modifier.size(30.dp)) }
                    }
                }
                DigitKey(0, canType, keyColors) { onEvent(LoginPinUnlockEvent.Digit(0)) }
                IconButton(
                    onClick = { onEvent(LoginPinUnlockEvent.Delete) },
                    enabled = !state.isVerifying && state.enteredCount > 0,
                    modifier = Modifier.size(76.dp),
                ) { Icon(BackspaceIcon, contentDescription = strings.delete, tint = colors.text) }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(
                onClick = { onEvent(LoginPinUnlockEvent.ForgotPin) },
                colors = ButtonDefaults.textButtonColors(contentColor = colors.accent),
            ) { Text(strings.forgot, fontWeight = FontWeight.SemiBold) }
            TextButton(
                onClick = { onEvent(LoginPinUnlockEvent.SwitchAccount) },
                colors = ButtonDefaults.textButtonColors(contentColor = colors.accent),
            ) { Text(strings.switchAccount, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun DigitKey(digit: Int, enabled: Boolean, colors: ButtonColors, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = colors,
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier.size(76.dp),
    ) {
        Text(digit.toString(), fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
    }
}

private val BackspaceIcon: ImageVector = ImageVector.Builder("Backspace", 24.dp, 24.dp, 24f, 24f, autoMirror = true)
    .addPath(
        addPathNodes("M22 3H7c-.69 0-1.23.35-1.59.88L0 12l5.41 8.11c.36.53.9.89 1.59.89h15c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-3 12.59L17.59 17 14 13.41 10.41 17 9 15.59 12.59 12 9 8.41 10.41 7 14 10.59 17.59 7 19 8.41 15.41 12 19 15.59z"),
        fill = SolidColor(Color.Black),
    )
    .build()

private val FingerprintIcon: ImageVector = ImageVector.Builder("Fingerprint", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes(
            "M17.81 4.47c-.08 0-.16-.02-.23-.06C15.66 3.42 14 3 12.01 3c-1.98 0-3.86.47-5.57 1.41-.24.13-.54.04-.68-.2-.13-.24-.04-.55.2-.68C7.82 2.52 9.86 2 12.01 2c2.13 0 3.99.47 6.03 1.52.25.13.34.43.21.67-.09.18-.26.28-.44.28zM3.5 9.72c-.1 0-.2-.03-.29-.09-.23-.16-.28-.47-.12-.7.99-1.4 2.25-2.5 3.75-3.27C9.98 4.04 14 4.03 17.15 5.65c1.5.77 2.76 1.86 3.75 3.25.16.22.11.54-.12.7-.23.16-.54.11-.7-.12-.9-1.26-2.04-2.25-3.39-2.94-2.87-1.47-6.54-1.47-9.4.01-1.36.7-2.5 1.7-3.4 2.96-.08.14-.23.21-.39.21zm6.25 12.07c-.13 0-.26-.05-.35-.15-.87-.87-1.34-1.43-2.01-2.64-.69-1.23-1.05-2.73-1.05-4.34 0-2.97 2.54-5.39 5.66-5.39s5.66 2.42 5.66 5.39c0 .28-.22.5-.5.5s-.5-.22-.5-.5c0-2.42-2.09-4.39-4.66-4.39-2.57 0-4.66 1.97-4.66 4.39 0 1.44.32 2.77.93 3.85.64 1.15 1.08 1.64 1.85 2.42.19.2.19.51 0 .71-.11.1-.24.15-.37.15zm7.17-1.85c-1.19 0-2.24-.3-3.1-.89-1.49-1.01-2.38-2.65-2.38-4.39 0-.28.22-.5.5-.5s.5.22.5.5c0 1.41.72 2.74 1.94 3.56.71.48 1.54.71 2.54.71.24 0 .64-.03 1.04-.1.27-.05.53.13.58.41.05.27-.13.53-.41.58-.57.11-1.07.12-1.21.12zM14.91 22c-.04 0-.09-.01-.13-.02-1.59-.44-2.63-1.03-3.72-2.1-1.4-1.39-2.17-3.24-2.17-5.22 0-1.62 1.38-2.94 3.08-2.94 1.7 0 3.08 1.32 3.08 2.94 0 1.07.93 1.94 2.08 1.94s2.08-.87 2.08-1.94c0-3.77-3.25-6.83-7.25-6.83-2.84 0-5.44 1.58-6.61 4.03-.39.81-.59 1.76-.59 2.8 0 .78.07 2.01.67 3.61.1.26-.03.55-.29.64-.26.1-.55-.04-.64-.29-.49-1.31-.73-2.61-.73-3.96 0-1.2.23-2.29.68-3.24 1.33-2.79 4.28-4.6 7.51-4.6 4.55 0 8.25 3.51 8.25 7.83 0 1.62-1.38 2.94-3.08 2.94s-3.08-1.32-3.08-2.94c0-1.07-.93-1.94-2.08-1.94s-2.08.87-2.08 1.94c0 1.71.66 3.31 1.87 4.51.95.94 1.86 1.46 3.27 1.85.27.07.42.35.35.61-.05.23-.26.38-.47.38z",
        ),
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

@Preview
@Composable
internal fun LoginPinUnlockPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginPinUnlock(state = LoginPinUnlockState(enteredCount = 2), onEvent = {})
    }
}
