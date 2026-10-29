package com.yourapp.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/*
 * Profile · Learning progress (education)
 * A "continue where you left off" card with progress, totals, and earned
 * certificates you can download.
 *
 * The design uses Lexend (good for reading); set it in your Typography.
 */

data class ProfileCertificate(val id: String, val course: String, val completed: String)

data class ProfileLearningState(
    val name: String = "[Full name]",
    val initials: String = "AB",
    val since: String = "Learning since 2024",
    val courseTitle: String? = "[Course title]",
    val lessonLine: String = "Lesson 14 of 22 · [Lesson name]",
    val courseProgress: Float = 0.65f,
    val courses: String = "6",
    val hours: String = "42 h",
    val certificatesCount: String = "3",
    val certificates: List<ProfileCertificate> = emptyList(),
)

sealed interface ProfileLearningEvent {
    data object Resume : ProfileLearningEvent
    data class DownloadCertificate(val id: String) : ProfileLearningEvent
}

data class ProfileLearningStrings(
    val continueLearning: String = "CONTINUE LEARNING",
    val resume: String = "Resume",
    val courses: String = "Courses",
    val learned: String = "Learned",
    val certificates: String = "Certificates",
    val completed: (String) -> String = { "Completed $it" },
    val download: (String) -> String = { "Download certificate for $it" },
    val progress: (Int) -> String = { "Course $it percent complete" },
)

@Composable
fun ProfileLearning(
    state: ProfileLearningState,
    onEvent: (ProfileLearningEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileLearningStrings = ProfileLearningStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    avatar: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxSize().background(colors.surfaceContainerLowest).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Avatar(state.initials, avatar, 52.dp, accent, onAccent)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(state.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(state.since, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        if (state.courseTitle != null) {
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge).background(accent).padding(20.dp)) {
                Text(strings.continueLearning, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.2.sp, color = onAccent.copy(alpha = 0.8f))
                Spacer(Modifier.height(6.dp))
                Text(state.courseTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, color = onAccent)
                Text(state.lessonLine, style = MaterialTheme.typography.bodyMedium, color = onAccent.copy(alpha = 0.85f))
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { state.courseProgress.coerceIn(0f, 1f) },
                    color = onAccent,
                    trackColor = onAccent.copy(alpha = 0.25f),
                    strokeCap = StrokeCap.Round,
                    modifier = Modifier.fillMaxWidth().height(6.dp).semantics { contentDescription = strings.progress((state.courseProgress * 100).roundToInt()) },
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { onEvent(ProfileLearningEvent.Resume) },
                    colors = ButtonDefaults.buttonColors(containerColor = onAccent, contentColor = accent),
                ) { Text(strings.resume, fontWeight = FontWeight.SemiBold) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(state.courses to strings.courses, state.hours to strings.learned, state.certificatesCount to strings.certificates).forEach { (v, l) ->
                Column(
                    Modifier.weight(1f).clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow).padding(14.dp).semantics(mergeDescendants = true) {},
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(v, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
                    Text(l, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
        if (state.certificates.isNotEmpty()) {
            Text(strings.certificates, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp).semantics { heading() })
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow)) {
                state.certificates.forEachIndexed { i, c ->
                    if (i > 0) HorizontalDivider(Modifier.padding(start = 68.dp), color = colors.outlineVariant)
                    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(accent.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(SchoolIcon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.course, style = MaterialTheme.typography.bodyLarge)
                            Text(strings.completed(c.completed), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        IconButton(onClick = { onEvent(ProfileLearningEvent.DownloadCertificate(c.id)) }) {
                            Icon(ReceiptIcon, contentDescription = strings.download(c.course), tint = colors.onSurfaceVariant)
                        }
                    }
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

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val ReceiptIcon = vectorIcon(
    "Receipt",
    "M18 17H6v-2h12v2zm0-4H6v-2h12v2zm0-4H6V7h12v2zM3 22l1.5-1.5L6 22l1.5-1.5L9 22l1.5-1.5L12 22l1.5-1.5L15 22l1.5-1.5L18 22l1.5-1.5L21 22V2l-1.5 1.5L18 2l-1.5 1.5L15 2l-1.5 1.5L12 2l-1.5 1.5L9 2 7.5 3.5 6 2 4.5 3.5 3 2v20z",
)
private val SchoolIcon = vectorIcon("School", "M5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82zM12 3L1 9l11 6 9-4.91V17h2V9L12 3z")

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
internal fun ProfileLearningPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileLearning(
            state = ProfileLearningState(
                certificates = listOf(
                    ProfileCertificate("1", "Kotlin fundamentals", "May 2026"),
                    ProfileCertificate("2", "Compose layouts", "Aug 2026"),
                ),
            ),
            onEvent = {},
            accent = Color(0xFF4B32C3),
            onAccent = Color.White,
        )
    }
}
