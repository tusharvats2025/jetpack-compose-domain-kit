package com.yourapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Profile · Dating
 * A full-bleed photo card with page indicators, name, age and distance,
 * then a prompt answer and interests. Pass and Like float at the bottom.
 *
 * Tap the left or right half of the photo to move between photos (wired
 * here); swipe gestures for pass/like are yours to add. The design uses
 * DM Sans; set it in your Typography to match.
 */

data class ProfileDatingState(
    val name: String = "[Name]",
    val age: Int = 27,
    val verified: Boolean = true,
    val subtitle: String = "[Job] · 3 km away",
    val photos: List<@Composable () -> Unit> = emptyList(),
    val photoIndex: Int = 0,
    val promptQuestion: String = "My ideal weekend",
    val promptAnswer: String = "",
    val interests: List<String> = emptyList(),
)

sealed interface ProfileDatingEvent {
    data class ShowPhoto(val index: Int) : ProfileDatingEvent
    data object Report : ProfileDatingEvent
    data object Pass : ProfileDatingEvent
    data object Like : ProfileDatingEvent
}

data class ProfileDatingStrings(
    val photoOf: (Int, Int) -> String = { i, n -> "Photo $i of $n" },
    val report: String = "Report or block",
    val pass: String = "Pass",
    val like: String = "Like",
    val verified: String = "Verified",
)

@OptIn(ExperimentalLayoutApi::class) // FlowRow on older Compose versions
@Composable
fun ProfileDating(
    state: ProfileDatingState,
    onEvent: (ProfileDatingEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileDatingStrings = ProfileDatingStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val colors = MaterialTheme.colorScheme
    val count = state.photos.size
    Box(modifier.fillMaxSize().background(colors.surface)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().aspectRatio(3f / 4f).background(colors.surfaceContainerHigh)) {
                state.photos.getOrNull(state.photoIndex)?.invoke()
                // Tap zones for previous / next photo.
                Row(Modifier.matchParentSize()) {
                    Box(Modifier.weight(1f).fillMaxHeight().clickable(enabled = state.photoIndex > 0) { onEvent(ProfileDatingEvent.ShowPhoto(state.photoIndex - 1)) })
                    Box(Modifier.weight(1f).fillMaxHeight().clickable(enabled = state.photoIndex < count - 1) { onEvent(ProfileDatingEvent.ShowPhoto(state.photoIndex + 1)) })
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clearAndSetSemantics { contentDescription = strings.photoOf(state.photoIndex + 1, count) },
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(count) { i ->
                        Box(Modifier.weight(1f).height(3.dp).background(if (i == state.photoIndex) Color.White else Color.White.copy(alpha = 0.4f), CircleShape))
                    }
                }
                IconButton(
                    onClick = { onEvent(ProfileDatingEvent.Report) },
                    modifier = Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.statusBars).padding(top = 12.dp, end = 4.dp),
                ) { Icon(FlagIcon, contentDescription = strings.report, tint = Color.White) }
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))))
                        .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 20.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${state.name}, ${state.age}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        if (state.verified) {
                            Spacer(Modifier.width(6.dp))
                            Icon(VerifiedIcon, contentDescription = strings.verified, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(state.subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                }
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (state.promptAnswer.isNotBlank()) {
                    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(accent.copy(alpha = 0.07f)).padding(16.dp)) {
                        Text(state.promptQuestion, style = MaterialTheme.typography.labelLarge, color = accent)
                        Spacer(Modifier.height(6.dp))
                        Text(state.promptAnswer, style = MaterialTheme.typography.titleMedium)
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.interests.forEach { i ->
                        Text(i, style = MaterialTheme.typography.labelLarge, modifier = Modifier.border(1.dp, colors.outlineVariant, CircleShape).padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
                Spacer(Modifier.height(96.dp))
            }
        }
        Row(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            FilledIconButton(
                onClick = { onEvent(ProfileDatingEvent.Pass) },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.surface, contentColor = colors.onSurfaceVariant),
                modifier = Modifier.size(64.dp).border(1.dp, colors.outlineVariant, CircleShape),
            ) { Icon(CloseIcon, contentDescription = strings.pass, modifier = Modifier.size(28.dp)) }
            FilledIconButton(
                onClick = { onEvent(ProfileDatingEvent.Like) },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = accent, contentColor = onAccent),
                modifier = Modifier.size(64.dp),
            ) { Icon(HeartIcon, contentDescription = strings.like, modifier = Modifier.size(28.dp)) }
        }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val CloseIcon = vectorIcon("Close", "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z")
private val HeartIcon = vectorIcon(
    "Heart",
    "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z",
)
private val VerifiedIcon = vectorIcon(
    "Verified",
    "M23 12l-2.44-2.79.34-3.69-3.61-.82-1.89-3.2L12 2.96 8.6 1.5 6.71 4.69 3.1 5.5l.34 3.7L1 12l2.44 2.79-.34 3.7 3.61.82L8.6 22.5l3.4-1.47 3.4 1.46 1.89-3.19 3.61-.82-.34-3.69L23 12zm-12.91 4.72l-3.8-3.81 1.48-1.48 2.32 2.33 5.85-5.87 1.48 1.48-7.33 7.35z", autoMirror = true,
)
private val FlagIcon = vectorIcon("Flag", "M14.4 6L14 4H5v17h2v-7h5.6l.4 2h7V6z")

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
internal fun ProfileDatingPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileDating(
            state = ProfileDatingState(
                photos = List<@Composable () -> Unit>(4) { {} },
                promptAnswer = "A long walk, a new café and an early night.",
                interests = listOf("Hiking", "Cooking", "Live music", "Board games"),
            ),
            onEvent = {},
            accent = Color(0xFFA21C46),
            onAccent = Color.White,
        )
    }
}
