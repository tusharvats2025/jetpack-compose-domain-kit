package com.yourapp.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Login · Check your email
 * Shown after LoginForgotPassword (or a magic-link sign-in). Opens the
 * mail app, lets the user resend after a countdown and points to spam.
 *
 * Opening the mail app is yours, for example:
 *   Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_EMAIL)
 * Tick resendInSeconds down in your ViewModel.
 */

data class LoginCheckEmailState(
    val email: String = "name@example.com",
    val expiresInMinutes: Int = 30,
    val resendInSeconds: Int = 0,
    val isResending: Boolean = false,
)

sealed interface LoginCheckEmailEvent {
    data object OpenEmailApp : LoginCheckEmailEvent
    data object Resend : LoginCheckEmailEvent
    data object UseDifferentEmail : LoginCheckEmailEvent
}

data class LoginCheckEmailStrings(
    val title: String = "Check your email",
    val sentTo: String = "We sent a reset link to ",
    val expires: (minutes: Int) -> String = { ". It expires in $it minutes." },
    val openEmailApp: String = "Open email app",
    val resend: String = "Resend link",
    val resendIn: (seconds: Int) -> String = { "Resend link in %d:%02d".format(it / 60, it % 60) },
    val helpPrefix: String = "Can't find it? Check your spam folder, or ",
    val differentEmail: String = "try a different email",
    val helpSuffix: String = ".",
)

@Composable
fun LoginCheckEmail(
    state: LoginCheckEmailState,
    onEvent: (LoginCheckEmailEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginCheckEmailStrings = LoginCheckEmailStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val canResend = state.resendInSeconds <= 0 && !state.isResending

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 72.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(112.dp).background(colors.primaryContainer.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.size(76.dp).background(colors.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(MailIcon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(34.dp))
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Text(
            buildAnnotatedString {
                append(strings.sentTo)
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.onSurface)) { append(state.email) }
                append(strings.expires(state.expiresInMinutes))
            },
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(LoginCheckEmailEvent.OpenEmailApp) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text(strings.openEmailApp) }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { onEvent(LoginCheckEmailEvent.Resend) },
            enabled = canResend,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            when {
                state.isResending -> CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                state.resendInSeconds > 0 -> Text(strings.resendIn(state.resendInSeconds))
                else -> Text(strings.resend)
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Text(
            buildAnnotatedString {
                append(strings.helpPrefix)
                withLink(
                    LinkAnnotation.Clickable(
                        "different-email",
                        TextLinkStyles(SpanStyle(color = colors.primary, fontWeight = FontWeight.SemiBold)),
                    ) { onEvent(LoginCheckEmailEvent.UseDifferentEmail) },
                ) { append(strings.differentEmail) }
                append(strings.helpSuffix)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

private val MailIcon: ImageVector = ImageVector.Builder("Mail", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes("M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"),
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

@Preview(showBackground = true)
@Composable
internal fun LoginCheckEmailPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginCheckEmail(state = LoginCheckEmailState(resendInSeconds = 45), onEvent = {})
    }
}
