package com.yourapp.ui.screens.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Login · Neo-brutalist
 * Loud background, thick black borders, hard offset shadows, mono labels.
 * The design pairs Space Grotesk (display) with Space Mono (labels); pass
 * them as displayFont and monoFont.
 */

data class LoginBrutalistState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface LoginBrutalistEvent {
    data class EmailChanged(val value: String) : LoginBrutalistEvent
    data class PasswordChanged(val value: String) : LoginBrutalistEvent
    data object ForgotPassword : LoginBrutalistEvent
    data object Submit : LoginBrutalistEvent
    data object SignUp : LoginBrutalistEvent
}

data class LoginBrutalistStrings(
    val appName: String = "[APP NAME]",
    val title: String = "LOG\nIN.",
    val email: String = "EMAIL",
    val emailPlaceholder: String = "name@example.com",
    val password: String = "PASSWORD",
    val passwordPlaceholder: String = "Enter password",
    val show: String = "SHOW",
    val hide: String = "HIDE",
    val forgot: String = "FORGOT IT?",
    val submit: String = "LET ME IN",
    val noAccount: String = "NO ACCOUNT?",
    val signUp: String = "SIGN UP",
)

data class LoginBrutalistColors(
    val background: Color = Color(0xFFFFD60A),
    val ink: Color = Color(0xFF0A0A0A),
    val paper: Color = Color(0xFFFFFFFF),
    val error: Color = Color(0xFFD00000),
)

@Composable
fun LoginBrutalist(
    state: LoginBrutalistState,
    onEvent: (LoginBrutalistEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginBrutalistStrings = LoginBrutalistStrings(),
    colors: LoginBrutalistColors = LoginBrutalistColors(),
    displayFont: FontFamily = FontFamily.Default,
    monoFont: FontFamily = FontFamily.Monospace,
) {
    var showPassword by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val enabled = !state.isLoading
    val display = TextStyle(fontFamily = displayFont, color = colors.ink)
    val mono = TextStyle(fontFamily = monoFont, color = colors.ink, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.sp)

    Column(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 24.dp),
    ) {
        Text(
            strings.appName,
            style = mono.copy(fontSize = 18.sp, color = colors.paper),
            modifier = Modifier
                .rotate(-2f)
                .background(colors.ink)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )
        Spacer(Modifier.height(32.dp))
        Text(
            strings.title,
            style = display.copy(fontSize = 56.sp, lineHeight = 54.sp, fontWeight = FontWeight.Bold, letterSpacing = (-2).sp),
        )
        Spacer(Modifier.height(32.dp))

        HardShadow(colors.ink, offset = 6.dp) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.paper)
                    .border(3.dp, colors.ink)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                BoxField(
                    label = strings.email,
                    value = state.email,
                    onValueChange = { onEvent(LoginBrutalistEvent.EmailChanged(it)) },
                    placeholder = strings.emailPlaceholder,
                    mono = mono,
                    display = display,
                    colors = colors,
                    enabled = enabled,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.semantics { contentType = ContentType.EmailAddress },
                )
                BoxField(
                    label = strings.password,
                    value = state.password,
                    onValueChange = { onEvent(LoginBrutalistEvent.PasswordChanged(it)) },
                    placeholder = strings.passwordPlaceholder,
                    mono = mono,
                    display = display,
                    colors = colors,
                    enabled = enabled,
                    isError = state.error != null,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focus.clearFocus()
                        onEvent(LoginBrutalistEvent.Submit)
                    }),
                    modifier = Modifier.semantics { contentType = ContentType.Password },
                    trailing = {
                        TextButton(onClick = { showPassword = !showPassword }, shape = RectangleShape) {
                            Text(if (showPassword) strings.hide else strings.show, style = mono)
                        }
                    },
                )
                if (state.error != null) {
                    Text(state.error.uppercase(), style = mono.copy(color = colors.error))
                }
                TextButton(
                    onClick = { onEvent(LoginBrutalistEvent.ForgotPassword) },
                    shape = RectangleShape,
                    contentPadding = PaddingValues(0.dp),
                ) { Text(strings.forgot, style = mono.copy(fontSize = 13.sp)) }
            }
        }

        Spacer(Modifier.height(28.dp))
        HardShadow(colors.paper, offset = 6.dp, modifier = Modifier.padding(end = 6.dp)) {
            Button(
                onClick = { onEvent(LoginBrutalistEvent.Submit) },
                enabled = enabled,
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.ink,
                    contentColor = colors.paper,
                    disabledContainerColor = colors.ink,
                    disabledContentColor = colors.paper.copy(alpha = 0.7f),
                ),
                contentPadding = PaddingValues(horizontal = 20.dp),
                modifier = Modifier.fillMaxWidth().height(60.dp).border(3.dp, colors.ink),
            ) {
                Text(
                    strings.submit,
                    style = display.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colors.paper),
                    modifier = Modifier.weight(1f),
                )
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 3.dp, color = colors.paper)
                } else {
                    Icon(ForwardIcon, contentDescription = null)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        HorizontalDivider(thickness = 3.dp, color = colors.ink)
        Row(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(strings.noAccount, style = mono.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), modifier = Modifier.weight(1f))
            TextButton(onClick = { onEvent(LoginBrutalistEvent.SignUp) }, shape = RectangleShape) {
                Text(strings.signUp, style = mono.copy(fontSize = 13.sp))
            }
        }
    }
}

/** Draws a solid block behind content, shifted down-right. */
@Composable
private fun HardShadow(
    color: Color,
    offset: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier) {
        Box(
            Modifier
                .matchParentSize()
                .offset(x = offset, y = offset)
                .background(color),
        )
        content()
    }
}

@Composable
private fun BoxField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    mono: TextStyle,
    display: TextStyle,
    colors: LoginBrutalistColors,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = mono)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = display.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
            cursorBrush = SolidColor(colors.ink),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            modifier = modifier.keepAboveKeyboard()
                .fillMaxWidth()
                .semantics { contentDescription = label },
            decorationBox = { inner ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(3.dp, if (isError) colors.error else colors.ink)
                        .padding(start = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(placeholder, style = display.copy(fontSize = 16.sp, color = colors.ink.copy(alpha = 0.45f)))
                        }
                        inner()
                    }
                    trailing?.invoke()
                }
            },
        )
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

@Preview
@Composable
internal fun LoginBrutalistPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginBrutalist(state = LoginBrutalistState(), onEvent = {})
    }
}
