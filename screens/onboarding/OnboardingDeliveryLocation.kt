package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Onboarding · Delivery location (food delivery)
 * A map on top, then "Use my current location", an address search with
 * suggestions, and manual entry.
 *
 * The map is yours: pass Google Maps Compose (or a static image) in the
 * map slot. Location permission, geocoding and suggestions are yours too.
 * The design uses Outfit; set it in your Typography to match.
 */

data class OnboardingAddressSuggestion(val id: String, val title: String, val detail: String)

data class OnboardingDeliveryLocationState(
    val query: String = "",
    val suggestions: List<OnboardingAddressSuggestion> = emptyList(),
    val isLocating: Boolean = false,
    val error: String? = null,
)

sealed interface OnboardingDeliveryLocationEvent {
    data object UseCurrentLocation : OnboardingDeliveryLocationEvent
    data class QueryChanged(val value: String) : OnboardingDeliveryLocationEvent
    data class PickSuggestion(val id: String) : OnboardingDeliveryLocationEvent
    data object EnterManually : OnboardingDeliveryLocationEvent
}

data class OnboardingDeliveryLocationStrings(
    val title: String = "Where should we deliver?",
    val body: String = "We'll show what's available near you.",
    val useCurrent: String = "Use my current location",
    val searchLabel: String = "Or search for an address",
    val searchPlaceholder: String = "Area, street or landmark",
    val manual: String = "Enter address manually",
)

@Composable
fun OnboardingDeliveryLocation(
    state: OnboardingDeliveryLocationState,
    onEvent: (OnboardingDeliveryLocationEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingDeliveryLocationStrings = OnboardingDeliveryLocationStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    map: (@Composable BoxScope.() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)

    Column(modifier.fillMaxSize().background(colors.surface)) {
        // Map area: shrinks while the keyboard is open so the search stays visible.
        Box(
            Modifier
                .fillMaxWidth()
                .weight(0.42f)
                .background(colors.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            map?.invoke(this)
            Icon(PinIcon, contentDescription = null, tint = accent, modifier = Modifier.size(44.dp))
        }

        Column(
            Modifier
                .fillMaxWidth()
                .weight(0.58f)
                .offset(y = (-24).dp)
                .background(colors.surface, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 16.dp),
        ) {
            Text(strings.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onEvent(OnboardingDeliveryLocationEvent.UseCurrentLocation) },
                enabled = !state.isLocating,
                shape = shape,
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                if (state.isLocating) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
                } else {
                    Icon(MyLocationIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(strings.useCurrent, fontWeight = FontWeight.SemiBold)
                }
            }
            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.error, style = MaterialTheme.typography.bodySmall, color = colors.error)
            }
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(OnboardingDeliveryLocationEvent.QueryChanged(it)) },
                label = { Text(strings.searchLabel) },
                placeholder = { Text(strings.searchPlaceholder) },
                leadingIcon = { Icon(SearchIcon, contentDescription = null) },
                singleLine = true,
                shape = shape,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, focusedLabelColor = accent, cursorColor = accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.AddressStreet },
            )
            state.suggestions.forEach { s ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onEvent(OnboardingDeliveryLocationEvent.PickSuggestion(s.id)) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(PinIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(s.title, style = MaterialTheme.typography.bodyLarge)
                        Text(s.detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
            }
            TextButton(
                onClick = { onEvent(OnboardingDeliveryLocationEvent.EnterManually) },
                colors = ButtonDefaults.textButtonColors(contentColor = accent),
                modifier = Modifier.padding(top = 8.dp),
                contentPadding = PaddingValues(0.dp),
            ) { Text(strings.manual, fontWeight = FontWeight.SemiBold) }
        }
    }
}

private fun vectorIcon(name: String, path: String): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val PinIcon = vectorIcon("Pin", "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z")
private val SearchIcon = vectorIcon(
    "Search",
    "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z",
)
private val MyLocationIcon = vectorIcon(
    "MyLocation",
    "M12 8c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm8.94 3c-.46-4.17-3.77-7.48-7.94-7.94V1h-2v2.06C6.83 3.52 3.52 6.83 3.06 11H1v2h2.06c.46 4.17 3.77 7.48 7.94 7.94V23h2v-2.06c4.17-.46 7.48-3.77 7.94-7.94H23v-2h-2.06zM12 19c-3.87 0-7-3.13-7-7s3.13-7 7-7 7 3.13 7 7-3.13 7-7 7z",
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
internal fun OnboardingDeliveryLocationPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingDeliveryLocation(state = OnboardingDeliveryLocationState(), onEvent = {}, accent = Color(0xFFB93A0A))
    }
}
