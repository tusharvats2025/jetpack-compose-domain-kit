package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Checkout · One-page checkout
 * Everything on one screen for returning buyers: address, delivery speed,
 * payment and a collapsible summary, with Place order pinned at the bottom.
 * Best when address and payment are usually already known.
 */

data class CheckoutDeliveryOption(val id: String, val title: String, val eta: String, val price: String)

data class CheckoutOnePageState(
    val addressLabel: String = "Home · [Name]",
    val address: String = "[Flat, street], [Area], [City] [PIN]",
    val deliveryOptions: List<CheckoutDeliveryOption> = emptyList(),
    val selectedDelivery: String? = null,
    val payment: String = "UPI · name@bank",
    val itemsSummary: String = "3 items",
    val lines: List<Pair<String, String>> = emptyList(),
    val total: String = "",
    val isPlacing: Boolean = false,
)

sealed interface CheckoutOnePageEvent {
    data object Back : CheckoutOnePageEvent
    data object ChangeAddress : CheckoutOnePageEvent
    data class SelectDelivery(val id: String) : CheckoutOnePageEvent
    data object ChangePayment : CheckoutOnePageEvent
    data object PlaceOrder : CheckoutOnePageEvent
    data object OpenTerms : CheckoutOnePageEvent
}

data class CheckoutOnePageStrings(
    val back: String = "Back",
    val title: String = "Checkout",
    val deliverTo: String = "Deliver to",
    val change: String = "Change",
    val delivery: String = "Delivery",
    val payment: String = "Payment",
    val summary: String = "Order summary",
    val total: String = "Total",
    val placeOrder: (String) -> String = { "Place order · $it" },
    val terms: String = "By placing the order you agree to the Terms.",
)

@Composable
fun CheckoutOnePage(
    state: CheckoutOnePageState,
    onEvent: (CheckoutOnePageEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutOnePageStrings = CheckoutOnePageStrings(),
) {
    val colors = MaterialTheme.colorScheme
    var summaryOpen by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxSize().background(colors.surfaceContainerLowest).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(CheckoutOnePageEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Panel {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.deliverTo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onEvent(CheckoutOnePageEvent.ChangeAddress) }) { Text(strings.change) }
                }
                Text(state.addressLabel, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(state.address, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Panel {
                Text(strings.delivery, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.deliveryOptions.forEach { o ->
                        val selected = o.id == state.selectedDelivery
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, MaterialTheme.shapes.medium)
                                .selectable(selected = selected, role = Role.RadioButton) { onEvent(CheckoutOnePageEvent.SelectDelivery(o.id)) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = selected, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(o.title, style = MaterialTheme.typography.bodyLarge)
                                Text(o.eta, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                            Text(o.price, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).clickable(role = Role.Button) { onEvent(CheckoutOnePageEvent.ChangePayment) }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(strings.payment, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(state.payment, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                Text(strings.change, style = MaterialTheme.typography.labelLarge, color = colors.primary)
            }
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .toggleable(value = summaryOpen, role = Role.Button, onValueChange = { summaryOpen = it })
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(strings.summary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Text("${state.itemsSummary} · ${state.total}", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    Icon(
                        ChevronIcon,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp).size(20.dp).graphicsRotation(if (summaryOpen) 270f else 90f),
                    )
                }
                if (summaryOpen) {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                        state.lines.forEach { (l, v) -> PriceRow(l, v) }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colors.outlineVariant)
                        PriceRow(strings.total, state.total, strong = true)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { onEvent(CheckoutOnePageEvent.PlaceOrder) },
                    enabled = !state.isPlacing && state.selectedDelivery != null,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    if (state.isPlacing) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                    else Text(strings.placeOrder(state.total))
                }
                TextButton(onClick = { onEvent(CheckoutOnePageEvent.OpenTerms) }) {
                    Text(strings.terms, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surface).padding(16.dp), content = content)
}

private fun Modifier.graphicsRotation(degrees: Float): Modifier = this.then(Modifier.rotate(degrees))

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

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val ChevronIcon = vectorIcon("Chevron", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z", autoMirror = true)

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
internal fun CheckoutOnePagePreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutOnePage(
            state = CheckoutOnePageState(
                deliveryOptions = listOf(
                    CheckoutDeliveryOption("std", "Standard", "Arrives Thu", "Free"),
                    CheckoutDeliveryOption("exp", "Express", "Arrives tomorrow", "₹99"),
                ),
                selectedDelivery = "std",
                lines = listOf("Items" to "₹2,397", "Discount" to "−₹150", "Delivery" to "Free"),
                total = "₹2,247",
            ),
            onEvent = {},
        )
    }
}
