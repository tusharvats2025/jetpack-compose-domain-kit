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
 * Login · Workspace SSO (enterprise)
 * Email-first: the user types a work email and you decide where it goes
 * (password, magic link or the company's identity provider). A separate
 * SSO button and a workspace URL option cover the other routes.
 *
 * Squarer corners and a quieter palette suit B2B tools. The design uses
 * IBM Plex Sans; set it in your Typography to match.
 */

data class LoginWorkspaceSsoState(
    val email: String = "",
    val isChecking: Boolean = false,
    val error: String? = null,
)

sealed interface LoginWorkspaceSsoEvent {
    data class EmailChanged(val value: String) : LoginWorkspaceSsoEvent
    data object Continue : LoginWorkspaceSsoEvent
    data object SignInWithSso : LoginWorkspaceSsoEvent
    data object EnterWorkspaceUrl : LoginWorkspaceSsoEvent
}

data class LoginWorkspaceSsoStrings(
    val productName: String = "[Product name]",
    val title: String = "Sign in to your workspace",
    val subtitle: String = "Use your work email. We'll send you to your company's sign-in page if it has one.",
    val email: String = "Work email",
    val emailPlaceholder: String = "you@company.com",
    val submit: String = "Continue",
    val or: String = "or",
    val sso: String = "Sign in with SSO",
    val workspaceUrl: String = "Enter a workspace URL instead",
    val adminNote: String = "Accounts are created by your organisation. Can't get in? Contact your admin.",
)

@Composable
fun LoginWorkspaceSso(
    state: LoginWorkspaceSsoState,
    onEvent: (LoginWorkspaceSsoEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginWorkspaceSsoStrings = LoginWorkspaceSsoStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    logo: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(6.dp)
    val enabled = !state.isChecking

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surfaceContainerLow)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).background(accent, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                CompositionLocalProvider(LocalContentColor provides onAccent) { logo?.invoke() }
            }
            Spacer(Modifier.width(10.dp))
            Text(strings.productName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(40.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(strings.subtitle, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))

        OutlinedCard(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = colors.surface),
        ) {
            Column(Modifier.padding(20.dp)) {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onEvent(LoginWorkspaceSsoEvent.EmailChanged(it)) },
                    label = { Text(strings.email) },
                    placeholder = { Text(strings.emailPlaceholder) },
                    singleLine = true,
                    enabled = enabled,
                    shape = shape,
                    isError = state.error != null,
                    supportingText = state.error?.let { { Text(it) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        focusedLabelColor = accent,
                        cursorColor = accent,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { onEvent(LoginWorkspaceSsoEvent.Continue) }),
                    modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.EmailAddress },
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = { onEvent(LoginWorkspaceSsoEvent.Continue) },
                    enabled = enabled,
                    shape = shape,
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    if (state.isChecking) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
                    } else {
                        Text(strings.submit, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f))
            Text(strings.or, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp))
            HorizontalDivider(Modifier.weight(1f))
        }
        OutlinedButton(
            onClick = { onEvent(LoginWorkspaceSsoEvent.SignInWithSso) },
            enabled = enabled,
            shape = shape,
            colors = ButtonDefaults.outlinedButtonColors(containerColor = colors.surface, contentColor = colors.onSurface),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Icon(KeyIcon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(strings.sso)
        }
        TextButton(
            onClick = { onEvent(LoginWorkspaceSsoEvent.EnterWorkspaceUrl) },
            colors = ButtonDefaults.textButtonColors(contentColor = accent),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).heightIn(min = 48.dp),
        ) { Text(strings.workspaceUrl) }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surfaceContainerHigh, RoundedCornerShape(10.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(InfoIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(strings.adminNote, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}

private val KeyIcon: ImageVector = ImageVector.Builder("Key", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes("M12.65 10C11.83 7.67 9.61 6 7 6c-3.31 0-6 2.69-6 6s2.69 6 6 6c2.61 0 4.83-1.67 5.65-4H17v4h4v-4h2v-4H12.65zM7 14c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z"),
        fill = SolidColor(Color.Black),
    )
    .build()

private val InfoIcon: ImageVector = ImageVector.Builder("Info", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"),
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
internal fun LoginWorkspaceSsoPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginWorkspaceSso(state = LoginWorkspaceSsoState(), onEvent = {}, accent = Color(0xFF2B45C4))
    }
}
