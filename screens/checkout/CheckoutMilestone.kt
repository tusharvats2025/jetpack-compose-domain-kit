package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Checkout · Hire and fund (freelance)
 * Fund the first milestone of a fixed-price project held in escrow. Shows
 * all milestones, what gets paid now, the service fee, and how the money
 * is protected until the client approves the work.
 *
 * Escrow and release are server-side; this screen only starts funding.
 * The design uses IBM Plex Sans.
 */

data class CheckoutMilestoneItem(val title: String, val note: String, val amount: String, val payNow: Boolean)

data class CheckoutMilestoneState(
    val freelancer: String = "[Freelancer name]",
    val initials: String = "AB",
    val project: String = "[Project title] · Fixed price",
    val milestones: List<CheckoutMilestoneItem> = emptyList(),
    val lines: List<Pair<String, String>> = emptyList(),
    val payNow: String = "",
    val isPaying: Boolean = false,
)

sealed interface CheckoutMilestoneEvent {
    data object Back : CheckoutMilestoneEvent
    data object Fund : CheckoutMilestoneEvent
}

data class CheckoutMilestoneStrings(
    val back: String = "Back",
    val title: String = "Hire and fund",
    val milestones: String = "MILESTONES",
    val protection: String = "Held safely until you approve the work. Not satisfied? Request changes or open a dispute.",
    val payNow: String = "Pay now",
    val fund: (String) -> String = { "Fund milestone 1 · $it" },
)

@Composable
fun CheckoutMilestone(
    state: CheckoutMilestoneState,
    onEvent: (CheckoutMilestoneEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutMilestoneStrings = CheckoutMilestoneStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    avatar: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(10.dp)
    Column(modifier.fillMaxSize().background(colors.surfaceContainerLow).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(CheckoutMilestoneEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth().clip(shape).background(colors.surface).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(state.initials, avatar, 44.dp, accent, onAccent)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(state.freelancer, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(state.project, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            Text(strings.milestones, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.2.sp, color = colors.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, top = 4.dp).semantics { heading() })
            Column(Modifier.fillMaxWidth().clip(shape).background(colors.surface)) {
                state.milestones.forEachIndexed { i, m ->
                    if (i > 0) HorizontalDivider(Modifier.padding(start = 56.dp), color = colors.outlineVariant)
                    Row(
                        Modifier.fillMaxWidth().background(if (m.payNow) accent.copy(alpha = 0.06f) else Color.Transparent).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier.size(28.dp).background(if (m.payNow) accent else colors.surfaceContainerHigh, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Text("${i + 1}", style = MaterialTheme.typography.labelLarge, color = if (m.payNow) onAccent else colors.onSurfaceVariant) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(m.title, style = MaterialTheme.typography.bodyLarge, fontWeight = if (m.payNow) FontWeight.SemiBold else FontWeight.Normal)
                            Text(m.note, style = MaterialTheme.typography.bodySmall, color = if (m.payNow) accent else colors.onSurfaceVariant)
                        }
                        Text(m.amount, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().clip(shape).background(colors.secondaryContainer).padding(14.dp), verticalAlignment = Alignment.Top) {
                Icon(ShieldIcon, contentDescription = null, tint = colors.onSecondaryContainer, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(strings.protection, style = MaterialTheme.typography.bodySmall, color = colors.onSecondaryContainer)
            }
            Column(Modifier.fillMaxWidth().clip(shape).background(colors.surface).padding(16.dp)) {
                state.lines.forEach { (l, v) -> PriceRow(l, v) }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = colors.outlineVariant)
                PriceRow(strings.payNow, state.payNow, strong = true)
            }
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = { onEvent(CheckoutMilestoneEvent.Fund) },
            enabled = !state.isPaying,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
        ) {
            if (state.isPaying) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
            else Text(strings.fund(state.payNow), fontWeight = FontWeight.SemiBold)
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

/** Your picture, or initials on a tinted circle. */
@Composable
private fun Avatar(initials: String, image: (@Composable () -> Unit)?, size: Dp, container: Color, content: Color) {
    Box(Modifier.size(size).clip(CircleShape).background(container), contentAlignment = Alignment.Center) {
        if (image != null) image()
        else Text(initials, color = content, fontWeight = FontWeight.SemiBold, fontSize = (size.value * 0.36f).sp)
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
private val ShieldIcon = vectorIcon(
    "Shield",
    "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z",
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
internal fun CheckoutMilestonePreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutMilestone(
            state = CheckoutMilestoneState(
                milestones = listOf(
                    CheckoutMilestoneItem("Wireframes", "Due in 7 days · Fund now", "₹8,000", payNow = true),
                    CheckoutMilestoneItem("Visual design", "Funded after milestone 1", "₹12,000", payNow = false),
                    CheckoutMilestoneItem("Handoff", "Funded after milestone 2", "₹5,000", payNow = false),
                ),
                lines = listOf("Milestone 1" to "₹8,000", "Service fee (5%)" to "₹400"),
                payNow = "₹8,400",
            ),
            onEvent = {},
            accent = Color(0xFF2B45C4),
        )
    }
}
