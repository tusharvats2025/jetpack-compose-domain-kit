package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Onboarding · Style picker (fashion)
 * A grid of looks to tap, plus the usual size, to tune a fashion feed.
 * Square corners, an italic serif headline and letter-spaced labels.
 *
 * Put each look's photo in its image slot (AsyncImage with Crop). The
 * design pairs Bodoni Moda with Jost; pass them as serifFont and sansFont.
 */

data class OnboardingLook(
    val id: String,
    val label: String,
    val image: (@Composable () -> Unit)? = null,
)

data class OnboardingStylePickerState(
    val step: Int = 2,
    val totalSteps: Int = 3,
    val looks: List<OnboardingLook> = emptyList(),
    val selectedLooks: Set<String> = emptySet(),
    val sizes: List<String> = listOf("XS", "S", "M", "L", "XL"),
    val selectedSize: String? = null,
)

sealed interface OnboardingStylePickerEvent {
    data object Skip : OnboardingStylePickerEvent
    data class ToggleLook(val id: String) : OnboardingStylePickerEvent
    data class PickSize(val size: String) : OnboardingStylePickerEvent
    data object Continue : OnboardingStylePickerEvent
}

data class OnboardingStylePickerStrings(
    val step: (step: Int, total: Int) -> String = { s, t -> "STEP $s / $t" },
    val skip: String = "SKIP",
    val title: String = "Pick what you'd wear",
    val body: String = "Choose any. We'll tune your feed.",
    val size: String = "USUAL SIZE",
    val submit: String = "CONTINUE",
)

data class OnboardingStylePickerColors(
    val tone: Color = Color(0xFFCFC8C0),
    val background: Color = Color(0xFFFFFFFF),
    val ink: Color = Color(0xFF111111),
    val muted: Color = Color(0xFF4F4A45),
    val line: Color = Color(0xFFD9D4CE),
    val onInk: Color = Color(0xFFFFFFFF),
)

@Composable
fun OnboardingStylePicker(
    state: OnboardingStylePickerState,
    onEvent: (OnboardingStylePickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingStylePickerStrings = OnboardingStylePickerStrings(),
    colors: OnboardingStylePickerColors = OnboardingStylePickerColors(),
    serifFont: FontFamily = FontFamily.Serif,
    sansFont: FontFamily = FontFamily.Default,
) {
    val sans = TextStyle(fontFamily = sansFont, color = colors.ink)
    val label = sans.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)

    Column(
        modifier
            .fillMaxSize()
            .background(colors.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(strings.step(state.step, state.totalSteps), style = label.copy(color = colors.muted), modifier = Modifier.weight(1f))
            TextButton(onClick = { onEvent(OnboardingStylePickerEvent.Skip) }, shape = RectangleShape) {
                Text(strings.skip, style = label)
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            strings.title,
            style = TextStyle(fontFamily = serifFont, fontStyle = FontStyle.Italic, fontSize = 34.sp, lineHeight = 40.sp, color = colors.ink),
        )
        Spacer(Modifier.height(8.dp))
        Text(strings.body, style = sans.copy(fontSize = 15.sp, color = colors.muted))
        Spacer(Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.looks.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { look ->
                        val selected = look.id in state.selectedLooks
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(3f / 4f)
                                .background(colors.tone)
                                .clipToBounds()
                                .then(if (selected) Modifier.border(2.dp, colors.ink) else Modifier)
                                .toggleable(
                                    value = selected,
                                    role = Role.Checkbox,
                                    onValueChange = { onEvent(OnboardingStylePickerEvent.ToggleLook(look.id)) },
                                )
                                .semantics { contentDescription = look.label },
                        ) {
                            look.image?.invoke()
                            if (selected) {
                                Box(
                                    Modifier.align(Alignment.TopEnd).padding(6.dp).size(24.dp).background(colors.ink, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) { Icon(CheckIcon, contentDescription = null, tint = colors.onInk, modifier = Modifier.size(16.dp)) }
                            }
                        }
                    }
                    // Keep the last row's tiles the same width as the others.
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
        Text(strings.size, style = label.copy(color = colors.muted))
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.sizes.forEach { size ->
                val selected = size == state.selectedSize
                Box(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .background(if (selected) colors.ink else colors.background)
                        .border(1.dp, if (selected) colors.ink else colors.line)
                        .selectable(selected = selected, role = Role.RadioButton) { onEvent(OnboardingStylePickerEvent.PickSize(size)) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(size, style = sans.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = if (selected) colors.onInk else colors.ink))
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { onEvent(OnboardingStylePickerEvent.Continue) },
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(containerColor = colors.ink, contentColor = colors.onInk),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(strings.submit, style = label.copy(fontSize = 14.sp, letterSpacing = 3.sp, color = colors.onInk)) }
    }
}

private val CheckIcon: ImageVector = ImageVector.Builder("Check", 24.dp, 24.dp, 24f, 24f)
    .addPath(addPathNodes("M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"), fill = SolidColor(Color.Black))
    .build()

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

@Preview
@Composable
internal fun OnboardingStylePickerPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingStylePicker(
            state = OnboardingStylePickerState(
                looks = (1..6).map { OnboardingLook("look$it", "Look $it") },
                selectedLooks = setOf("look1", "look5"),
                selectedSize = "M",
            ),
            onEvent = {},
        )
    }
}
