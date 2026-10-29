package com.yourapp.ui.screens.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Settings · Help and support
 * Search first, then topic tiles, popular questions, and contact options
 * with an honest reply time.
 *
 * Topics carry an icon key so you can map your own categories.
 */

enum class SettingsHelpIcon { Orders, Payments, Refunds, Account, Delivery, Safety }

data class SettingsHelpTopic(val id: String, val label: String, val icon: SettingsHelpIcon)
data class SettingsHelpQuestion(val id: String, val question: String)

data class SettingsHelpState(
    val query: String = "",
    val topics: List<SettingsHelpTopic> = emptyList(),
    val questions: List<SettingsHelpQuestion> = emptyList(),
    val replyTime: String = "5 minutes",
)

sealed interface SettingsHelpEvent {
    data object Back : SettingsHelpEvent
    data class QueryChanged(val value: String) : SettingsHelpEvent
    data object Search : SettingsHelpEvent
    data class OpenTopic(val id: String) : SettingsHelpEvent
    data class OpenQuestion(val id: String) : SettingsHelpEvent
    data object Chat : SettingsHelpEvent
    data object Email : SettingsHelpEvent
}

data class SettingsHelpStrings(
    val back: String = "Back",
    val title: String = "Help and support",
    val search: String = "Describe your problem",
    val popular: String = "Popular questions",
    val stillNeed: String = "Still need help?",
    val chat: String = "Chat with us",
    val email: String = "Email us",
    val replies: (String) -> String = { "Usually replies in $it" },
)

@Composable
fun SettingsHelp(
    state: SettingsHelpState,
    onEvent: (SettingsHelpEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: SettingsHelpStrings = SettingsHelpStrings(),
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        TopBar(strings.title, strings.back, { onEvent(SettingsHelpEvent.Back) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(SettingsHelpEvent.QueryChanged(it)) },
                placeholder = { Text(strings.search) },
                leadingIcon = { Icon(SearchIcon, contentDescription = null) },
                singleLine = true,
                shape = CircleShape,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onEvent(SettingsHelpEvent.Search) }),
                modifier = Modifier.keepAboveKeyboard().fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.topics.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { t ->
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(colors.surfaceContainerLow)
                                    .clickable(role = Role.Button) { onEvent(SettingsHelpEvent.OpenTopic(t.id)) }
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    when (t.icon) {
                                        SettingsHelpIcon.Orders -> BagIcon
                                        SettingsHelpIcon.Payments -> CardIcon
                                        SettingsHelpIcon.Refunds -> RefundIcon
                                        SettingsHelpIcon.Account -> PersonIcon
                                        SettingsHelpIcon.Delivery -> TruckIcon
                                        SettingsHelpIcon.Safety -> ShieldIcon
                                    },
                                    contentDescription = null,
                                    tint = colors.primary,
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(t.label, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            if (state.questions.isNotEmpty()) {
                SectionLabel(strings.popular)
                Group {
                    state.questions.forEachIndexed { i, q ->
                        if (i > 0) RowDivider()
                        LinkRow(q.question, { onEvent(SettingsHelpEvent.OpenQuestion(q.id)) })
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Column(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.primaryContainer.copy(alpha = 0.45f)).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(strings.stillNeed, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onEvent(SettingsHelpEvent.Chat) }, modifier = Modifier.weight(1f)) {
                        Icon(ChatIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.chat)
                    }
                    OutlinedButton(onClick = { onEvent(SettingsHelpEvent.Email) }, modifier = Modifier.weight(1f)) {
                        Icon(MailIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.email)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(strings.replies(state.replyTime), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
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
private val SearchIcon = vectorIcon(
    "Search",
    "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z",
)
private val ShieldIcon = vectorIcon(
    "Shield",
    "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z",
)
private val BagIcon = vectorIcon(
    "Bag",
    "M18 6h-2c0-2.21-1.79-4-4-4S8 3.79 8 6H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-6-2c1.1 0 2 .9 2 2h-4c0-1.1.9-2 2-2zm6 16H6V8h2v2c0 .55.45 1 1 1s1-.45 1-1V8h4v2c0 .55.45 1 1 1s1-.45 1-1V8h2v12z",
)
private val CardIcon = vectorIcon(
    "Card",
    "M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z",
)
private val MailIcon = vectorIcon("Mail", "M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z")
private val ChatIcon = vectorIcon("Chat", "M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 9h12v2H6V9zm8 5H6v-2h8v2zm4-6H6V6h12v2z")
private val TruckIcon = vectorIcon(
    "Truck",
    "M20 8h-3V4H3c-1.1 0-2 .9-2 2v11h2c0 1.66 1.34 3 3 3s3-1.34 3-3h6c0 1.66 1.34 3 3 3s3-1.34 3-3h2v-5l-3-4zM6 18.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm13.5-9l1.96 2.5H17V9.5h2.5zm-1.5 9c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z",
)
private val RefundIcon = vectorIcon(
    "Refund",
    "M12.5 8c-2.65 0-5.05.99-6.9 2.6L2 7v9h9l-3.62-3.62c1.39-1.16 3.16-1.88 5.12-1.88 3.54 0 6.55 2.31 7.6 5.5l2.37-.78C21.08 11.03 17.15 8 12.5 8z", autoMirror = true,
)
private val PersonIcon = vectorIcon("Person", "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z")

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
internal fun SettingsHelpPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        SettingsHelp(
            state = SettingsHelpState(
                topics = listOf(
                    SettingsHelpTopic("orders", "Orders", SettingsHelpIcon.Orders),
                    SettingsHelpTopic("payments", "Payments", SettingsHelpIcon.Payments),
                    SettingsHelpTopic("refunds", "Refunds", SettingsHelpIcon.Refunds),
                    SettingsHelpTopic("account", "Account", SettingsHelpIcon.Account),
                    SettingsHelpTopic("delivery", "Delivery", SettingsHelpIcon.Delivery),
                    SettingsHelpTopic("safety", "Safety", SettingsHelpIcon.Safety),
                ),
                questions = listOf(
                    SettingsHelpQuestion("q1", "Where is my refund?"),
                    SettingsHelpQuestion("q2", "How do I change my delivery address?"),
                    SettingsHelpQuestion("q3", "I can't sign in"),
                ),
            ),
            onEvent = {},
        )
    }
}
