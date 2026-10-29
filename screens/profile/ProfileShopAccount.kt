package com.yourapp.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Profile · Shop account
 * A retail account home: greeting, loyalty tier with progress to the next,
 * quick shortcuts, the most recent order with tracking, and settings.
 *
 * accent defaults to MaterialTheme.colorScheme.primary.
 */

data class ProfileShopOrder(val title: String, val status: String, val eta: String)

data class ProfileShopAccountState(
    val name: String = "[Name]",
    val email: String = "name@example.com",
    val initials: String = "AB",
    val tier: String = "Gold member",
    val program: String = "[Store] rewards",
    val points: String = "2,450",
    val tierProgress: Float = 0.8f,
    val nextTierNote: String = "550 points to Platinum",
    val recentOrder: ProfileShopOrder? = null,
)

sealed interface ProfileShopAccountEvent {
    data object Orders : ProfileShopAccountEvent
    data object Wishlist : ProfileShopAccountEvent
    data object Addresses : ProfileShopAccountEvent
    data object Coupons : ProfileShopAccountEvent
    data object TrackOrder : ProfileShopAccountEvent
    data object Rewards : ProfileShopAccountEvent
    data object PaymentMethods : ProfileShopAccountEvent
    data object Settings : ProfileShopAccountEvent
}

data class ProfileShopAccountStrings(
    val hi: (String) -> String = { "Hi, $it" },
    val points: String = "points",
    val orders: String = "Orders",
    val wishlist: String = "Wishlist",
    val addresses: String = "Addresses",
    val coupons: String = "Coupons",
    val recent: String = "Recent order",
    val track: String = "Track order",
    val payments: String = "Payment methods",
    val settings: String = "Settings",
)

@Composable
fun ProfileShopAccount(
    state: ProfileShopAccountState,
    onEvent: (ProfileShopAccountEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileShopAccountStrings = ProfileShopAccountStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
    avatar: (@Composable () -> Unit)? = null,
    orderImage: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier.fillMaxSize().background(colors.surfaceContainerLowest).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
            Avatar(state.initials, avatar, 52.dp, accent.copy(alpha = 0.14f), accent)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(strings.hi(state.name), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(state.email, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        // Loyalty card
        Column(
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .background(accent)
                .clickable(role = Role.Button) { onEvent(ProfileShopAccountEvent.Rewards) }
                .padding(18.dp),
        ) {
            Row {
                Text(state.tier, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = onAccent, modifier = Modifier.weight(1f))
                Text(state.program, style = MaterialTheme.typography.labelMedium, color = onAccent.copy(alpha = 0.8f))
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(state.points, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = onAccent)
                Spacer(Modifier.width(6.dp))
                Text(strings.points, style = MaterialTheme.typography.bodyMedium, color = onAccent.copy(alpha = 0.85f), modifier = Modifier.padding(bottom = 4.dp))
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { state.tierProgress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = onAccent,
                trackColor = onAccent.copy(alpha = 0.25f),
                strokeCap = StrokeCap.Round,
            )
            Spacer(Modifier.height(6.dp))
            Text(state.nextTierNote, style = MaterialTheme.typography.bodySmall, color = onAccent.copy(alpha = 0.85f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple(BagIcon, strings.orders, ProfileShopAccountEvent.Orders),
                Triple(HeartIcon, strings.wishlist, ProfileShopAccountEvent.Wishlist),
                Triple(PinIcon, strings.addresses, ProfileShopAccountEvent.Addresses),
                Triple(TagIcon, strings.coupons, ProfileShopAccountEvent.Coupons),
            ).forEach { (icon, label, event) ->
                Column(
                    Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.medium)
                        .background(colors.surfaceContainerLow)
                        .clickable(role = Role.Button) { onEvent(event) }
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(icon, contentDescription = null, tint = accent)
                    Spacer(Modifier.height(6.dp))
                    Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
                }
            }
        }
        state.recentOrder?.let { order ->
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.recent, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Row(
                        Modifier.background(accent.copy(alpha = 0.12f), CircleShape).padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(TruckIcon, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(order.status, style = MaterialTheme.typography.labelMedium, color = accent)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(56.dp).clip(MaterialTheme.shapes.small).background(colors.surfaceContainerHigh)) { orderImage?.invoke() }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(order.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(order.eta, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { onEvent(ProfileShopAccountEvent.TrackOrder) }, modifier = Modifier.fillMaxWidth()) { Text(strings.track) }
            }
        }
        Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow)) {
            LinkRow(strings.payments, { onEvent(ProfileShopAccountEvent.PaymentMethods) })
            HorizontalDivider(Modifier.padding(start = 16.dp), color = colors.outlineVariant)
            LinkRow(strings.settings, { onEvent(ProfileShopAccountEvent.Settings) })
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

@Composable
private fun LinkRow(title: String, onClick: () -> Unit, value: String? = null, subtitle: String? = null) {
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp))
        }
        Icon(ChevronIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp).size(20.dp))
    }
}

private fun vectorIcon(name: String, path: String, autoMirror: Boolean = false): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f, autoMirror = autoMirror)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val ChevronIcon = vectorIcon("Chevron", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z", autoMirror = true)
private val BagIcon = vectorIcon(
    "Bag",
    "M18 6h-2c0-2.21-1.79-4-4-4S8 3.79 8 6H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-6-2c1.1 0 2 .9 2 2h-4c0-1.1.9-2 2-2zm6 16H6V8h2v2c0 .55.45 1 1 1s1-.45 1-1V8h4v2c0 .55.45 1 1 1s1-.45 1-1V8h2v12z",
)
private val HeartIcon = vectorIcon(
    "Heart",
    "M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z",
)
private val PinIcon = vectorIcon(
    "Pin",
    "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z",
)
private val TruckIcon = vectorIcon(
    "Truck",
    "M20 8h-3V4H3c-1.1 0-2 .9-2 2v11h2c0 1.66 1.34 3 3 3s3-1.34 3-3h6c0 1.66 1.34 3 3 3s3-1.34 3-3h2v-5l-3-4zM6 18.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm13.5-9l1.96 2.5H17V9.5h2.5zm-1.5 9c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z",
)
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
internal fun ProfileShopAccountPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileShopAccount(
            state = ProfileShopAccountState(recentOrder = ProfileShopOrder("[Product name] + 2 more", "Out for delivery", "Arriving today by 7 pm")),
            onEvent = {},
            accent = Color(0xFFB93A0A),
            onAccent = Color.White,
        )
    }
}
