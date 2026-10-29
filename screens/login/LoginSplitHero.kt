package com.yourapp.ui.screens.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
 * Login · Split hero
 * A brand-coloured top with your logo and a headline, and a white sheet
 * with the form. The top runs behind the status bar.
 *
 * accent defaults to MaterialTheme.colorScheme.primary. The design uses
 * Plus Jakarta Sans; set it in your Typography to match.
 */

data class LoginSplitHeroState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface LoginSplitHeroEvent {
    data class EmailChanged(val value: String) : LoginSplitHeroEvent
    data class PasswordChanged(val value: String) : LoginSplitHeroEvent
    data object ForgotPassword : LoginSplitHeroEvent
    data object Submit : LoginSplitHeroEvent
    data object SignUp : LoginSplitHeroEvent
}

data class LoginSplitHeroStrings(
    val appName: String = "[App name]",
    val headline: String = "Pick up where\nyou left off",
    val formTitle: String = "Log in",
    val email: String = "Email",
    val password: String = "Password",
    val show: String = "Show",
    val hide: String = "Hide",
    val forgot: String = "Forgot password?",
    val submit: String = "Log in",
    val noAccount: String = "Don't have an account?",
    val signUp: String = "Sign up",
)

@Composable
fun LoginSplitHero(
    state: LoginSplitHeroState,
    onEvent: (LoginSplitHeroEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginSplitHeroStrings = LoginSplitHeroStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    logo: (@Composable () -> Unit)? = null,
) {
    var showPassword by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val enabled = !state.isLoading
    val colors = MaterialTheme.colorScheme
    val fieldShape = RoundedCornerShape(14.dp)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accent,
        cursorColor = accent,
        focusedLabelColor = accent,
        unfocusedContainerColor = colors.surfaceContainerLow,
        focusedContainerColor = colors.surfaceContainerLow,
    )

    Box(modifier.fillMaxSize().background(accent)) {
        // Decorative rings. Purely visual, so no semantics.
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 70.dp, y = (-60).dp)
                .size(260.dp)
                .border(36.dp, onAccent.copy(alpha = 0.10f), CircleShape),
        )
        Box(
            Modifier
                .offset(x = (-50).dp, y = 170.dp)
                .size(150.dp)
                .background(onAccent.copy(alpha = 0.08f), CircleShape),
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // Hero
            Column(
                Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 28.dp, end = 28.dp, top = 48.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (logo != null) {
                        Box(
                            Modifier.size(52.dp).background(Color.White, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center,
                        ) { logo() }
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        strings.appName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = onAccent,
                    )
                }
                Text(
                    strings.headline,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = onAccent,
                )
            }

            // Sheet
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(colors.surface, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                    .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp),
            ) {
                Text(strings.formTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(20.dp))
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onEvent(LoginSplitHeroEvent.EmailChanged(it)) },
                    label = { Text(strings.email) },
                    singleLine = true,
                    enabled = enabled,
                    shape = fieldShape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.EmailAddress },
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onEvent(LoginSplitHeroEvent.PasswordChanged(it)) },
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
                            colors = ButtonDefaults.textButtonColors(contentColor = accent),
                        ) { Text(if (showPassword) strings.hide else strings.show) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focus.clearFocus()
                        onEvent(LoginSplitHeroEvent.Submit)
                    }),
                    modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.Password },
                )
                TextButton(
                    onClick = { onEvent(LoginSplitHeroEvent.ForgotPassword) },
                    colors = ButtonDefaults.textButtonColors(contentColor = accent),
                    modifier = Modifier.align(Alignment.End),
                ) { Text(strings.forgot, fontWeight = FontWeight.SemiBold) }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onEvent(LoginSplitHeroEvent.Submit) },
                    enabled = enabled,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
                    } else {
                        Text(strings.submit, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(strings.noAccount, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    TextButton(
                        onClick = { onEvent(LoginSplitHeroEvent.SignUp) },
                        colors = ButtonDefaults.textButtonColors(contentColor = accent),
                    ) { Text(strings.signUp, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
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
internal fun LoginSplitHeroPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginSplitHero(
            state = LoginSplitHeroState(),
            onEvent = {},
            accent = Color(0xFF0B5D56),
            onAccent = Color.White,
        )
    }
}
