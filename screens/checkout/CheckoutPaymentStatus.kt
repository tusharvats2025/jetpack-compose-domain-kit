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
 * Checkout · Payment status
 * The two moments between "Pay" and success:
 *  - Pending: a UPI collect request is waiting for approval in the
 *    user's UPI app, with steps and a countdown.
 *  - Failed: the payment didn't go through; reassures that nothing was
 *    charged (or that refunds are automatic), and offers retry or another
 *    method.
 *
 * Poll or listen for the result in your ViewModel; this screen updates
 * from state. Tick expiresIn down yourself.
 */

enum class CheckoutPaymentPhase { Pending, Failed }

data class CheckoutPaymentStatusState(
    val phase: CheckoutPaymentPhase = CheckoutPaymentPhase.Pending,
    val amount: String = "₹2,247",
    val upiApp: String = "[App 1]",
    val upiId: String = "name@bank",
    val expiresIn: String = "04:52",
    val reason: String = "Your bank declined the payment. You haven't been charged.",
    val refundDays: String = "5–7",
    val reference: String? = null,
)

sealed interface CheckoutPaymentStatusEvent {
    data object OpenUpiApp : CheckoutPaymentStatusEvent
    data object Cancel : CheckoutPaymentStatusEvent
    data object Retry : CheckoutPaymentStatusEvent
    data object OtherMethod : CheckoutPaymentStatusEvent
    data object Help : CheckoutPaymentStatusEvent
}

data class CheckoutPaymentStatusStrings(
    val approve: (String, String) -> String = { amount, _ -> "Approve $amount in your UPI app" },
    val sentTo: (String) -> String = { "We sent a request to $it. This screen updates by itself once you approve." },
    val step1: (String) -> String = { "Open $it and find the request" },
    val step2: String = "Check the amount and enter your UPI PIN",
    val expires: String = "Request expires in",
    val open: (String) -> String = { "Open $it" },
    val cancel: String = "Cancel and pay another way",
    val failedTitle: String = "Payment didn't go through",
    val saved: String = "Your cart and address are saved.",
    val refund: (String) -> String = { "If money left your account, it's refunded automatically within $it days." },
    val reference: (String) -> String = { "Reference: $it" },
    val retry: String = "Try again",
    val other: String = "Use another payment method",
    val help: String = "Get help",
    val waiting: String = "Waiting for payment approval",
)

@Composable
fun CheckoutPaymentStatus(
    state: CheckoutPaymentStatusState,
    onEvent: (CheckoutPaymentStatusEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutPaymentStatusStrings = CheckoutPaymentStatusStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val pending = state.phase == CheckoutPaymentPhase.Pending
    Column(
        modifier.fillMaxSize().background(colors.surface).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))
        Box(Modifier.size(112.dp).semantics { if (pending) contentDescription = strings.waiting }, contentAlignment = Alignment.Center) {
            if (pending) {
                CircularProgressIndicator(Modifier.fillMaxSize(), strokeWidth = 4.dp, trackColor = colors.surfaceContainerHighest)
                Icon(HourglassIcon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(44.dp))
            } else {
                Box(Modifier.fillMaxSize().background(colors.errorContainer, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(ErrorIcon, contentDescription = null, tint = colors.onErrorContainer, modifier = Modifier.size(52.dp))
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            if (pending) strings.approve(state.amount, state.upiApp) else strings.failedTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (pending) strings.sentTo(state.upiId) else state.reason,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Column(
            Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (pending) {
                listOf(strings.step1(state.upiApp), strings.step2).forEachIndexed { i, s ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(26.dp).background(colors.primaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = colors.onPrimaryContainer)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(s, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                listOf(CheckIcon to strings.saved, RefundIcon to strings.refund(state.refundDays)).forEach { (icon, s) ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(s, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (pending) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(strings.expires, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                Spacer(Modifier.width(6.dp))
                Text(state.expiresIn, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        } else if (state.reference != null) {
            Text(strings.reference(state.reference), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, fontFamily = FontFamily.Monospace)
        }
        Spacer(Modifier.weight(1f, fill = false).heightIn(min = 24.dp))
        if (pending) {
            Button(onClick = { onEvent(CheckoutPaymentStatusEvent.OpenUpiApp) }, modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(52.dp)) { Text(strings.open(state.upiApp)) }
            TextButton(onClick = { onEvent(CheckoutPaymentStatusEvent.Cancel) }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) { Text(strings.cancel) }
        } else {
            Button(onClick = { onEvent(CheckoutPaymentStatusEvent.Retry) }, modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(52.dp)) { Text(strings.retry) }
            OutlinedButton(onClick = { onEvent(CheckoutPaymentStatusEvent.OtherMethod) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp)) { Text(strings.other) }
            TextButton(onClick = { onEvent(CheckoutPaymentStatusEvent.Help) }, modifier = Modifier.padding(top = 4.dp)) { Text(strings.help) }
        }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val CheckIcon = vectorIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")
private val RefundIcon = vectorIcon(
    "Refund",
    "M12.5 8c-2.65 0-5.05.99-6.9 2.6L2 7v9h9l-3.62-3.62c1.39-1.16 3.16-1.88 5.12-1.88 3.54 0 6.55 2.31 7.6 5.5l2.37-.78C21.08 11.03 17.15 8 12.5 8z", autoMirror = true,
)
private val ErrorIcon = vectorIcon("Error", "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z")
private val HourglassIcon = vectorIcon(
    "Hourglass",
    "M6 2v6h.01L6 8.01 10 12l-4 4 .01.01H6V22h12v-5.99h-.01L18 16l-4-4 4-3.99-.01-.01H18V2H6zm10 14.5V20H8v-3.5l4-4 4 4zm-4-5l-4-4V4h8v3.5l-4 4z",
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

@Preview(showBackground = true, name = "Pending")
@Composable
internal fun CheckoutPaymentStatusPendingPreview() {
    MaterialTheme(colorScheme = PreviewColors) { CheckoutPaymentStatus(state = CheckoutPaymentStatusState(), onEvent = {}) }
}

@Preview(showBackground = true, name = "Failed")
@Composable
internal fun CheckoutPaymentStatusFailedPreview() {
    MaterialTheme(colorScheme = PreviewColors) { CheckoutPaymentStatus(state = CheckoutPaymentStatusState(phase = CheckoutPaymentPhase.Failed, reference = "TXN-48213"), onEvent = {}) }
}
