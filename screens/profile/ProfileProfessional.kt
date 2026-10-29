package com.yourapp.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Profile · Professional (freelance)
 * A public profile for hiring: rating and reviews up top, Hire and
 * Message, skills, an experience timeline and a portfolio strip.
 *
 * accent defaults to MaterialTheme.colorScheme.primary. The design uses
 * IBM Plex Sans; set it in your Typography to match.
 */

data class ProfileExperience(val title: String, val place: String, val current: Boolean)

data class ProfileProfessionalState(
    val name: String = "[Full name]",
    val initials: String = "AB",
    val verified: Boolean = true,
    val headline: String = "[Role] · [Specialty]",
    val rating: String = "4.9",
    val reviews: String = "(32 reviews) · [City]",
    val skills: List<String> = emptyList(),
    val moreSkills: Int = 0,
    val experience: List<ProfileExperience> = emptyList(),
    val portfolio: List<@Composable () -> Unit> = emptyList(),
)

sealed interface ProfileProfessionalEvent {
    data object Back : ProfileProfessionalEvent
    data object Hire : ProfileProfessionalEvent
    data object Message : ProfileProfessionalEvent
    data object Reviews : ProfileProfessionalEvent
    data object AllSkills : ProfileProfessionalEvent
    data object Portfolio : ProfileProfessionalEvent
    data class OpenPortfolioItem(val index: Int) : ProfileProfessionalEvent
}

data class ProfileProfessionalStrings(
    val back: String = "Back",
    val verified: String = "Verified",
    val hire: String = "Hire",
    val message: String = "Message",
    val skills: String = "Skills",
    val experience: String = "Experience",
    val portfolio: String = "Portfolio",
    val seeAll: String = "See all",
)

@OptIn(ExperimentalLayoutApi::class) // FlowRow on older Compose versions
@Composable
fun ProfileProfessional(
    state: ProfileProfessionalState,
    onEvent: (ProfileProfessionalEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileProfessionalStrings = ProfileProfessionalStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    avatar: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(8.dp)
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        IconButton(onClick = { onEvent(ProfileProfessionalEvent.Back) }, modifier = Modifier.padding(start = 4.dp, top = 8.dp)) {
            Icon(BackIcon, contentDescription = strings.back)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(state.initials, avatar, 72.dp, accent, onAccent)
                Spacer(Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(state.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        if (state.verified) {
                            Spacer(Modifier.width(6.dp))
                            Icon(VerifiedIcon, contentDescription = strings.verified, tint = accent, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text(state.headline, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    Row(
                        Modifier.clickable(role = Role.Button) { onEvent(ProfileProfessionalEvent.Reviews) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(StarIcon, contentDescription = null, tint = Color(0xFFE0A100), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(state.rating, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Text(state.reviews, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onEvent(ProfileProfessionalEvent.Hire) },
                    shape = shape,
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
                    modifier = Modifier.weight(1f).height(48.dp),
                ) { Text(strings.hire, fontWeight = FontWeight.SemiBold) }
                OutlinedButton(onClick = { onEvent(ProfileProfessionalEvent.Message) }, shape = shape, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text(strings.message)
                }
            }
            Heading(strings.skills)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.skills.forEach { s ->
                    Text(s, style = MaterialTheme.typography.labelLarge, modifier = Modifier.background(accent.copy(alpha = 0.10f), shape).padding(horizontal = 10.dp, vertical = 6.dp))
                }
                if (state.moreSkills > 0) {
                    Text(
                        "+${state.moreSkills}",
                        style = MaterialTheme.typography.labelLarge,
                        color = accent,
                        modifier = Modifier.clip(shape).clickable { onEvent(ProfileProfessionalEvent.AllSkills) }.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
            Heading(strings.experience)
            state.experience.forEachIndexed { i, e ->
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(16.dp)) {
                        Box(
                            Modifier.padding(top = 4.dp).size(12.dp).then(
                                if (e.current) Modifier.background(accent, CircleShape) else Modifier.border(2.dp, colors.outline, CircleShape),
                            ),
                        )
                        if (i < state.experience.lastIndex) Box(Modifier.width(2.dp).weight(1f).background(colors.outlineVariant))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.padding(bottom = 16.dp)) {
                        Text(e.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(e.place, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { Heading(strings.portfolio) }
                TextButton(onClick = { onEvent(ProfileProfessionalEvent.Portfolio) }, modifier = Modifier.padding(top = 16.dp)) { Text(strings.seeAll, color = accent) }
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                state.portfolio.forEachIndexed { i, item ->
                    Box(Modifier.clickable { onEvent(ProfileProfessionalEvent.OpenPortfolioItem(i)) }) {
                        ImageSlot(item, Modifier.size(width = 160.dp, height = 112.dp), shape)
                    }
                }
            }
        }
    }
}

@Composable
private fun Heading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp).semantics { heading() },
    )
}

/** Your picture, or initials on a tinted circle. */
@Composable
private fun Avatar(initials: String, image: (@Composable () -> Unit)?, size: Dp, container: Color, content: Color) {
    Box(Modifier.size(size).clip(CircleShape).background(container), contentAlignment = Alignment.Center) {
        if (image != null) image()
        else Text(initials, color = content, fontWeight = FontWeight.SemiBold, fontSize = (size.value * 0.36f).sp)
    }
}

/** Shows your image, or a soft placeholder when there is none. */
@Composable
private fun ImageSlot(image: (@Composable () -> Unit)?, modifier: Modifier, shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.medium) {
    Box(modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
        image?.invoke()
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val StarIcon = vectorIcon("Star", "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z")
private val VerifiedIcon = vectorIcon(
    "Verified",
    "M23 12l-2.44-2.79.34-3.69-3.61-.82-1.89-3.2L12 2.96 8.6 1.5 6.71 4.69 3.1 5.5l.34 3.7L1 12l2.44 2.79-.34 3.7 3.61.82L8.6 22.5l3.4-1.47 3.4 1.46 1.89-3.19 3.61-.82-.34-3.69L23 12zm-12.91 4.72l-3.8-3.81 1.48-1.48 2.32 2.33 5.85-5.87 1.48 1.48-7.33 7.35z", autoMirror = true,
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
internal fun ProfileProfessionalPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileProfessional(
            state = ProfileProfessionalState(
                skills = listOf("Kotlin", "Jetpack Compose", "System design", "Testing"),
                moreSkills = 3,
                experience = listOf(
                    ProfileExperience("Senior Android Engineer", "Acme · 2022 – Present", current = true),
                    ProfileExperience("Android Engineer", "Globex · 2019 – 2022", current = false),
                ),
                portfolio = List<@Composable () -> Unit>(3) { {} },
            ),
            onEvent = {},
            accent = Color(0xFF2B45C4),
        )
    }
}
