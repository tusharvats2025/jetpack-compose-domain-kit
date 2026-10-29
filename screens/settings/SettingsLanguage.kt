package com.yourapp.ui.screens.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Settings · Language
 * App language picker with suggested languages first, each shown in its
 * own script with the English name underneath, and a search box.
 *
 * Apply with AppCompatDelegate.setApplicationLocales(
 * LocaleListCompat.forLanguageTags(tag)) and declare locales_config.xml so
 * the choice also appears in Android 13+ system settings.
 */

data class SettingsLanguageOption(
    /** BCP 47 tag, e.g. "hi". */
    val tag: String,
    val nativeName: String,
    val englishName: String,
    val note: String? = null,
)

data class SettingsLanguageState(
    val query: String = "",
    val suggested: List<SettingsLanguageOption> = emptyList(),
    val all: List<SettingsLanguageOption> = emptyList(),
    val selectedTag: String = "en",
)

sealed interface SettingsLanguageEvent {
    data object Back : SettingsLanguageEvent
    data class QueryChanged(val value: String) : SettingsLanguageEvent
    data class Select(val tag: String) : SettingsLanguageEvent
}

data class SettingsLanguageStrings(
    val back: String = "Back",
    val title: String = "Language",
    val search: String = "Search languages",
    val suggested: String = "Suggested",
    val all: String = "All languages",
)

@Composable
fun SettingsLanguage(
    state: SettingsLanguageState,
    onEvent: (SettingsLanguageEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: SettingsLanguageStrings = SettingsLanguageStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val q = state.query.trim()
    val match = { o: SettingsLanguageOption ->
        q.isEmpty() || o.nativeName.contains(q, ignoreCase = true) || o.englishName.contains(q, ignoreCase = true)
    }
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(SettingsLanguageEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(SettingsLanguageEvent.QueryChanged(it)) },
                placeholder = { Text(strings.search) },
                leadingIcon = { Icon(SearchIcon, contentDescription = null) },
                singleLine = true,
                shape = CircleShape,
                modifier = Modifier.keepAboveKeyboard().fillMaxWidth(),
            )
            Column(Modifier.selectableGroup()) {
                listOf(strings.suggested to state.suggested.filter(match), strings.all to state.all.filter(match)).forEach { (label, options) ->
                    if (options.isNotEmpty()) {
                        SectionLabel(label)
                        Group {
                            options.forEachIndexed { i, o ->
                                if (i > 0) RowDivider()
                                val selected = o.tag == state.selectedTag
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .selectable(selected = selected, role = Role.RadioButton) { onEvent(SettingsLanguageEvent.Select(o.tag)) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(o.nativeName, style = MaterialTheme.typography.bodyLarge, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                                        Text(
                                            listOfNotNull(o.englishName.takeIf { it != o.nativeName }, o.note).joinToString(" · "),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.onSurfaceVariant,
                                        )
                                    }
                                    if (selected) Icon(CheckIcon, contentDescription = null, tint = colors.primary)
                                }
                            }
                        }
                    }
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

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val CheckIcon = vectorIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")
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
internal fun SettingsLanguagePreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        SettingsLanguage(
            state = SettingsLanguageState(
                suggested = listOf(
                    SettingsLanguageOption("en", "English", "English", "Device language"),
                    SettingsLanguageOption("hi", "हिन्दी", "Hindi"),
                ),
                all = listOf(
                    SettingsLanguageOption("bn", "বাংলা", "Bengali"),
                    SettingsLanguageOption("ta", "தமிழ்", "Tamil"),
                    SettingsLanguageOption("te", "తెలుగు", "Telugu"),
                    SettingsLanguageOption("mr", "मराठी", "Marathi"),
                ),
            ),
            onEvent = {},
        )
    }
}
