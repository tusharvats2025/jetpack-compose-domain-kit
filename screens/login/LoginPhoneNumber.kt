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
import androidx.compose.ui.semantics.contentDescription
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
import androidx.compose.ui.unit.sp

/*
 * Login · Phone number (step 1 of 2)
 * Country code picker, mobile number and "Send code". Pairs with
 * LoginOtpVerify for step 2. Opening a country list is up to you: handle
 * CountryCodeClick and show your own picker or bottom sheet.
 *
 * accent defaults to MaterialTheme.colorScheme.primary.
 */

data class LoginPhoneNumberState(
    val countryCode: String = "+91",
    val phone: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
)

sealed interface LoginPhoneNumberEvent {
    data object CountryCodeClick : LoginPhoneNumberEvent
    data class PhoneChanged(val value: String) : LoginPhoneNumberEvent
    data object UseEmail : LoginPhoneNumberEvent
    data object SendCode : LoginPhoneNumberEvent
    data object OpenTerms : LoginPhoneNumberEvent
    data object OpenPrivacy : LoginPhoneNumberEvent
}

data class LoginPhoneNumberStrings(
    val step: String = "Step 1 of 2",
    val title: String = "Enter your number",
    val subtitle: String = "We'll text you a 6-digit code to verify it.",
    val phone: String = "Mobile number",
    val countryCodeDescription: String = "Country code",
    val ratesNote: String = "Standard message rates may apply.",
    val useEmail: String = "Use email instead",
    val legalPrefix: String = "By continuing you agree to the ",
    val terms: String = "Terms",
    val and: String = " and ",
    val privacy: String = "Privacy Policy",
    val legalSuffix: String = ".",
    val sendCode: String = "Send code",
)

@Composable
fun LoginPhoneNumber(
    state: LoginPhoneNumberState,
    onEvent: (LoginPhoneNumberEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginPhoneNumberStrings = LoginPhoneNumberStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    val canSend = state.phone.isNotBlank() && !state.isSending

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp),
    ) {
        StepBar(done = 1, total = 2, accent = accent, track = colors.outlineVariant)
        Spacer(Modifier.height(14.dp))
        Text(strings.step, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(strings.subtitle, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        Text(strings.phone, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Top) {
            OutlinedButton(
                onClick = { onEvent(LoginPhoneNumberEvent.CountryCodeClick) },
                shape = shape,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.surfaceContainerLow,
                    contentColor = colors.onSurface,
                ),
                contentPadding = PaddingValues(start = 14.dp, end = 8.dp),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .height(56.dp)
                    .semantics { contentDescription = "${strings.countryCodeDescription}, ${state.countryCode}" },
            ) {
                Text(state.countryCode, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Icon(DropDownIcon, contentDescription = null)
            }
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = state.phone,
                onValueChange = { value -> onEvent(LoginPhoneNumberEvent.PhoneChanged(value.filter { it.isDigit() || it == ' ' })) },
                placeholder = { Text("00000 00000") },
                singleLine = true,
                enabled = !state.isSending,
                shape = shape,
                isError = state.error != null,
                textStyle = MaterialTheme.typography.titleMedium.copy(letterSpacing = 1.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    cursorColor = accent,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (canSend) onEvent(LoginPhoneNumberEvent.SendCode) }),
                modifier = Modifier.keepAboveKeyboard()
                    .weight(1f)
                    .padding(top = 8.dp)
                    .semantics {
                        contentType = ContentType.PhoneNumber
                        contentDescription = strings.phone
                    },
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            state.error ?: strings.ratesNote,
            style = MaterialTheme.typography.bodySmall,
            color = if (state.error != null) colors.error else colors.onSurfaceVariant,
        )
        TextButton(
            onClick = { onEvent(LoginPhoneNumberEvent.UseEmail) },
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = accent),
            modifier = Modifier.padding(top = 8.dp),
        ) { Text(strings.useEmail, fontWeight = FontWeight.SemiBold) }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        val link = TextLinkStyles(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold))
        Text(
            buildAnnotatedString {
                append(strings.legalPrefix)
                withLink(LinkAnnotation.Clickable("terms", link) { onEvent(LoginPhoneNumberEvent.OpenTerms) }) {
                    append(strings.terms)
                }
                append(strings.and)
                withLink(LinkAnnotation.Clickable("privacy", link) { onEvent(LoginPhoneNumberEvent.OpenPrivacy) }) {
                    append(strings.privacy)
                }
                append(strings.legalSuffix)
            },
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { onEvent(LoginPhoneNumberEvent.SendCode) },
            enabled = canSend,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            if (state.isSending) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
            } else {
                Text(strings.sendCode, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StepBar(done: Int, total: Int, accent: Color, track: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(if (i < done) accent else track, RoundedCornerShape(2.dp)),
            )
        }
    }
}

private val DropDownIcon: ImageVector = ImageVector.Builder("DropDown", 24.dp, 24.dp, 24f, 24f)
    .addPath(addPathNodes("M7 10l5 5 5-5z"), fill = SolidColor(Color.Black))
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
internal fun LoginPhoneNumberPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginPhoneNumber(state = LoginPhoneNumberState(phone = "98765 43210"), onEvent = {}, accent = Color(0xFF1F4FBF))
    }
}
