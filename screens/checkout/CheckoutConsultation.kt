package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Checkout · Consultation booking (healthcare)
 * Book a doctor: who, video or clinic, a time slot (taken slots disabled),
 * which family member it's for, the cancellation policy and the fee.
 *
 * Slot availability must be re-checked on your server at payment time.
 * The design uses Public Sans.
 */

enum class CheckoutConsultMode { Video, Clinic }

data class CheckoutTimeSlot(val id: String, val label: String, val available: Boolean = true)

data class CheckoutConsultationState(
    val doctor: String = "Dr. [Name]",
    val initials: String = "DR",
    val speciality: String = "General physician · 9 years",
    val languages: String = "[Languages spoken]",
    val mode: CheckoutConsultMode = CheckoutConsultMode.Video,
    val day: String = "Today, [date]",
    val slots: List<CheckoutTimeSlot> = emptyList(),
    val selectedSlot: String? = null,
    val patient: String = "You · [Full name]",
    val fee: String = "₹499",
    val policy: String = "Free cancellation up to 2 hours before",
    val isPaying: Boolean = false,
)

sealed interface CheckoutConsultationEvent {
    data object Back : CheckoutConsultationEvent
    data class SetMode(val mode: CheckoutConsultMode) : CheckoutConsultationEvent
    data class SelectSlot(val id: String) : CheckoutConsultationEvent
    data object OtherDays : CheckoutConsultationEvent
    data object ChangePatient : CheckoutConsultationEvent
    data object Book : CheckoutConsultationEvent
}

data class CheckoutConsultationStrings(
    val back: String = "Back",
    val title: String = "Book consultation",
    val video: String = "Video call",
    val clinic: String = "Clinic visit",
    val otherDays: String = "Other days",
    val patient: String = "Patient",
    val change: String = "Change",
    val fee: String = "Fee",
    val book: String = "Book and pay",
    val unavailable: String = "unavailable",
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class) // FlowRow, SegmentedButton on older versions
@Composable
fun CheckoutConsultation(
    state: CheckoutConsultationState,
    onEvent: (CheckoutConsultationEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutConsultationStrings = CheckoutConsultationStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    doctorPhoto: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surfaceContainerLowest).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(CheckoutConsultationEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(state.initials, doctorPhoto, 56.dp, accent.copy(alpha = 0.14f), accent)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(state.doctor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(state.speciality, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    Text(state.languages, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                CheckoutConsultMode.entries.forEachIndexed { i, m ->
                    SegmentedButton(
                        selected = m == state.mode,
                        onClick = { onEvent(CheckoutConsultationEvent.SetMode(m)) },
                        shape = SegmentedButtonDefaults.itemShape(i, CheckoutConsultMode.entries.size),
                        colors = SegmentedButtonDefaults.colors(activeContainerColor = accent.copy(alpha = 0.14f)),
                        icon = { Icon(if (m == CheckoutConsultMode.Video) VideoIcon else MedicalIcon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    ) { Text(if (m == CheckoutConsultMode.Video) strings.video else strings.clinic) }
                }
            }
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).padding(16.dp)) {
                Text(state.day, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                FlowRow(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.slots.forEach { s ->
                        val selected = s.id == state.selectedSlot
                        Text(
                            s.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = when {
                                !s.available -> colors.onSurface.copy(alpha = 0.38f)
                                selected -> onAccent
                                else -> colors.onSurface
                            },
                            textDecoration = if (s.available) null else TextDecoration.LineThrough,
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.small)
                                .background(if (selected) accent else Color.Transparent)
                                .border(1.dp, if (selected) accent else colors.outlineVariant, MaterialTheme.shapes.small)
                                .selectable(selected = selected, enabled = s.available, role = Role.RadioButton) { onEvent(CheckoutConsultationEvent.SelectSlot(s.id)) }
                                .semantics { if (!s.available) stateDescription = strings.unavailable }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        )
                    }
                    TextButton(onClick = { onEvent(CheckoutConsultationEvent.OtherDays) }) { Text(strings.otherDays, color = accent) }
                }
            }
            Row(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).clickable(role = Role.Button) { onEvent(CheckoutConsultationEvent.ChangePatient) }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(strings.patient, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    Text(state.patient, style = MaterialTheme.typography.bodyLarge)
                }
                Text(strings.change, style = MaterialTheme.typography.labelLarge, color = accent)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                Icon(CheckIcon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(state.policy, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
        }
        Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(strings.fee, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                    Text(state.fee, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onEvent(CheckoutConsultationEvent.Book) },
                    enabled = state.selectedSlot != null && !state.isPaying,
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
                    modifier = Modifier.height(52.dp),
                ) {
                    if (state.isPaying) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
                    else Text(strings.book, fontWeight = FontWeight.SemiBold)
                }
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
private val CheckIcon = vectorIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")
private val VideoIcon = vectorIcon("Video", "M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z")
private val MedicalIcon = vectorIcon(
    "Medical",
    "M19 3H5c-1.1 0-1.99.9-1.99 2L3 19c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-1 11h-4v4h-4v-4H6v-4h4V6h4v4h4v4z",
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
internal fun CheckoutConsultationPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutConsultation(
            state = CheckoutConsultationState(
                slots = listOf(
                    CheckoutTimeSlot("1", "4:00 pm"), CheckoutTimeSlot("2", "4:30 pm"), CheckoutTimeSlot("3", "5:00 pm", available = false),
                    CheckoutTimeSlot("4", "5:30 pm"), CheckoutTimeSlot("5", "6:00 pm"),
                ),
                selectedSlot = "2",
            ),
            onEvent = {},
            accent = Color(0xFF0D5F78),
        )
    }
}
