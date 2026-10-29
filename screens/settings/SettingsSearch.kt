package com.yourapp.ui.screens.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Settings · Searchable settings
 * Search across every setting, with the matched part highlighted and the
 * path shown under each result. Also covers the no-results state, which
 * suggests phone settings and popular entries instead of a dead end.
 *
 * Search your own index of settings (title, path, keywords) in the
 * ViewModel and pass the results here.
 */

data class SettingsSearchResult(
    val id: String,
    val title: String,
    val path: String,
    /** Extra keyword that matched, shown as "also matches …". */
    val alsoMatches: String? = null,
)

data class SettingsSearchState(
    val query: String = "",
    val results: List<SettingsSearchResult> = emptyList(),
    val recent: List<String> = emptyList(),
    val popular: List<SettingsSearchResult> = emptyList(),
)

sealed interface SettingsSearchEvent {
    data object Back : SettingsSearchEvent
    data class QueryChanged(val value: String) : SettingsSearchEvent
    data object Clear : SettingsSearchEvent
    data class Open(val id: String) : SettingsSearchEvent
    data class UseRecent(val query: String) : SettingsSearchEvent
    data object OpenPhoneSettings : SettingsSearchEvent
}

data class SettingsSearchStrings(
    val back: String = "Back",
    val placeholder: String = "Search settings",
    val clear: String = "Clear search",
    val count: (Int) -> String = { if (it == 1) "1 result" else "$it results" },
    val recent: String = "Recent searches",
    val noneTitle: (String) -> String = { "No settings match \"$it\"" },
    val noneBody: String = "This might be a phone setting rather than one in [App name].",
    val phoneSettings: String = "Open phone settings",
    val popular: String = "Popular settings",
    val alsoMatches: (String) -> String = { " · also matches \"$it\"" },
)

@OptIn(ExperimentalLayoutApi::class) // FlowRow on older Compose versions
@Composable
fun SettingsSearch(
    state: SettingsSearchState,
    onEvent: (SettingsSearchEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: SettingsSearchStrings = SettingsSearchStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val query = state.query.trim()
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(SettingsSearchEvent.Back) }) { Icon(BackIcon, contentDescription = strings.back) }
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(SettingsSearchEvent.QueryChanged(it)) },
                placeholder = { Text(strings.placeholder) },
                leadingIcon = { Icon(SearchIcon, contentDescription = null) },
                trailingIcon = if (state.query.isNotEmpty()) {
                    { IconButton(onClick = { onEvent(SettingsSearchEvent.Clear) }) { Icon(CloseIcon, contentDescription = strings.clear) } }
                } else null,
                singleLine = true,
                shape = CircleShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.keepAboveKeyboard().weight(1f),
            )
        }
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            when {
                query.isEmpty() -> {
                    if (state.recent.isNotEmpty()) {
                        SectionLabel(strings.recent, colors.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.recent.forEach { r ->
                                SuggestionChip(onClick = { onEvent(SettingsSearchEvent.UseRecent(r)) }, label = { Text(r) })
                            }
                        }
                    }
                }
                state.results.isNotEmpty() -> {
                    Text(
                        strings.count(state.results.size),
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 8.dp).semantics { liveRegion = LiveRegionMode.Polite },
                    )
                    Group {
                        state.results.forEachIndexed { i, r ->
                            if (i > 0) RowDivider()
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable(role = Role.Button) { onEvent(SettingsSearchEvent.Open(r.id)) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                Text(highlight(r.title, query, colors.primary), style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    highlight(r.path + (r.alsoMatches?.let(strings.alsoMatches) ?: ""), query, colors.primary),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    if (state.recent.isNotEmpty()) {
                        SectionLabel(strings.recent, colors.onSurfaceVariant)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.recent.forEach { r ->
                                SuggestionChip(onClick = { onEvent(SettingsSearchEvent.UseRecent(r)) }, label = { Text(r) })
                            }
                        }
                    }
                }
                else -> {
                    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(88.dp).background(colors.surfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(SearchIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(40.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(strings.noneTitle(query), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(6.dp))
                        Text(strings.noneBody, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(onClick = { onEvent(SettingsSearchEvent.OpenPhoneSettings) }) { Text(strings.phoneSettings) }
                    }
                    if (state.popular.isNotEmpty()) {
                        SectionLabel(strings.popular, colors.onSurfaceVariant)
                        Group {
                            state.popular.forEachIndexed { i, r ->
                                if (i > 0) RowDivider()
                                LinkRow(r.title, { onEvent(SettingsSearchEvent.Open(r.id)) })
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Bolds and tints every case-insensitive match of query. */
private fun highlight(text: String, query: String, color: Color) = buildAnnotatedString {
    if (query.isEmpty()) {
        append(text)
        return@buildAnnotatedString
    }
    var start = 0
    while (true) {
        val at = text.indexOf(query, start, ignoreCase = true)
        if (at < 0) {
            append(text.substring(start))
            break
        }
        append(text.substring(start, at))
        withStyle(SpanStyle(color = color, fontWeight = FontWeight.SemiBold)) { append(text.substring(at, at + query.length)) }
        start = at + query.length
    }
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

@Composable
private fun SectionLabel(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp).semantics { heading() },
    )
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val ChevronIcon = vectorIcon("Chevron", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z", autoMirror = true)
private val CloseIcon = vectorIcon("Close", "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z")
private val SearchIcon = vectorIcon(
    "Search",
    "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z",
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
internal fun SettingsSearchPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        SettingsSearch(
            state = SettingsSearchState(
                query = "notif",
                results = listOf(
                    SettingsSearchResult("n", "Notifications", "Settings › General"),
                    SettingsSearchResult("e", "Email notifications", "Settings › Notifications"),
                    SettingsSearchResult("q", "Quiet hours", "Settings › Notifications", alsoMatches = "mute notifications"),
                ),
                recent = listOf("dark mode", "password", "language"),
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "No results")
@Composable
internal fun SettingsSearchEmptyPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        SettingsSearch(
            state = SettingsSearchState(
                query = "bluetooth",
                popular = listOf(SettingsSearchResult("n", "Notifications", ""), SettingsSearchResult("a", "Appearance", "")),
            ),
            onEvent = {},
        )
    }
}
