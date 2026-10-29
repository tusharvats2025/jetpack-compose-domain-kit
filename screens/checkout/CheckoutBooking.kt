package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Checkout · Booking (events)
 * Confirm a seat or ticket booking while seats are held: a visible
 * countdown, the event summary, who the tickets go to, and the total.
 *
 * Tick heldFor down in your ViewModel and release the hold on your server
 * when it reaches zero; show an expired message instead of paying.
 */

data class CheckoutBookingState(
    val heldFor: String = "07:42",
    val heldSeconds: Int = 462,
    val event: String = "[Event name]",
    val whenLine: String = "Sat, [date] · 7:30 pm",
    val venue: String = "[Venue], [City]",
    val seats: String = "2 seats · Row F, 11–12",
    val name: String = "",
    val email: String = "",
    val lines: List<Pair<String, String>> = emptyList(),
    val total: String = "",
    val isPaying: Boolean = false,
)

sealed interface CheckoutBookingEvent {
    data object Back : CheckoutBookingEvent
    data class NameChanged(val value: String) : CheckoutBookingEvent
    data class EmailChanged(val value: String) : CheckoutBookingEvent
    data object Pay : CheckoutBookingEvent
}

data class CheckoutBookingStrings(
    val back: String = "Back",
    val title: String = "Confirm booking",
    val held: String = "Seats held for you",
    val heldDescription: (String) -> String = { "Seats held for $it" },
    val sendTo: String = "Send tickets to",
    val name: String = "Full name",
    val namePlaceholder: String = "As on your ID",
    val email: String = "Email",
    val total: String = "Total",
    val pay: (String) -> String = { "Pay $it" },
    val expired: String = "Your hold expired. Pick seats again.",
)

@Composable
fun CheckoutBooking(
    state: CheckoutBookingState,
    onEvent: (CheckoutBookingEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutBookingStrings = CheckoutBookingStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    poster: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val expired = state.heldSeconds <= 0
    val urgent = state.heldSeconds in 1..120
    Column(modifier.fillMaxSize().background(colors.surfaceContainerLowest).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(CheckoutBookingEvent.Back) })
        Row(
            Modifier
                .fillMaxWidth()
                .background(if (urgent || expired) colors.errorContainer else accent.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clearAndSetSemantics { contentDescription = if (expired) strings.expired else strings.heldDescription(state.heldFor) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(ClockIcon, contentDescription = null, tint = if (urgent || expired) colors.onErrorContainer else accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (expired) strings.expired else strings.held, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (!expired) Text(state.heldFor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(12.dp)) {
                ImageSlot(poster, Modifier.size(width = 72.dp, height = 96.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(state.event, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(state.whenLine, style = MaterialTheme.typography.bodyMedium)
                    Text(state.venue, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text(state.seats, style = MaterialTheme.typography.labelLarge, color = accent)
                }
            }
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(strings.sendTo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { onEvent(CheckoutBookingEvent.NameChanged(it)) },
                    label = { Text(strings.name) },
                    placeholder = { Text(strings.namePlaceholder) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.PersonFullName },
                )
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onEvent(CheckoutBookingEvent.EmailChanged(it)) },
                    label = { Text(strings.email) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.EmailAddress },
                )
            }
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(16.dp)) {
                state.lines.forEach { (l, v) -> PriceRow(l, v) }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colors.outlineVariant)
                PriceRow(strings.total, state.total, strong = true)
            }
        }
        Button(
            onClick = { onEvent(CheckoutBookingEvent.Pay) },
            enabled = !expired && !state.isPaying && state.name.isNotBlank() && state.email.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
        ) {
            if (state.isPaying) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
            else Text(strings.pay(state.total), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TopBar(title: String, backLabel: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) { Icon(BackIcon, contentDescription = backLabel) }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        actions()
    }
}

@Composable
private fun PriceRow(label: String, value: String, strong: Boolean = false, valueColor: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(
            label,
            style = if (strong) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (strong) FontWeight.SemiBold else FontWeight.Normal,
            color = if (strong) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = if (strong) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (strong) FontWeight.SemiBold else FontWeight.Normal,
            color = valueColor,
        )
    }
}

/** Shows your image, or a soft placeholder when there is none. */
@Composable
private fun ImageSlot(image: (@Composable () -> Unit)?, modifier: Modifier, shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium) {
    Box(modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
        image?.invoke()
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val ClockIcon = vectorIcon(
    "Clock",
    "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z",
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
internal fun CheckoutBookingPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutBooking(
            state = CheckoutBookingState(
                name = "Asha Rao",
                email = "name@example.com",
                lines = listOf("2 × ₹750" to "₹1,500", "Booking fee" to "₹60"),
                total = "₹1,560",
            ),
            onEvent = {},
            accent = Color(0xFF0B5D56),
            onAccent = Color.White,
        )
    }
}
