package com.yourapp.ui.screens.profile

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Profile · Cover and stats
 * A social profile: cover image, avatar overlapping the cover, name, bio,
 * counts, actions and a tabbed post grid. One file covers five states:
 *  - your own profile (Edit / Share)
 *  - someone else's (Follow / Message, mutual followers, More menu)
 *  - empty, new user (add cover, add bio, first-post prompt)
 *  - private account (Request to follow, locked content)
 *  - loading (shimmer-free skeleton)
 *
 * Load the cover and post images with your image loader and pass them in.
 */

data class ProfileCoverStat(val value: String, val label: String)

enum class ProfileRelation { Own, NotFollowing, Following, Requested }

data class ProfileCoverStatsState(
    val isLoading: Boolean = false,
    val relation: ProfileRelation = ProfileRelation.Own,
    val isPrivate: Boolean = false,
    val isVerified: Boolean = false,
    val name: String = "[Full name]",
    val username: String = "@username",
    val initials: String = "AB",
    val bio: String? = null,
    val mutuals: String? = null,
    val stats: List<ProfileCoverStat> = emptyList(),
    val tabs: List<String> = listOf("Posts", "Saved", "Tagged"),
    val selectedTab: Int = 0,
    val posts: List<@Composable () -> Unit> = emptyList(),
)

sealed interface ProfileCoverStatsEvent {
    data object Back : ProfileCoverStatsEvent
    data object Settings : ProfileCoverStatsEvent
    data object More : ProfileCoverStatsEvent
    data object EditProfile : ProfileCoverStatsEvent
    data object Share : ProfileCoverStatsEvent
    data object Follow : ProfileCoverStatsEvent
    data object Message : ProfileCoverStatsEvent
    data object AddCover : ProfileCoverStatsEvent
    data object AddBio : ProfileCoverStatsEvent
    data object CreatePost : ProfileCoverStatsEvent
    data class SelectTab(val index: Int) : ProfileCoverStatsEvent
    data class OpenPost(val index: Int) : ProfileCoverStatsEvent
}

data class ProfileCoverStatsStrings(
    val title: String = "Profile",
    val back: String = "Back",
    val settings: String = "Settings",
    val more: String = "More options, including report and block",
    val editProfile: String = "Edit profile",
    val share: String = "Share profile",
    val follow: String = "Follow",
    val following: String = "Following",
    val requestFollow: String = "Request to follow",
    val requested: String = "Requested",
    val message: String = "Message",
    val addCover: String = "Add cover",
    val addBio: String = "Add a bio",
    val noPosts: String = "No posts yet",
    val noPostsBody: String = "Your posts will show up here. Share your first one.",
    val createPost: String = "Create a post",
    val privateTitle: String = "This account is private",
    val privateBody: String = "Follow them to see their posts.",
    val verified: String = "Verified",
    val loading: String = "Loading profile",
)

@OptIn(ExperimentalMaterial3Api::class) // PrimaryTabRow on older Material 3 versions
@Composable
fun ProfileCoverStats(
    state: ProfileCoverStatsState,
    onEvent: (ProfileCoverStatsEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileCoverStatsStrings = ProfileCoverStatsStrings(),
    cover: (@Composable () -> Unit)? = null,
    avatar: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val own = state.relation == ProfileRelation.Own
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        // Top app bar: title for your own profile, back + username for others.
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (own) {
                Text(strings.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 12.dp).weight(1f))
                IconButton(onClick = { onEvent(ProfileCoverStatsEvent.Settings) }) { Icon(SettingsIcon, contentDescription = strings.settings) }
            } else {
                IconButton(onClick = { onEvent(ProfileCoverStatsEvent.Back) }) { Icon(BackIcon, contentDescription = strings.back) }
                Text(state.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = { onEvent(ProfileCoverStatsEvent.More) }) { Icon(MoreIcon, contentDescription = strings.more) }
            }
        }
        if (state.isLoading) {
            Skeleton(strings.loading)
            return@Column
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // Cover, inset from the edges so the avatar can overlap it cleanly.
            if (!state.isPrivate) {
                Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(112.dp)) {
                    ImageSlot(cover, Modifier.matchParentSize(), RoundedCornerShape(20.dp))
                    if (cover == null && own) {
                        TextButton(onClick = { onEvent(ProfileCoverStatsEvent.AddCover) }, modifier = Modifier.align(Alignment.Center)) {
                            Icon(CameraIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.addCover)
                        }
                    }
                }
            }
            Column(Modifier.padding(horizontal = 16.dp)) {
                // Avatar overlaps the cover; zIndex-free because it's drawn after the cover.
                Box(
                    Modifier
                        .padding(start = 8.dp)
                        .offset(y = if (state.isPrivate) 16.dp else (-44).dp)
                        .border(4.dp, colors.surface, CircleShape),
                ) { Avatar(state.initials, avatar, 88.dp, colors.primary, colors.onPrimary) }
                Column(Modifier.offset(y = if (state.isPrivate) 24.dp else (-32).dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(state.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        if (state.isVerified) {
                            Spacer(Modifier.width(6.dp))
                            Icon(VerifiedIcon, contentDescription = strings.verified, tint = colors.primary, modifier = Modifier.size(18.dp))
                        }
                        if (state.isPrivate) {
                            Spacer(Modifier.width(6.dp))
                            Icon(LockIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(state.username, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    if (state.bio != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(state.bio, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    } else if (own) {
                        TextButton(onClick = { onEvent(ProfileCoverStatsEvent.AddBio) }, contentPadding = PaddingValues(0.dp)) {
                            Icon(AddIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.addBio)
                        }
                    }
                    if (state.mutuals != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(state.mutuals, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        state.stats.forEach { s ->
                            Column(Modifier.semantics(mergeDescendants = true) {}) {
                                Text(s.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(s.label, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (state.relation) {
                            ProfileRelation.Own -> {
                                Button(onClick = { onEvent(ProfileCoverStatsEvent.EditProfile) }, modifier = Modifier.weight(1f)) {
                                    Icon(EditIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(strings.editProfile)
                                }
                                OutlinedButton(onClick = { onEvent(ProfileCoverStatsEvent.Share) }, modifier = Modifier.weight(1f)) { Text(strings.share) }
                            }
                            ProfileRelation.Following, ProfileRelation.NotFollowing, ProfileRelation.Requested -> {
                                val followLabel = when {
                                    state.relation == ProfileRelation.Following -> strings.following
                                    state.relation == ProfileRelation.Requested -> strings.requested
                                    state.isPrivate -> strings.requestFollow
                                    else -> strings.follow
                                }
                                if (state.relation == ProfileRelation.NotFollowing) {
                                    Button(onClick = { onEvent(ProfileCoverStatsEvent.Follow) }, modifier = Modifier.weight(1f)) { Text(followLabel) }
                                } else {
                                    OutlinedButton(onClick = { onEvent(ProfileCoverStatsEvent.Follow) }, modifier = Modifier.weight(1f)) { Text(followLabel) }
                                }
                                if (!state.isPrivate || state.relation == ProfileRelation.Following) {
                                    OutlinedButton(onClick = { onEvent(ProfileCoverStatsEvent.Message) }, modifier = Modifier.weight(1f)) { Text(strings.message) }
                                }
                            }
                        }
                    }
                }
            }

            val locked = state.isPrivate && state.relation != ProfileRelation.Following && !own
            if (locked) {
                Spacer(Modifier.height(32.dp))
                HorizontalDivider()
                Message(LockIcon, strings.privateTitle, strings.privateBody)
            } else {
                PrimaryTabRow(selectedTabIndex = state.selectedTab, containerColor = colors.surface, modifier = Modifier.offset(y = (-16).dp)) {
                    state.tabs.forEachIndexed { i, t ->
                        Tab(selected = i == state.selectedTab, onClick = { onEvent(ProfileCoverStatsEvent.SelectTab(i)) }, text = { Text(t) })
                    }
                }
                if (state.posts.isEmpty()) {
                    Message(CameraIcon, strings.noPosts, if (own) strings.noPostsBody else "")
                    if (own) {
                        Button(
                            onClick = { onEvent(ProfileCoverStatsEvent.CreatePost) },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        ) { Text(strings.createPost) }
                    }
                } else {
                    Column(Modifier.offset(y = (-16).dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        state.posts.chunked(3).forEachIndexed { r, row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                row.forEachIndexed { c, post ->
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .background(colors.surfaceContainerHigh)
                                            .clickable { onEvent(ProfileCoverStatsEvent.OpenPost(r * 3 + c)) },
                                    ) { post() }
                                }
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Message(icon: ImageVector, title: String, body: String) {
    val colors = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(72.dp).border(2.dp, colors.onSurface, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        if (body.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(body, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(16.dp))
    }
}

/** Grey blocks in the shape of the real layout, announced once as loading. */
@Composable
private fun Skeleton(label: String) {
    val block = MaterialTheme.colorScheme.surfaceContainerHigh
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp).clearAndSetSemantics { contentDescription = label },
    ) {
        Box(Modifier.fillMaxWidth().height(112.dp).background(block, RoundedCornerShape(20.dp)))
        Box(Modifier.padding(start = 8.dp).offset(y = (-44).dp).size(88.dp).border(4.dp, MaterialTheme.colorScheme.surface, CircleShape).background(block, CircleShape))
        Column(Modifier.offset(y = (-32).dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.width(160.dp).height(18.dp).background(block, CircleShape))
            Box(Modifier.width(100.dp).height(12.dp).background(block, CircleShape))
            Box(Modifier.fillMaxWidth().height(12.dp).background(block, CircleShape))
            Box(Modifier.fillMaxWidth(0.7f).height(12.dp).background(block, CircleShape))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).height(40.dp).background(block, CircleShape))
                Box(Modifier.weight(1f).height(40.dp).background(block, CircleShape))
            }
            Spacer(Modifier.height(8.dp))
            repeat(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(3) { Box(Modifier.weight(1f).aspectRatio(1f).background(block)) }
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
private val LockIcon = vectorIcon(
    "Lock",
    "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z",
)
private val EditIcon = vectorIcon(
    "Edit",
    "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z",
)
private val SettingsIcon = vectorIcon(
    "Settings",
    "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z",
)
private val AddIcon = vectorIcon("Add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")
private val VerifiedIcon = vectorIcon(
    "Verified",
    "M23 12l-2.44-2.79.34-3.69-3.61-.82-1.89-3.2L12 2.96 8.6 1.5 6.71 4.69 3.1 5.5l.34 3.7L1 12l2.44 2.79-.34 3.7 3.61.82L8.6 22.5l3.4-1.47 3.4 1.46 1.89-3.19 3.61-.82-.34-3.69L23 12zm-12.91 4.72l-3.8-3.81 1.48-1.48 2.32 2.33 5.85-5.87 1.48 1.48-7.33 7.35z", autoMirror = true,
)
private val CameraIcon = vectorIcon(
    "Camera",
    "M12 15.2a3.2 3.2 0 1 0 0-6.4 3.2 3.2 0 0 0 0 6.4zM9 2L7.17 4H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2h-3.17L15 2H9zm3 15c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5z",
)
private val MoreIcon = vectorIcon(
    "More",
    "M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z",
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

@Preview(showBackground = true, name = "Own")
@Composable
internal fun ProfileCoverStatsPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileCoverStats(
            state = ProfileCoverStatsState(
                bio = "Short bio, up to two lines. What you make, where you are, what you care about.",
                stats = listOf(ProfileCoverStat("128", "Posts"), ProfileCoverStat("1.2k", "Followers"), ProfileCoverStat("312", "Following")),
                posts = List<@Composable () -> Unit>(6) { {} },
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Private, someone else's")
@Composable
internal fun ProfileCoverStatsPrivatePreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileCoverStats(
            state = ProfileCoverStatsState(
                relation = ProfileRelation.NotFollowing,
                isPrivate = true,
                name = "[Their name]",
                username = "@theirusername",
                initials = "EF",
                stats = listOf(ProfileCoverStat("23", "Posts"), ProfileCoverStat("140", "Followers"), ProfileCoverStat("98", "Following")),
            ),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
internal fun ProfileCoverStatsLoadingPreview() {
    MaterialTheme(colorScheme = PreviewColors) { ProfileCoverStats(state = ProfileCoverStatsState(isLoading = true), onEvent = {}) }
}
