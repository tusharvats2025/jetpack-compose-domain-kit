package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Checkout · Cart
 * Line items with quantity steppers, a coupon field, the price breakdown
 * and a sticky total with Checkout. When the cart is empty it shows an
 * empty state with a way back to shopping and to saved-for-later items.
 *
 * Amounts are preformatted strings, so you control currency and locale.
 * Removing happens when quantity goes below 1; confirm with an Undo
 * snackbar on your side.
 */

data class CheckoutCartItem(
    val id: String,
    val name: String,
    val variant: String,
    val price: String,
    val quantity: Int,
    val maxQuantity: Int = 10,
    val image: (@Composable () -> Unit)? = null,
)

data class CheckoutCartLine(val label: String, val value: String, val highlight: Boolean = false)

data class CheckoutCartState(
    val items: List<CheckoutCartItem> = emptyList(),
    val coupon: String = "",
    val couponError: String? = null,
    val couponApplied: Boolean = false,
    val lines: List<CheckoutCartLine> = emptyList(),
    val total: String = "",
    val savedForLater: Int = 0,
)

sealed interface CheckoutCartEvent {
    data object Back : CheckoutCartEvent
    data class ChangeQuantity(val id: String, val quantity: Int) : CheckoutCartEvent
    data class CouponChanged(val value: String) : CheckoutCartEvent
    data object ApplyCoupon : CheckoutCartEvent
    data object Checkout : CheckoutCartEvent
    data object StartShopping : CheckoutCartEvent
    data object OpenSaved : CheckoutCartEvent
}

data class CheckoutCartStrings(
    val back: String = "Back",
    val title: String = "Cart",
    val coupon: String = "Coupon code",
    val apply: String = "Apply",
    val applied: String = "Applied",
    val decrease: (String) -> String = { "Decrease quantity of $it" },
    val increase: (String) -> String = { "Increase quantity of $it" },
    val remove: (String) -> String = { "Remove $it" },
    val total: String = "Total",
    val checkout: String = "Checkout",
    val emptyTitle: String = "Your cart is empty",
    val emptyBody: String = "Things you add will show up here.",
    val startShopping: String = "Start shopping",
    val saved: String = "Saved for later",
    val savedCount: (Int) -> String = { if (it == 1) "1 item" else "$it items" },
)

@Composable
fun CheckoutCart(
    state: CheckoutCartState,
    onEvent: (CheckoutCartEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutCartStrings = CheckoutCartStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val count = state.items.sumOf { it.quantity }
    Column(modifier.fillMaxSize().background(colors.surfaceContainerLowest).safeDrawingPadding()) {
        TopBar(if (count > 0) "${strings.title} ($count)" else strings.title, strings.back, { onEvent(CheckoutCartEvent.Back) })
        if (state.items.isEmpty()) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(Modifier.size(104.dp).background(colors.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(BagIcon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(48.dp))
                }
                Spacer(Modifier.height(24.dp))
                Text(strings.emptyTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(strings.emptyBody, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { onEvent(CheckoutCartEvent.StartShopping) }, modifier = Modifier.height(48.dp)) { Text(strings.startShopping) }
            }
            if (state.savedForLater > 0) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(colors.surfaceContainerLow)
                        .clickable(role = Role.Button) { onEvent(CheckoutCartEvent.OpenSaved) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(strings.saved, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(strings.savedCount(state.savedForLater), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    Icon(ChevronIcon, contentDescription = null, tint = colors.onSurfaceVariant)
                }
            }
            return@Column
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.items.forEach { item ->
                Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(12.dp)) {
                    ImageSlot(item.image, Modifier.size(72.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(item.variant, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.price, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Stepper(
                                quantity = item.quantity,
                                max = item.maxQuantity,
                                decreaseLabel = if (item.quantity <= 1) strings.remove(item.name) else strings.decrease(item.name),
                                increaseLabel = strings.increase(item.name),
                                onChange = { onEvent(CheckoutCartEvent.ChangeQuantity(item.id, it)) },
                            )
                        }
                    }
                }
            }
            OutlinedTextField(
                value = state.coupon,
                onValueChange = { onEvent(CheckoutCartEvent.CouponChanged(it.uppercase())) },
                placeholder = { Text(strings.coupon) },
                leadingIcon = { Icon(TagIcon, contentDescription = null) },
                trailingIcon = {
                    TextButton(onClick = { onEvent(CheckoutCartEvent.ApplyCoupon) }, enabled = state.coupon.isNotBlank() && !state.couponApplied) {
                        Text(if (state.couponApplied) strings.applied else strings.apply)
                    }
                },
                isError = state.couponError != null,
                supportingText = state.couponError?.let { { Text(it) } },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = colors.surface, focusedContainerColor = colors.surface),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onEvent(CheckoutCartEvent.ApplyCoupon) }),
                modifier = Modifier.keepAboveKeyboard().fillMaxWidth(),
            )
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(16.dp)) {
                state.lines.forEach { l -> PriceRow(l.label, l.value, valueColor = if (l.highlight) colors.primary else Color.Unspecified) }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colors.outlineVariant)
                PriceRow(strings.total, state.total, strong = true)
            }
            Spacer(Modifier.height(8.dp))
        }
        BottomBar(strings.total, state.total, strings.checkout) { onEvent(CheckoutCartEvent.Checkout) }
    }
}

@Composable
private fun Stepper(quantity: Int, max: Int, decreaseLabel: String, increaseLabel: String, onChange: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.border(1.dp, colors.outlineVariant, CircleShape), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onChange(quantity - 1) }, modifier = Modifier.size(36.dp)) {
            Icon(if (quantity <= 1) DeleteIcon else RemoveIcon, contentDescription = decreaseLabel, modifier = Modifier.size(18.dp))
        }
        Text("$quantity", style = MaterialTheme.typography.titleSmall, modifier = Modifier.widthIn(min = 20.dp), textAlign = TextAlign.Center)
        IconButton(onClick = { onChange(quantity + 1) }, enabled = quantity < max, modifier = Modifier.size(36.dp)) {
            Icon(AddIcon, contentDescription = increaseLabel, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun BottomBar(label: String, total: String, action: String, onClick: () -> Unit) {
    Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(total, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Button(onClick = onClick, modifier = Modifier.height(52.dp)) { Text(action) }
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
private val ChevronIcon = vectorIcon("Chevron", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z", autoMirror = true)
private val DeleteIcon = vectorIcon("Delete", "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z")
private val BagIcon = vectorIcon(
    "Bag",
    "M18 6h-2c0-2.21-1.79-4-4-4S8 3.79 8 6H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-6-2c1.1 0 2 .9 2 2h-4c0-1.1.9-2 2-2zm6 16H6V8h2v2c0 .55.45 1 1 1s1-.45 1-1V8h4v2c0 .55.45 1 1 1s1-.45 1-1V8h2v12z",
)
private val AddIcon = vectorIcon("Add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")
private val RemoveIcon = vectorIcon("Remove", "M19 13H5v-2h14v2z")
private val TagIcon = vectorIcon(
    "Tag",
    "M21.41 11.58l-9-9C12.05 2.22 11.55 2 11 2H4c-1.1 0-2 .9-2 2v7c0 .55.22 1.05.59 1.42l9 9c.36.36.86.58 1.41.58.55 0 1.05-.22 1.41-.59l7-7c.37-.36.59-.86.59-1.41 0-.55-.23-1.06-.59-1.42zM5.5 7C4.67 7 4 6.33 4 5.5S4.67 4 5.5 4 7 4.67 7 5.5 6.33 7 5.5 7z",
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
internal fun CheckoutCartPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutCart(
            state = CheckoutCartState(
                items = listOf(
                    CheckoutCartItem("1", "[Product name]", "Size M · Navy", "₹1,299", 1),
                    CheckoutCartItem("2", "[Product name]", "One size", "₹549", 2),
                ),
                lines = listOf(
                    CheckoutCartLine("Subtotal", "₹2,397"),
                    CheckoutCartLine("Discount", "−₹150", highlight = true),
                    CheckoutCartLine("Delivery", "Free", highlight = true),
                ),
                total = "₹2,247",
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
internal fun CheckoutCartEmptyPreview() {
    MaterialTheme(colorScheme = PreviewColors) { CheckoutCart(state = CheckoutCartState(savedForLater = 2), onEvent = {}) }
}
