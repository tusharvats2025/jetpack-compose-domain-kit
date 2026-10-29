package com.yourapp.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Onboarding · KYC intro (fintech)
 * Sets expectations before identity verification: why it's needed, how
 * long it takes, what to keep ready, and how the data is protected.
 *
 * Documents default to Indian KYC (PAN, Aadhaar, selfie); pass your own
 * list for other markets. The design uses Manrope; set it in Typography.
 */

enum class OnboardingKycDocIcon { Card, Home, Face }

data class OnboardingKycDocument(val title: String, val purpose: String, val icon: OnboardingKycDocIcon)

data class OnboardingKycIntroState(
    val minutes: Int = 5,
    val documents: List<OnboardingKycDocument> = listOf(
        OnboardingKycDocument("PAN card", "For identity", OnboardingKycDocIcon.Card),
        OnboardingKycDocument("Aadhaar", "For address, verified by OTP", OnboardingKycDocIcon.Home),
        OnboardingKycDocument("A quick selfie", "To match you with your documents", OnboardingKycDocIcon.Face),
    ),
)

sealed interface OnboardingKycIntroEvent {
    data object Close : OnboardingKycIntroEvent
    data object Start : OnboardingKycIntroEvent
    data object WhyNeeded : OnboardingKycIntroEvent
}

data class OnboardingKycIntroStrings(
    val close: String = "Close",
    val title: String = "Verify your identity",
    val body: (minutes: Int) -> String = { "Required by regulation before you can send money. Takes about $it minutes." },
    val keepReady: String = "Keep these ready",
    val privacy: String = "Encrypted and used only for verification.",
    val start: String = "Start verification",
    val why: String = "Why do we need this?",
)

@Composable
fun OnboardingKycIntro(
    state: OnboardingKycIntroState,
    onEvent: (OnboardingKycIntroEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingKycIntroStrings = OnboardingKycIntroStrings(),
    accent: Color = MaterialTheme.colorScheme.primary,
    onAccent: Color = MaterialTheme.colorScheme.onPrimary,
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surfaceContainerLow)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
    ) {
        IconButton(onClick = { onEvent(OnboardingKycIntroEvent.Close) }, modifier = Modifier.offset(x = (-12).dp)) {
            Icon(CloseIcon, contentDescription = strings.close)
        }
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier.size(64.dp).background(accent.copy(alpha = 0.12f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) { Icon(ShieldIcon, contentDescription = null, tint = accent, modifier = Modifier.size(32.dp)) }
        Spacer(Modifier.height(24.dp))
        Text(strings.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(strings.body(state.minutes), style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        Text(
            strings.keepReady.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.2.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(Modifier.height(10.dp))
        Column(
            Modifier.fillMaxWidth().background(colors.surface, RoundedCornerShape(16.dp)),
        ) {
            state.documents.forEachIndexed { i, doc ->
                if (i > 0) HorizontalDivider(Modifier.padding(start = 72.dp), color = colors.outlineVariant)
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).background(accent.copy(alpha = 0.10f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            when (doc.icon) {
                                OnboardingKycDocIcon.Card -> CardIcon
                                OnboardingKycDocIcon.Home -> HomeIcon
                                OnboardingKycDocIcon.Face -> PersonIcon
                            },
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(doc.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(doc.purpose, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(LockIcon, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(strings.privacy, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { onEvent(OnboardingKycIntroEvent.Start) },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = onAccent),
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) { Text(strings.start, fontWeight = FontWeight.Bold) }
        TextButton(
            onClick = { onEvent(OnboardingKycIntroEvent.WhyNeeded) },
            colors = ButtonDefaults.textButtonColors(contentColor = accent),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(48.dp),
        ) { Text(strings.why) }
    }
}

private fun vectorIcon(name: String, path: String): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val CloseIcon = vectorIcon("Close", "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z")
private val ShieldIcon = vectorIcon("Shield", "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm-2 16l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z")
private val CardIcon = vectorIcon("Card", "M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z")
private val HomeIcon = vectorIcon("Home", "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z")
private val PersonIcon = vectorIcon("Person", "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z")
private val LockIcon = vectorIcon(
    "Lock",
    "M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z",
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
internal fun OnboardingKycIntroPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingKycIntro(state = OnboardingKycIntroState(), onEvent = {}, accent = Color(0xFF1F4FBF))
    }
}
