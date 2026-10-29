package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Checkout · Order success
 * Confirmation that says what happens next: where the receipt went, the
 * order ID (copyable), when it arrives and how it was paid. Then track or
 * keep shopping.
 *
 * Copy with LocalClipboard on CopyOrderId and show a short confirmation.
 */

data class CheckoutSuccessState(
    val email: String = "name@example.com",
    val orderId: String = "#[ORDER-ID]",
    val arrival: String = "Arriving Sun 11, 11 am – 2 pm",
    val paid: String = "Paid ₹2,247 by UPI",
)

sealed interface CheckoutSuccessEvent {
    data object CopyOrderId : CheckoutSuccessEvent
    data object Track : CheckoutSuccessEvent
    data object ContinueShopping : CheckoutSuccessEvent
}

data class CheckoutSuccessStrings(
    val title: String = "Order placed",
    val sent: (String) -> String = { "We've sent the details to $it." },
    val orderId: String = "Order ID",
    val copy: String = "Copy order ID",
    val track: String = "Track order",
    val continueShopping: String = "Continue shopping",
)

@Composable
fun CheckoutSuccess(
    state: CheckoutSuccessState,
    onEvent: (CheckoutSuccessEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutSuccessStrings = CheckoutSuccessStrings(),
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxSize().background(colors.surface).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Box(Modifier.size(120.dp).background(colors.primaryContainer.copy(alpha = 0.5f), CircleShape), contentAlignment = Alignment.Center) {
            Box(Modifier.size(80.dp).background(colors.primary, CircleShape), contentAlignment = Alignment.Center) {
                Icon(CheckIcon, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(44.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Spacer(Modifier.height(6.dp))
        Text(strings.sent(state.email), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow)) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(strings.orderId, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    Text(state.orderId, style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
                }
                IconButton(onClick = { onEvent(CheckoutSuccessEvent.CopyOrderId) }) { Icon(CopyIcon, contentDescription = strings.copy) }
            }
            HorizontalDivider(color = colors.outlineVariant)
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).background(colors.primary.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(TruckIcon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(state.arrival, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(state.paid, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.weight(1f, fill = false).heightIn(min = 32.dp))
        Button(onClick = { onEvent(CheckoutSuccessEvent.Track) }, modifier = Modifier.fillMaxWidth().padding(top = 32.dp).height(52.dp)) { Text(strings.track) }
        TextButton(onClick = { onEvent(CheckoutSuccessEvent.ContinueShopping) }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) { Text(strings.continueShopping) }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val CheckIcon = vectorIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")
private val TruckIcon = vectorIcon(
    "Truck",
    "M20 8h-3V4H3c-1.1 0-2 .9-2 2v11h2c0 1.66 1.34 3 3 3s3-1.34 3-3h6c0 1.66 1.34 3 3 3s3-1.34 3-3h2v-5l-3-4zM6 18.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm13.5-9l1.96 2.5H17V9.5h2.5zm-1.5 9c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z",
)
private val CopyIcon = vectorIcon(
    "Copy",
    "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z",
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

@Preview(showBackground = true)
@Composable
internal fun CheckoutSuccessPreview() {
    MaterialTheme(colorScheme = PreviewColors) { CheckoutSuccess(state = CheckoutSuccessState(), onEvent = {}) }
}
