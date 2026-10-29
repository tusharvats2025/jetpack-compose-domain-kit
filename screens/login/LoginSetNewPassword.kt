package com.yourapp.ui.screens.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
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
 * Login · Set new password
 * Reached from the reset link. New password with a strength meter, a
 * confirm field that shows when the two match, and an option to sign out
 * of other devices.
 *
 * Strength is yours to compute (0 to 4) so it matches your server rules.
 */

data class LoginSetNewPasswordState(
    val email: String = "name@example.com",
    val password: String = "",
    val confirm: String = "",
    val strength: Int = 0,
    val signOutOthers: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
)

sealed interface LoginSetNewPasswordEvent {
    data object Back : LoginSetNewPasswordEvent
    data class PasswordChanged(val value: String) : LoginSetNewPasswordEvent
    data class ConfirmChanged(val value: String) : LoginSetNewPasswordEvent
    data class SignOutOthersChanged(val value: Boolean) : LoginSetNewPasswordEvent
    data object Save : LoginSetNewPasswordEvent
}

data class LoginSetNewPasswordStrings(
    val back: String = "Back",
    val title: String = "Set a new password",
    val forEmail: (email: String) -> String = { "For $it" },
    val password: String = "New password",
    val confirm: String = "Confirm password",
    val showPassword: String = "Show password",
    val hidePassword: String = "Hide password",
    val strengthLabels: List<String> = listOf("Too weak", "Weak", "Fair", "Good", "Strong password"),
    val match: String = "Passwords match",
    val noMatch: String = "Passwords don't match yet",
    val signOutOthers: String = "Sign out of all other devices",
    val save: String = "Save and sign in",
)

@Composable
fun LoginSetNewPassword(
    state: LoginSetNewPasswordState,
    onEvent: (LoginSetNewPasswordEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginSetNewPasswordStrings = LoginSetNewPasswordStrings(),
) {
    var show by remember { mutableStateOf(false) }
    val focus = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    val strength = state.strength.coerceIn(0, 4)
    val matches = state.confirm.isNotEmpty() && state.confirm == state.password
    val canSave = matches && strength >= 3 && !state.isSaving
    val strengthColor = when (strength) {
        4 -> colors.primary
        3 -> colors.tertiary
        else -> colors.error
    }

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
    ) {
        IconButton(onClick = { onEvent(LoginSetNewPasswordEvent.Back) }, modifier = Modifier.offset(x = (-12).dp)) {
            Icon(BackIcon, contentDescription = strings.back)
        }
        Spacer(Modifier.height(12.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(strings.forEmail(state.email), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { onEvent(LoginSetNewPasswordEvent.PasswordChanged(it)) },
            label = { Text(strings.password) },
            singleLine = true,
            enabled = !state.isSaving,
            visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { show = !show }) {
                    Icon(
                        if (show) VisibilityOffIcon else VisibilityIcon,
                        contentDescription = if (show) strings.hidePassword else strings.showPassword,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.NewPassword },
        )
        if (state.password.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics { contentDescription = strings.strengthLabels[strength] },
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(4) { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(if (i < strength) strengthColor else colors.outlineVariant, RoundedCornerShape(2.dp)),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(strings.strengthLabels[strength], style = MaterialTheme.typography.bodySmall, color = strengthColor)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.confirm,
            onValueChange = { onEvent(LoginSetNewPasswordEvent.ConfirmChanged(it)) },
            label = { Text(strings.confirm) },
            singleLine = true,
            enabled = !state.isSaving,
            isError = state.confirm.isNotEmpty() && !matches,
            visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = if (matches) {
                { Icon(CheckIcon, contentDescription = null, tint = colors.primary) }
            } else null,
            supportingText = if (state.confirm.isNotEmpty()) {
                { Text(if (matches) strings.match else strings.noMatch) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focus.clearFocus()
                if (canSave) onEvent(LoginSetNewPasswordEvent.Save)
            }),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.NewPassword },
        )
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(
                    value = state.signOutOthers,
                    role = Role.Checkbox,
                    onValueChange = { onEvent(LoginSetNewPasswordEvent.SignOutOthersChanged(it)) },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = state.signOutOthers, onCheckedChange = null)
            Spacer(Modifier.width(12.dp))
            Text(strings.signOutOthers, style = MaterialTheme.typography.bodyMedium)
        }
        if (state.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.error, style = MaterialTheme.typography.bodyMedium, color = colors.error)
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(LoginSetNewPasswordEvent.Save) },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
            } else {
                Text(strings.save)
            }
        }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val CheckIcon = vectorIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")
private val VisibilityIcon = vectorIcon(
    "Visibility",
    "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z",
)
private val VisibilityOffIcon = vectorIcon(
    "VisibilityOff",
    "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z",
)

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
internal fun LoginSetNewPasswordPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginSetNewPassword(
            state = LoginSetNewPasswordState(password = "correct-horse-battery", confirm = "correct-horse-battery", strength = 4),
            onEvent = {},
        )
    }
}
