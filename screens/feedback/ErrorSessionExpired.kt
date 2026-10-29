package com.yourapp.ui.screens.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Error · Session expired
 * A blocking sheet over the current screen: the user must sign in again,
 * and is told their unsaved work is safe. The content behind is dimmed and
 * not interactive.
 *
 * Put the screen the user was on in the background slot, or leave it empty.
 */

data class ErrorSessionExpiredState(
    val email: String = "name@example.com",
    val hasSavedDraft: Boolean = true,
)

sealed interface ErrorSessionExpiredEvent {
    data object SignInAgain : ErrorSessionExpiredEvent
    data object UseDifferentAccount : ErrorSessionExpiredEvent
}

data class ErrorSessionExpiredStrings(
    val heading: String = "Please sign in again",
    val body: String = "You've been signed out for your security.",
    val draftSaved: String = " Your unsent draft is saved and will be here when you're back.",
    val signIn: (email: String) -> String = { "Sign in as $it" },
    val different: String = "Use a different account",
)

@Composable
fun ErrorSessionExpired(
    state: ErrorSessionExpiredState,
    onEvent: (ErrorSessionExpiredEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ErrorSessionExpiredStrings = ErrorSessionExpiredStrings(),
    background: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Box(modifier.fillMaxSize().background(colors.surface)) {
        background()
        // Scrim: dims and blocks the screen behind.
        Box(Modifier.fillMaxSize().background(colors.scrim.copy(alpha = 0.45f)))
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(colors.surfaceContainerLow, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .semantics { paneTitle = strings.heading }
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(width = 32.dp, height = 4.dp).background(colors.outlineVariant, CircleShape))
            Spacer(Modifier.height(24.dp))
            Box(Modifier.size(64.dp).background(colors.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                Icon(LockClockIcon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(strings.heading, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                strings.body + if (state.hasSavedDraft) strings.draftSaved else "",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = { onEvent(ErrorSessionExpiredEvent.SignInAgain) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(strings.signIn(state.email))
            }
            TextButton(onClick = { onEvent(ErrorSessionExpiredEvent.UseDifferentAccount) }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Text(strings.different)
            }
        }
    }
}

private val LockClockIcon: ImageVector = ImageVector.Builder("Lock", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes("M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"),
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
internal fun ErrorSessionExpiredPreview() {
    MaterialTheme(colorScheme = PreviewColors) { ErrorSessionExpired(state = ErrorSessionExpiredState(), onEvent = {}) }
}
