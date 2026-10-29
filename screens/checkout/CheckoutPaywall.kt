package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Checkout · Subscription paywall
 * Benefits, plan choice with the yearly saving, a clear free-trial button
 * and the billing terms spelled out under it, plus Restore and Terms.
 * Play policy expects the price, period and trial terms to be this clear.
 *
 * Always dark with a bright accent. Get prices from Play Billing
 * (ProductDetails) rather than hard-coding them. The design uses Sora.
 */

data class CheckoutPlan(val id: String, val name: String, val price: String, val badge: String? = null)

data class CheckoutPaywallState(
    val appName: String = "[App name]",
    val benefits: List<String> = listOf("Unlimited projects", "Sync across devices", "No ads"),
    val plans: List<CheckoutPlan> = emptyList(),
    val selectedPlan: String? = null,
    val trialTerms: String = "Free for 7 days, then ₹2,999 per year. Cancel anytime before the trial ends and you won't be charged.",
    val ctaLabel: String = "Start 7-day free trial",
    val isPurchasing: Boolean = false,
)

sealed interface CheckoutPaywallEvent {
    data object Close : CheckoutPaywallEvent
    data class SelectPlan(val id: String) : CheckoutPaywallEvent
    data object Subscribe : CheckoutPaywallEvent
    data object Restore : CheckoutPaywallEvent
    data object Terms : CheckoutPaywallEvent
}

data class CheckoutPaywallStrings(
    val close: String = "Close",
    val title: (String) -> String = { "Unlock $it Pro" },
    val restore: String = "Restore purchase",
    val terms: String = "Terms",
)

data class CheckoutPaywallColors(
    val background: Color = Color(0xFF0E1113),
    val surface: Color = Color(0xFF1A1F23),
    val outline: Color = Color(0xFF3A4248),
    val text: Color = Color(0xFFF1F4F2),
    val muted: Color = Color(0xFFA9B2AE),
    val accent: Color = Color(0xFFC8F03C),
    val onAccent: Color = Color(0xFF0E1113),
)

@Composable
fun CheckoutPaywall(
    state: CheckoutPaywallState,
    onEvent: (CheckoutPaywallEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutPaywallStrings = CheckoutPaywallStrings(),
    colors: CheckoutPaywallColors = CheckoutPaywallColors(),
) {
    Column(
        modifier.fillMaxSize().background(colors.background).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
    ) {
        IconButton(onClick = { onEvent(CheckoutPaywallEvent.Close) }, modifier = Modifier.offset(x = (-12).dp).padding(top = 8.dp)) {
            Icon(CloseIcon, contentDescription = strings.close, tint = colors.muted)
        }
        Spacer(Modifier.height(24.dp))
        Text(strings.title(state.appName), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = colors.text)
        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            state.benefits.forEach { b ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(24.dp).background(colors.accent, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(CheckIcon, contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(b, style = MaterialTheme.typography.bodyLarge, color = colors.text)
                }
            }
        }
        Spacer(Modifier.height(32.dp))
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.plans.forEach { p ->
                val selected = p.id == state.selectedPlan
                Box {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = if (p.badge != null) 10.dp else 0.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.surface)
                            .border(if (selected) 2.dp else 1.dp, if (selected) colors.accent else colors.outline, RoundedCornerShape(18.dp))
                            .selectable(selected = selected, role = Role.RadioButton) { onEvent(CheckoutPaywallEvent.SelectPlan(p.id)) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = colors.accent, unselectedColor = colors.muted))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(p.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = colors.text)
                            Text(p.price, style = MaterialTheme.typography.bodyMedium, color = colors.muted)
                        }
                    }
                    if (p.badge != null) {
                        Text(
                            p.badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.onAccent,
                            modifier = Modifier.align(Alignment.TopEnd).padding(end = 16.dp).background(colors.accent, CircleShape).padding(horizontal = 10.dp, vertical = 3.dp),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.weight(1f, fill = false).heightIn(min = 32.dp))
        Button(
            onClick = { onEvent(CheckoutPaywallEvent.Subscribe) },
            enabled = state.selectedPlan != null && !state.isPurchasing,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp).height(58.dp),
        ) {
            if (state.isPurchasing) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.onAccent)
            else Text(state.ctaLabel, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        Text(state.trialTerms, style = MaterialTheme.typography.bodySmall, color = colors.muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) {
            TextButton(onClick = { onEvent(CheckoutPaywallEvent.Restore) }) { Text(strings.restore, color = colors.text) }
            TextButton(onClick = { onEvent(CheckoutPaywallEvent.Terms) }) { Text(strings.terms, color = colors.text) }
        }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val CloseIcon = vectorIcon("Close", "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z")
private val CheckIcon = vectorIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")

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
internal fun CheckoutPaywallPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutPaywall(
            state = CheckoutPaywallState(
                plans = listOf(
                    CheckoutPlan("year", "Yearly", "₹2,999 / year · ₹250 / month", badge = "SAVE 40%"),
                    CheckoutPlan("month", "Monthly", "₹399 / month"),
                ),
                selectedPlan = "year",
            ),
            onEvent = {},
        )
    }
}
