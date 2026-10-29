package com.yourapp.ui.screens.login

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
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Login · Forgot password
 * One field and one button. On success, navigate to LoginCheckEmail.
 *
 * For privacy, show the same success whether or not the email has an
 * account, so the screen can't be used to find out who is registered.
 */

data class LoginForgotPasswordState(
    val email: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
)

sealed interface LoginForgotPasswordEvent {
    data object Back : LoginForgotPasswordEvent
    data class EmailChanged(val value: String) : LoginForgotPasswordEvent
    data object Send : LoginForgotPasswordEvent
    data object BackToSignIn : LoginForgotPasswordEvent
}

data class LoginForgotPasswordStrings(
    val back: String = "Back",
    val title: String = "Forgot your password?",
    val body: String = "Enter the email you signed up with and we'll send you a link to reset it.",
    val email: String = "Email",
    val send: String = "Send reset link",
    val backToSignIn: String = "Back to sign in",
)

@Composable
fun LoginForgotPassword(
    state: LoginForgotPasswordState,
    onEvent: (LoginForgotPasswordEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginForgotPasswordStrings = LoginForgotPasswordStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val canSend = state.email.isNotBlank() && !state.isSending

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
    ) {
        IconButton(onClick = { onEvent(LoginForgotPasswordEvent.Back) }, modifier = Modifier.offset(x = (-12).dp)) {
            Icon(BackIcon, contentDescription = strings.back)
        }
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier.size(56.dp).background(colors.primaryContainer, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(LockIcon, contentDescription = null, tint = colors.onPrimaryContainer)
        }
        Spacer(Modifier.height(24.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(LoginForgotPasswordEvent.EmailChanged(it)) },
            label = { Text(strings.email) },
            singleLine = true,
            enabled = !state.isSending,
            isError = state.error != null,
            supportingText = state.error?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (canSend) onEvent(LoginForgotPasswordEvent.Send) }),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.EmailAddress },
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onEvent(LoginForgotPasswordEvent.Send) },
            enabled = canSend,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (state.isSending) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
            } else {
                Text(strings.send)
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        TextButton(
            onClick = { onEvent(LoginForgotPasswordEvent.BackToSignIn) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Icon(BackIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(strings.backToSignIn)
        }
    }
}

private val BackIcon: ImageVector = ImageVector.Builder("Back", 24.dp, 24.dp, 24f, 24f, autoMirror = true)
    .addPath(addPathNodes("M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"), fill = SolidColor(Color.Black))
    .build()

private val LockIcon: ImageVector = ImageVector.Builder("Lock", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes("M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"),
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

@Preview(showBackground = true)
@Composable
internal fun LoginForgotPasswordPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginForgotPassword(state = LoginForgotPasswordState(email = "name@example.com"), onEvent = {})
    }
}
