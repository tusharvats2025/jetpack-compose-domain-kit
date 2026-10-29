package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Checkout · Order review
 * The last look before paying: items, where and when, how, and the bill
 * with savings called out. Also covers "changed at review": when prices
 * moved or stock ran out since the item was added, a banner says so,
 * affected items are flagged, and the button asks to accept the changes.
 */

sealed interface CheckoutReviewChange {
    data class PriceUp(val was: String, val now: String, val note: String) : CheckoutReviewChange
    data object OutOfStock : CheckoutReviewChange
}

data class CheckoutReviewItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val image: (@Composable () -> Unit)? = null,
    val change: CheckoutReviewChange? = null,
)

data class CheckoutReviewDetail(val id: String, val label: String, val value: String)

data class CheckoutReviewState(
    val items: List<CheckoutReviewItem> = emptyList(),
    val details: List<CheckoutReviewDetail> = emptyList(),
    val lines: List<Pair<String, String>> = emptyList(),
    val total: String = "",
    val savings: String? = null,
    /** Shown above everything when something changed since it was added. */
    val changesSummary: String? = null,
    val notice: String? = null,
    val isPlacing: Boolean = false,
)

sealed interface CheckoutReviewEvent {
    data object Back : CheckoutReviewEvent
    data object EditItems : CheckoutReviewEvent
    data class EditDetail(val id: String) : CheckoutReviewEvent
    data class RemoveItem(val id: String) : CheckoutReviewEvent
    data class FindSimilar(val id: String) : CheckoutReviewEvent
    data object PlaceOrder : CheckoutReviewEvent
    data object OpenTerms : CheckoutReviewEvent
}

data class CheckoutReviewStrings(
    val back: String = "Back",
    val title: String = "Review order",
    val items: (Int) -> String = { if (it == 1) "1 item" else "$it items" },
    val edit: String = "Edit",
    val toPay: String = "To pay",
    val newTotal: String = "New total",
    val save: (String) -> String = { "You save $it on this order" },
    val changesHint: String = "Check them before you pay.",
    val outOfStock: String = "OUT OF STOCK",
    val remove: String = "Remove",
    val similar: String = "Find similar",
    val placeOrder: (String) -> String = { "Place order · $it" },
    val accept: (String) -> String = { "Accept changes and pay $it" },
    val terms: String = "By placing the order you agree to the Terms and Return policy.",
)

@Composable
fun CheckoutReview(
    state: CheckoutReviewState,
    onEvent: (CheckoutReviewEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutReviewStrings = CheckoutReviewStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val changed = state.changesSummary != null
    val blocked = state.items.any { it.change == CheckoutReviewChange.OutOfStock }
    Column(modifier.fillMaxSize().background(colors.surfaceContainerLowest).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(CheckoutReviewEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (state.changesSummary != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(colors.tertiaryContainer)
                        .padding(16.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(WarningIcon, contentDescription = null, tint = colors.onTertiaryContainer)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(state.changesSummary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = colors.onTertiaryContainer)
                        Text(strings.changesHint, style = MaterialTheme.typography.bodySmall, color = colors.onTertiaryContainer)
                    }
                }
            }
            if (changed) {
                state.items.forEach { item -> ChangedItem(item, onEvent, strings) }
            } else {
                Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.items(state.items.sumOf { it.quantity }), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onEvent(CheckoutReviewEvent.EditItems) }) { Text(strings.edit) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.items.take(4).forEach { item ->
                            Box {
                                ImageSlot(item.image, Modifier.size(56.dp))
                                if (item.quantity > 1) {
                                    Text(
                                        "×${item.quantity}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.onPrimary,
                                        modifier = Modifier.align(Alignment.BottomEnd).background(colors.primary, CircleShape).padding(horizontal = 5.dp, vertical = 1.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface)) {
                    state.details.forEachIndexed { i, d ->
                        if (i > 0) HorizontalDivider(Modifier.padding(start = 16.dp), color = colors.outlineVariant)
                        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(d.label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                                Text(d.value, style = MaterialTheme.typography.bodyLarge)
                            }
                            TextButton(onClick = { onEvent(CheckoutReviewEvent.EditDetail(d.id)) }) { Text(strings.edit) }
                        }
                    }
                }
            }
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(16.dp)) {
                state.lines.forEach { (l, v) -> PriceRow(l, v, valueColor = if (v.startsWith("−") || v == "Free") colors.primary else Color.Unspecified) }
                if (state.notice != null) {
                    Text(state.notice, style = MaterialTheme.typography.bodySmall, color = colors.tertiary, modifier = Modifier.padding(top = 4.dp))
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colors.outlineVariant)
                PriceRow(if (changed) strings.newTotal else strings.toPay, state.total, strong = true)
                if (state.savings != null && !changed) {
                    Text(
                        strings.save(state.savings),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp).background(colors.primary.copy(alpha = 0.08f), MaterialTheme.shapes.small).padding(8.dp),
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
        Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = { onEvent(CheckoutReviewEvent.PlaceOrder) },
                    enabled = !state.isPlacing && !blocked,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    if (state.isPlacing) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                    else Text(if (changed) strings.accept(state.total) else strings.placeOrder(state.total))
                }
                if (!changed) {
                    TextButton(onClick = { onEvent(CheckoutReviewEvent.OpenTerms) }) {
                        Text(strings.terms, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangedItem(item: CheckoutReviewItem, onEvent: (CheckoutReviewEvent) -> Unit, strings: CheckoutReviewStrings) {
    val colors = MaterialTheme.colorScheme
    val out = item.change == CheckoutReviewChange.OutOfStock
    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.surface)
            .border(1.dp, if (item.change != null) colors.tertiary.copy(alpha = 0.6f) else colors.outlineVariant, MaterialTheme.shapes.large)
            .padding(12.dp),
    ) {
        Box(Modifier.alphaIf(out)) { ImageSlot(item.image, Modifier.size(64.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                if (item.quantity > 1) "${item.name} × ${item.quantity}" else item.name,
                style = MaterialTheme.typography.titleSmall,
                color = if (out) colors.onSurfaceVariant else colors.onSurface,
            )
            when (val c = item.change) {
                is CheckoutReviewChange.PriceUp -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(c.now, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Text(c.was, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, textDecoration = TextDecoration.LineThrough)
                    }
                    Text(c.note, style = MaterialTheme.typography.labelSmall, color = colors.tertiary, fontWeight = FontWeight.Bold)
                }
                CheckoutReviewChange.OutOfStock -> {
                    Text(strings.outOfStock, style = MaterialTheme.typography.labelSmall, color = colors.error, fontWeight = FontWeight.Bold)
                    Row {
                        TextButton(onClick = { onEvent(CheckoutReviewEvent.RemoveItem(item.id)) }, contentPadding = PaddingValues(horizontal = 0.dp)) { Text(strings.remove) }
                        Spacer(Modifier.width(12.dp))
                        TextButton(onClick = { onEvent(CheckoutReviewEvent.FindSimilar(item.id)) }, contentPadding = PaddingValues(horizontal = 0.dp)) { Text(strings.similar) }
                    }
                }
                null -> Unit
            }
        }
    }
}

private fun Modifier.alphaIf(dim: Boolean): Modifier = if (dim) this.then(Modifier.alpha(0.5f)) else this

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
private val WarningIcon = vectorIcon("Warning", "M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z")

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
internal fun CheckoutReviewPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutReview(
            state = CheckoutReviewState(
                items = listOf(CheckoutReviewItem("1", "[Product]", 1), CheckoutReviewItem("2", "[Product]", 2)),
                details = listOf(
                    CheckoutReviewDetail("address", "Deliver to", "Home · [Area], [City]"),
                    CheckoutReviewDetail("slot", "Delivery", "Sun 11 · 11 am – 2 pm"),
                    CheckoutReviewDetail("payment", "Payment", "UPI · [App 1]"),
                ),
                lines = listOf("Items" to "₹2,397", "Coupon SAVE150" to "−₹150", "Delivery" to "Free"),
                total = "₹2,247",
                savings = "₹150",
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Changed at review")
@Composable
internal fun CheckoutReviewChangedPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutReview(
            state = CheckoutReviewState(
                changesSummary = "2 things changed since you added them",
                items = listOf(
                    CheckoutReviewItem("1", "[Product name]", 1, change = CheckoutReviewChange.PriceUp("₹1,299", "₹1,349", "PRICE WENT UP ₹50")),
                    CheckoutReviewItem("2", "[Product name]", 2, change = CheckoutReviewChange.OutOfStock),
                ),
                lines = listOf("Items (1)" to "₹1,349", "Delivery" to "₹40"),
                notice = "Free delivery no longer applies below ₹1,499.",
                total = "₹1,389",
            ),
            onEvent = {},
        )
    }
}
