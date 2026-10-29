package com.yourapp.ui.screens.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Login · Social-first
 * Provider buttons first, then a passwordless email link as the fallback.
 * You decide which providers appear and bring their brand icons.
 *
 * accent defaults to MaterialTheme.colorScheme.primary.
 */

/** One "Continue with …" row. icon is your brand asset; null leaves a blank slot. */
data class LoginSocialFirstProvider(
    val id: String,
    val label: String,
    val icon: (@Composable () -> Unit)? = null,
)

data class LoginSocialFirstState(
    val providers: List<LoginSocialFirstProvider> = emptyList(),
    val email: String = "",
    val isSendingLink: Boolean = false,
    val error: String? = null,
)

sealed interface LoginSocialFirstEvent {
    data class ProviderClick(val id: String) : LoginSocialFirstEvent
    data class EmailChanged(val value: String) : LoginSocialFirstEvent
    data object SendLink : LoginSocialFirstEvent
    data object OpenTerms : LoginSocialFirstEvent
    data object OpenPrivacy : LoginSocialFirstEvent
}

data class LoginSocialFirstStrings(
    val appName: String = "[App name]",
    val title: String = "Sign in with one tap",
    val subtitle: String = "Use an account you already have.",
    val divider: String = "or get a sign-in link by email",
    val email: String = "Email",
    val sendLink: String = "Send sign-in link",
    val legalPrefix: String = "By continuing you agree to the ",
    val terms: String = "Terms",
    val and: String = " and ",
    val privacy: String = "Privacy Policy",
    val legalSuffix: String = ".",
)

@Composable
fun LoginSocialFirst(
    state: LoginSocialFirstState,
    onEvent: (LoginSocialFirstEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginSocialFirstStrings = LoginSocialFirstStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    logo: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surfaceContainer)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(accent, CircleShape), contentAlignment = Alignment.Center) {
                CompositionLocalProvider(LocalContentColor provides onAccent) { logo?.invoke() }
            }
            Spacer(Modifier.width(10.dp))
            Text(strings.appName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(36.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(strings.subtitle, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.providers.forEach { provider ->
                OutlinedButton(
                    onClick = { onEvent(LoginSocialFirstEvent.ProviderClick(provider.id)) },
                    shape = shape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = colors.surface,
                        contentColor = colors.onSurface,
                    ),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) { provider.icon?.invoke() }
                    Spacer(Modifier.width(14.dp))
                    Text(provider.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(Modifier.weight(1f))
            Text(
                strings.divider,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            HorizontalDivider(Modifier.weight(1f))
        }

        Row(verticalAlignment = Alignment.Top) {
            OutlinedTextField(
                value = state.email,
                onValueChange = { onEvent(LoginSocialFirstEvent.EmailChanged(it)) },
                label = { Text(strings.email) },
                singleLine = true,
                enabled = !state.isSendingLink,
                shape = shape,
                isError = state.error != null,
                supportingText = state.error?.let { { Text(it) } },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    focusedLabelColor = accent,
                    cursorColor = accent,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onEvent(LoginSocialFirstEvent.SendLink) }),
                modifier = Modifier.keepAboveKeyboard().weight(1f).semantics { contentType = ContentType.EmailAddress },
            )
            Spacer(Modifier.width(8.dp))
            FilledIconButton(
                onClick = { onEvent(LoginSocialFirstEvent.SendLink) },
                enabled = !state.isSendingLink,
                shape = shape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = accent, contentColor = onAccent),
                modifier = Modifier.padding(top = 8.dp).size(56.dp),
            ) {
                if (state.isSendingLink) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
                } else {
                    Icon(SendIcon, contentDescription = strings.sendLink)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        val link = TextLinkStyles(SpanStyle(color = accent, fontWeight = FontWeight.Medium))
        Text(
            buildAnnotatedString {
                append(strings.legalPrefix)
                withLink(LinkAnnotation.Clickable("terms", link) { onEvent(LoginSocialFirstEvent.OpenTerms) }) {
                    append(strings.terms)
                }
                append(strings.and)
                withLink(LinkAnnotation.Clickable("privacy", link) { onEvent(LoginSocialFirstEvent.OpenPrivacy) }) {
                    append(strings.privacy)
                }
                append(strings.legalSuffix)
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private val SendIcon: ImageVector = ImageVector.Builder("Send", 24.dp, 24.dp, 24f, 24f, autoMirror = true)
    .addPath(addPathNodes("M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"), fill = SolidColor(Color.Black))
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
internal fun LoginSocialFirstPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginSocialFirst(
            state = LoginSocialFirstState(
                providers = listOf(
                    LoginSocialFirstProvider("google", "Continue with Google"),
                    LoginSocialFirstProvider("facebook", "Continue with Facebook"),
                    LoginSocialFirstProvider("phone", "Continue with phone number"),
                ),
            ),
            onEvent = {},
            accent = Color(0xFFB93A0A),
            onAccent = Color.White,
        )
    }
}
