package com.yourapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Profile · Patient and family (healthcare)
 * A health profile with family member tabs, the basics a doctor needs,
 * health-ID linking, and records. Each member is a separate profile.
 *
 * Treat everything here as sensitive health data: encrypt at rest, keep
 * it out of logs and analytics. The design uses Public Sans.
 */

data class ProfileFamilyMember(val id: String, val label: String, val initials: String)
data class ProfileHealthItem(val id: String, val label: String, val value: String)
data class ProfileRecordGroup(val id: String, val label: String, val count: String)

data class ProfilePatientState(
    val members: List<ProfileFamilyMember> = emptyList(),
    val selectedMember: String? = null,
    val name: String = "[Full name]",
    val facts: List<String> = emptyList(),
    val healthId: String? = null,
    val healthIdLinked: Boolean = false,
    val items: List<ProfileHealthItem> = emptyList(),
    val records: List<ProfileRecordGroup> = emptyList(),
)

sealed interface ProfilePatientEvent {
    data class SelectMember(val id: String) : ProfilePatientEvent
    data object AddMember : ProfilePatientEvent
    data object LinkHealthId : ProfilePatientEvent
    data class OpenItem(val id: String) : ProfilePatientEvent
    data class OpenRecords(val id: String) : ProfilePatientEvent
}

data class ProfilePatientStrings(
    val title: String = "Health profile",
    val add: String = "Add",
    val healthId: (String) -> String = { "Health ID: $it" },
    val link: String = "Link",
    val records: String = "RECORDS",
)

@Composable
fun ProfilePatient(
    state: ProfilePatientState,
    onEvent: (ProfilePatientEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfilePatientStrings = ProfilePatientStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxSize().background(colors.surfaceContainerLowest).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(strings.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()).selectableGroup(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            state.members.forEach { m ->
                val selected = m.id == state.selectedMember
                Column(
                    Modifier.clip(MaterialTheme.shapes.medium).selectable(selected = selected, role = Role.Tab) { onEvent(ProfilePatientEvent.SelectMember(m.id)) }.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.border(if (selected) 2.dp else 0.dp, if (selected) accent else Color.Transparent, CircleShape).padding(3.dp)) {
                        Avatar(m.initials, null, 52.dp, if (selected) accent else colors.surfaceContainerHigh, if (selected) onAccent else colors.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(m.label, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
            Column(
                Modifier.clip(MaterialTheme.shapes.medium).clickable(role = Role.Button) { onEvent(ProfilePatientEvent.AddMember) }.padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(Modifier.padding(3.dp).size(52.dp).border(1.dp, colors.outline, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(AddIcon, contentDescription = null, tint = colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                Text(strings.add, style = MaterialTheme.typography.labelMedium)
            }
        }
        Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).border(1.dp, colors.outlineVariant, MaterialTheme.shapes.large).padding(16.dp)) {
            Text(state.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                state.facts.forEach { f ->
                    Text(f, style = MaterialTheme.typography.labelMedium, modifier = Modifier.background(accent.copy(alpha = 0.08f), CircleShape).padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }
            if (state.healthId != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.healthId(state.healthId), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    if (!state.healthIdLinked) {
                        TextButton(onClick = { onEvent(ProfilePatientEvent.LinkHealthId) }) { Text(strings.link, color = accent) }
                    }
                }
            }
        }
        Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surface).border(1.dp, colors.outlineVariant, MaterialTheme.shapes.large)) {
            state.items.forEachIndexed { i, item ->
                if (i > 0) HorizontalDivider(Modifier.padding(start = 16.dp), color = colors.outlineVariant)
                LinkRow(item.label, { onEvent(ProfilePatientEvent.OpenItem(item.id)) }, value = item.value)
            }
        }
        Text(
            strings.records,
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.2.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp).semantics { heading() },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            state.records.forEachIndexed { i, r ->
                Column(
                    Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.large)
                        .background(colors.surface)
                        .border(1.dp, colors.outlineVariant, MaterialTheme.shapes.large)
                        .clickable(role = Role.Button) { onEvent(ProfilePatientEvent.OpenRecords(r.id)) }
                        .padding(16.dp),
                ) {
                    Icon(if (i % 2 == 0) ReceiptIcon else MedicalIcon, contentDescription = null, tint = accent)
                    Spacer(Modifier.height(10.dp))
                    Text(r.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(r.count, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
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

private val ChevronIcon = vectorIcon("Chevron", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z", autoMirror = true)
private val AddIcon = vectorIcon("Add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")
private val ReceiptIcon = vectorIcon(
    "Receipt",
    "M18 17H6v-2h12v2zm0-4H6v-2h12v2zm0-4H6V7h12v2zM3 22l1.5-1.5L6 22l1.5-1.5L9 22l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 22V2l-1.5 1.5L18 2l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v20z",
)
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
internal fun ProfilePatientPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfilePatient(
            state = ProfilePatientState(
                members = listOf(ProfileFamilyMember("me", "You", "AB"), ProfileFamilyMember("mom", "Mother", "M"), ProfileFamilyMember("son", "Son", "S")),
                selectedMember = "me",
                facts = listOf("34 years", "Blood group O+", "172 cm · 68 kg"),
                healthId = "12-3456-7890-1234",
                items = listOf(ProfileHealthItem("allergies", "Allergies", "None"), ProfileHealthItem("medicines", "Medicines", "2 active"), ProfileHealthItem("conditions", "Conditions", "Add")),
                records = listOf(ProfileRecordGroup("rx", "Prescriptions", "4 files"), ProfileRecordGroup("labs", "Lab reports", "2 files")),
            ),
            onEvent = {},
            accent = Color(0xFF0D5F78),
        )
    }
}
