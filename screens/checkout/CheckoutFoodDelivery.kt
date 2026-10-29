package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Checkout · Food delivery
 * Restaurant cart with veg/non-veg marks, a tip for the delivery partner
 * (stated to go to them in full), delivery instructions and the bill, with
 * a sticky pay bar. Food-app conventions for India.
 *
 * accent defaults to MaterialTheme.colorScheme.primary. The design uses
 * Outfit; set it in your Typography to match.
 */

data class CheckoutDish(val id: String, val name: String, val quantity: Int, val price: String, val isVeg: Boolean)

data class CheckoutFoodDeliveryState(
    val restaurant: String = "[Restaurant name]",
    val subtitle: String = "Delivery in 30–35 min · Home",
    val dishes: List<CheckoutDish> = emptyList(),
    val tips: List<String> = listOf("₹20", "₹30", "₹50"),
    val selectedTip: Int? = null,
    val instructions: List<String> = listOf("Leave at door", "Don't ring the bell", "Avoid calling"),
    val selectedInstructions: Set<Int> = emptySet(),
    val bill: List<Pair<String, String>> = emptyList(),
    val total: String = "",
    val isPaying: Boolean = false,
)

sealed interface CheckoutFoodDeliveryEvent {
    data object Back : CheckoutFoodDeliveryEvent
    data object AddMore : CheckoutFoodDeliveryEvent
    data class SelectTip(val index: Int?) : CheckoutFoodDeliveryEvent
    data object CustomTip : CheckoutFoodDeliveryEvent
    data class ToggleInstruction(val index: Int) : CheckoutFoodDeliveryEvent
    data object ViewBill : CheckoutFoodDeliveryEvent
    data object Pay : CheckoutFoodDeliveryEvent
}

data class CheckoutFoodDeliveryStrings(
    val back: String = "Back",
    val veg: String = "Vegetarian",
    val nonVeg: String = "Non-vegetarian",
    val addMore: String = "+ Add more items",
    val tipTitle: String = "Tip your delivery partner",
    val tipBody: String = "All of it goes to them.",
    val other: String = "Other",
    val instructions: String = "Delivery instructions",
    val viewBill: String = "View bill",
    val pay: String = "Pay with UPI",
)

@OptIn(ExperimentalLayoutApi::class) // FlowRow on older Compose versions
@Composable
fun CheckoutFoodDelivery(
    state: CheckoutFoodDeliveryState,
    onEvent: (CheckoutFoodDeliveryEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutFoodDeliveryStrings = CheckoutFoodDeliveryStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val colors = MaterialTheme.colorScheme
    val chipColors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent.copy(alpha = 0.14f), selectedLabelColor = accent)
    Column(modifier.fillMaxSize().background(colors.surfaceContainerLowest).safeDrawingPadding()) {
        TopBar(state.restaurant, strings.back, { onEvent(CheckoutFoodDeliveryEvent.Back) })
        Text(state.subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, modifier = Modifier.padding(start = 52.dp, bottom = 8.dp).offset(y = (-8).dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.dishes.forEach { d ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        VegMark(d.isVeg, if (d.isVeg) strings.veg else strings.nonVeg)
                        Spacer(Modifier.width(10.dp))
                        Text("${d.name} × ${d.quantity}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(d.price, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                }
                TextButton(onClick = { onEvent(CheckoutFoodDeliveryEvent.AddMore) }, contentPadding = PaddingValues(0.dp)) { Text(strings.addMore, color = accent) }
            }
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(16.dp)) {
                Text(strings.tipTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(strings.tipBody, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.tips.forEachIndexed { i, t ->
                        FilterChip(
                            selected = state.selectedTip == i,
                            onClick = { onEvent(CheckoutFoodDeliveryEvent.SelectTip(if (state.selectedTip == i) null else i)) },
                            label = { Text(t) },
                            colors = chipColors,
                        )
                    }
                    FilterChip(selected = false, onClick = { onEvent(CheckoutFoodDeliveryEvent.CustomTip) }, label = { Text(strings.other) })
                }
            }
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(16.dp)) {
                Text(strings.instructions, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.instructions.forEachIndexed { i, s ->
                        FilterChip(
                            selected = i in state.selectedInstructions,
                            onClick = { onEvent(CheckoutFoodDeliveryEvent.ToggleInstruction(i)) },
                            label = { Text(s) },
                            colors = chipColors,
                        )
                    }
                }
            }
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(16.dp)) {
                state.bill.forEach { (l, v) -> PriceRow(l, v) }
            }
            Spacer(Modifier.height(8.dp))
        }
        Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(state.total, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        strings.viewBill,
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        modifier = Modifier.clickable(role = Role.Button) { onEvent(CheckoutFoodDeliveryEvent.ViewBill) },
                    )
                }
                Button(
                    onClick = { onEvent(CheckoutFoodDeliveryEvent.Pay) },
                    enabled = !state.isPaying,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
                    modifier = Modifier.height(52.dp),
                ) {
                    if (state.isPaying) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
                    else Text(strings.pay, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** Indian food label: green square-dot for veg, red/brown triangle for non-veg. */
@Composable
private fun VegMark(isVeg: Boolean, label: String) {
    val c = if (isVeg) Color(0xFF1B8A3A) else Color(0xFFA0301A)
    Box(
        Modifier.size(16.dp).border(1.5.dp, c, RoundedCornerShape(3.dp)).semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(8.dp).background(c, if (isVeg) CircleShape else RoundedCornerShape(1.dp)))
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

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)

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
internal fun CheckoutFoodDeliveryPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutFoodDelivery(
            state = CheckoutFoodDeliveryState(
                dishes = listOf(CheckoutDish("1", "[Dish name]", 1, "₹280", true), CheckoutDish("2", "[Dish name]", 2, "₹520", false)),
                selectedTip = 1,
                selectedInstructions = setOf(0),
                bill = listOf("Item total" to "₹800", "Delivery fee" to "₹35", "Taxes and charges" to "₹52", "Tip" to "₹30"),
                total = "₹917",
            ),
            onEvent = {},
            accent = Color(0xFFB93A0A),
            onAccent = Color.White,
        )
    }
}
