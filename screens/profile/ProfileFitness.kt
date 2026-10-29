package com.yourapp.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Profile · Fitness
 * Level and streak up top, totals, a simple weekly bar chart, and
 * achievements (locked ones shown dimmed with a lock).
 *
 * The chart is drawn with plain Boxes, scaled to the week's maximum, and
 * described in one sentence for screen readers. The design uses Sora.
 */

data class ProfileFitnessStat(val value: String, val label: String)
data class ProfileAchievement(val id: String, val label: String, val unlocked: Boolean)

data class ProfileFitnessState(
    val name: String = "[Full name]",
    val initials: String = "AB",
    val levelLine: String = "Level 7 · Runner",
    val streak: ProfileFitnessStat = ProfileFitnessStat("12", "Day streak"),
    val workouts: ProfileFitnessStat = ProfileFitnessStat("86", "Workouts"),
    val minutes: ProfileFitnessStat = ProfileFitnessStat("1,940", "Minutes"),
    /** Minutes per day, Monday first. */
    val week: List<Int> = listOf(30, 45, 0, 40, 25, 60, 14),
    val achievements: List<ProfileAchievement> = emptyList(),
)

sealed interface ProfileFitnessEvent {
    data object Edit : ProfileFitnessEvent
    data object AllAchievements : ProfileFitnessEvent
    data class OpenAchievement(val id: String) : ProfileFitnessEvent
}

data class ProfileFitnessStrings(
    val edit: String = "Edit profile",
    val thisWeek: String = "This week",
    val total: (Int) -> String = { "$it min" },
    val days: List<String> = listOf("M", "T", "W", "T", "F", "S", "S"),
    val dayNames: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    val chartDescription: (String) -> String = { "Minutes per day this week: $it" },
    val achievements: String = "Achievements",
    val seeAll: String = "See all",
    val locked: String = "Locked",
)

@Composable
fun ProfileFitness(
    state: ProfileFitnessState,
    onEvent: (ProfileFitnessEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileFitnessStrings = ProfileFitnessStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    avatar: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val max = (state.week.maxOrNull() ?: 0).coerceAtLeast(1)
    Column(
        modifier.fillMaxSize().background(colors.surfaceContainerLowest).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Avatar(state.initials, avatar, 56.dp, accent, onAccent)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(state.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    state.levelLine,
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    modifier = Modifier.padding(top = 2.dp).background(accent.copy(alpha = 0.12f), CircleShape).padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
            IconButton(onClick = { onEvent(ProfileFitnessEvent.Edit) }) { Icon(EditIcon, contentDescription = strings.edit) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(FireIcon to state.streak, BoltIcon to state.workouts, ClockIcon to state.minutes).forEach { (icon, stat) ->
                Column(
                    Modifier.weight(1f).clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow).padding(14.dp).semantics(mergeDescendants = true) {},
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(stat.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(stat.label, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
        Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow).padding(16.dp)) {
            Row {
                Text(strings.thisWeek, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(strings.total(state.week.sum()), style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.height(16.dp))
            val description = state.week.mapIndexed { i, m -> "${strings.dayNames.getOrElse(i) { "" }} $m" }.joinToString(", ")
            Row(
                Modifier.fillMaxWidth().height(120.dp).clearAndSetSemantics { contentDescription = strings.chartDescription(description) },
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                state.week.forEachIndexed { i, m ->
                    val isToday = i == state.week.lastIndex
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight((m / max.toFloat()).coerceAtLeast(0.04f))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(if (isToday) accent.copy(alpha = 0.45f) else accent),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                strings.days.forEach { d ->
                    Text(d, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
        }
        Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(strings.achievements, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                TextButton(onClick = { onEvent(ProfileFitnessEvent.AllAchievements) }) { Text(strings.seeAll, color = accent) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.achievements.take(4).forEach { a ->
                    Column(
                        Modifier.weight(1f).clip(MaterialTheme.shapes.medium).clickable { onEvent(ProfileFitnessEvent.OpenAchievement(a.id)) }.padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier.size(48.dp).background(if (a.unlocked) accent.copy(alpha = 0.14f) else colors.surfaceContainerHigh, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(if (a.unlocked) TrophyIcon else LockIcon, contentDescription = if (a.unlocked) null else strings.locked, tint = if (a.unlocked) accent else colors.onSurfaceVariant, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            a.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (a.unlocked) colors.onSurface else colors.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
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

private val LockIcon = vectorIcon(
    "Lock",
    "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z",
)
private val EditIcon = vectorIcon(
    "Edit",
    "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
)
private val BoltIcon = vectorIcon(
    "Bolt",
    "M11 21h-1l1-7H7.5c-.88 0-.33-.75-.31-.78C8.48 10.94 10.42 7.54 13.01 3h1l-1 7h3.51c.4 0 .62.19.4.66C12.97 17.55 11 21 11 21z",
)
private val ClockIcon = vectorIcon(
    "Clock",
    "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z",
)
private val FireIcon = vectorIcon(
    "Fire",
    "M13.5.67s.74 2.65.74 4.8c0 2.06-1.35 3.73-3.41 3.73-2.07 0-3.63-1.67-3.63-3.73l.03-.36C5.21 7.51 4 10.62 4 14c0 4.42 3.58 8 8 8s8-3.58 8-8C20 8.61 17.41 3.8 13.5.67zM11.71 19c-1.78 0-3.22-1.4-3.22-3.14 0-1.62 1.05-2.76 2.81-3.12 1.77-.36 3.6-1.21 4.62-2.58.39 1.29.59 2.65.59 4.04 0 2.65-2.15 4.8-4.8 4.8z",
)
private val TrophyIcon = vectorIcon(
    "Trophy",
    "M19 5h-2V3H7v2H5c-1.1 0-2 .9-2 2v1c0 2.55 1.92 4.63 4.39 4.94.63 1.5 1.98 2.63 3.61 2.96V19H7v2h10v-2h-4v-3.1c1.63-.33 2.98-1.46 3.61-2.96C19.08 12.63 21 10.55 21 8V7c0-1.1-.9-2-2-2zM5 8V7h2v3.82C5.84 10.4 5 9.3 5 8zm14 0c0 1.3-.84 2.4-2 2.82V7h2v1z",
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
internal fun ProfileFitnessPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileFitness(
            state = ProfileFitnessState(
                achievements = listOf(
                    ProfileAchievement("5k", "First 5K", true),
                    ProfileAchievement("streak", "10-day streak", true),
                    ProfileAchievement("pb", "Personal best", true),
                    ProfileAchievement("marathon", "Marathon", false),
                ),
            ),
            onEvent = {},
            accent = Color(0xFF2F6B3F),
            onAccent = Color.White,
        )
    }
}
