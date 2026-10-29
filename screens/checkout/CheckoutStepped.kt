package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Checkout · Stepped checkout
 * A progress stepper (Address, Delivery, Payment, Review) with the
 * delivery-slot step shown: pick a day, then a time slot. Full slots are
 * disabled with the reason in the label. Good for groceries and food.
 */

data class CheckoutDay(val id: String, val weekday: String, val date: String)
data class CheckoutSlot(val id: String, val label: String, val available: Boolean = true)

data class CheckoutSteppedState(
    val steps: List<String> = listOf("Address", "Delivery", "Payment", "Review"),
    val currentStep: Int = 1,
    val days: List<CheckoutDay> = emptyList(),
    val selectedDay: String? = null,
    val slots: List<CheckoutSlot> = emptyList(),
    val selectedSlot: String? = null,
    val deliveringTo: String = "Home · [Area], [City]",
)

sealed interface CheckoutSteppedEvent {
    data object Back : CheckoutSteppedEvent
    data class SelectDay(val id: String) : CheckoutSteppedEvent
    data class SelectSlot(val id: String) : CheckoutSteppedEvent
    data object Continue : CheckoutSteppedEvent
}

data class CheckoutSteppedStrings(
    val back: String = "Back",
    val title: String = "Checkout",
    val heading: String = "When should it arrive?",
    val body: String = "Pick a day and a time slot.",
    val full: String = "Full",
    val deliveringTo: (String) -> String = { "Delivering to $it" },
    val next: String = "Continue to payment",
    val stepState: (Int, Int, String) -> String = { i, n, s -> "Step $i of $n, $s" },
)

@Composable
fun CheckoutStepped(
    state: CheckoutSteppedState,
    onEvent: (CheckoutSteppedEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutSteppedStrings = CheckoutSteppedStrings(),
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(CheckoutSteppedEvent.Back) })
        // Stepper
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clearAndSetSemantics {
                contentDescription = strings.stepState(state.currentStep + 1, state.steps.size, state.steps.getOrElse(state.currentStep) { "" })
            },
            verticalAlignment = Alignment.Top,
        ) {
            state.steps.forEachIndexed { i, step ->
                val done = i < state.currentStep
                val current = i == state.currentStep
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f).height(2.dp).background(if (i == 0) Color.Transparent else if (done || current) colors.primary else colors.outlineVariant))
                        Box(
                            Modifier.size(24.dp).background(if (done || current) colors.primary else colors.surfaceContainerHigh, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (done) Icon(CheckIcon, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(14.dp))
                            else Text("${i + 1}", style = MaterialTheme.typography.labelSmall, color = if (current) colors.onPrimary else colors.onSurfaceVariant)
                        }
                        Box(Modifier.weight(1f).height(2.dp).background(if (i == state.steps.lastIndex) Color.Transparent else if (done) colors.primary else colors.outlineVariant))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(step, style = MaterialTheme.typography.labelSmall, fontWeight = if (current) FontWeight.SemiBold else FontWeight.Normal, color = if (current) colors.onSurface else colors.onSurfaceVariant)
                }
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 16.dp)) {
            Text(strings.heading, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(strings.body, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.selectableGroup().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.days.forEach { d ->
                    val selected = d.id == state.selectedDay
                    Column(
                        Modifier
                            .width(68.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(if (selected) colors.primary else colors.surfaceContainerLow)
                            .selectable(selected = selected, role = Role.RadioButton) { onEvent(CheckoutSteppedEvent.SelectDay(d.id)) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(d.weekday, style = MaterialTheme.typography.labelMedium, color = if (selected) colors.onPrimary else colors.onSurfaceVariant)
                        Text(d.date, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (selected) colors.onPrimary else colors.onSurface)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.slots.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { s ->
                            val selected = s.id == state.selectedSlot
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, MaterialTheme.shapes.medium)
                                    .background(if (selected) colors.primary.copy(alpha = 0.08f) else Color.Transparent)
                                    .selectable(selected = selected, enabled = s.available, role = Role.RadioButton) { onEvent(CheckoutSteppedEvent.SelectSlot(s.id)) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    if (s.available) s.label else "${s.label} · ${strings.full}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (!s.available) colors.onSurface.copy(alpha = 0.38f) else if (selected) colors.primary else colors.onSurface,
                                    textDecoration = if (s.available) null else TextDecoration.LineThrough,
                                )
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).background(colors.surfaceContainerLow).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(PinIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(strings.deliveringTo(state.deliveringTo), style = MaterialTheme.typography.bodySmall)
            }
        }
        Button(
            onClick = { onEvent(CheckoutSteppedEvent.Continue) },
            enabled = state.selectedDay != null && state.selectedSlot != null,
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
        ) { Text(strings.next) }
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

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val CheckIcon = vectorIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")
private val PinIcon = vectorIcon(
    "Pin",
    "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z",
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
internal fun CheckoutSteppedPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutStepped(
            state = CheckoutSteppedState(
                days = listOf(CheckoutDay("10", "Today", "10"), CheckoutDay("11", "Sun", "11"), CheckoutDay("12", "Mon", "12"), CheckoutDay("13", "Tue", "13")),
                selectedDay = "11",
                slots = listOf(CheckoutSlot("a", "8 – 11 am"), CheckoutSlot("b", "11 am – 2 pm"), CheckoutSlot("c", "2 – 5 pm", available = false), CheckoutSlot("d", "5 – 8 pm")),
                selectedSlot = "b",
            ),
            onEvent = {},
        )
    }
}
