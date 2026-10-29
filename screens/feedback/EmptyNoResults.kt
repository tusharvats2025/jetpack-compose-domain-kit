package com.yourapp.ui.screens.feedback

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Empty · No results
 * Search with filters that returned nothing. Instead of a dead end it
 * says which filter is blocking results and offers to drop it.
 *
 * Work out relaxedCount on your side (the count without the most
 * restrictive filter). Colours come from MaterialTheme.
 */

data class EmptyNoResultsFilter(val id: String, val label: String)

data class EmptyNoResultsState(
    val query: String = "",
    val filters: List<EmptyNoResultsFilter> = emptyList(),
    /** Results if the most restrictive filter were removed; null if unknown. */
    val relaxedCount: Int? = null,
    val blockingFilter: String? = null,
)

sealed interface EmptyNoResultsEvent {
    data object Back : EmptyNoResultsEvent
    data class QueryChanged(val value: String) : EmptyNoResultsEvent
    data object ClearQuery : EmptyNoResultsEvent
    data class RemoveFilter(val id: String) : EmptyNoResultsEvent
    data object ShowRelaxed : EmptyNoResultsEvent
    data object ClearFilters : EmptyNoResultsEvent
}

data class EmptyNoResultsStrings(
    val back: String = "Back",
    val search: String = "Search",
    val clear: String = "Clear search",
    val removeFilter: (String) -> String = { "Remove filter: $it" },
    val heading: String = "No matches with these filters",
    val relaxed: (count: Int, query: String, filter: String) -> String = { n, q, f ->
        "There are $n results for \"$q\" if you remove \"$f\"."
    },
    val generic: String = "Try a different word or fewer filters.",
    val showRelaxed: (Int) -> String = { "Show $it results" },
    val clearFilters: String = "Clear all filters",
)

@Composable
fun EmptyNoResults(
    state: EmptyNoResultsState,
    onEvent: (EmptyNoResultsEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: EmptyNoResultsStrings = EmptyNoResultsStrings(),
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(EmptyNoResultsEvent.Back) }) { Icon(BackIcon, contentDescription = strings.back) }
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(EmptyNoResultsEvent.QueryChanged(it)) },
                placeholder = { Text(strings.search) },
                singleLine = true,
                shape = CircleShape,
                trailingIcon = if (state.query.isNotEmpty()) {
                    { IconButton(onClick = { onEvent(EmptyNoResultsEvent.ClearQuery) }) { Icon(CloseIcon, contentDescription = strings.clear) } }
                } else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.keepAboveKeyboard().weight(1f),
            )
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.filters.forEach { f ->
                InputChip(
                    selected = true,
                    onClick = { onEvent(EmptyNoResultsEvent.RemoveFilter(f.id)) },
                    label = { Text(f.label) },
                    trailingIcon = { Icon(CloseIcon, contentDescription = strings.removeFilter(f.label), modifier = Modifier.size(16.dp)) },
                )
            }
        }
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 32.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(96.dp).background(colors.surfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) {
                Icon(SearchOffIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(strings.heading, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            val relaxed = state.relaxedCount
            val blocking = state.blockingFilter
            Text(
                if (relaxed != null && blocking != null) strings.relaxed(relaxed, state.query, blocking) else strings.generic,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            if (relaxed != null && relaxed > 0) {
                Button(onClick = { onEvent(EmptyNoResultsEvent.ShowRelaxed) }, modifier = Modifier.height(48.dp)) {
                    Text(strings.showRelaxed(relaxed))
                }
                Spacer(Modifier.height(8.dp))
            }
            TextButton(onClick = { onEvent(EmptyNoResultsEvent.ClearFilters) }) { Text(strings.clearFilters) }
        }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val CloseIcon = vectorIcon("Close", "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z")
private val SearchOffIcon = vectorIcon(
    "SearchOff",
    "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14zM7 9h5v1H7z",
)

/**
 * Keeps a focused field visible above the keyboard. It scrolls the field into view
 * when it gains focus, and again as the keyboard opens and the screen shrinks.
 */
@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.keepAboveKeyboard(): Modifier = composed {
    val requester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }
    val keyboardHeight = WindowInsets.ime.getBottom(LocalDensity.current)
    LaunchedEffect(focused, keyboardHeight) {
        if (focused) requester.bringIntoView()
    }
    bringIntoViewRequester(requester).onFocusChanged { focused = it.hasFocus }
}

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
internal fun EmptyNoResultsPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        EmptyNoResults(
            state = EmptyNoResultsState(
                query = "wireless headphones",
                filters = listOf(EmptyNoResultsFilter("price", "Under ₹500"), EmptyNoResultsFilter("stock", "In stock")),
                relaxedCount = 34,
                blockingFilter = "Under ₹500",
            ),
            onEvent = {},
        )
    }
}
