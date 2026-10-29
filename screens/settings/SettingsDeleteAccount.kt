package com.yourapp.ui.screens.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Settings · Delete account
 * The final step of account deletion: consequences in plain language, a
 * data-export link, and a typed confirmation before the destructive button
 * unlocks. "Keep my account" is the easier, more prominent way out.
 *
 * Play policy requires in-app account deletion; this screen covers it.
 */

data class SettingsDeleteAccountState(
    val step: Int = 2,
    val totalSteps: Int = 2,
    val confirmText: String = "",
    val graceDays: Int = 30,
    val isDeleting: Boolean = false,
)

sealed interface SettingsDeleteAccountEvent {
    data object Back : SettingsDeleteAccountEvent
    data object DownloadData : SettingsDeleteAccountEvent
    data class ConfirmChanged(val value: String) : SettingsDeleteAccountEvent
    data object Delete : SettingsDeleteAccountEvent
    data object Keep : SettingsDeleteAccountEvent
}

data class SettingsDeleteAccountStrings(
    val back: String = "Back",
    val step: (Int, Int) -> String = { s, t -> "Step $s of $t" },
    val title: String = "Delete your account?",
    val intro: String = "This can't be undone. Here's what happens:",
    val deleted: String = "Your profile, posts and saved items are deleted.",
    val subscription: String = "Your subscription ends with no refund for the current period.",
    val grace: (Int) -> String = { "You have $it days to change your mind by signing back in." },
    val download: String = "Download a copy of your data first",
    val confirmWord: String = "DELETE",
    val confirmLabel: (String) -> String = { "Type $it to confirm" },
    val delete: String = "Delete my account",
    val keep: String = "Keep my account",
)

@Composable
fun SettingsDeleteAccount(
    state: SettingsDeleteAccountState,
    onEvent: (SettingsDeleteAccountEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: SettingsDeleteAccountStrings = SettingsDeleteAccountStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val confirmed = state.confirmText.trim() == strings.confirmWord
    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onEvent(SettingsDeleteAccountEvent.Back) }, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(BackIcon, contentDescription = strings.back)
            }
            Spacer(Modifier.weight(1f))
            Text(strings.step(state.step, state.totalSteps), style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
        }
        Spacer(Modifier.height(16.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(strings.intro, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        Column(
            Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.errorContainer.copy(alpha = 0.5f)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            listOf(DeleteIcon to strings.deleted, RefundIcon to strings.subscription, ClockIcon to strings.grace(state.graceDays)).forEach { (icon, text) ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(icon, contentDescription = null, tint = colors.error, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        TextButton(onClick = { onEvent(SettingsDeleteAccountEvent.DownloadData) }, contentPadding = PaddingValues(0.dp), modifier = Modifier.padding(top = 8.dp)) {
            Text(strings.download)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.confirmText,
            onValueChange = { onEvent(SettingsDeleteAccountEvent.ConfirmChanged(it)) },
            label = { Text(strings.confirmLabel(strings.confirmWord)) },
            placeholder = { Text(strings.confirmWord) },
            singleLine = true,
            enabled = !state.isDeleting,
            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Done),
            modifier = Modifier.keepAboveKeyboard().fillMaxWidth(),
        )
        Spacer(Modifier.weight(1f, fill = false).heightIn(min = 24.dp))
        Button(
            onClick = { onEvent(SettingsDeleteAccountEvent.Delete) },
            enabled = confirmed && !state.isDeleting,
            colors = ButtonDefaults.buttonColors(containerColor = colors.error, contentColor = colors.onError),
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(52.dp),
        ) {
            if (state.isDeleting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = colors.onError)
            else Text(strings.delete)
        }
        Button(
            onClick = { onEvent(SettingsDeleteAccountEvent.Keep) },
            colors = ButtonDefaults.buttonColors(containerColor = colors.secondaryContainer, contentColor = colors.onSecondaryContainer),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(52.dp),
        ) { Text(strings.keep) }
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val BackIcon = vectorIcon("Back", "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z", autoMirror = true)
private val DeleteIcon = vectorIcon("Delete", "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z")
private val RefundIcon = vectorIcon(
    "Refund",
    "M12.5 8c-2.65 0-5.05.99-6.9 2.6L2 7v9h9l-3.62-3.62c1.39-1.16 3.16-1.88 5.12-1.88 3.54 0 6.55 2.31 7.6 5.5l2.37-.78C21.08 11.03 17.15 8 12.5 8z", autoMirror = true,
)
private val ClockIcon = vectorIcon(
    "Clock",
    "M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z",
)

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
internal fun SettingsDeleteAccountPreview() {
    MaterialTheme(colorScheme = PreviewColors) { SettingsDeleteAccount(state = SettingsDeleteAccountState(), onEvent = {}) }
}
