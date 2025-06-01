package io.availe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.availe.components.chat.ChatInputFieldContainer
import io.availe.components.chat.ChatSidebar
import io.availe.components.chat.ChatThread
import io.availe.state.AppDimensions
import io.availe.state.AppState
import io.availe.state.rememberAppState
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt

@Composable
@Preview
fun App() {
    val appState = rememberAppState()
    val focusManager = LocalFocusManager.current
    MaterialTheme(colorScheme = lightColorScheme()) {
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    }
            ) {
                MainLayout(appState, innerPadding)
            }
        }
    }
}

@Composable
private fun MainLayout(
    appState: AppState,
    innerPadding: PaddingValues
) {
    val isPhone = appState.isCompactScreen
    val drawerWidthDp: Dp =
        if (isPhone) appState.screenWidth * AppDimensions.SIDEBAR_FRACTION_PHONE
        else AppDimensions.SIDEBAR_WIDTH_DESKTOP

    if (isPhone) {
        DismissibleNavigationDrawer(
            drawerState = appState.drawerState,
            drawerContent = {
                Box(
                    Modifier
                        .width(drawerWidthDp)
                        .fillMaxHeight()
                ) {
                    ChatSidebar(
                        viewModel = appState.chatViewModel,
                        closeDrawer = { appState.scope.launch { appState.drawerState.close() } },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        ) {
            PhoneContent(appState, innerPadding)
        }
    } else {
        DesktopContent(appState, innerPadding, drawerWidthDp)
    }
}

@Composable
private fun PhoneContent(appState: AppState, innerPadding: PaddingValues) {
    Box(Modifier.fillMaxSize()) {
        MainChatArea(appState, innerPadding)
        DrawerToggle(
            Modifier
                .align(Alignment.TopStart)
                .offset(AppDimensions.TOGGLE_GAP, AppDimensions.TOGGLE_GAP)
        ) {
            appState.scope.launch {
                if (appState.drawerState.isClosed) appState.drawerState.open()
                else appState.drawerState.close()
            }
        }
    }
}

@Composable
private fun DesktopContent(
    appState: AppState,
    innerPadding: PaddingValues,
    drawerWidthDp: Dp
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val fullWidthPx = with(density) { drawerWidthDp.toPx() }
    val widthPx = remember { Animatable(fullWidthPx) }
    LaunchedEffect(Unit) { widthPx.snapTo(fullWidthPx) }

    Row(Modifier.fillMaxSize()) {
        Layout(
            modifier = Modifier
                .clipToBounds()
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val new = (widthPx.value + delta).coerceIn(0f, fullWidthPx)
                        scope.launch { widthPx.snapTo(new) }
                    },
                    onDragStopped = {
                        val shouldOpen = widthPx.value > fullWidthPx / 2
                        scope.launch {
                            widthPx.animateTo(
                                if (shouldOpen) fullWidthPx else 0f,
                                tween(300, easing = FastOutSlowInEasing)
                            )
                        }
                    }
                ),
            content = {
                ChatSidebar(
                    viewModel = appState.chatViewModel,
                    closeDrawer = {
                        scope.launch {
                            widthPx.animateTo(
                                0f,
                                tween(300, easing = FastOutSlowInEasing)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        ) { measurables, constraints ->
            val sidebarPlaceable = measurables.first().measure(
                constraints.copy(
                    minWidth = fullWidthPx.roundToInt(),
                    maxWidth = fullWidthPx.roundToInt()
                )
            )
            val layoutWidth = widthPx.value.roundToInt()
            val layoutHeight = sidebarPlaceable.height
            layout(layoutWidth, layoutHeight) {
                val offsetX = (widthPx.value - fullWidthPx).roundToInt()
                sidebarPlaceable.place(offsetX, 0)
            }
        }

        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            MainChatArea(appState, innerPadding)
            DrawerToggle(
                Modifier
                    .align(Alignment.TopStart)
                    .offset(AppDimensions.TOGGLE_GAP, AppDimensions.TOGGLE_GAP)
            ) {
                scope.launch {
                    val shouldOpen = widthPx.value < fullWidthPx / 2
                    widthPx.animateTo(
                        if (shouldOpen) fullWidthPx else 0f,
                        tween(300, easing = FastOutSlowInEasing)
                    )
                }
            }
        }
    }
}

@Composable
private fun MainChatArea(
    appState: AppState,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChatThread(
            state = appState.listState,
            responsiveWidth = appState.responsiveWidth,
            viewModel = appState.chatViewModel,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Spacer(Modifier.height(8.dp))
        ChatInputFieldContainer(
            modifier = Modifier.fillMaxWidth(appState.responsiveWidth),
            textContent = appState.textContent,
            onTextChange = { appState.textContent = it },
            targetUrl = appState.targetUrl,
            onTargetUrlChange = { url, _ -> appState.targetUrl = url },
            snackbarHostState = appState.snackbarHostState,
            onSend = { msg, url ->
                appState.chatViewModel.send(msg, url)
                appState.textContent = ""
            },
            uploadedFiles = appState.uploadedFiles,
            onFileUploaded = { file ->
                appState.uploadedFiles = appState.uploadedFiles + file
            }
        )
    }
}

@Composable
private fun DrawerToggle(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .size(AppDimensions.TOGGLE_SIZE)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text("≡", fontSize = 24.sp)
    }
}
