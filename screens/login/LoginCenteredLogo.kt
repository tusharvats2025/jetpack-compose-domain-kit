package com.yourapp.ui.screens.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Login · Centered logo
 * Logo, title, email and password, one primary button and an optional
 * provider button. Also covers the Error and Loading states.
 *
 * Copy this file into your project and change anything. It needs only
 * Compose Material 3 and takes colours and type from your MaterialTheme.
 */

/** Everything the screen shows. You own it; the screen only reads it. */
data class LoginCenteredLogoState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface LoginCenteredLogoEvent {
    data class EmailChanged(val value: String) : LoginCenteredLogoEvent
    data class PasswordChanged(val value: String) : LoginCenteredLogoEvent
    data object Submit : LoginCenteredLogoEvent
    data object ForgotPassword : LoginCenteredLogoEvent
    data object CreateAccount : LoginCenteredLogoEvent
}

/** All visible copy, so the screen can be localised. */
data class LoginCenteredLogoStrings(
    val title: String = "Welcome back",
    val subtitle: String = "Sign in to continue",
    val email: String = "Email",
    val password: String = "Password",
    val show: String = "Show",
    val hide: String = "Hide",
    val forgot: String = "Forgot password?",
    val submit: String = "Sign in",
    val submitting: String = "Signing in",
    val or: String = "or",
    val newHere: String = "New here?",
    val createAccount: String = "Create account",
    val loadingHint: String = "This usually takes a few seconds.",
)

@Composable
fun LoginCenteredLogo(
    state: LoginCenteredLogoState,
    onEvent: (LoginCenteredLogoEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginCenteredLogoStrings = LoginCenteredLogoStrings(),
    logo: (@Composable () -> Unit)? = null,
    providerButton: (@Composable () -> Unit)? = null,
) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val enabled = !state.isLoading

    ScreenColumn(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (logo != null) {
            logo()
            Spacer(Modifier.height(20.dp))
        }
        Text(strings.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            strings.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(LoginCenteredLogoEvent.EmailChanged(it)) },
            label = { Text(strings.email) },
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.keepAboveKeyboard()
                .fillMaxWidth()
                .semantics { contentType = ContentType.EmailAddress },
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(LoginCenteredLogoEvent.PasswordChanged(it)) },
            label = { Text(strings.password) },
            singleLine = true,
            enabled = enabled,
            isError = state.error != null,
            supportingText = state.error?.let { { Text(it) } },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }, enabled = enabled) {
                    Text(if (showPassword) strings.hide else strings.show)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focus.clearFocus()
                onEvent(LoginCenteredLogoEvent.Submit)
            }),
            modifier = Modifier.keepAboveKeyboard()
                .fillMaxWidth()
                .semantics { contentType = ContentType.Password },
        )
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = { onEvent(LoginCenteredLogoEvent.ForgotPassword) }, enabled = enabled) {
                Text(strings.forgot)
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { onEvent(LoginCenteredLogoEvent.Submit) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current,
                )
                Spacer(Modifier.width(10.dp))
                Text(strings.submitting)
            } else {
                Text(strings.submit)
            }
        }

        if (providerButton != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(Modifier.weight(1f))
                Text(
                    strings.or,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider(Modifier.weight(1f))
            }
            Box(Modifier.fillMaxWidth()) { providerButton() }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        if (state.isLoading) {
            Text(
                strings.loadingHint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(strings.newHere, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = { onEvent(LoginCenteredLogoEvent.CreateAccount) }) {
                    Text(strings.createAccount)
                }
            }
        }
    }
}

/**
 * Fills the screen, scrolls when the keyboard or a small phone needs it,
 * and lets Spacer(Modifier.weight(1f)) push the footer to the bottom.
 */
@Composable
private fun ScreenColumn(
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = horizontalAlignment,
        content = content,
    )
}

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
internal fun LoginCenteredLogoPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginCenteredLogo(
            state = LoginCenteredLogoState(),
            onEvent = {},
            providerButton = {
                OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text("Continue with Google")
                }
            },
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
internal fun LoginCenteredLogoErrorPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginCenteredLogo(
            state = LoginCenteredLogoState(
                email = "name@example.com",
                password = "secret",
                error = "Email or password is incorrect.",
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
internal fun LoginCenteredLogoLoadingPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginCenteredLogo(
            state = LoginCenteredLogoState(email = "name@example.com", password = "secret", isLoading = true),
            onEvent = {},
        )
    }
}
