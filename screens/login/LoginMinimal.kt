package com.yourapp.ui.screens.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Login · Minimal
 * Type-led and monochrome: an uppercase eyebrow, a big headline, underline
 * fields and one square button. The design uses Schibsted Grotesk; pass it
 * as fontFamily (Google Fonts downloadable font or a bundled font).
 *
 * Colours are fixed for this look and live in LoginMinimalColors.
 */

data class LoginMinimalState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface LoginMinimalEvent {
    data class EmailChanged(val value: String) : LoginMinimalEvent
    data class PasswordChanged(val value: String) : LoginMinimalEvent
    data object ResetPassword : LoginMinimalEvent
    data object Submit : LoginMinimalEvent
    data object CreateAccount : LoginMinimalEvent
}

data class LoginMinimalStrings(
    val eyebrow: String = "Log in",
    val title: String = "Good to\nsee you\nagain.",
    val email: String = "Email",
    val emailPlaceholder: String = "name@example.com",
    val password: String = "Password",
    val passwordPlaceholder: String = "Enter password",
    val show: String = "Show",
    val hide: String = "Hide",
    val reset: String = "Reset password",
    val submit: String = "Continue",
    val firstTime: String = "First time?",
    val createAccount: String = "Create an account",
)

data class LoginMinimalColors(
    val background: Color = Color(0xFFFFFFFF),
    val ink: Color = Color(0xFF111111),
    val muted: Color = Color(0xFF5A5A5A),
    val line: Color = Color(0xFF8A8A8A),
    val onInk: Color = Color(0xFFFFFFFF),
    val error: Color = Color(0xFFB3261E),
)

@Composable
fun LoginMinimal(
    state: LoginMinimalState,
    onEvent: (LoginMinimalEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginMinimalStrings = LoginMinimalStrings(),
    colors: LoginMinimalColors = LoginMinimalColors(),
    fontFamily: FontFamily = FontFamily.Default,
) {
    var showPassword by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val enabled = !state.isLoading
    val text = TextStyle(fontFamily = fontFamily, color = colors.ink)

    Column(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 32.dp, end = 32.dp, top = 64.dp, bottom = 24.dp),
    ) {
        Text(
            strings.eyebrow.uppercase(),
            style = text.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.6.sp, color = colors.muted),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            strings.title,
            style = text.copy(fontSize = 52.sp, lineHeight = 52.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-2).sp),
        )
        Spacer(Modifier.height(64.dp))

        UnderlineField(
            label = strings.email,
            value = state.email,
            onValueChange = { onEvent(LoginMinimalEvent.EmailChanged(it)) },
            placeholder = strings.emailPlaceholder,
            text = text,
            colors = colors,
            lineWidth = 2.dp,
            lineColor = colors.ink,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.semantics { contentType = ContentType.EmailAddress },
        )
        Spacer(Modifier.height(28.dp))
        UnderlineField(
            label = strings.password,
            value = state.password,
            onValueChange = { onEvent(LoginMinimalEvent.PasswordChanged(it)) },
            placeholder = strings.passwordPlaceholder,
            text = text,
            colors = colors,
            lineWidth = 1.dp,
            lineColor = if (state.error != null) colors.error else colors.line,
            enabled = enabled,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focus.clearFocus()
                onEvent(LoginMinimalEvent.Submit)
            }),
            modifier = Modifier.semantics { contentType = ContentType.Password },
            trailing = {
                TextButton(
                    onClick = { showPassword = !showPassword },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.ink),
                ) {
                    Text(
                        if (showPassword) strings.hide else strings.show,
                        style = text.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, textDecoration = TextDecoration.Underline),
                    )
                }
            },
        )
        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.error, style = text.copy(fontSize = 14.sp, color = colors.error))
        }
        TextButton(
            onClick = { onEvent(LoginMinimalEvent.ResetPassword) },
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = colors.ink),
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text(strings.reset, style = text.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, textDecoration = TextDecoration.Underline))
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(LoginMinimalEvent.Submit) },
            enabled = enabled,
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.ink, contentColor = colors.onInk),
            contentPadding = PaddingValues(horizontal = 24.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp),
        ) {
            Text(strings.submit, style = text.copy(fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.onInk))
            Spacer(Modifier.weight(1f))
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.onInk)
            } else {
                Icon(ForwardIcon, contentDescription = null)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(strings.firstTime, style = text.copy(fontSize = 14.sp, color = colors.muted))
            TextButton(
                onClick = { onEvent(LoginMinimalEvent.CreateAccount) },
                colors = ButtonDefaults.textButtonColors(contentColor = colors.ink),
            ) {
                Text(strings.createAccount, style = text.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline))
            }
        }
    }
}

/** Uppercase label, large text, a single line underneath. No box. */
@Composable
private fun UnderlineField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    text: TextStyle,
    colors: LoginMinimalColors,
    lineWidth: Dp,
    lineColor: Color,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            label.uppercase(),
            style = text.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.2.sp, color = colors.muted),
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = text.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium),
            cursorBrush = SolidColor(colors.ink),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = modifier.keepAboveKeyboard().fillMaxWidth(),
            decorationBox = { inner ->
                Row(Modifier.heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(placeholder, style = text.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium, color = colors.line))
                        }
                        inner()
                    }
                    trailing?.invoke()
                }
            },
        )
        HorizontalDivider(thickness = lineWidth, color = lineColor)
    }
}

private val ForwardIcon: ImageVector = ImageVector.Builder("Forward", 24.dp, 24.dp, 24f, 24f, autoMirror = true)
    .addPath(addPathNodes("M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z"), fill = SolidColor(Color.Black))
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
internal fun LoginMinimalPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginMinimal(state = LoginMinimalState(), onEvent = {})
    }
}
