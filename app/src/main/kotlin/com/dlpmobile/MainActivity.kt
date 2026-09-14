package com.dlpmobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dlpmobile.ui.DownloadScreen
import com.dlpmobile.ui.FormatPickerScreen
import com.dlpmobile.ui.HomeScreen
import com.dlpmobile.ui.theme.DlpTheme
import com.dlpmobile.vm.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // URL shared from another app (e.g. YouTube share sheet)
        val sharedUrl: String? = intent
            ?.takeIf { it.action == Intent.ACTION_SEND }
            ?.getStringExtra(Intent.EXTRA_TEXT)

        setContent {
            DlpTheme {
                val vm: MainViewModel = viewModel()
                val state by vm.uiState.collectAsState()

                // Inject shared URL once, before first composition
                if (sharedUrl != null && state.url.isBlank()) {
                    vm.onSharedUrl(sharedUrl)
                }

                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {

                    composable("home") {
                        HomeScreen(
                            url = state.url,
                            isLoading = state.isFetchingFormats,
                            error = state.fetchError,
                            onUrlChange = vm::onUrlChange,
                            onFetch = {
                                vm.fetchFormats()
                            },
                        )
                        // Navigate to format picker once formats have loaded
                        if (state.formats.isNotEmpty() && !state.isFetchingFormats) {
                            navController.navigate("formats") {
                                launchSingleTop = true
                            }
                        }
                    }

                    composable("formats") {
                        FormatPickerScreen(
                            formats = state.formats,
                            selectedFormat = state.selectedFormat,
                            onBack = { navController.popBackStack() },
                            onSelect = vm::onFormatSelected,
                            onDownload = {
                                vm.startDownload()
                                navController.navigate("download") {
                                    launchSingleTop = true
                                }
                            },
                        )
                    }

                    composable("download") {
                        DownloadScreen(
                            isDownloading = state.isDownloading,
                            logLines = state.downloadLog,
                            error = state.downloadError,
                            isFinished = state.downloadFinished,
                            onCancel = vm::cancelDownload,
                            onDone = {
                                vm.resetDownload()
                                navController.popBackStack("home", inclusive = false)
                            },
                        )
                    }
                }
            }
        }
    }
}
