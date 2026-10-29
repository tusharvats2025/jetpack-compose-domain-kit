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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Profile · Style profile (fashion)
 * Sizes per category, fit preference, style tags and saved looks: what a
 * fashion app needs to recommend well. Square, letter-spaced, editorial.
 *
 * tone is the warm grey behind empty image slots. The design pairs Bodoni
 * Moda with Jost; pass them as serifFont and sansFont.
 */

data class ProfileSize(val category: String, val value: String)

data class ProfileStyleState(
    val name: String = "[Name]",
    val initials: String = "AB",
    val sizes: List<ProfileSize> = emptyList(),
    val fit: String = "Relaxed",
    val styles: List<String> = emptyList(),
    val savedCount: Int = 0,
    val savedLooks: List<@Composable () -> Unit> = emptyList(),
)

sealed interface ProfileStyleEvent {
    data object Settings : ProfileStyleEvent
    data object EditSizes : ProfileStyleEvent
    data object AddStyle : ProfileStyleEvent
    data object AllSaved : ProfileStyleEvent
    data class OpenLook(val index: Int) : ProfileStyleEvent
    data object OrdersAndReturns : ProfileStyleEvent
}

data class ProfileStyleStrings(
    val subtitle: String = "STYLE PROFILE",
    val settings: String = "Settings",
    val sizes: String = "MY SIZES",
    val edit: String = "EDIT",
    val fit: (String) -> String = { "Fit preference: $it" },
    val style: String = "YOUR STYLE",
    val add: String = "+ Add",
    val saved: (Int) -> String = { "SAVED LOOKS · $it" },
    val seeAll: String = "SEE ALL",
    val orders: String = "Orders and returns",
)

@OptIn(ExperimentalLayoutApi::class) // FlowRow on older Compose versions
@Composable
fun ProfileStyle(
    state: ProfileStyleState,
    onEvent: (ProfileStyleEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileStyleStrings = ProfileStyleStrings(),
    tone: Color = Color(0xFFCFC8C0),
    ink: Color = Color(0xFF111111),
    serifFont: FontFamily = FontFamily.Serif,
    sansFont: FontFamily = FontFamily.Default,
    avatar: (@Composable () -> Unit)? = null,
) {
    val sans = TextStyle(fontFamily = sansFont, color = ink)
    val label = sans.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
    val line = ink.copy(alpha = 0.15f)
    Column(
        modifier.fillMaxSize().background(Color.White).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(state.initials, avatar, 52.dp, tone, ink)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(state.name, style = TextStyle(fontFamily = serifFont, fontStyle = FontStyle.Italic, fontSize = 26.sp, color = ink))
                Text(strings.subtitle, style = label.copy(fontSize = 11.sp, color = ink.copy(alpha = 0.6f)))
            }
            IconButton(onClick = { onEvent(ProfileStyleEvent.Settings) }) { Icon(SettingsIcon, contentDescription = strings.settings, tint = ink) }
        }
        HeaderRow(strings.sizes, strings.edit, label) { onEvent(ProfileStyleEvent.EditSizes) }
        Row(Modifier.fillMaxWidth().border(1.dp, line)) {
            state.sizes.forEachIndexed { i, s ->
                if (i > 0) Box(Modifier.width(1.dp).height(72.dp).background(line))
                Column(Modifier.weight(1f).padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(s.category, style = sans.copy(fontSize = 12.sp, color = ink.copy(alpha = 0.6f)))
                    Text(s.value, style = sans.copy(fontSize = 20.sp, fontWeight = FontWeight.Medium))
                }
            }
        }
        Text(strings.fit(state.fit), style = sans.copy(fontSize = 13.sp, color = ink.copy(alpha = 0.7f)), modifier = Modifier.padding(top = 10.dp))
        Text(strings.style, style = label, modifier = Modifier.padding(top = 28.dp, bottom = 12.dp).semantics { heading() })
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.styles.forEach { s ->
                Text(s, style = sans.copy(fontSize = 13.sp), modifier = Modifier.border(1.dp, ink).padding(horizontal = 14.dp, vertical = 8.dp))
            }
            Text(
                strings.add,
                style = sans.copy(fontSize = 13.sp, color = ink.copy(alpha = 0.7f)),
                modifier = Modifier.border(1.dp, line).clickable(role = Role.Button) { onEvent(ProfileStyleEvent.AddStyle) }.padding(horizontal = 14.dp, vertical = 8.dp),
            )
        }
        HeaderRow(strings.saved(state.savedCount), strings.seeAll, label) { onEvent(ProfileStyleEvent.AllSaved) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { i ->
                Box(
                    Modifier.weight(1f).aspectRatio(3f / 4f).background(tone).clickable(enabled = i < state.savedLooks.size) { onEvent(ProfileStyleEvent.OpenLook(i)) },
                ) { state.savedLooks.getOrNull(i)?.invoke() }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 24.dp)
                .border(1.dp, line)
                .clickable(role = Role.Button) { onEvent(ProfileStyleEvent.OrdersAndReturns) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(strings.orders, style = sans.copy(fontSize = 15.sp), modifier = Modifier.weight(1f))
            Icon(ChevronIcon, contentDescription = null, tint = ink)
        }
    }
}

@Composable
private fun HeaderRow(title: String, action: String, style: TextStyle, onAction: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = style, modifier = Modifier.weight(1f).semantics { heading() })
        TextButton(onClick = onAction, shape = RectangleShape) { Text(action, style = style) }
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

private val ChevronIcon = vectorIcon("Chevron", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z", autoMirror = true)
private val SettingsIcon = vectorIcon(
    "Settings",
    "M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.05.24.24.41.48.41h3.84c.24 0 .44-.17.47-.41l.36-2.54c.59-.24 1.13-.56 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z",
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

@Preview
@Composable
internal fun ProfileStylePreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileStyle(
            state = ProfileStyleState(
                sizes = listOf(ProfileSize("Tops", "M"), ProfileSize("Bottoms", "32"), ProfileSize("Shoes", "UK 8")),
                styles = listOf("Minimal", "Streetwear", "Workwear"),
                savedCount = 12,
            ),
            onEvent = {},
        )
    }
}
