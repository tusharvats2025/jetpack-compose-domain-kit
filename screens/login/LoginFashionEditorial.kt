package com.yourapp.ui.screens.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Login · Fashion editorial
 * A full-bleed campaign image on top, an italic serif headline and an
 * email-first form. Guests can browse without signing in.
 *
 * Put your campaign image in the hero slot (Image with ContentScale.Crop).
 * The design pairs Bodoni Moda (headline) with Jost (everything else);
 * pass them as serifFont and sansFont.
 */

data class LoginFashionEditorialState(
    val email: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
)

sealed interface LoginFashionEditorialEvent {
    data object Skip : LoginFashionEditorialEvent
    data class EmailChanged(val value: String) : LoginFashionEditorialEvent
    data object Continue : LoginFashionEditorialEvent
    data object BrowseAsGuest : LoginFashionEditorialEvent
    data object CreateAccount : LoginFashionEditorialEvent
}

data class LoginFashionEditorialStrings(
    val brand: String = "[BRAND]",
    val skip: String = "SKIP",
    val collection: String = "[SEASON / COLLECTION]",
    val headline: String = "Your wardrobe,\nsaved for you.",
    val email: String = "EMAIL",
    val emailPlaceholder: String = "name@example.com",
    val submit: String = "CONTINUE",
    val guest: String = "Browse as guest",
    val createAccount: String = "Create account",
)

data class LoginFashionEditorialColors(
    val tone: Color = Color(0xFFCFC8C0),
    val background: Color = Color(0xFFFFFFFF),
    val ink: Color = Color(0xFF111111),
    val muted: Color = Color(0xFF4F4A45),
    val onInk: Color = Color(0xFFFFFFFF),
    val error: Color = Color(0xFFB3261E),
)

@Composable
fun LoginFashionEditorial(
    state: LoginFashionEditorialState,
    onEvent: (LoginFashionEditorialEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: LoginFashionEditorialStrings = LoginFashionEditorialStrings(),
    colors: LoginFashionEditorialColors = LoginFashionEditorialColors(),
    serifFont: FontFamily = FontFamily.Serif,
    sansFont: FontFamily = FontFamily.Default,
    hero: (@Composable BoxScope.() -> Unit)? = null,
) {
    val sans = TextStyle(fontFamily = sansFont, color = colors.ink)
    val enabled = !state.isLoading

    Column(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState()),
    ) {
        // Hero: the tone colour shows until your image loads, or if you pass none.
        Box(
            Modifier
                .fillMaxWidth()
                .height(430.dp)
                .background(colors.tone),
        ) {
            hero?.invoke(this)
            Column(
                Modifier
                    .matchParentSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        strings.brand,
                        style = sans.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 5.sp),
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(
                        onClick = { onEvent(LoginFashionEditorialEvent.Skip) },
                        shape = RectangleShape,
                    ) {
                        Text(strings.skip, style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp))
                    }
                }
                Text(
                    strings.collection,
                    style = sans.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp),
                )
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 16.dp),
        ) {
            Text(
                strings.headline,
                style = TextStyle(
                    fontFamily = serifFont,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Medium,
                    fontSize = 36.sp,
                    lineHeight = 40.sp,
                    letterSpacing = (-0.5).sp,
                    color = colors.ink,
                ),
            )
            Spacer(Modifier.height(28.dp))
            Text(strings.email, style = sans.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp, color = colors.muted))
            BasicTextField(
                value = state.email,
                onValueChange = { onEvent(LoginFashionEditorialEvent.EmailChanged(it)) },
                enabled = enabled,
                singleLine = true,
                textStyle = sans.copy(fontSize = 18.sp),
                cursorBrush = SolidColor(colors.ink),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { onEvent(LoginFashionEditorialEvent.Continue) }),
                modifier = Modifier.keepAboveKeyboard()
                    .fillMaxWidth()
                    .semantics {
                        contentType = ContentType.EmailAddress
                        contentDescription = strings.email
                    },
                decorationBox = { inner ->
                    Box(Modifier.heightIn(min = 44.dp), contentAlignment = Alignment.CenterStart) {
                        if (state.email.isEmpty()) {
                            Text(strings.emailPlaceholder, style = sans.copy(fontSize = 18.sp, color = colors.muted.copy(alpha = 0.6f)))
                        }
                        inner()
                    }
                },
            )
            HorizontalDivider(thickness = 1.5.dp, color = if (state.error != null) colors.error else colors.ink)
            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.error, style = sans.copy(fontSize = 13.sp, color = colors.error))
            }
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = { onEvent(LoginFashionEditorialEvent.Continue) },
                enabled = enabled,
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = colors.ink, contentColor = colors.onInk),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.onInk)
                } else {
                    Text(strings.submit, style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 3.sp, color = colors.onInk))
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { onEvent(LoginFashionEditorialEvent.BrowseAsGuest) }, shape = RectangleShape, contentPadding = PaddingValues(0.dp)) {
                    Text(strings.guest, style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium))
                }
                TextButton(onClick = { onEvent(LoginFashionEditorialEvent.CreateAccount) }, shape = RectangleShape, contentPadding = PaddingValues(0.dp)) {
                    Text(strings.createAccount, style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium))
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

@Preview
@Composable
internal fun LoginFashionEditorialPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        LoginFashionEditorial(state = LoginFashionEditorialState(), onEvent = {})
    }
}
