package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Checkout · Payment method
 * UPI apps first (the common choice in India), a UPI ID option, saved
 * cards, then netbanking, wallets and cash on delivery. The pay button
 * names the method so the user knows what happens next.
 *
 * Collect card details on your payment gateway's hosted page or SDK,
 * never in your own fields. UPI app icons come from your assets.
 */

data class CheckoutUpiApp(val id: String, val name: String, val icon: (@Composable () -> Unit)? = null)
data class CheckoutSavedCard(val id: String, val last4: String, val expiry: String, val brand: String = "")
data class CheckoutOtherMethod(val id: String, val title: String, val note: String? = null)

data class CheckoutPaymentMethodState(
    val amount: String = "",
    val upiApps: List<CheckoutUpiApp> = emptyList(),
    val cards: List<CheckoutSavedCard> = emptyList(),
    val others: List<CheckoutOtherMethod> = emptyList(),
    /** Id of the selected UPI app or card. */
    val selectedId: String? = null,
    val gateway: String = "[gateway]",
    val isPaying: Boolean = false,
)

sealed interface CheckoutPaymentMethodEvent {
    data object Back : CheckoutPaymentMethodEvent
    data class Select(val id: String) : CheckoutPaymentMethodEvent
    data object UpiId : CheckoutPaymentMethodEvent
    data object AddCard : CheckoutPaymentMethodEvent
    data class OpenOther(val id: String) : CheckoutPaymentMethodEvent
    data object Pay : CheckoutPaymentMethodEvent
}

data class CheckoutPaymentMethodStrings(
    val back: String = "Back",
    val title: String = "Payment",
    val upi: String = "UPI",
    val upiId: String = "Pay with a UPI ID",
    val cards: String = "Cards",
    val expires: (String) -> String = { "Exp $it" },
    val addCard: String = "Add a card",
    val more: String = "More ways to pay",
    val secure: (String) -> String = { "Card details are entered on $it's secure page" },
    val pay: (amount: String, method: String) -> String = { a, m -> "Pay $a with $m" },
    val payPlain: (String) -> String = { "Pay $it" },
)

@Composable
fun CheckoutPaymentMethod(
    state: CheckoutPaymentMethodState,
    onEvent: (CheckoutPaymentMethodEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutPaymentMethodStrings = CheckoutPaymentMethodStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val selectedName = state.upiApps.firstOrNull { it.id == state.selectedId }?.name
        ?: state.cards.firstOrNull { it.id == state.selectedId }?.let { "•••• ${it.last4}" }
    Column(modifier.fillMaxSize().background(colors.surfaceContainerLowest).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(CheckoutPaymentMethodEvent.Back) }) {
            Text(state.amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp).selectableGroup()) {
            Label(strings.upi)
            Group {
                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.upiApps.forEach { app ->
                        val selected = app.id == state.selectedId
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(MaterialTheme.shapes.medium)
                                .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, MaterialTheme.shapes.medium)
                                .selectable(selected = selected, role = Role.RadioButton) { onEvent(CheckoutPaymentMethodEvent.Select(app.id)) }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(Modifier.size(36.dp).clip(MaterialTheme.shapes.small).background(colors.surfaceContainerHigh), contentAlignment = Alignment.Center) { app.icon?.invoke() }
                            Spacer(Modifier.height(6.dp))
                            Text(app.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                RowDivider()
                LinkRow(strings.upiId, { onEvent(CheckoutPaymentMethodEvent.UpiId) })
            }
            Label(strings.cards)
            Group {
                state.cards.forEach { c ->
                    val selected = c.id == state.selectedId
                    Row(
                        Modifier.fillMaxWidth().selectable(selected = selected, role = Role.RadioButton) { onEvent(CheckoutPaymentMethodEvent.Select(c.id)) }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Icon(CardIcon, contentDescription = null, tint = colors.onSurfaceVariant)
                        Spacer(Modifier.width(12.dp))
                        Text("${c.brand} •••• ${c.last4}".trim(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(strings.expires(c.expiry), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    RowDivider()
                }
                TextButton(onClick = { onEvent(CheckoutPaymentMethodEvent.AddCard) }, modifier = Modifier.padding(8.dp)) {
                    Icon(AddIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.addCard)
                }
            }
            Label(strings.more)
            Group {
                state.others.forEachIndexed { i, o ->
                    if (i > 0) RowDivider()
                    LinkRow(o.title, { onEvent(CheckoutPaymentMethodEvent.OpenOther(o.id)) }, subtitle = o.note)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(LockIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.secure(state.gateway), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                Button(
                    onClick = { onEvent(CheckoutPaymentMethodEvent.Pay) },
                    enabled = state.selectedId != null && !state.isPaying,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    if (state.isPaying) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                    else Text(if (selectedName != null) strings.pay(state.amount, selectedName) else strings.payPlain(state.amount))
                }
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp).semantics { heading() })
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

/** Rounded card that holds a run of rows. */
@Composable
private fun Group(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        content = content,
    )
}

@Composable
private fun RowDivider() {
    HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun LinkRow(title: String, onClick: () -> Unit, value: String? = null, subtitle: String? = null) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp))
        }
        Icon(ChevronIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp).size(20.dp))
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val ChevronIcon = vectorIcon("Chevron", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z", autoMirror = true)
private val LockIcon = vectorIcon(
    "Lock",
    "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z",
)
private val CardIcon = vectorIcon(
    "Card",
    "M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z",
)
private val AddIcon = vectorIcon("Add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")

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
internal fun CheckoutPaymentMethodPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutPaymentMethod(
            state = CheckoutPaymentMethodState(
                amount = "₹2,247",
                upiApps = listOf(CheckoutUpiApp("a1", "[App 1]"), CheckoutUpiApp("a2", "[App 2]"), CheckoutUpiApp("a3", "[App 3]"), CheckoutUpiApp("a4", "[App 4]")),
                cards = listOf(CheckoutSavedCard("c1", "4242", "08/28", "Visa")),
                others = listOf(CheckoutOtherMethod("nb", "Netbanking"), CheckoutOtherMethod("w", "Wallets"), CheckoutOtherMethod("cod", "Cash on delivery", "₹20 handling fee")),
                selectedId = "a1",
            ),
            onEvent = {},
        )
    }
}
