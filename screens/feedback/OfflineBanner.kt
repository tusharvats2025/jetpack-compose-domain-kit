package com.yourapp.ui.screens.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Placement · Offline banner
 * Not a screen: a banner you place at the top of any screen that can keep
 * showing cached content while offline. Slides in and out with
 * the connection, and announces itself to screen readers.
 *
 *   Column {
 *       TopAppBar(...)
 *       OfflineBanner(visible = !isOnline, onRetry = ::retry)
 *       Content()
 *   }
 *
 * Mark items that aren't available offline in your own list (dimmed, with
 * a reason) rather than hiding them.
 */

data class OfflineBannerStrings(
    val message: String = "You're offline. Showing what's saved.",
    val retry: String = "Retry",
)

@Composable
fun OfflineBanner(
    visible: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    strings: OfflineBannerStrings = OfflineBannerStrings(),
) {
    val colors = MaterialTheme.colorScheme
    AnimatedVisibility(visible = visible, enter = expandVertically(), exit = shrinkVertically(), modifier = modifier) {
        Surface(
            color = colors.inverseSurface,
            contentColor = colors.inverseOnSurface,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            Row(Modifier.padding(start = 14.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(WifiOffIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(strings.message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f).padding(vertical = 12.dp))
                TextButton(onClick = onRetry, colors = ButtonDefaults.textButtonColors(contentColor = colors.inversePrimary)) {
                    Text(strings.retry, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

private val WifiOffIcon: ImageVector = ImageVector.Builder("WifiOff", 24.dp, 24.dp, 24f, 24f)
    .addPath(
        addPathNodes("M23.64 7c-.45-.34-4.93-4-11.64-4-1.5 0-2.89.19-4.15.48L18.18 13.8 23.64 7zm-6.6 8.22L3.27 1.44 2 2.72l2.05 2.06C1.91 5.76.59 6.82.36 7l11.63 14.49.01.01.01-.01 3.9-4.86 3.32 3.32 1.27-1.27-3.46-3.46z"),
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
internal fun OfflineBannerPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
            Text("Home", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
            OfflineBanner(visible = true, onRetry = {})
        }
    }
}
