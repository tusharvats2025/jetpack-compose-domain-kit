package com.yourapp.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Checkout · Course purchase (education)
 * A course checkout: cover, title, instructor rating, key facts, what's
 * included, an applied coupon you can remove, the refund promise, and the
 * price with the original struck through.
 *
 * The design uses Lexend; set it in your Typography to match.
 */

data class CheckoutCourseState(
    val title: String = "[Course title]",
    val byline: String = "By [Instructor]",
    val rating: String = "4.8 (2.1k ratings)",
    val facts: List<String> = listOf("12 hours", "48 lessons", "Certificate", "Hindi, English"),
    val included: List<String> = emptyList(),
    val coupon: String? = null,
    val couponNote: String = "",
    val refundNote: String = "7-day refund if it's not for you.",
    val originalPrice: String? = null,
    val price: String = "",
    val isPaying: Boolean = false,
)

sealed interface CheckoutCourseEvent {
    data object Back : CheckoutCourseEvent
    data object RemoveCoupon : CheckoutCourseEvent
    data object Buy : CheckoutCourseEvent
}

data class CheckoutCourseStrings(
    val back: String = "Back",
    val included: String = "What's included",
    val applied: (String) -> String = { " applied · $it" },
    val remove: String = "Remove",
    val buy: String = "Buy course",
    val wasPrice: (String) -> String = { "Was $it" },
)

@OptIn(ExperimentalLayoutApi::class) // FlowRow on older Compose versions
@Composable
fun CheckoutCourse(
    state: CheckoutCourseState,
    onEvent: (CheckoutCourseEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: CheckoutCourseStrings = CheckoutCourseStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    cover: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surface)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                ImageSlot(cover, Modifier.matchParentSize(), RectangleShape)
                FilledIconButton(
                    onClick = { onEvent(CheckoutCourseEvent.Back) },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = colors.surface, contentColor = colors.onSurface),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars).padding(12.dp),
                ) { Icon(BackIcon, contentDescription = strings.back) }
            }
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column {
                    Text(state.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(state.byline, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        Text(" · ", color = colors.onSurfaceVariant)
                        Icon(StarIcon, contentDescription = null, tint = Color(0xFFE0A100), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(state.rating, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.facts.forEach { f ->
                        Text(f, style = MaterialTheme.typography.labelLarge, modifier = Modifier.background(accent.copy(alpha = 0.08f), CircleShape).padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
                if (state.included.isNotEmpty()) {
                    Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(strings.included, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
                        state.included.forEach { i ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(CheckIcon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(i, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                if (state.coupon != null) {
                    Row(
                        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).border(1.dp, accent.copy(alpha = 0.4f), MaterialTheme.shapes.medium).padding(start = 14.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(TagIcon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(state.coupon) }
                                append(strings.applied(state.couponNote))
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                        )
                        TextButton(onClick = { onEvent(CheckoutCourseEvent.RemoveCoupon) }) { Text(strings.remove) }
                    }
                }
                Text(state.refundNote, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
            Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    if (state.originalPrice != null) {
                        Text(
                            state.originalPrice,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.semantics { contentDescription = strings.wasPrice(state.originalPrice) },
                        )
                    }
                    Text(state.price, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onEvent(CheckoutCourseEvent.Buy) },
                    enabled = !state.isPaying,
                    colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
                    modifier = Modifier.height(52.dp),
                ) {
                    if (state.isPaying) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = onAccent)
                    else Text(strings.buy, fontWeight = FontWeight.SemiBold)
                }
            }
        }
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
private val CheckIcon = vectorIcon("Check", "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z")
private val StarIcon = vectorIcon("Star", "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z")
private val TagIcon = vectorIcon(
    "Tag",
    "M21.41 11.58l-9-9C12.05 2.22 11.55 2 11 2H4c-1.1 0-2 .9-2 2v7c0 .55.22 1.05.59 1.42l9 9c.36.36.86.58 1.41.58.55 0 1.05-.22 1.41-.59l7-7c.37-.36.59-.86.59-1.41 0-.55-.23-1.06-.59-1.42zM5.5 7C4.67 7 4 6.33 4 5.5S4.67 4 5.5 4 7 4.67 7 5.5 6.33 7 5.5 7z",
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
internal fun CheckoutCoursePreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        CheckoutCourse(
            state = CheckoutCourseState(
                included = listOf("Lifetime access, including updates", "Download lessons to watch offline", "6 practice projects with feedback"),
                coupon = "LEARN60",
                couponNote = "63% off",
                originalPrice = "₹3,499",
                price = "₹1,299",
            ),
            onEvent = {},
            accent = Color(0xFF4B32C3),
            onAccent = Color.White,
        )
    }
}
