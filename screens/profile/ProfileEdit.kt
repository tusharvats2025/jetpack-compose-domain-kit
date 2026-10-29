package com.yourapp.ui.screens.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Profile · Edit profile
 * Photo, name, username (with change limits), bio with a live count and
 * website. Save sits in the top bar; an unsaved-changes bar appears once
 * something changed, with Discard.
 *
 * Handle the system back gesture with BackHandler(enabled = hasChanges)
 * and confirm in your own dialog before discarding.
 */

data class ProfileEditState(
    val initials: String = "AB",
    val name: String = "",
    val username: String = "",
    val usernameNote: String? = "You can change this once every 30 days.",
    val usernameError: String? = null,
    val bio: String = "",
    val bioLimit: Int = 150,
    val website: String = "",
    val hasChanges: Boolean = false,
    val isSaving: Boolean = false,
)

sealed interface ProfileEditEvent {
    data object Close : ProfileEditEvent
    data object Save : ProfileEditEvent
    data object ChangePhoto : ProfileEditEvent
    data class NameChanged(val value: String) : ProfileEditEvent
    data class UsernameChanged(val value: String) : ProfileEditEvent
    data class BioChanged(val value: String) : ProfileEditEvent
    data class WebsiteChanged(val value: String) : ProfileEditEvent
    data object Discard : ProfileEditEvent
}

data class ProfileEditStrings(
    val close: String = "Close",
    val title: String = "Edit profile",
    val save: String = "Save",
    val changePhoto: String = "Change photo",
    val name: String = "Name",
    val username: String = "Username",
    val bio: String = "Bio",
    val bioPlaceholder: String = "Tell people about yourself",
    val website: String = "Website",
    val unsaved: String = "You have unsaved changes",
    val discard: String = "Discard",
)

@Composable
fun ProfileEdit(
    state: ProfileEditState,
    onEvent: (ProfileEditEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileEditStrings = ProfileEditStrings(),
    avatar: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(ProfileEditEvent.Close) }) { Icon(CloseIcon, contentDescription = strings.close) }
            Text(strings.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Button(onClick = { onEvent(ProfileEditEvent.Save) }, enabled = state.hasChanges && !state.isSaving) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = LocalContentColor.current)
                else Text(strings.save)
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(state.initials, avatar, 72.dp, colors.primary, colors.onPrimary)
                Spacer(Modifier.width(16.dp))
                TextButton(onClick = { onEvent(ProfileEditEvent.ChangePhoto) }) { Text(strings.changePhoto) }
            }
            OutlinedTextField(
                value = state.name,
                onValueChange = { onEvent(ProfileEditEvent.NameChanged(it)) },
                label = { Text(strings.name) },
                singleLine = true,
                modifier = Modifier.keepAboveKeyboard().fillMaxWidth().semantics { contentType = ContentType.PersonFullName },
            )
            OutlinedTextField(
                value = state.username,
                onValueChange = { onEvent(ProfileEditEvent.UsernameChanged(it.trim().lowercase())) },
                label = { Text(strings.username) },
                prefix = { Text("@") },
                singleLine = true,
                isError = state.usernameError != null,
                supportingText = (state.usernameError ?: state.usernameNote)?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                modifier = Modifier.keepAboveKeyboard().fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.bio,
                onValueChange = { if (it.length <= state.bioLimit) onEvent(ProfileEditEvent.BioChanged(it)) },
                label = { Text(strings.bio) },
                placeholder = { Text(strings.bioPlaceholder) },
                minLines = 3,
                maxLines = 5,
                supportingText = {
                    Text("${state.bio.length} / ${state.bioLimit}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                },
                modifier = Modifier.keepAboveKeyboard().fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.website,
                onValueChange = { onEvent(ProfileEditEvent.WebsiteChanged(it)) },
                label = { Text(strings.website) },
                placeholder = { Text("https://") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.keepAboveKeyboard().fillMaxWidth(),
            )
        }
        if (state.hasChanges) {
            Surface(color = colors.inverseSurface, contentColor = colors.inverseOnSurface, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(Modifier.padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.unsaved, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(vertical = 14.dp))
                    TextButton(onClick = { onEvent(ProfileEditEvent.Discard) }, colors = ButtonDefaults.textButtonColors(contentColor = colors.inversePrimary)) {
                        Text(strings.discard)
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

private val CloseIcon = vectorIcon("Close", "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z")

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
internal fun ProfileEditPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileEdit(state = ProfileEditState(name = "Asha Rao", username = "asha", bio = "Designer and weekend potter.", hasChanges = true), onEvent = {})
    }
}
