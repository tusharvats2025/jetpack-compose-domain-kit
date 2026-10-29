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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Login · Sign up
 * Name, email and a new password with a live strength meter and rule
 * checklist, a terms checkbox, and an optional provider button.
 *
 * The rules are yours: pass them in state. loginSignUpRules() below is a
 * sensible default you can replace.
 */

/** One password requirement and whether the current password meets it. */
data class LoginSignUpRule(val label: String, val met: Boolean)

/** A default rule set. Call it from your ViewModel whenever the password changes. */
fun loginSignUpRules(password: String): List<LoginSignUpRule> = listOf(
    LoginSignUpRule("8+ characters", password.length >= 8),
    LoginSignUpRule("A number", password.any { it.isDigit() }),
    LoginSignUpRule("A symbol", password.any { !it.isLetterOrDigit() }),
)

data class LoginSignUpState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val rules: List<LoginSignUpRule> = loginSignUpRules(""),
    val acceptedTerms: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val error: String? = null,
)

sealed interface LoginSignUpEvent {
    data object Back : LoginSignUpEvent
    data class NameChanged(val value: String) : LoginSignUpEvent
    data class EmailChanged(val value: String) : LoginSignUpEvent
    data class PasswordChanged(val value: String) : LoginSignUpEvent
    data class TermsChanged(val value: Boolean) : LoginSignUpEvent
    data object OpenTerms : LoginSignUpEvent
    data object OpenPrivacy : LoginSignUpEvent
    data object Submit : LoginSignUpEvent
    data object SignIn : LoginSignUpEvent
}

data class LoginSignUpStrings(
    val back: String = "Back",
    val title: String = "Create your account",
    val subtitle: String = "It takes less than a minute.",
    val name: String = "Full name",
    val email: String = "Email",
    val password: String = "Password",
    val show: String = "Show",
    val hide: String = "Hide",
    val strength: (met: Int, total: Int) -> String = { met, total -> "Password strength: $met of $total" },
    val termsPrefix: String = "I agree to the ",
    val terms: String = "Terms",
    val and: String = " and ",
    val privacy: String = "Privacy Policy",
    val submit: String = "Create account",
    val or: String = "or",
    val haveAccount: String = "Already have an account?",
    val signIn: String = "Sign in",
)

@OptIn(ExperimentalLayoutApi::class) // FlowRow; harmless once it is stable in your Compose version
@Composable
fun LoginSignUp(
    state: LoginSignUpState,
    onEvent: (LoginSignUpEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginSignUpStrings = LoginSignUpStrings(),
    providerButton: (@Composable () -> Unit)? = null,
) {
    var showPassword by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    val enabled = !state.isLoading
    val metCount = state.rules.count { it.met }
    val canSubmit = enabled && state.acceptedTerms && state.rules.all { it.met } &&
    state.name.isNotBlank() && state.email.isNotBlank()

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
    ) {
        IconButton(onClick = { onEvent(LoginSignUpEvent.Back) }, modifier = Modifier.offset(x = (-12).dp)) {
            Icon(BackIcon, contentDescription = strings.back)
        }
        Spacer(Modifier.height(12.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(strings.subtitle, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = state.name,
            onValueChange = { onEvent(LoginSignUpEvent.NameChanged(it)) },
            label = { Text(strings.name) },
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.PersonFullName },
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = { onEvent(LoginSignUpEvent.EmailChanged(it)) },
            label = { Text(strings.email) },
            singleLine = true,
            enabled = enabled,
            isError = state.emailError != null,
            supportingText = state.emailError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.EmailAddress },
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(LoginSignUpEvent.PasswordChanged(it)) },
            label = { Text(strings.password) },
            singleLine = true,
            enabled = enabled,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) strings.hide else strings.show)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.NewPassword },
        )
        Spacer(Modifier.height(10.dp))
        StrengthMeter(
            met = metCount,
            total = state.rules.size,
            description = strings.strength(metCount, state.rules.size),
        )
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            state.rules.forEach { rule ->
                Text(
                    (if (rule.met) "✓ " else "○ ") + rule.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (rule.met) colors.primary else colors.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = state.acceptedTerms,
                onCheckedChange = { onEvent(LoginSignUpEvent.TermsChanged(it)) },
                enabled = enabled,
            )
            val link = TextLinkStyles(SpanStyle(color = colors.primary, fontWeight = FontWeight.SemiBold))
            Text(
                buildAnnotatedString {
                    append(strings.termsPrefix)
                    withLink(LinkAnnotation.Clickable("terms", link) { onEvent(LoginSignUpEvent.OpenTerms) }) { append(strings.terms) }
                    append(strings.and)
                    withLink(LinkAnnotation.Clickable("privacy", link) { onEvent(LoginSignUpEvent.OpenPrivacy) }) { append(strings.privacy) }
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.error, style = MaterialTheme.typography.bodyMedium, color = colors.error)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onEvent(LoginSignUpEvent.Submit) },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
            } else {
                Text(strings.submit)
            }
        }

        if (providerButton != null) {
            Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f))
                Text(strings.or, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp))
                HorizontalDivider(Modifier.weight(1f))
            }
            Box(Modifier.fillMaxWidth()) { providerButton() }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(strings.haveAccount, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            TextButton(onClick = { onEvent(LoginSignUpEvent.SignIn) }) { Text(strings.signIn) }
        }
    }
}

/** Segmented bar: one segment per rule, filled for each rule met. */
@Composable
private fun StrengthMeter(met: Int, total: Int, description: String) {
    val colors = MaterialTheme.colorScheme
    val fill = when {
        total == 0 -> colors.outlineVariant
        met == total -> colors.primary
        met * 2 >= total -> colors.tertiary
        else -> colors.error
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(total) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(if (i < met) fill else colors.outlineVariant, RoundedCornerShape(2.dp)),
            )
        }
    }
}

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
internal fun LoginSignUpPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginSignUp(
            state = LoginSignUpState(
                name = "Asha Rao",
                email = "name@example.com",
                password = "summer2026",
                rules = loginSignUpRules("summer2026"),
                acceptedTerms = true,
            ),
            onEvent = {},
        )
    }
}
