package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Checkout · Send money (fintech)
 * Pay a person: verified payee, a big amount with an on-screen keypad, an
 * optional note, the source account, and the pay button that repeats the
 * amount. The system keyboard never opens for the amount.
 *
 * The amount stays a string of digits (and one decimal point) so you
 * control formatting and limits. The PIN step comes after Pay.
 */

data class CheckoutSendMoneyState(
    val payeeName: String = "[Payee name]",
    val payeeHandle: String = "name@bank",
    val payeeInitials: String = "PS",
    val payeeVerified: Boolean = true,
    val amount: String = "",
    val currency: String = "₹",
    val note: String = "",
    val fromAccount: String = "[Bank] ••4821",
    val error: String? = null,
    val isPaying: Boolean = false,
)

sealed interface CheckoutSendMoneyEvent {
    data object Back : CheckoutSendMoneyEvent
    data class Key(val key: Char) : CheckoutSendMoneyEvent
    data object Backspace : CheckoutSendMoneyEvent
    data class NoteChanged(val value: String) : CheckoutSendMoneyEvent
    data object ChangeAccount : CheckoutSendMoneyEvent
    data object Pay : CheckoutSendMoneyEvent
}

data class CheckoutSendMoneyStrings(
    val back: String = "Back",
    val verified: String = "Verified",
    val amountDescription: (String) -> String = { "Amount, $it" },
    val note: String = "Add a note",
    val payFrom: String = "Pay from",
    val change: String = "Change",
    val decimal: String = "Decimal point",
    val delete: String = "Delete last digit",
    val pay: (String) -> String = { "Pay $it" },
)

@Composable
fun CheckoutSendMoney(
    state: CheckoutSendMoneyState,
    onEvent: (CheckoutSendMoneyEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutSendMoneyStrings = CheckoutSendMoneyStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val colors = MaterialTheme.colorScheme
    val display = state.amount.ifEmpty { "0" }
    val canPay = state.amount.toDoubleOrNull()?.let { it > 0 } == true && !state.isPaying
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(CheckoutSendMoneyEvent.Back) }) { Icon(BackIcon, contentDescription = strings.back) }
            Avatar(state.payeeInitials, null, 40.dp, accent.copy(alpha = 0.14f), accent)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(state.payeeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(state.payeeHandle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    if (state.payeeVerified) {
                        Spacer(Modifier.width(4.dp))
                        Icon(VerifiedIcon, contentDescription = strings.verified, tint = accent, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.clearAndSetSemantics { contentDescription = strings.amountDescription(state.currency + display) },
            ) {
                Text(state.currency, style = MaterialTheme.typography.headlineMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, end = 4.dp))
                Text(display, fontSize = 52.sp, fontWeight = FontWeight.Bold, color = if (state.amount.isEmpty()) colors.onSurfaceVariant else colors.onSurface)
            }
            if (state.error != null) Text(state.error, style = MaterialTheme.typography.bodySmall, color = colors.error)
            Spacer(Modifier.height(12.dp))
            BasicTextField(
                value = state.note,
                onValueChange = { if (it.length <= 50) onEvent(CheckoutSendMoneyEvent.NoteChanged(it)) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface, textAlign = TextAlign.Center),
                cursorBrush = SolidColor(accent),
                modifier = Modifier.keepAboveKeyboard().widthIn(min = 160.dp).semantics { contentDescription = strings.note },
                decorationBox = { inner ->
                    Box(Modifier.background(colors.surfaceContainerHigh, CircleShape).padding(horizontal = 16.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                        if (state.note.isEmpty()) Text(strings.note, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        inner()
                    }
                },
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(MaterialTheme.shapes.large)
                .background(colors.surfaceContainerLow)
                .clickable(role = Role.Button) { onEvent(CheckoutSendMoneyEvent.ChangeAccount) }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(BankIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(strings.payFrom, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                Text(state.fromAccount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Text(strings.change, style = MaterialTheme.typography.labelLarge, color = accent)
        }
        val keys = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '0', '<')
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
            keys.chunked(3).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { k ->
                        Box(
                            Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable(role = Role.Button) {
                                    if (k == '<') onEvent(CheckoutSendMoneyEvent.Backspace) else onEvent(CheckoutSendMoneyEvent.Key(k))
                                }
                                .semantics {
                                    when (k) {
                                        '<' -> contentDescription = strings.delete
                                        '.' -> contentDescription = strings.decimal
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (k == '<') Icon(BackIcon, contentDescription = null)
                            else Text(k.toString(), fontSize = 24.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        Button(
            onClick = { onEvent(CheckoutSendMoneyEvent.Pay) },
            enabled = canPay,
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp).height(56.dp),
        ) {
            if (state.isPaying) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
            else Text(strings.pay(state.currency + display), fontWeight = FontWeight.Bold)
        }
    }
}

/** Your picture, or initials on a tinted circle. */
@Composable
private fun Avatar(initials: String, image: (@Composable () -> Unit)?, size: Dp, container: Color, content: Color) {
    Box(Modifier.size(size).clip(CircleShape).background(container), contentAlignment = Alignment.Center) {
        if (image != null) image()
        else Text(initials, color = content, fontWeight = FontWeight.SemiBold, fontSize = (size.value * 0.36f).sp)
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val ChevronIcon = vectorIcon("Chevron", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z", autoMirror = true)
private val VerifiedIcon = vectorIcon(
    "Verified",
    "M23 12l-2.44-2.79.34-3.69-3.61-.82-1.89-3.2L12 2.96 8.6 1.5 6.71 4.69 3.1 5.5l.34 3.7L1 12l2.44 2.79-.34 3.7 3.61.82L8.6 22.5l3.4-1.47 3.4 1.46 1.89-3.19 3.61-.82-.34-3.69L23 12zm-12.91 4.72l-3.8-3.81 1.48-1.48 2.32 2.33 5.85-5.87 1.48 1.48-7.33 7.35z", autoMirror = true,
)
private val BankIcon = vectorIcon("Bank", "M4 10v7h3v-7H4zm6 0v7h3v-7h-3zM2 22h19v-3H2v3zm14-12v7h3v-7h-3zm-4.5-9L2 6v2h19V6l-9.5-5z")

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
internal fun CheckoutSendMoneyPreview() {
    MaterialTheme(colorScheme = PreviewColors) { CheckoutSendMoney(state = CheckoutSendMoneyState(amount = "2500"), onEvent = {}, accent = Color(0xFF1F4FBF)) }
}
