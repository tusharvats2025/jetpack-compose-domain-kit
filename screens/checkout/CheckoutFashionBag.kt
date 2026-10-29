package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Checkout · Fashion bag
 * A fashion bag: brand above product, size and quantity as tappable
 * selectors, low-stock warnings, move to wishlist, and returns and
 * delivery promises above the total. Square, letter-spaced, editorial.
 *
 * Opening size/quantity pickers is yours (a bottom sheet). tone fills
 * empty image slots. The design pairs Bodoni Moda with Jost.
 */

data class CheckoutBagItem(
    val id: String,
    val brand: String,
    val name: String,
    val price: String,
    val size: String,
    val quantity: Int,
    val lowStock: String? = null,
    val image: (@Composable () -> Unit)? = null,
)

data class CheckoutFashionBagState(
    val items: List<CheckoutBagItem> = emptyList(),
    val returnsLine: String = "Free returns within 15 days",
    val deliveryLine: String = "Delivered by Tue, [date]",
    val total: String = "",
)

sealed interface CheckoutFashionBagEvent {
    data object Back : CheckoutFashionBagEvent
    data class ChangeSize(val id: String) : CheckoutFashionBagEvent
    data class ChangeQuantity(val id: String) : CheckoutFashionBagEvent
    data class MoveToWishlist(val id: String) : CheckoutFashionBagEvent
    data class Remove(val id: String) : CheckoutFashionBagEvent
    data object Checkout : CheckoutFashionBagEvent
}

data class CheckoutFashionBagStrings(
    val back: String = "Back",
    val title: String = "Your bag",
    val size: (String) -> String = { "Size $it" },
    val qty: (Int) -> String = { "Qty $it" },
    val changeSize: (String) -> String = { "Size $it, change size" },
    val changeQty: (Int) -> String = { "Quantity $it, change quantity" },
    val wishlist: String = "Move to wishlist",
    val remove: String = "Remove",
    val total: String = "Total",
    val checkout: String = "CHECKOUT",
)

@Composable
fun CheckoutFashionBag(
    state: CheckoutFashionBagState,
    onEvent: (CheckoutFashionBagEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutFashionBagStrings = CheckoutFashionBagStrings(),
    tone: Color = Color(0xFFCFC8C0),
    ink: Color = Color(0xFF111111),
    sansFont: FontFamily = FontFamily.Default,
) {
    val sans = TextStyle(fontFamily = sansFont, color = ink)
    val line = ink.copy(alpha = 0.15f)
    Column(modifier.fillMaxSize().background(Color.White).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(CheckoutFashionBagEvent.Back) }) { Icon(BackIcon, contentDescription = strings.back, tint = ink) }
            Text("${strings.title} (${state.items.size})", style = sans.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            state.items.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                    Box(Modifier.size(width = 96.dp, height = 128.dp).background(tone)) { item.image?.invoke() }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.brand, style = sans.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 2.sp))
                        Text(item.name, style = sans.copy(fontSize = 15.sp), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(item.price, style = sans.copy(fontSize = 15.sp, fontWeight = FontWeight.SemiBold), modifier = Modifier.padding(top = 4.dp))
                        if (item.lowStock != null) {
                            Text(item.lowStock, style = sans.copy(fontSize = 12.sp, color = Color(0xFFB3261E)), modifier = Modifier.padding(top = 2.dp))
                        }
                        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Selector(strings.size(item.size), strings.changeSize(item.size), line, sans) { onEvent(CheckoutFashionBagEvent.ChangeSize(item.id)) }
                            Selector(strings.qty(item.quantity), strings.changeQty(item.quantity), line, sans) { onEvent(CheckoutFashionBagEvent.ChangeQuantity(item.id)) }
                        }
                        Row(Modifier.padding(top = 4.dp)) {
                            TextButton(onClick = { onEvent(CheckoutFashionBagEvent.MoveToWishlist(item.id)) }, shape = RectangleShape, contentPadding = PaddingValues(horizontal = 0.dp)) {
                                Text(strings.wishlist, style = sans.copy(fontSize = 13.sp, textDecoration = TextDecoration.Underline))
                            }
                            Spacer(Modifier.width(16.dp))
                            TextButton(onClick = { onEvent(CheckoutFashionBagEvent.Remove(item.id)) }, shape = RectangleShape, contentPadding = PaddingValues(horizontal = 0.dp)) {
                                Text(strings.remove, style = sans.copy(fontSize = 13.sp, textDecoration = TextDecoration.Underline))
                            }
                        }
                    }
                }
                HorizontalDivider(color = line)
            }
            Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(RefundIcon to state.returnsLine, TruckIcon to state.deliveryLine).forEach { (icon, text) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, contentDescription = null, tint = ink, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(text, style = sans.copy(fontSize = 13.sp))
                    }
                }
            }
        }
        HorizontalDivider(color = line)
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(strings.total, style = sans.copy(fontSize = 12.sp, color = ink.copy(alpha = 0.6f)))
                Text(state.total, style = sans.copy(fontSize = 20.sp, fontWeight = FontWeight.SemiBold))
            }
            Button(
                onClick = { onEvent(CheckoutFashionBagEvent.Checkout) },
                enabled = state.items.isNotEmpty(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(containerColor = ink, contentColor = Color.White),
                modifier = Modifier.height(52.dp).widthIn(min = 160.dp),
            ) { Text(strings.checkout, style = sans.copy(fontSize = 14.sp, letterSpacing = 3.sp, color = Color.White)) }
        }
    }
}

@Composable
private fun Selector(label: String, description: String, line: Color, style: TextStyle, onClick: () -> Unit) {
    Row(
        Modifier
            .border(1.dp, line)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = style.copy(fontSize = 13.sp))
        Icon(ChevronIcon, contentDescription = null, modifier = Modifier.size(16.dp).rotate(90f))
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val ChevronIcon = vectorIcon("Chevron", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z", autoMirror = true)
private val TruckIcon = vectorIcon(
    "Truck",
    "M20 8h-3V4H3c-1.1 0-2 .9-2 2v11h2c0 1.66 1.34 3 3 3s3-1.34 3-3h6c0 1.66 1.34 3 3 3s3-1.34 3-3h2v-5l-3-4zM6 18.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm13.5-9l1.96 2.5H17V9.5h2.5zm-1.5 9c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z",
)
private val RefundIcon = vectorIcon(
    "Refund",
    "M12.5 8c-2.65 0-5.05.99-6.9 2.6L2 7v9h9l-3.62-3.62c1.39-1.16 3.16-1.88 5.12-1.88 3.54 0 6.55 2.31 7.6 5.5l2.37-.78C21.08 11.03 17.15 8 12.5 8z", autoMirror = true,
)

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
internal fun CheckoutFashionBagPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutFashionBag(
            state = CheckoutFashionBagState(
                items = listOf(
                    CheckoutBagItem("1", "[BRAND]", "[Product name]", "₹3,499", "M", 1),
                    CheckoutBagItem("2", "[BRAND]", "[Product name]", "₹2,199", "32", 1, lowStock = "Only 2 left in size 32"),
                ),
                total = "₹5,698",
            ),
            onEvent = {},
        )
    }
}
