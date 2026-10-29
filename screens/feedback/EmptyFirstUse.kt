package com.yourapp.ui.screens.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/*
 * Empty · First use
 * The empty state of a list the user hasn't filled yet: what the feature
 * is for, one clear action, and a secondary way in. Keeps your top title
 * and bottom navigation so the user still knows where they are.
 *
 * Pass your NavigationBar as bottomBar. Colours come from MaterialTheme.
 */

data class EmptyFirstUseState(
    val title: String = "Projects",
)

sealed interface EmptyFirstUseEvent {
    data object Create : EmptyFirstUseEvent
    data object Import : EmptyFirstUseEvent
}

data class EmptyFirstUseStrings(
    val heading: String = "No projects yet",
    val body: String = "Projects keep related items together. Create your first one to get started.",
    val create: String = "Create project",
    val importFile: String = "Import from a file",
)

@Composable
fun EmptyFirstUse(
    state: EmptyFirstUseState,
    onEvent: (EmptyFirstUseEvent) -> Unit,
    modifier: Modifier = Modifier,
    strings: EmptyFirstUseStrings = EmptyFirstUseStrings(),
    icon: ImageVector = FolderIcon,
    bottomBar: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Scaffold(
        modifier = modifier,
        bottomBar = bottomBar,
        containerColor = colors.surface,
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Text(
                state.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp),
            )
            Column(
                Modifier.fillMaxWidth().padding(top = 96.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(104.dp).background(colors.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Icon(icon, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(48.dp)) }
                Spacer(Modifier.height(24.dp))
                Text(strings.heading, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(
                    strings.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 320.dp),
                )
                Spacer(Modifier.height(24.dp))
                Button(onClick = { onEvent(EmptyFirstUseEvent.Create) }, modifier = Modifier.height(52.dp)) {
                    Icon(AddIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(strings.create)
                }
                TextButton(onClick = { onEvent(EmptyFirstUseEvent.Import) }, modifier = Modifier.padding(top = 4.dp)) {
                    Text(strings.importFile)
                }
            }
        }
    }
}

private fun vectorIcon(name: String, path: String): ImageVector =
ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
    .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
    .build()

private val FolderIcon = vectorIcon("Folder", "M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z")
private val AddIcon = vectorIcon("Add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")

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
internal fun EmptyFirstUsePreview() {
    MaterialTheme(colorScheme = PreviewColors) {
        EmptyFirstUse(
            state = EmptyFirstUseState(),
            onEvent = {},
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(selected = false, onClick = {}, icon = { Icon(AddIcon, null) }, label = { Text("Home") })
                    NavigationBarItem(selected = true, onClick = {}, icon = { Icon(FolderIcon, null) }, label = { Text("Projects") })
                }
            },
        )
    }
}
