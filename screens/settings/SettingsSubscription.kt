package com.yourapp.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
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
import androidx.compose.ui.unit.sp

/*
 * Settings · Subscription
 * Current plan with renewal date, a usage meter, management links, and a
 * cancel action that says what happens next.
 *
 * Manage Play subscriptions through Play Billing; "Cancel" can deep-link
 * to https://play.google.com/store/account/subscriptions?sku=…&package=….
 */

data class SettingsSubscriptionState(
    val planName: String = "Pro · Yearly",
    val isActive: Boolean = true,
    val renewal: String = "Renews on 12 Mar 2027 for ₹1,999",
    val usageLabel: String = "Storage",
    val usageText: String = "38 of 100 GB",
    val usage: Float = 0.38f,
    val paymentMethod: String? = "UPI · name@bank",
)

sealed interface SettingsSubscriptionEvent {
    data object Back : SettingsSubscriptionEvent
    data object ChangePlan : SettingsSubscriptionEvent
    data object PaymentMethod : SettingsSubscriptionEvent
    data object BillingHistory : SettingsSubscriptionEvent
    data object Restore : SettingsSubscriptionEvent
    data object Cancel : SettingsSubscriptionEvent
}

data class SettingsSubscriptionStrings(
    val back: String = "Back",
    val title: String = "Subscription",
    val current: String = "Current plan",
    val active: String = "ACTIVE",
    val inactive: String = "ENDED",
    val manage: String = "Manage",
    val changePlan: String = "Change plan",
    val paymentMethod: String = "Payment method",
    val billing: String = "Billing history",
    val restore: String = "Restore purchase",
    val cancel: String = "Cancel subscription",
    val cancelNote: String = "You keep Pro until the end of the current period.",
)

@Composable
fun SettingsSubscription(
    state: SettingsSubscriptionState,
    onEvent: (SettingsSubscriptionEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: SettingsSubscriptionStrings = SettingsSubscriptionStrings(),
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(SettingsSubscriptionEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Brush.linearGradient(listOf(colors.primary, colors.tertiary)))
                    .padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.current, style = MaterialTheme.typography.labelLarge, color = colors.onPrimary.copy(alpha = 0.85f), modifier = Modifier.weight(1f))
                    Text(
                        if (state.isActive) strings.active else strings.inactive,
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 1.sp,
                        color = colors.onPrimary,
                        modifier = Modifier.background(colors.onPrimary.copy(alpha = 0.18f), CircleShape).padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(state.planName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colors.onPrimary)
                Spacer(Modifier.height(4.dp))
                Text(state.renewal, style = MaterialTheme.typography.bodyMedium, color = colors.onPrimary.copy(alpha = 0.85f))
            }
            Spacer(Modifier.height(16.dp))
            Group {
                Column(Modifier.padding(16.dp)) {
                    Row {
                        Text(state.usageLabel, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(state.usageText, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { state.usage.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        strokeCap = StrokeCap.Round,
                    )
                }
            }
            SectionLabel(strings.manage)
            Group {
                LinkRow(strings.changePlan, { onEvent(SettingsSubscriptionEvent.ChangePlan) })
                RowDivider()
                LinkRow(strings.paymentMethod, { onEvent(SettingsSubscriptionEvent.PaymentMethod) }, subtitle = state.paymentMethod)
                RowDivider()
                LinkRow(strings.billing, { onEvent(SettingsSubscriptionEvent.BillingHistory) })
                RowDivider()
                LinkRow(strings.restore, { onEvent(SettingsSubscriptionEvent.Restore) })
            }
            if (state.isActive) {
                TextButton(
                    onClick = { onEvent(SettingsSubscriptionEvent.Cancel) },
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.error),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                ) { Text(strings.cancel) }
                Text(
                    strings.cancelNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
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
private fun SectionLabel(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp).semantics { heading() },
    )
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
internal fun SettingsSubscriptionPreview() {
    MaterialTheme(colorScheme = PreviewColors) { SettingsSubscription(state = SettingsSubscriptionState(), onEvent = {}) }
}
