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
import androidx.compose.ui.graphics.StrokeCap
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
 * Profile · Fintech account
 * Dark identity header with KYC status and UPI ID (copy, QR), then linked
 * bank accounts with a primary marker, today's limit usage and security.
 *
 * Mask account numbers on your server; only pass the last four digits.
 * The header colour is `ground`. The design uses Manrope.
 */

data class ProfileBankAccount(val id: String, val name: String, val detail: String, val isPrimary: Boolean)

data class ProfileFintechAccountState(
    val name: String = "[Full name]",
    val initials: String = "AB",
    val kycVerified: Boolean = true,
    val upiId: String = "name@bank",
    val accounts: List<ProfileBankAccount> = emptyList(),
    val limitUsed: String = "₹12,000",
    val limitTotal: String = "₹1,00,000",
    val limitProgress: Float = 0.12f,
    val appLockOn: Boolean = true,
)

sealed interface ProfileFintechAccountEvent {
    data object CopyUpi : ProfileFintechAccountEvent
    data object ShowQr : ProfileFintechAccountEvent
    data class OpenAccount(val id: String) : ProfileFintechAccountEvent
    data object AddAccount : ProfileFintechAccountEvent
    data object AppLock : ProfileFintechAccountEvent
    data object ChangePin : ProfileFintechAccountEvent
}

data class ProfileFintechAccountStrings(
    val kyc: String = "KYC verified",
    val upi: String = "UPI ID",
    val copy: String = "Copy UPI ID",
    val qr: String = "Show QR code",
    val linked: String = "LINKED ACCOUNTS",
    val primary: String = "PRIMARY",
    val addAccount: String = "Add bank account",
    val dailyLimit: String = "Daily limit",
    val of: (String, String) -> String = { used, total -> "$used of $total" },
    val resets: String = "Resets at midnight",
    val security: String = "SECURITY",
    val appLock: String = "App lock",
    val on: String = "On",
    val off: String = "Off",
    val changePin: String = "Change PIN",
)

@Composable
fun ProfileFintechAccount(
    state: ProfileFintechAccountState,
    onEvent: (ProfileFintechAccountEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: ProfileFintechAccountStrings = ProfileFintechAccountStrings(),
    ground: Color = Color(0xFF0A1A2F),
    accent: Color = Color(0xFF5EE0B5),
    avatar: (@Composable () -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colors.surfaceContainerLowest).verticalScroll(rememberScrollState())) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(ground, RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(state.initials, avatar, 56.dp, Color.White.copy(alpha = 0.12f), Color.White)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(state.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    if (state.kycVerified) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(VerifiedIcon, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(strings.kyc, style = MaterialTheme.typography.labelMedium, color = accent)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.08f), MaterialTheme.shapes.medium).padding(start = 14.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f).padding(vertical = 10.dp)) {
                    Text(strings.upi, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                    Text(state.upiId, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                IconButton(onClick = { onEvent(ProfileFintechAccountEvent.CopyUpi) }) { Icon(CopyIcon, contentDescription = strings.copy, tint = Color.White) }
                TextButton(onClick = { onEvent(ProfileFintechAccountEvent.ShowQr) }) { Text("QR", color = accent, fontWeight = FontWeight.Bold) }
            }
        }
        Column(Modifier.padding(16.dp)) {
            Label(strings.linked)
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow)) {
                state.accounts.forEach { a ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onEvent(ProfileFintechAccountEvent.OpenAccount(a.id)) }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(40.dp).background(colors.surfaceContainerHigh, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(BankIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(a.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(a.detail, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        if (a.isPrimary) {
                            Text(
                                strings.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.primary,
                                modifier = Modifier.background(colors.primary.copy(alpha = 0.10f), CircleShape).padding(horizontal = 8.dp, vertical = 3.dp),
                            )
                        }
                    }
                    HorizontalDivider(Modifier.padding(start = 68.dp), color = colors.outlineVariant)
                }
                TextButton(onClick = { onEvent(ProfileFintechAccountEvent.AddAccount) }, modifier = Modifier.padding(8.dp)) {
                    Icon(AddIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.addAccount)
                }
            }
            Spacer(Modifier.height(14.dp))
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow).padding(16.dp)) {
                Row {
                    Text(strings.dailyLimit, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text(strings.of(state.limitUsed, state.limitTotal), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                }
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(progress = { state.limitProgress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp), strokeCap = StrokeCap.Round)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(InfoIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.resets, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            Label(strings.security)
            Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(colors.surfaceContainerLow)) {
                LinkRow(strings.appLock, { onEvent(ProfileFintechAccountEvent.AppLock) }, value = if (state.appLockOn) strings.on else strings.off)
                HorizontalDivider(Modifier.padding(start = 16.dp), color = colors.outlineVariant)
                LinkRow(strings.changePin, { onEvent(ProfileFintechAccountEvent.ChangePin) })
            }
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 8.dp).semantics { heading() },
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
private val InfoIcon = vectorIcon("Info", "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z")
private val AddIcon = vectorIcon("Add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")
private val VerifiedIcon = vectorIcon(
    "Verified",
    "M23 12l-2.44-2.79.34-3.69-3.61-.82-1.89-3.2L12 2.96 8.6 1.5 6.71 4.69 3.1 5.5l.34 3.7L1 12l2.44 2.79-.34 3.7 3.61.82L8.6 22.5l3.4-1.47 3.4 1.46 1.89-3.19 3.61-.82-.34-3.69L23 12zm-12.91 4.72l-3.8-3.81 1.48-1.48 2.32 2.33 5.85-5.87 1.48 1.48-7.33 7.35z", autoMirror = true,
)
private val BankIcon = vectorIcon("Bank", "M4 10v7h3v-7H4zm6 0v7h3v-7h-3zM2 22h19v-3H2v3zm14-12v7h3v-7h-3zm-4.5-9L2 6v2h19V6l-9.5-5z")
private val CopyIcon = vectorIcon(
    "Copy",
    "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z",
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
internal fun ProfileFintechAccountPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        ProfileFintechAccount(
            state = ProfileFintechAccountState(
                accounts = listOf(
                    ProfileBankAccount("1", "HDFC ••4821", "Savings · Primary", isPrimary = true),
                    ProfileBankAccount("2", "SBI ••0937", "Current", isPrimary = false),
                ),
            ),
            onEvent = {},
        )
    }
}
