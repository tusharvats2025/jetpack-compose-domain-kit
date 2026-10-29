package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Onboarding · Photos and prompts (dating)
 * A six-slot photo grid where the first photo is the main one, and
 * written prompts. Continue unlocks at the minimum number of photos.
 *
 * Picking, cropping and uploading photos is yours (PickVisualMedia);
 * pass each loaded photo as a composable in photos. The design uses
 * DM Sans; set it in your Typography to match.
 */

data class OnboardingDatingPrompt(val id: String, val question: String, val answer: String)

data class OnboardingDatingPhotosState(
    val step: Int = 3,
    val totalSteps: Int = 5,
    val photos: List<@Composable () -> Unit> = emptyList(),
    val slots: Int = 6,
    val minimumPhotos: Int = 2,
    val prompts: List<OnboardingDatingPrompt> = emptyList(),
)

sealed interface OnboardingDatingPhotosEvent {
    data object Back : OnboardingDatingPhotosEvent
    data object AddPhoto : OnboardingDatingPhotosEvent
    data class RemovePhoto(val index: Int) : OnboardingDatingPhotosEvent
    data class EditPrompt(val id: String) : OnboardingDatingPhotosEvent
    data object AddPrompt : OnboardingDatingPhotosEvent
    data object Continue : OnboardingDatingPhotosEvent
}

data class OnboardingDatingPhotosStrings(
    val back: String = "Back",
    val title: String = "Add your photos",
    val body: (minimum: Int) -> String = { "At least $it. Clear photos of your face work best." },
    val main: String = "MAIN",
    val addPhoto: String = "Add photo",
    val removePhoto: (index: Int) -> String = { "Remove photo $it" },
    val prompts: String = "Prompts",
    val addPrompt: String = "+ Add another prompt",
    val submit: String = "Continue",
)

@Composable
fun OnboardingDatingPhotos(
    state: OnboardingDatingPhotosState,
    onEvent: (OnboardingDatingPhotosEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingDatingPhotosStrings = OnboardingDatingPhotosStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val colors = MaterialTheme.colorScheme
    val tileShape = RoundedCornerShape(14.dp)

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(OnboardingDatingPhotosEvent.Back) }, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(BackIcon, contentDescription = strings.back)
            }
            LinearProgressIndicator(
                progress = { state.step / state.totalSteps.coerceAtLeast(1).toFloat() },
                modifier = Modifier.weight(1f).height(6.dp),
                color = accent,
                strokeCap = StrokeCap.Round,
            )
            Spacer(Modifier.width(12.dp))
            Text("${state.step}/${state.totalSteps}", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
        }
        Spacer(Modifier.height(24.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(strings.body(state.minimumPhotos), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            (0 until state.slots).chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { i ->
                        val photo = state.photos.getOrNull(i)
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(3f / 4f)
                                .clip(tileShape)
                                .background(colors.surfaceContainerHigh),
                        ) {
                            if (photo != null) {
                                photo()
                                if (i == 0) {
                                    Text(
                                        strings.main,
                                        color = onAccent,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(8.dp)
                                            .background(accent, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    )
                                }
                                IconButton(
                                    onClick = { onEvent(OnboardingDatingPhotosEvent.RemovePhoto(i)) },
                                    modifier = Modifier.align(Alignment.TopEnd),
                                ) {
                                    Box(
                                        Modifier.size(26.dp).background(colors.surface, CircleShape),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(CloseIcon, contentDescription = strings.removePhoto(i + 1), modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else {
                                // Empty slot: dashed outline and a plus.
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .dashedBorder(colors.outline, 14.dp)
                                        .clickable(role = Role.Button) { onEvent(OnboardingDatingPhotosEvent.AddPhoto) }
                                        .semantics { contentDescription = strings.addPhoto },
                                    contentAlignment = Alignment.Center,
                                ) { Icon(AddIcon, contentDescription = null, tint = accent) }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text(strings.prompts, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            state.prompts.forEach { prompt ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, colors.outlineVariant, RoundedCornerShape(14.dp))
                        .clickable { onEvent(OnboardingDatingPhotosEvent.EditPrompt(prompt.id)) }
                        .padding(16.dp),
                ) {
                    Text(prompt.question, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(prompt.answer, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
            OutlinedButton(
                onClick = { onEvent(OnboardingDatingPhotosEvent.AddPrompt) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = accent),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(strings.addPrompt) }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onEvent(OnboardingDatingPhotosEvent.Continue) },
            enabled = state.photos.size >= state.minimumPhotos,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(strings.submit, fontWeight = FontWeight.Bold) }
    }
}

/** A dashed rounded outline, for empty slots. */
private fun Modifier.dashedBorder(color: Color, radius: Dp): Modifier = drawBehind {
    val stroke = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())))
    drawRoundRect(color = color, style = stroke, cornerRadius = CornerRadius(radius.toPx()))
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val CloseIcon = vectorIcon("Close", "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z")
private val AddIcon = vectorIcon("Add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")

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
internal fun OnboardingDatingPhotosPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingDatingPhotos(
            state = OnboardingDatingPhotosState(
                photos = listOf<@Composable () -> Unit>(
                    { Box(Modifier.fillMaxSize().background(Color(0xFFE8C9CF))) },
                    { Box(Modifier.fillMaxSize().background(Color(0xFFD9D2E6))) },
                ),
                prompts = listOf(OnboardingDatingPrompt("weekend", "My ideal weekend", "[Your answer]")),
            ),
            onEvent = {},
            accent = Color(0xFFA21C46),
        )
    }
}
