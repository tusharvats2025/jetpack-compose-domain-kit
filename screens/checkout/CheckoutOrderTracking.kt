package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Checkout · Order tracking
 * A live map on top with ETA and on-time status, the delivery partner with
 * message and call, a vertical progress timeline and the order summary.
 *
 * The map is yours (Google Maps Compose with the rider's position).
 * Masked calling between buyer and partner is usually done by your
 * telephony provider; the screen only sends the event.
 */

data class CheckoutTrackStep(val label: String, val time: String?, val done: Boolean, val current: Boolean = false)

data class CheckoutOrderTrackingState(
    val eta: String = "12 min",
    val status: String = "ON TIME",
    val isLate: Boolean = false,
    val partnerName: String = "[Partner name]",
    val partnerInitials: String = "RK",
    val steps: List<CheckoutTrackStep> = emptyList(),
    val summary: String = "3 items · ₹2,247",
)

sealed interface CheckoutOrderTrackingEvent {
    data object Back : CheckoutOrderTrackingEvent
    data object MessagePartner : CheckoutOrderTrackingEvent
    data object CallPartner : CheckoutOrderTrackingEvent
    data object OrderDetails : CheckoutOrderTrackingEvent
}

data class CheckoutOrderTrackingStrings(
    val back: String = "Back",
    val arriving: String = "Arriving in",
    val partner: String = "Your delivery partner",
    val message: String = "Message delivery partner",
    val call: String = "Call delivery partner",
    val details: String = "Order details",
)

@Composable
fun CheckoutOrderTracking(
    state: CheckoutOrderTrackingState,
    onEvent: (CheckoutOrderTrackingEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutOrderTrackingStrings = CheckoutOrderTrackingStrings(),
    map: (@Composable BoxScope.() -> Unit)? = null,
    partnerPhoto: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface)) {
        Box(Modifier.fillMaxWidth().weight(0.38f).background(colors.surfaceContainerHigh)) {
            map?.invoke(this)
            FilledIconButton(
                onClick = { onEvent(CheckoutOrderTrackingEvent.Back) },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.surface, contentColor = colors.onSurface),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(12.dp),
            ) { Icon(BackIcon, contentDescription = strings.back) }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .weight(0.62f)
                .offset(y = (-20).dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(colors.surface)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite }) {
                Column(Modifier.weight(1f)) {
                    Text(strings.arriving, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                    Text(state.eta, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
                Text(
                    state.status,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (state.isLate) colors.error else Color(0xFF2E7D32),
                    modifier = Modifier.background((if (state.isLate) colors.error else Color(0xFF2E7D32)).copy(alpha = 0.12f), CircleShape).padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(state.partnerInitials, partnerPhoto, 44.dp, colors.secondaryContainer, colors.onSecondaryContainer)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(state.partnerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(strings.partner, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                FilledTonalIconButton(onClick = { onEvent(CheckoutOrderTrackingEvent.MessagePartner) }) { Icon(ChatIcon, contentDescription = strings.message, modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.width(4.dp))
                FilledIconButton(onClick = { onEvent(CheckoutOrderTrackingEvent.CallPartner) }) { Icon(PhoneIcon, contentDescription = strings.call, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.height(20.dp))
            state.steps.forEachIndexed { i, s ->
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Column(Modifier.width(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier.size(20.dp).then(
                                when {
                                    s.done -> Modifier.background(colors.primary, CircleShape)
                                    s.current -> Modifier.border(3.dp, colors.primary, CircleShape)
                                    else -> Modifier.border(2.dp, colors.outlineVariant, CircleShape)
                                },
                            ),
                            contentAlignment = Alignment.Center,
                        ) { if (s.done) Icon(CheckIcon, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(12.dp)) }
                        if (i < state.steps.lastIndex) {
                            Box(Modifier.width(2.dp).weight(1f).background(if (s.done) colors.primary else colors.outlineVariant))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Row(Modifier.weight(1f).padding(bottom = 18.dp)) {
                        Text(
                            s.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (s.current) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (s.done || s.current) colors.onSurface else colors.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        if (s.time != null) Text(s.time, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable(role = Role.Button) { onEvent(CheckoutOrderTrackingEvent.OrderDetails) }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(state.summary, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                Text(strings.details, style = MaterialTheme.typography.labelLarge, color = colors.primary)
                Icon(ChevronIcon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
            }
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
private val CheckIcon = vectorIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")
private val PhoneIcon = vectorIcon(
    "Phone",
    "M16 1H8C6.34 1 5 2.34 5 4v16c0 1.66 1.34 3 3 3h8c1.66 0 3-1.34 3-3V4c0-1.66-1.34-3-3-3zm-2 20h-4v-1h4v1zm3.25-3H6.75V4h10.5v14z",
)
private val ChatIcon = vectorIcon("Chat", "M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 9h12v2H6V9zm8 5H6v-2h8v2zm4-6H6V6h12v2z")

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
internal fun CheckoutOrderTrackingPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutOrderTracking(
            state = CheckoutOrderTrackingState(
                steps = listOf(
                    CheckoutTrackStep("Order placed", "10:02 am", done = true),
                    CheckoutTrackStep("Packed", "10:15 am", done = true),
                    CheckoutTrackStep("Out for delivery", "10:40 am", done = false, current = true),
                    CheckoutTrackStep("Delivered", null, done = false),
                ),
            ),
            onEvent = {},
        )
    }
}
