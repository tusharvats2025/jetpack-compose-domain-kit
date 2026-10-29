package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Checkout · Address picker
 * Saved addresses as radio cards (with the default marked), use current
 * location, and add a new address. Deliver here confirms the choice.
 *
 * Geocoding the current location and the add-address form are yours.
 */

enum class CheckoutAddressKind { Home, Work, Other }

data class CheckoutAddress(
    val id: String,
    val label: String,
    val kind: CheckoutAddressKind,
    val contact: String,
    val lines: String,
    val isDefault: Boolean = false,
)

data class CheckoutAddressPickerState(
    val addresses: List<CheckoutAddress> = emptyList(),
    val selectedId: String? = null,
    val isLocating: Boolean = false,
)

sealed interface CheckoutAddressPickerEvent {
    data object Back : CheckoutAddressPickerEvent
    data object UseCurrentLocation : CheckoutAddressPickerEvent
    data class Select(val id: String) : CheckoutAddressPickerEvent
    data class Edit(val id: String) : CheckoutAddressPickerEvent
    data object AddNew : CheckoutAddressPickerEvent
    data object Confirm : CheckoutAddressPickerEvent
}

data class CheckoutAddressPickerStrings(
    val back: String = "Back",
    val title: String = "Select address",
    val currentLocation: String = "Use my current location",
    val saved: String = "Saved addresses",
    val default: String = "DEFAULT",
    val edit: String = "Edit",
    val add: String = "Add a new address",
    val confirm: String = "Deliver here",
)

@Composable
fun CheckoutAddressPicker(
    state: CheckoutAddressPickerState,
    onEvent: (CheckoutAddressPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutAddressPickerStrings = CheckoutAddressPickerStrings(),
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(CheckoutAddressPickerEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            OutlinedButton(
                onClick = { onEvent(CheckoutAddressPickerEvent.UseCurrentLocation) },
                enabled = !state.isLocating,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                if (state.isLocating) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else {
                    Icon(PinIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.currentLocation)
                }
            }
            Text(
                strings.saved,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp).semantics { heading() },
            )
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.addresses.forEach { a ->
                    val selected = a.id == state.selectedId
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.large)
                            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, MaterialTheme.shapes.large)
                            .background(if (selected) colors.primary.copy(alpha = 0.05f) else colors.surface)
                            .selectable(selected = selected, role = Role.RadioButton) { onEvent(CheckoutAddressPickerEvent.Select(a.id)) }
                            .padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(top = 2.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    when (a.kind) {
                                        CheckoutAddressKind.Home -> HomeIcon
                                        CheckoutAddressKind.Work -> WorkIcon
                                        CheckoutAddressKind.Other -> PinIcon
                                    },
                                    contentDescription = null,
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(a.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                if (a.isDefault) {
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        strings.default,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.primary,
                                        modifier = Modifier.background(colors.primary.copy(alpha = 0.1f), CircleShape).padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(a.contact, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            Text(a.lines, style = MaterialTheme.typography.bodyMedium)
                        }
                        TextButton(onClick = { onEvent(CheckoutAddressPickerEvent.Edit(a.id)) }) { Text(strings.edit) }
                    }
                }
            }
            TextButton(onClick = { onEvent(CheckoutAddressPickerEvent.AddNew) }, modifier = Modifier.padding(top = 8.dp)) {
                Icon(AddIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(strings.add)
            }
        }
        Button(
            onClick = { onEvent(CheckoutAddressPickerEvent.Confirm) },
            enabled = state.selectedId != null,
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
        ) { Text(strings.confirm) }
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
private val PinIcon = vectorIcon(
    "Pin",
    "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z",
)
private val AddIcon = vectorIcon("Add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")
private val HomeIcon = vectorIcon("Home", "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z")
private val WorkIcon = vectorIcon(
    "Work",
    "M20 6h-4V4c0-1.11-.89-2-2-2h-4c-1.11 0-2 .89-2 2v2H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-6 0h-4V4h4v2z",
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
internal fun CheckoutAddressPickerPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutAddressPicker(
            state = CheckoutAddressPickerState(
                addresses = listOf(
                    CheckoutAddress("home", "Home", CheckoutAddressKind.Home, "[Name] · +91 00000 00000", "[Flat, street], [Area], [City] [PIN]", isDefault = true),
                    CheckoutAddress("work", "Work", CheckoutAddressKind.Work, "[Name] · +91 00000 00000", "[Office, floor], [Area], [City] [PIN]"),
                ),
                selectedId = "home",
            ),
            onEvent = {},
        )
    }
}
