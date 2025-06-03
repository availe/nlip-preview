package io.availe.state

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.availe.config.HttpClientProvider
import io.availe.config.NetworkConfig
import io.availe.repositories.KtorChatRepository
import io.availe.util.getScreenWidthDp
import io.availe.viewmodels.ChatViewModel
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberAppState(): AppState {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { addPlatformFileSupport() }
            .build()
    }
    val httpClient = HttpClientProvider.httpClient
    val listState = rememberLazyListState()
    val screenWidth = getScreenWidthDp()
    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    return remember(httpClient, listState, screenWidth, snackbarHostState, drawerState, scope) {
        AppState(
            httpClient = httpClient,
            listState = listState,
            screenWidth = screenWidth,
            snackbarHostState = snackbarHostState,
            drawerState = drawerState,
            scope = scope
        )
    }
}

data class AppState(
    val httpClient: HttpClient,
    val listState: LazyListState,
    val screenWidth: Dp,
    val snackbarHostState: SnackbarHostState,
    val drawerState: DrawerState,
    val scope: CoroutineScope
) {
    val responsiveWidth: Float = when {
        screenWidth < AppDimensions.COMPACT_WIDTH -> 0.9f
        screenWidth < AppDimensions.MEDIUM_WIDTH -> 0.7f
        else -> 0.63f
    }
    val isCompactScreen = screenWidth < AppDimensions.COMPACT_WIDTH
    var textContent by mutableStateOf("")
    var targetUrl by mutableStateOf("${NetworkConfig.serverUrl}/nlip/")
    var uploadedFiles by mutableStateOf(listOf<PlatformFile>())
    var isSidebarOpen by mutableStateOf(true)
    val chatRepository = KtorChatRepository(httpClient)
    val chatViewModel = ChatViewModel(chatRepository)
}

object AppDimensions {
    val COMPACT_WIDTH = 600.dp
    val MEDIUM_WIDTH = 840.dp
    val SIDEBAR_WIDTH_DESKTOP = 250.dp
    const val SIDEBAR_FRACTION_PHONE = 0.80f
    val TOGGLE_SIZE = 56.dp
    val TOGGLE_GAP = 12.dp
}
