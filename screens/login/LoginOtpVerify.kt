package com.yourapp.ui.screens.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Login · OTP verify (step 2 of 2)
 * Six digit boxes backed by one text field, so paste, SMS autofill and
 * backspace all work. Pairs with LoginPhoneNumber.
 *
 * The countdown is yours: tick resendInSeconds down in your ViewModel.
 * accent defaults to MaterialTheme.colorScheme.primary.
 */

data class LoginOtpVerifyState(
    val phone: String = "+91 00000 00000",
    val code: String = "",
    val codeLength: Int = 6,
    val resendInSeconds: Int = 0,
    val isAutoReading: Boolean = false,
    val isVerifying: Boolean = false,
    val error: String? = null,
)

sealed interface LoginOtpVerifyEvent {
    data object ChangeNumber : LoginOtpVerifyEvent
    data class CodeChanged(val value: String) : LoginOtpVerifyEvent
    data object Resend : LoginOtpVerifyEvent
    data object Verify : LoginOtpVerifyEvent
}

data class LoginOtpVerifyStrings(
    val step: String = "Step 2 of 2",
    val changeNumber: String = "Change number",
    val title: String = "Enter the code",
    val sentTo: (phone: String) -> String = { "Sent by SMS to $it." },
    val codeDescription: String = "Verification code",
    val autoReading: String = "Waiting to read the code from your messages",
    val didntGetIt: String = "Didn't get it?",
    val resendIn: (seconds: Int) -> String = { "Resend in %d:%02d".format(it / 60, it % 60) },
    val resend: String = "Resend code",
    val verify: String = "Verify and continue",
)

@Composable
fun LoginOtpVerify(
    state: LoginOtpVerifyState,
    onEvent: (LoginOtpVerifyEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginOtpVerifyStrings = LoginOtpVerifyStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val colors = MaterialTheme.colorScheme
    val focusRequester = remember { FocusRequester() }
    val complete = state.code.length == state.codeLength
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(2) { Box(Modifier.weight(1f).height(4.dp).background(accent, RoundedCornerShape(2.dp))) }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(strings.step, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            TextButton(
                onClick = { onEvent(LoginOtpVerifyEvent.ChangeNumber) },
                colors = ButtonDefaults.textButtonColors(contentColor = accent),
            ) { Text(strings.changeNumber, fontWeight = FontWeight.SemiBold) }
        }
        Spacer(Modifier.height(14.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(strings.sentTo(state.phone), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        BasicTextField(
            value = state.code,
            onValueChange = { value ->
                val digits = value.filter { it.isDigit() }.take(state.codeLength)
                onEvent(LoginOtpVerifyEvent.CodeChanged(digits))
                if (digits.length == state.codeLength && !state.isVerifying) onEvent(LoginOtpVerifyEvent.Verify)
            },
            enabled = !state.isVerifying,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (complete) onEvent(LoginOtpVerifyEvent.Verify) }),
            modifier = Modifier.keepAboveKeyboard()
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .semantics {
                    contentType = ContentType.SmsOtpCode
                    contentDescription = strings.codeDescription
                },
            decorationBox = { innerTextField ->
                // The real text field stays invisible; the boxes draw the digits.
                Box(Modifier.fillMaxWidth()) {
                    Box(Modifier.size(1.dp).alpha(0f)) { innerTextField() }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(state.codeLength) { i ->
                            val char = state.code.getOrNull(i)
                            val isCurrent = i == state.code.length
                            val borderColor = when {
                                state.error != null -> colors.error
                                isCurrent -> accent
                                else -> colors.outline
                            }
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .background(
                                        if (isCurrent) colors.surface else colors.surfaceContainerLow,
                                        RoundedCornerShape(12.dp),
                                    )
                                    .border(if (isCurrent) 2.dp else 1.5.dp, borderColor, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    char?.toString() ?: "",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.onSurface,
                                )
                            }
                        }
                    }
                }
            },
        )

        if (state.error != null) {
            Spacer(Modifier.height(10.dp))
            Text(state.error, style = MaterialTheme.typography.bodyMedium, color = colors.error)
        } else if (state.isAutoReading) {
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(accent.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(MessageIcon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(strings.autoReading, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(strings.didntGetIt, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            if (state.resendInSeconds > 0) {
                Text(strings.resendIn(state.resendInSeconds), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            } else {
                TextButton(
                    onClick = { onEvent(LoginOtpVerifyEvent.Resend) },
                    colors = ButtonDefaults.textButtonColors(contentColor = accent),
                ) { Text(strings.resend, fontWeight = FontWeight.SemiBold) }
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(LoginOtpVerifyEvent.Verify) },
            enabled = complete && !state.isVerifying,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            if (state.isVerifying) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
            } else {
                Text(strings.verify, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private val MessageIcon: ImageVector = ImageVector.Builder("Message", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes("M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM9 11H7V9h2v2zm4 0h-2V9h2v2zm4 0h-2V9h2v2z"),
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
internal fun LoginOtpVerifyPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginOtpVerify(
            state = LoginOtpVerifyState(code = "47", resendInSeconds = 28, isAutoReading = true),
            onEvent = {},
            accent = Color(0xFF1F4FBF),
        )
    }
}
