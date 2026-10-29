package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Onboarding · Join workspace (enterprise)
 * After accepting an invite: shows the workspace, lets the user pick a
 * team, and says which role they'll get. Squarer corners suit B2B tools.
 * The design uses IBM Plex Sans; set it in your Typography to match.
 */

data class OnboardingTeam(val id: String, val name: String, val members: Int)

data class OnboardingJoinWorkspaceState(
    val workspaceName: String = "[Workspace name]",
    val workspaceInitial: String = "W",
    val invitedBy: String = "[Name]",
    val memberCount: Int = 48,
    val role: String = "Member",
    val teams: List<OnboardingTeam> = emptyList(),
    val selectedTeamId: String? = null,
    val isJoining: Boolean = false,
)

sealed interface OnboardingJoinWorkspaceEvent {
    data class PickTeam(val id: String) : OnboardingJoinWorkspaceEvent
    data object Join : OnboardingJoinWorkspaceEvent
    data object SwitchAccount : OnboardingJoinWorkspaceEvent
}

data class OnboardingJoinWorkspaceStrings(
    val invited: (by: String, members: Int) -> String = { by, n -> "Invited by $by · $n members" },
    val title: String = "Join your team",
    val body: String = "Pick the team you work in. You can be added to more later.",
    val people: (count: Int) -> String = { if (it == 1) "1 person" else "$it people" },
    val roleNotePrefix: String = "You'll join as ",
    val roleNoteSuffix: String = ". Your admin sets roles.",
    val join: String = "Join workspace",
    val switchAccount: String = "Not your work account? Switch",
)

@Composable
fun OnboardingJoinWorkspace(
    state: OnboardingJoinWorkspaceState,
    onEvent: (OnboardingJoinWorkspaceEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingJoinWorkspaceStrings = OnboardingJoinWorkspaceStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    workspaceLogo: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(8.dp)

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surfaceContainerLow)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 24.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.surface, RoundedCornerShape(10.dp))
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(10.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(44.dp).background(accent, shape), contentAlignment = Alignment.Center) {
                if (workspaceLogo != null) workspaceLogo()
                else Text(state.workspaceInitial, color = onAccent, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(state.workspaceName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(strings.invited(state.invitedBy, state.memberCount), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(32.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(strings.body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))

        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.teams.forEach { team ->
                val selected = team.id == state.selectedTeamId
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(if (selected) accent.copy(alpha = 0.08f) else colors.surface)
                        .border(if (selected) 2.dp else 1.dp, if (selected) accent else colors.outlineVariant, shape)
                        .selectable(selected = selected, role = Role.RadioButton) { onEvent(OnboardingJoinWorkspaceEvent.PickTeam(team.id)) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = accent))
                    Spacer(Modifier.width(12.dp))
                    Text(team.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Text(strings.people(team.members), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Top) {
            Icon(InfoIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                buildAnnotatedString {
                    append(strings.roleNotePrefix)
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.onSurface)) { append(state.role) }
                    append(strings.roleNoteSuffix)
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(OnboardingJoinWorkspaceEvent.Join) },
            enabled = state.selectedTeamId != null && !state.isJoining,
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (state.isJoining) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
            else Text(strings.join, fontWeight = FontWeight.SemiBold)
        }
        TextButton(
            onClick = { onEvent(OnboardingJoinWorkspaceEvent.SwitchAccount) },
            colors = ButtonDefaults.textButtonColors(contentColor = accent),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(48.dp),
        ) { Text(strings.switchAccount) }
    }
}

private val InfoIcon: ImageVector = ImageVector.Builder("Info", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z"),
        fill = SolidColor(Color.Black),
    )
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

@Preview(showBackground = true)
@Composable
internal fun OnboardingJoinWorkspacePreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingJoinWorkspace(
            state = OnboardingJoinWorkspaceState(
                teams = listOf(
                    OnboardingTeam("eng", "Engineering", 21),
                    OnboardingTeam("design", "Design", 8),
                    OnboardingTeam("sales", "Sales", 12),
                    OnboardingTeam("ops", "Operations", 7),
                ),
                selectedTeamId = "design",
            ),
            onEvent = {},
            accent = Color(0xFF2B45C4),
        )
    }
}
