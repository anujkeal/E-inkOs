package com.example.e_inkoslauncher.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.hapticfeedback.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.*
import androidx.lifecycle.viewmodel.compose.*
import com.example.e_inkoslauncher.data.AppInfo
import com.example.e_inkoslauncher.theme.*
import kotlinx.coroutines.launch
import kotlin.math.*

// =============================================================================
// MAIN SCREEN  –  Nothing OS Monochrome Launcher
// All composables for the launcher home screen, app drawer, and widgets
// =============================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val scope  = rememberCoroutineScope()

    // Handle Android back presses while the app drawer is open.
    // This prevents the Activity from being closed immediately and lets the UI
    // dismiss the drawer instead of jumping back to the system home screen.
    BackHandler(enabled = state.isDrawerOpen) {
        viewModel.closeDrawer()
    }

    // 3-page horizontal pager (quick-tiles | home | productivity)
    val pager = rememberPagerState(initialPage = 1) { 3 }

    // Sync ViewModel page ↔ pager
    LaunchedEffect(pager.currentPage) {
        viewModel.setPage(pager.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        // ── Wallpaper ────────────────────────────────────────────────────────
        WallpaperBackground()

        // ── Pager ─────────────────────────────────────────────────────────────
        HorizontalPager(
            state    = pager,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> QuickTilesPage(state, viewModel)
                1 -> HomePage(state, viewModel, pager)
                2 -> ProductivityPage(state, viewModel)
            }
        }

        // ── App Drawer overlay ───────────────────────────────────────────────
        if (state.isDrawerOpen) {
            AppDrawerSheet(
                apps        = state.filteredApps,
                query       = state.searchQuery,
                onQuery     = { viewModel.onSearchQueryChanged(it) },
                onLaunch    = { app ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    app.launchIntent?.let {
                        it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        // launched from LocalContext in AppDrawerSheet
                    }
                    viewModel.closeDrawer()
                },
                onDismiss   = { viewModel.closeDrawer() }
            )
        }
    }
}
