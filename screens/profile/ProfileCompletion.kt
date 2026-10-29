package com.yourapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Profile · Profile completion
 * A progress ring with what's done and what's left, each open item with a
 * direct Add action. Say why it's worth finishing; let people do it later.
 */

data class ProfileCompletionTask(val id: String, val label: String, val done: Boolean)

data class ProfileCompletionState(
    val tasks: List<ProfileCompletionTask> = emptyList(),
)

sealed interface ProfileCompletionEvent {
    data object Back : ProfileCompletionEvent
    data class Complete(val id: String) : ProfileCompletionEvent
    data object Later : ProfileCompletionEvent
}

data class ProfileCompletionStrings(
    val back: String = "Back",
    val title: String = "Complete your profile",
    val left: (Int) -> String = { if (it == 1) "1 thing left. Complete profiles get more replies." else "$it things left. Complete profiles get more replies." },
    val done: String = "All done. Nice work.",
    val percent: (Int) -> String = { "$it%" },
    val progress: (Int) -> String = { "Profile $it percent complete" },
    val add: String = "Add",
    val later: String = "Finish later",
)

@Composable
fun ProfileCompletion(
    state: ProfileCompletionState,
    onEvent: (ProfileCompletionEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileCompletionStrings = ProfileCompletionStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val total = state.tasks.size.coerceAtLeast(1)
    val done = state.tasks.count { it.done }
    val percent = done * 100 / total
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        IconButton(onClick = { onEvent(ProfileCompletionEvent.Back) }, modifier = Modifier.padding(start = 4.dp, top = 8.dp)) {
            Icon(BackIcon, contentDescription = strings.back)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(84.dp).clearAndSetSemantics { contentDescription = strings.progress(percent) }, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { done / total.toFloat() },
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp,
                        trackColor = colors.surfaceContainerHighest,
                        strokeCap = StrokeCap.Round,
                    )
                    Text(strings.percent(percent), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(strings.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (done == state.tasks.size) strings.done else strings.left(state.tasks.size - done),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow)) {
                state.tasks.forEachIndexed { i, t ->
                    if (i > 0) HorizontalDivider(Modifier.padding(start = 56.dp), color = colors.outlineVariant)
                    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp).heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(24.dp).then(
                                if (t.done) Modifier.background(colors.primary, CircleShape) else Modifier.border(2.dp, colors.outlineVariant, CircleShape),
                            ),
                            contentAlignment = Alignment.Center,
                        ) { if (t.done) Icon(CheckIcon, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(16.dp)) }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            t.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (t.done) colors.onSurfaceVariant else colors.onSurface,
                            textDecoration = if (t.done) TextDecoration.LineThrough else null,
                            modifier = Modifier.weight(1f),
                        )
                        if (!t.done) TextButton(onClick = { onEvent(ProfileCompletionEvent.Complete(t.id)) }) { Text(strings.add) }
                    }
                }
            }
        }
        OutlinedButton(onClick = { onEvent(ProfileCompletionEvent.Later) }, modifier = Modifier.fillMaxWidth().padding(20.dp).height(52.dp)) {
            Text(strings.later)
        }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val CheckIcon = vectorIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")

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
internal fun ProfileCompletionPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileCompletion(
            state = ProfileCompletionState(
                tasks = listOf(
                    ProfileCompletionTask("photo", "Add a profile photo", true),
                    ProfileCompletionTask("email", "Verify your email", true),
                    ProfileCompletionTask("interests", "Pick your interests", true),
                    ProfileCompletionTask("bio", "Write a short bio", false),
                    ProfileCompletionTask("phone", "Add a phone number", false),
                ),
            ),
            onEvent = {},
        )
    }
}
