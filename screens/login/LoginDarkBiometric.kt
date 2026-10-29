package com.yourapp.ui.screens.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Login · Dark scheme with biometrics
 * Always dark, with a bright accent, a fingerprint button next to
 * "Sign in" and a create-account card at the bottom. Hide the fingerprint
 * button with showBiometric = false when the device has none enrolled.
 *
 * Starting the BiometricPrompt is yours: handle the Biometric event.
 * The design uses Sora; set it in your Typography to match.
 */

data class LoginDarkBiometricState(
    val email: String = "",
    val password: String = "",
    val showBiometric: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface LoginDarkBiometricEvent {
    data class EmailChanged(val value: String) : LoginDarkBiometricEvent
    data class PasswordChanged(val value: String) : LoginDarkBiometricEvent
    data object ForgotPassword : LoginDarkBiometricEvent
    data object Submit : LoginDarkBiometricEvent
    data object Biometric : LoginDarkBiometricEvent
    data object CreateAccount : LoginDarkBiometricEvent
}

data class LoginDarkBiometricStrings(
    val title: String = "Welcome back",
    val subtitle: String = "Unlock [App name] to continue.",
    val email: String = "Email",
    val password: String = "Password",
    val show: String = "Show",
    val hide: String = "Hide",
    val forgot: String = "Forgot password?",
    val submit: String = "Sign in",
    val biometric: String = "Sign in with fingerprint",
    val newHere: String = "New to [App name]?",
    val createAccount: String = "Create account",
)

data class LoginDarkBiometricColors(
    val background: Color = Color(0xFF0E1113),
    val surface: Color = Color(0xFF1A1F23),
    val outline: Color = Color(0xFF3A4248),
    val text: Color = Color(0xFFF1F4F2),
    val muted: Color = Color(0xFFA9B2AE),
    val accent: Color = Color(0xFFC8F03C),
    val onAccent: Color = Color(0xFF0E1113),
    val error: Color = Color(0xFFFFB4AB),
)

@Composable
fun LoginDarkBiometric(
    state: LoginDarkBiometricState,
    onEvent: (LoginDarkBiometricEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginDarkBiometricStrings = LoginDarkBiometricStrings(),
    colors: LoginDarkBiometricColors = LoginDarkBiometricColors(),
    logo: (@Composable () -> Unit)? = null,
) {
    var showPassword by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val enabled = !state.isLoading
    val fieldShape = RoundedCornerShape(16.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = colors.text,
        unfocusedTextColor = colors.text,
        disabledTextColor = colors.muted,
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
        disabledContainerColor = colors.surface,
        focusedBorderColor = colors.accent,
        unfocusedBorderColor = colors.outline,
        disabledBorderColor = colors.outline,
        focusedLabelColor = colors.accent,
        unfocusedLabelColor = colors.muted,
        cursorColor = colors.accent,
        errorBorderColor = colors.error,
        errorLabelColor = colors.error,
        errorSupportingTextColor = colors.error,
        errorContainerColor = colors.surface,
        errorTextColor = colors.text,
    )

    Column(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
    ) {
        Box(
            Modifier.size(60.dp).background(colors.accent, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides colors.onAccent) { logo?.invoke() }
        }
        Spacer(Modifier.height(32.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = colors.text)
        Spacer(Modifier.height(8.dp))
        Text(strings.subtitle, style = MaterialTheme.typography.bodyLarge, color = colors.muted)
        Spacer(Modifier.height(36.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(LoginDarkBiometricEvent.EmailChanged(it)) },
            label = { Text(strings.email) },
            singleLine = true,
            enabled = enabled,
            shape = fieldShape,
            colors = fieldColors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.EmailAddress },
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(LoginDarkBiometricEvent.PasswordChanged(it)) },
            label = { Text(strings.password) },
            singleLine = true,
            enabled = enabled,
            shape = fieldShape,
            colors = fieldColors,
            isError = state.error != null,
            supportingText = state.error?.let { { Text(it) } },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(
                    onClick = { showPassword = !showPassword },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.accent),
                ) { Text(if (showPassword) strings.hide else strings.show) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focus.clearFocus()
                onEvent(LoginDarkBiometricEvent.Submit)
            }),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.Password },
        )
        TextButton(
            onClick = { onEvent(LoginDarkBiometricEvent.ForgotPassword) },
            colors = ButtonDefaults.textButtonColors(contentColor = colors.accent),
            modifier = Modifier.align(Alignment.End),
        ) { Text(strings.forgot) }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onEvent(LoginDarkBiometricEvent.Submit) },
                enabled = enabled,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent,
                    disabledContainerColor = colors.accent.copy(alpha = 0.5f),
                    disabledContentColor = colors.onAccent,
                ),
                modifier = Modifier.weight(1f).height(58.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.onAccent)
                } else {
                    Text(strings.submit, fontWeight = FontWeight.SemiBold)
                }
            }
            if (state.showBiometric) {
                OutlinedIconButton(
                    onClick = { onEvent(LoginDarkBiometricEvent.Biometric) },
                    enabled = enabled,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.5.dp, colors.outline),
                    colors = IconButtonDefaults.outlinedIconButtonColors(
                        containerColor = colors.surface,
                        contentColor = colors.accent,
                    ),
                    modifier = Modifier.size(58.dp),
                ) { Icon(FingerprintIcon, contentDescription = strings.biometric) }
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(16.dp))
                .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(strings.newHere, style = MaterialTheme.typography.bodyMedium, color = colors.muted, modifier = Modifier.weight(1f))
            TextButton(
                onClick = { onEvent(LoginDarkBiometricEvent.CreateAccount) },
                colors = ButtonDefaults.textButtonColors(contentColor = colors.accent),
            ) { Text(strings.createAccount, fontWeight = FontWeight.SemiBold) }
        }
    }
}

private val FingerprintIcon: ImageVector = ImageVector.Builder("Fingerprint", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes(
            "M17.81 4.47c-.08 0-.16-.02-.23-.06C15.66 3.42 14 3 12.01 3c-1.98 0-3.86.47-5.57 1.41-.24.13-.54.04-.68-.2-.13-.24-.04-.55.2-.68C7.82 2.52 9.86 2 12.01 2c2.13 0 3.99.47 6.03 1.52.25.13.34.43.21.67-.09.18-.26.28-.44.28zM3.5 9.72c-.1 0-.2-.03-.29-.09-.23-.16-.28-.47-.12-.7.99-1.4 2.25-2.5 3.75-3.27C9.98 4.04 14 4.03 17.15 5.65c1.5.77 2.76 1.86 3.75 3.25.16.22.11.54-.12.7-.23.16-.54.11-.7-.12-.9-1.26-2.04-2.25-3.39-2.94-2.87-1.47-6.54-1.47-9.4.01-1.36.7-2.5 1.7-3.4 2.96-.08.14-.23.21-.39.21zm6.25 12.07c-.13 0-.26-.05-.35-.15-.87-.87-1.34-1.43-2.01-2.64-.69-1.23-1.05-2.73-1.05-4.34 0-2.97 2.54-5.39 5.66-5.39s5.66 2.42 5.66 5.39c0 .28-.22.5-.5.5s-.5-.22-.5-.5c0-2.42-2.09-4.39-4.66-4.39-2.57 0-4.66 1.97-4.66 4.39 0 1.44.32 2.77.93 3.85.64 1.15 1.08 1.64 1.85 2.42.19.2.19.51 0 .71-.11.1-.24.15-.37.15zm7.17-1.85c-1.19 0-2.24-.3-3.1-.89-1.49-1.01-2.38-2.65-2.38-4.39 0-.28.22-.5.5-.5s.5.22.5.5c0 1.41.72 2.74 1.94 3.56.71.48 1.54.71 2.54.71.24 0 .64-.03 1.04-.1.27-.05.53.13.58.41.05.27-.13.53-.41.58-.57.11-1.07.12-1.21.12zM14.91 22c-.04 0-.09-.01-.13-.02-1.59-.44-2.63-1.03-3.72-2.1-1.4-1.39-2.17-3.24-2.17-5.22 0-1.62 1.38-2.94 3.08-2.94 1.7 0 3.08 1.32 3.08 2.94 0 1.07.93 1.94 2.08 1.94s2.08-.87 2.08-1.94c0-3.77-3.25-6.83-7.25-6.83-2.84 0-5.44 1.58-6.61 4.03-.39.81-.59 1.76-.59 2.8 0 .78.07 2.01.67 3.61.1.26-.03.55-.29.64-.26.1-.55-.04-.64-.29-.49-1.31-.73-2.61-.73-3.96 0-1.2.23-2.29.68-3.24 1.33-2.79 4.28-4.6 7.51-4.6 4.55 0 8.25 3.51 8.25 7.83 0 1.62-1.38 2.94-3.08 2.94s-3.08-1.32-3.08-2.94c0-1.07-.93-1.94-2.08-1.94s-2.08.87-2.08 1.94c0 1.71.66 3.31 1.87 4.51.95.94 1.86 1.46 3.27 1.85.27.07.42.35.35.61-.05.23-.26.38-.47.38z",
        ),
        fill = SolidColor(Color.Black),
    )
    .build()

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

@Preview
@Composable
internal fun LoginDarkBiometricPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginDarkBiometric(state = LoginDarkBiometricState(), onEvent = {})
    }
}
