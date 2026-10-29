package com.yourapp.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/*
 * Onboarding · Swipe carousel
 * Three (or more) swipeable benefit pages with an illustration, page dots,
 * Skip and Next. The last page's button finishes onboarding.
 *
 * The current page is UI state and lives here; PageShown tells you which
 * page the user saw, for analytics. Colours and type come from your
 * MaterialTheme.
 */

/** One page. illustration is your image or animation; null shows a soft placeholder. */
data class OnboardingCarouselPage(
    val title: String,
    val body: String,
    val illustration: (@Composable () -> Unit)? = null,
)

data class OnboardingCarouselState(
    val pages: List<OnboardingCarouselPage> = emptyList(),
)

sealed interface OnboardingCarouselEvent {
    data class PageShown(val index: Int) : OnboardingCarouselEvent
    data object Skip : OnboardingCarouselEvent
    data object Finish : OnboardingCarouselEvent
}

data class OnboardingCarouselStrings(
    val skip: String = "Skip",
    val next: String = "Next",
    val finish: String = "Get started",
    val pageOf: (page: Int, total: Int) -> String = { p, t -> "Page $p of $t" },
)

@OptIn(ExperimentalFoundationApi::class) // HorizontalPager on older Compose versions
@Composable
fun OnboardingCarousel(
    state: OnboardingCarouselState,
    onEvent: (OnboardingCarouselEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: OnboardingCarouselStrings = OnboardingCarouselStrings(),
) {
    val colors = MaterialTheme.colorScheme
    val pageCount = state.pages.size
    val pager = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()
    val isLast = pager.currentPage == pageCount - 1

    val latestOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(pager) {
        snapshotFlow { pager.currentPage }.collect { latestOnEvent(OnboardingCarouselEvent.PageShown(it)) }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(colors.surface)
            .safeDrawingPadding()
            .padding(bottom = 24.dp),
    ) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterEnd) {
            if (!isLast) {
                TextButton(onClick = { onEvent(OnboardingCarouselEvent.Skip) }) { Text(strings.skip) }
            } else {
                Spacer(Modifier.height(48.dp))
            }
        }

        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { index ->
            val page = state.pages[index]
            Column(
                Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp)
                        .background(colors.primaryContainer.copy(alpha = 0.45f), RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center,
                ) { page.illustration?.invoke() }
                Spacer(Modifier.height(24.dp))
                Text(page.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(
                    page.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(
            Modifier
                .align(Alignment.CenterHorizontally)
                .clearAndSetSemantics { contentDescription = strings.pageOf(pager.currentPage + 1, pageCount) },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(pageCount) { i ->
                val selected = i == pager.currentPage
                val width by animateDpAsState(if (selected) 24.dp else 8.dp, label = "dot")
                val color by animateColorAsState(if (selected) colors.primary else colors.outlineVariant, label = "dotColor")
                Box(Modifier.height(8.dp).width(width).background(color, CircleShape))
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                if (isLast) onEvent(OnboardingCarouselEvent.Finish)
                else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
            },
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth().height(56.dp),
        ) { Text(if (isLast) strings.finish else strings.next) }
    }
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
internal fun OnboardingCarouselPreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        OnboardingCarousel(
            state = OnboardingCarouselState(
                pages = listOf(
                    OnboardingCarouselPage("Plan your week\nin minutes", "One short sentence on the benefit this page shows."),
                    OnboardingCarouselPage("Share with your team", "Everyone sees the same plan, live."),
                    OnboardingCarouselPage("Never miss a thing", "Gentle reminders, only when you need them."),
                ),
            ),
            onEvent = {},
        )
    }
}
