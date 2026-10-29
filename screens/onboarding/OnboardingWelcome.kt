package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Onboarding · Single welcome
 * One brand screen instead of a carousel: hero with your logo and tagline,
 * a sentence on what the app does, Get started and an existing-account
 * link. The hero runs behind the status bar.
 *
 * accent defaults to MaterialTheme.colorScheme.primary.
 */

data class OnboardingWelcomeState(
    val isLoading: Boolean = false,
)

sealed interface OnboardingWelcomeEvent {
    data object GetStarted : OnboardingWelcomeEvent
    data object SignIn : OnboardingWelcomeEvent
    data object OpenTerms : OnboardingWelcomeEvent
    data object OpenPrivacy : OnboardingWelcomeEvent
}

data class OnboardingWelcomeStrings(
    val appName: String = "[App name]",
    val tagline: String = "Your tagline,\nin two lines.",
    val body: String = "A sentence on what the app does for the user, not what it is.",
    val getStarted: String = "Get started",
    val signIn: String = "I already have an account",
    val legalPrefix: String = "By continuing you agree to the ",
    val terms: String = "Terms",
    val and: String = " and ",
    val privacy: String = "Privacy Policy",
    val legalSuffix: String = ".",
)

@Composable
fun OnboardingWelcome(
    state: OnboardingWelcomeState,
    onEvent: (OnboardingWelcomeEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingWelcomeStrings = OnboardingWelcomeStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    logo: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .heightIn(min = 360.dp)
                .background(accent, RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(28.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Box(
                Modifier.size(64.dp).background(onAccent, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(LocalContentColor provides accent) { logo?.invoke() }
            }
            Spacer(Modifier.height(24.dp))
            Text(strings.appName, style = MaterialTheme.typography.titleMedium, color = onAccent.copy(alpha = 0.85f))
            Spacer(Modifier.height(8.dp))
            Text(strings.tagline, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold, color = onAccent)
        }

        Column(
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onEvent(OnboardingWelcomeEvent.GetStarted) },
                enabled = !state.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                if (state.isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
                else Text(strings.getStarted)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { onEvent(OnboardingWelcomeEvent.SignIn) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text(strings.signIn) }
            Spacer(Modifier.height(20.dp))
            val link = TextLinkStyles(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold))
            Text(
                buildAnnotatedString {
                    append(strings.legalPrefix)
                    withLink(LinkAnnotation.Clickable("terms", link) { onEvent(OnboardingWelcomeEvent.OpenTerms) }) { append(strings.terms) }
                    append(strings.and)
                    withLink(LinkAnnotation.Clickable("privacy", link) { onEvent(OnboardingWelcomeEvent.OpenPrivacy) }) { append(strings.privacy) }
                    append(strings.legalSuffix)
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
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
internal fun OnboardingWelcomePreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingWelcome(state = OnboardingWelcomeState(), onEvent = {})
    }
}
