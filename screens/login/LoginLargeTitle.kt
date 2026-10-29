package com.yourapp.ui.screens.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Login · Large title
 * No logo. A back button, a large two-line title, filled fields, a
 * "keep me signed in" checkbox and the main button at the bottom.
 *
 * Copy this file into your project and change anything. It needs only
 * Compose Material 3 and takes colours and type from your MaterialTheme.
 */

data class LoginLargeTitleState(
    val email: String = "",
    val password: String = "",
    val keepSignedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface LoginLargeTitleEvent {
    data object Back : LoginLargeTitleEvent
    data class EmailChanged(val value: String) : LoginLargeTitleEvent
    data class PasswordChanged(val value: String) : LoginLargeTitleEvent
    data class KeepSignedInChanged(val value: Boolean) : LoginLargeTitleEvent
    data object ForgotPassword : LoginLargeTitleEvent
    data object Submit : LoginLargeTitleEvent
    data object SignUp : LoginLargeTitleEvent
}

data class LoginLargeTitleStrings(
    val back: String = "Back",
    val title: String = "Sign in to\nyour account",
    val subtitle: String = "Use the email you registered with.",
    val email: String = "Email",
    val password: String = "Password",
    val show: String = "Show",
    val hide: String = "Hide",
    val keepSignedIn: String = "Keep me signed in",
    val forgot: String = "Forgot?",
    val submit: String = "Sign in",
    val noAccount: String = "No account yet?",
    val signUp: String = "Sign up",
)

@Composable
fun LoginLargeTitle(
    state: LoginLargeTitleState,
    onEvent: (LoginLargeTitleEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginLargeTitleStrings = LoginLargeTitleStrings(),
    showBack: Boolean = true,
) {
    var showPassword by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val enabled = !state.isLoading
    val colors = MaterialTheme.colorScheme

    ScreenColumn(modifier) {
        if (showBack) {
            IconButton(
                onClick = { onEvent(LoginLargeTitleEvent.Back) },
                modifier = Modifier.offset(x = (-12).dp),
            ) {
                Icon(BackIcon, contentDescription = strings.back)
            }
        }
        Spacer(Modifier.height(40.dp))
        Text(strings.title, style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(12.dp))
        Text(strings.subtitle, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(36.dp))

        TextField(
            value = state.email,
            onValueChange = { onEvent(LoginLargeTitleEvent.EmailChanged(it)) },
            label = { Text(strings.email) },
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.EmailAddress },
        )
        Spacer(Modifier.height(16.dp))
        TextField(
            value = state.password,
            onValueChange = { onEvent(LoginLargeTitleEvent.PasswordChanged(it)) },
            label = { Text(strings.password) },
            singleLine = true,
            enabled = enabled,
            isError = state.error != null,
            supportingText = state.error?.let { { Text(it) } },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) strings.hide else strings.show)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focus.clearFocus()
                onEvent(LoginLargeTitleEvent.Submit)
            }),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.Password },
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .toggleable(
                        value = state.keepSignedIn,
                        enabled = enabled,
                        role = Role.Checkbox,
                        onValueChange = { onEvent(LoginLargeTitleEvent.KeepSignedInChanged(it)) },
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // onCheckedChange = null: the whole row is the touch target.
                Checkbox(checked = state.keepSignedIn, onCheckedChange = null, enabled = enabled)
                Spacer(Modifier.width(10.dp))
                Text(strings.keepSignedIn, style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = { onEvent(LoginLargeTitleEvent.ForgotPassword) }, enabled = enabled) {
                Text(strings.forgot)
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(LoginLargeTitleEvent.Submit) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
            } else {
                Text(strings.submit)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(strings.noAccount, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            TextButton(onClick = { onEvent(LoginLargeTitleEvent.SignUp) }) { Text(strings.signUp) }
        }
    }
}

@Composable
private fun ScreenColumn(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp),
        content = content,
    )
}

/** Material "arrow back" path, so the file needs no icons dependency. */
private val BackIcon: ImageVector = ImageVector.Builder("Back", 24.dp, 24.dp, 24f, 24f, autoMirror = true)
    .addPath(addPathNodes("M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"), fill = SolidColor(Color.Black))
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

@Preview(showBackground = true)
@Composable
internal fun LoginLargeTitlePreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginLargeTitle(state = LoginLargeTitleState(email = "name@example.com"), onEvent = {})
    }
}
