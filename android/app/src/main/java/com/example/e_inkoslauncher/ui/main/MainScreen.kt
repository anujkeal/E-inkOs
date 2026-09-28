package com.example.e_inkoslauncher.ui.main

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

        // ── Pager ────────────────────────────────────────────────────────────
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

// =============================================================================
// WALLPAPER BACKGROUND (solid dark + subtle noise)
// =============================================================================
@Composable
fun WallpaperBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Dark base gradient (top → bottom)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A0A0A),
                    Color(0xFF141414),
                    Color(0xFF0A0A0A)
                )
            )
        )
        // Very subtle horizontal noise lines to simulate paper/film grain
        for (y in 0 until size.height.toInt() step 4) {
            val alpha = (Math.random() * 0.04f).toFloat()
            drawLine(
                color     = Color.White.copy(alpha = alpha),
                start     = Offset(0f, y.toFloat()),
                end       = Offset(size.width, y.toFloat()),
                strokeWidth = 1.5f
            )
        }
    }
}

// =============================================================================
// PAGE 1 – QUICK TILES
// =============================================================================
@Composable
fun QuickTilesPage(state: LauncherUiState, vm: MainScreenViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Tokens.ScreenMargin.dp)
            .padding(top = Tokens.StatusBarHeight.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(Tokens.CardGutter.dp)
    ) {
        Text(
            text  = "Quick Tiles",
            style = MaterialTheme.typography.titleLarge,
            color = OnDarkSecondary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        // Media / Now Playing card
        NothingCard(dark = false, modifier = Modifier.fillMaxWidth().height(130.dp)) {
            Column(
                modifier = Modifier.padding(Tokens.CardPaddingMd.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Now Playing", style = MaterialTheme.typography.labelMedium, color = OnLightTertiary)
                Column {
                    Text("Motion Sickness", style = MaterialTheme.typography.titleLarge, color = OnLightPrimary)
                    Text("Phoebe Bridgers", style = MaterialTheme.typography.bodyMedium, color = OnLightSecondary)
                }
                // Progress bar
                LinearProgressBar(fraction = 0.42f, dark = false)
            }
        }
        // Tile grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Tokens.CardGutter.dp)
        ) {
            listOf("Torch","Glyph","Rec","WiFi").forEachIndexed { i, label ->
                NothingCard(dark = (i % 2 == 0), modifier = Modifier.weight(1f).aspectRatio(1f)) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon24(name = label, dark = i % 2 == 0)
                            Spacer(Modifier.height(6.dp))
                            Text(label, style = MaterialTheme.typography.labelSmall,
                                color = if (i % 2 == 0) OnDarkSecondary else OnLightSecondary)
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// PAGE 2 – HOME (main launcher page)
// =============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomePage(
    state: LauncherUiState,
    vm:    MainScreenViewModel,
    pager: PagerState
) {
    val scope  = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val ctx    = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Tokens.ScreenMargin.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── Status Bar ───────────────────────────────────────────────────────
        StatusBar(hour = state.hour, minute = state.minute)

        Spacer(Modifier.height(4.dp))

        // ── Clock Hero ───────────────────────────────────────────────────────
        ClockHeroCard(
            hour       = state.hour,
            minute     = state.minute,
            second     = state.second,
            dateLabel  = state.dateLabel,
            clockStyle = state.clockStyle
        )

        Spacer(Modifier.height(Tokens.CardGutter.dp))

        // ── Row 1: Weather + Earbuds ─────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(Tokens.CardGutter.dp)
        ) {
            WeatherCard(state.weather, Modifier.weight(1f))
            EarbudsCard(state.earbuds, Modifier.weight(1f))
        }

        Spacer(Modifier.height(Tokens.CardGutter.dp))

        // ── Row 2: Flight+Focus stacked | Analog Clock ───────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            horizontalArrangement = Arrangement.spacedBy(Tokens.CardGutter.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Tokens.CardGutter.dp)
            ) {
                FlightCard(state.event, Modifier.weight(1f).fillMaxWidth())
                FocusCard(state.focus, Modifier.weight(1f).fillMaxWidth())
            }
            AnalogClockCard(
                hour   = state.hour,
                minute = state.minute,
                second = state.second,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Page Dots ────────────────────────────────────────────────────────
        PageDots(currentPage = pager.currentPage, totalPages = 3, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(12.dp))

        // ── Dock ─────────────────────────────────────────────────────────────
        DockBar(
            onOpenDrawer  = { vm.openDrawer() },
            onOpenPhone   = { },
            onOpenCamera  = { },
            onOpenMessages = { }
        )

        Spacer(Modifier.height(20.dp))
    }
}

// =============================================================================
// PAGE 3 – PRODUCTIVITY
// =============================================================================
@Composable
fun ProductivityPage(state: LauncherUiState, vm: MainScreenViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Tokens.ScreenMargin.dp)
            .padding(top = Tokens.StatusBarHeight.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(Tokens.CardGutter.dp)
    ) {
        Text(
            text  = "Productivity",
            style = MaterialTheme.typography.titleLarge,
            color = OnDarkSecondary,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        // Activity card
        NothingCard(dark = false, modifier = Modifier.fillMaxWidth().height(140.dp)) {
            Column(
                Modifier.padding(Tokens.CardPaddingMd.dp).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Activity", style = MaterialTheme.typography.labelMedium, color = OnLightTertiary)
                    Text("Today", style = MaterialTheme.typography.labelMedium, color = OnLightTertiary)
                }
                Text("8,342", style = MaterialTheme.typography.displayMedium, color = OnLightPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("steps", style = MaterialTheme.typography.bodyMedium, color = OnLightSecondary)
                    Spacer(Modifier.weight(1f))
                    LinearProgressBar(fraction = 0.70f, dark = false, modifier = Modifier.width(80.dp))
                }
            }
        }
        // ANC card
        NothingCard(dark = true, modifier = Modifier.fillMaxWidth().height(120.dp)) {
            Row(
                Modifier.padding(Tokens.CardPaddingMd.dp).fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Nothing X", style = MaterialTheme.typography.labelMedium, color = OnDarkSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text("ANC On", style = MaterialTheme.typography.titleLarge, color = OnDarkPrimary)
                    Spacer(Modifier.height(4.dp))
                    Text("Noise reduction: High", style = MaterialTheme.typography.bodyMedium, color = OnDarkSecondary)
                }
                // ANC level arc
                AncArcIndicator(level = 0.80f)
            }
        }
    }
}

// =============================================================================
// STATUS BAR
// =============================================================================
@Composable
fun StatusBar(hour: Int, minute: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(Tokens.StatusBarHeight.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text  = "%02d:%02d".format(hour, minute),
            style = MaterialTheme.typography.titleMedium,
            color = OnDarkPrimary,
            fontSize = 13.sp
        )
        // Punch-hole spacer
        Spacer(Modifier.width(32.dp))
        // Status icons (SVG-drawn)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            WifiIcon()
            SignalIcon()
            BatteryIcon(82)
        }
    }
}

// =============================================================================
// CLOCK HERO CARD
// =============================================================================
@Composable
fun ClockHeroCard(
    hour: Int, minute: Int, second: Int,
    dateLabel: String, clockStyle: ClockStyle
) {
    NothingCard(
        dark     = false,
        modifier = Modifier.fillMaxWidth().height(188.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Tokens.CardPaddingLg.dp, vertical = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.align(Alignment.CenterStart)) {
                when (clockStyle) {
                    ClockStyle.DOT_MATRIX -> DotMatrixClock(hour, minute, second)
                    ClockStyle.GROTESK    -> GroteskClock(hour, minute)
                    ClockStyle.SERIF      -> SerifClock(hour, minute)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = dateLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnLightSecondary
                )
            }
        }
    }
}

// =============================================================================
// DOT-MATRIX CLOCK  –  Canvas-based 5×7 pixel bitmap digits
// =============================================================================
private val DIGIT_BITMAPS = arrayOf(
    // 0
    intArrayOf(0,1,1,1,0, 1,0,0,0,1, 1,0,0,0,1, 1,0,0,0,1, 1,0,0,0,1, 1,0,0,0,1, 0,1,1,1,0),
    // 1
    intArrayOf(0,0,1,0,0, 0,1,1,0,0, 0,0,1,0,0, 0,0,1,0,0, 0,0,1,0,0, 0,0,1,0,0, 0,1,1,1,0),
    // 2
    intArrayOf(0,1,1,1,0, 1,0,0,0,1, 0,0,0,0,1, 0,0,0,1,0, 0,0,1,0,0, 0,1,0,0,0, 1,1,1,1,1),
    // 3
    intArrayOf(1,1,1,1,0, 0,0,0,0,1, 0,0,0,0,1, 0,1,1,1,0, 0,0,0,0,1, 0,0,0,0,1, 1,1,1,1,0),
    // 4
    intArrayOf(0,0,0,1,0, 0,0,1,1,0, 0,1,0,1,0, 1,0,0,1,0, 1,1,1,1,1, 0,0,0,1,0, 0,0,0,1,0),
    // 5
    intArrayOf(1,1,1,1,1, 1,0,0,0,0, 1,0,0,0,0, 1,1,1,1,0, 0,0,0,0,1, 0,0,0,0,1, 1,1,1,1,0),
    // 6
    intArrayOf(0,1,1,1,0, 1,0,0,0,0, 1,0,0,0,0, 1,1,1,1,0, 1,0,0,0,1, 1,0,0,0,1, 0,1,1,1,0),
    // 7
    intArrayOf(1,1,1,1,1, 0,0,0,0,1, 0,0,0,1,0, 0,0,1,0,0, 0,1,0,0,0, 0,1,0,0,0, 0,1,0,0,0),
    // 8
    intArrayOf(0,1,1,1,0, 1,0,0,0,1, 1,0,0,0,1, 0,1,1,1,0, 1,0,0,0,1, 1,0,0,0,1, 0,1,1,1,0),
    // 9
    intArrayOf(0,1,1,1,0, 1,0,0,0,1, 1,0,0,0,1, 0,1,1,1,1, 0,0,0,0,1, 0,0,0,0,1, 0,1,1,1,0)
)
private val COLON_BITMAP = intArrayOf(0,0,0,0,0, 0,0,1,0,0, 0,0,1,0,0, 0,0,0,0,0, 0,0,1,0,0, 0,0,1,0,0, 0,0,0,0,0)

@Composable
fun DotMatrixClock(hour: Int, minute: Int, second: Int) {
    val dotPx = 7.dp
    val gap   = 2.dp
    val colonGap = 6.dp

    // Pulsing colon
    val alpha by animateFloatAsState(
        targetValue = if (second % 2 == 0) 1f else 0.2f,
        animationSpec = tween(300),
        label = "colonPulse"
    )

    val digits = listOf(hour / 10, hour % 10, -1, minute / 10, minute % 10)

    Row(verticalAlignment = Alignment.CenterVertically) {
        digits.forEachIndexed { idx, d ->
            if (d == -1) {
                // Colon
                Spacer(Modifier.width(4.dp))
                Canvas(modifier = Modifier
                    .width(dotPx * 5 + gap * 4)
                    .height(dotPx * 7 + gap * 6)
                ) {
                    drawDotGlyph(COLON_BITMAP, dotPx.toPx(), gap.toPx(), Color(0xFF000000).copy(alpha = alpha))
                }
                Spacer(Modifier.width(4.dp))
            } else {
                Canvas(modifier = Modifier
                    .width(dotPx * 5 + gap * 4)
                    .height(dotPx * 7 + gap * 6)
                ) {
                    drawDotGlyph(DIGIT_BITMAPS[d], dotPx.toPx(), gap.toPx(), Color(0xFF000000))
                }
                if (idx < digits.lastIndex && digits[idx + 1] != -1) {
                    Spacer(Modifier.width(colonGap))
                }
            }
        }
    }
}

private fun DrawScope.drawDotGlyph(bitmap: IntArray, dotPx: Float, gap: Float, color: Color) {
    val step = dotPx + gap
    for (row in 0..6) {
        for (col in 0..4) {
            if (bitmap[row * 5 + col] == 1) {
                drawCircle(
                    color  = color,
                    radius = dotPx / 2f,
                    center = Offset(col * step + dotPx / 2f, row * step + dotPx / 2f)
                )
            }
        }
    }
}

@Composable
fun GroteskClock(hour: Int, minute: Int) {
    Text(
        text  = "%02d:%02d".format(hour, minute),
        style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
        color = OnLightPrimary
    )
}

@Composable
fun SerifClock(hour: Int, minute: Int) {
    Text(
        text  = "%02d:%02d".format(hour, minute),
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize   = 64.sp,
            fontFamily = FontFamily.Serif
        ),
        color = OnLightPrimary
    )
}

// =============================================================================
// WEATHER CARD
// =============================================================================
@Composable
fun WeatherCard(data: com.example.e_inkoslauncher.data.WeatherData, modifier: Modifier = Modifier) {
    NothingCard(dark = false, modifier = modifier) {
        Column(
            Modifier.padding(Tokens.CardPaddingMd.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(data.condition, style = MaterialTheme.typography.labelMedium, color = OnLightTertiary)
                SunIcon()
            }
            Text(data.tempDisplay, style = MaterialTheme.typography.displayMedium, color = OnLightPrimary)
            Text(data.city, style = MaterialTheme.typography.bodyMedium, color = OnLightSecondary)
        }
    }
}

// =============================================================================
// EARBUDS BATTERY CARD (dark)
// =============================================================================
@Composable
fun EarbudsCard(data: com.example.e_inkoslauncher.data.EarbudsData, modifier: Modifier = Modifier) {
    NothingCard(dark = true, modifier = modifier) {
        Column(
            Modifier.padding(Tokens.CardPaddingMd.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(data.brand, style = MaterialTheme.typography.labelMedium, color = OnDarkSecondary)
            Text(
                text  = "${data.batteryPercent}%",
                style = MaterialTheme.typography.displayMedium,
                color = OnDarkPrimary
            )
            Column {
                Text(data.modelName, style = MaterialTheme.typography.bodyMedium, color = OnDarkSecondary)
                Spacer(Modifier.height(6.dp))
                LinearProgressBar(fraction = data.batteryPercent / 100f, dark = true)
            }
        }
    }
}

// =============================================================================
// FLIGHT CARD
// =============================================================================
@Composable
fun FlightCard(data: com.example.e_inkoslauncher.data.EventData, modifier: Modifier = Modifier) {
    NothingCard(dark = false, modifier = modifier) {
        Column(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(data.flightCode, style = MaterialTheme.typography.titleSmall, color = OnLightPrimary)
            Text(data.destination, style = MaterialTheme.typography.bodyMedium, color = OnLightSecondary)
            Text(data.dateTime, style = MaterialTheme.typography.labelSmall, color = OnLightTertiary)
        }
    }
}

// =============================================================================
// FOCUS CARD
// =============================================================================
@Composable
fun FocusCard(data: com.example.e_inkoslauncher.data.FocusData, modifier: Modifier = Modifier) {
    NothingCard(dark = true, modifier = modifier) {
        Column(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Focus", style = MaterialTheme.typography.labelMedium, color = OnDarkSecondary)
            Text(data.durationLabel, style = MaterialTheme.typography.titleMedium, color = OnDarkPrimary)
            LinearProgressBar(fraction = data.progressFraction, dark = true)
        }
    }
}

// =============================================================================
// ANALOG CLOCK CARD
// =============================================================================
@Composable
fun AnalogClockCard(hour: Int, minute: Int, second: Int, modifier: Modifier = Modifier) {
    NothingCard(dark = false, modifier = modifier) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(12.dp)) {
            AnalogClock(hour, minute, second)
        }
    }
}

@Composable
fun AnalogClock(hour: Int, minute: Int, second: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx    = size.width / 2f
        val cy    = size.height / 2f
        val r     = minOf(cx, cy) * 0.88f
        val color = Color(0xFF000000)

        // Tick marks
        for (i in 0..11) {
            val angle = Math.toRadians(i * 30.0 - 90.0)
            val isQuarter = i % 3 == 0
            val tickLen = if (isQuarter) r * 0.14f else r * 0.08f
            val sw = if (isQuarter) 2.5f else 1.5f
            drawLine(
                color = color.copy(alpha = if (isQuarter) 1f else 0.5f),
                start = Offset(
                    (cx + cos(angle) * (r - tickLen)).toFloat(),
                    (cy + sin(angle) * (r - tickLen)).toFloat()
                ),
                end   = Offset(
                    (cx + cos(angle) * r).toFloat(),
                    (cy + sin(angle) * r).toFloat()
                ),
                strokeWidth = sw
            )
        }

        // Hour hand
        val hourAngle = Math.toRadians((hour % 12 + minute / 60.0) * 30.0 - 90.0)
        drawLine(color, Offset(cx, cy),
            Offset((cx + cos(hourAngle) * r * 0.52f).toFloat(), (cy + sin(hourAngle) * r * 0.52f).toFloat()),
            strokeWidth = 5f, cap = StrokeCap.Round)

        // Minute hand
        val minAngle = Math.toRadians(minute * 6.0 + second / 10.0 - 90.0)
        drawLine(color, Offset(cx, cy),
            Offset((cx + cos(minAngle) * r * 0.72f).toFloat(), (cy + sin(minAngle) * r * 0.72f).toFloat()),
            strokeWidth = 3.5f, cap = StrokeCap.Round)

        // Second hand (accent)
        val secAngle = Math.toRadians(second * 6.0 - 90.0)
        drawLine(Color(0xFF555555), Offset(cx, cy),
            Offset((cx + cos(secAngle) * r * 0.82f).toFloat(), (cy + sin(secAngle) * r * 0.82f).toFloat()),
            strokeWidth = 1.5f, cap = StrokeCap.Round)

        // Center dot
        drawCircle(color, radius = 5f, center = Offset(cx, cy))
    }
}

// =============================================================================
// PAGE DOTS
// =============================================================================
@Composable
fun PageDots(currentPage: Int, totalPages: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { i ->
            val isActive = i == currentPage
            val width by animateDpAsState(
                targetValue = if (isActive) 18.dp else Tokens.DotSize.dp,
                animationSpec = tween(300),
                label = "dotWidth$i"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(Tokens.DotSize.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(if (isActive) DotActive else DotInactive)
            )
        }
    }
}

// =============================================================================
// DOCK BAR
// =============================================================================
@Composable
fun DockBar(
    onOpenDrawer:   () -> Unit,
    onOpenPhone:    () -> Unit,
    onOpenCamera:   () -> Unit,
    onOpenMessages: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            Pair("Phone",   { onOpenPhone() }),
            Pair("Message", { onOpenMessages() }),
            Pair("Camera",  { onOpenCamera() }),
            Pair("Apps",    { onOpenDrawer() })
        ).forEach { (label, action) ->
            DockButton(label = label, onClick = action)
        }
    }
}

@Composable
fun DockButton(label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(Tokens.DockButtonSize.dp)
            .clip(CircleShape)
            .background(if (isPressed) OnDarkPrimary else SurfaceLight)
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
    ) {
        Icon24(name = label, dark = isPressed)
    }
}

// =============================================================================
// APP DRAWER
// =============================================================================
@Composable
fun AppDrawerSheet(
    apps:      List<AppInfo>,
    query:     String,
    onQuery:   (String) -> Unit,
    onLaunch:  (AppInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color(0xFF0F0F0F))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { }
                .padding(top = 12.dp)
        ) {
            // Handle bar
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF444444))
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))

            // Search bar
            TextField(
                value         = query,
                onValueChange = onQuery,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(50)),
                placeholder   = { Text("Search apps", color = Color(0xFF666666)) },
                singleLine    = true,
                colors        = TextFieldDefaults.colors(
                    focusedContainerColor   = Color(0xFF1E1E1E),
                    unfocusedContainerColor = Color(0xFF1A1A1A),
                    focusedTextColor        = Color.White,
                    unfocusedTextColor      = Color.White,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(Modifier.height(12.dp))

            // App list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(apps) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                app.launchIntent?.let { intent ->
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    ctx.startActivity(intent)
                                }
                                onLaunch(app)
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // App icon placeholder
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF2A2A2A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text  = app.label.firstOrNull()?.toString() ?: "?",
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text  = app.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// PRIMITIVE: NothingCard
// =============================================================================
@Composable
fun NothingCard(
    dark:     Boolean,
    modifier: Modifier = Modifier,
    radius:   Int      = Tokens.RadiusCard,
    content:  @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bg = when {
        dark  && isPressed -> SurfaceLight
        !dark && isPressed -> SurfaceDark
        dark               -> SurfaceDark
        else               -> SurfaceLight
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(radius.dp))
            .background(bg)
            .clickable(interactionSource = interactionSource, indication = null) { },
        content = content
    )
}

// =============================================================================
// PRIMITIVE: LinearProgressBar
// =============================================================================
@Composable
fun LinearProgressBar(
    fraction: Float,
    dark:     Boolean,
    modifier: Modifier = Modifier
) {
    val track = if (dark) TrackDark else TrackLight
    val fill  = if (dark) OnDarkPrimary else OnLightPrimary
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Tokens.BarHeight.dp)
            .clip(RoundedCornerShape(Tokens.RadiusBar.dp))
            .background(track)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(Tokens.RadiusBar.dp))
                .background(fill)
        )
    }
}

// =============================================================================
// PRIMITIVE: ANC Arc Indicator
// =============================================================================
@Composable
fun AncArcIndicator(level: Float, size: Dp = 64.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val sw = 6f
        val inset = sw / 2f
        val rect = Rect(inset, inset, this.size.width - inset, this.size.height - inset)
        // Track
        drawArc(
            color      = Color(0xFF333333),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter  = false,
            topLeft    = Offset(rect.left, rect.top),
            size       = Size(rect.width, rect.height),
            style      = Stroke(width = sw, cap = StrokeCap.Round)
        )
        // Fill
        drawArc(
            color      = Color.White,
            startAngle = 135f,
            sweepAngle = 270f * level,
            useCenter  = false,
            topLeft    = Offset(rect.left, rect.top),
            size       = Size(rect.width, rect.height),
            style      = Stroke(width = sw, cap = StrokeCap.Round)
        )
    }
}

// =============================================================================
// INLINE SVG ICONS (drawn in Canvas to stay grayscale)
// =============================================================================
@Composable
fun SunIcon(size: Dp = 20.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val c = Offset(this.size.width / 2f, this.size.height / 2f)
        val r = this.size.minDimension * 0.25f
        drawCircle(Color(0xFF333333), radius = r, center = c, style = Stroke(2f))
        for (i in 0..7) {
            val a = Math.toRadians(i * 45.0)
            drawLine(
                Color(0xFF333333),
                start = Offset((c.x + cos(a) * (r + 3)).toFloat(), (c.y + sin(a) * (r + 3)).toFloat()),
                end   = Offset((c.x + cos(a) * (r + 7)).toFloat(), (c.y + sin(a) * (r + 7)).toFloat()),
                strokeWidth = 1.5f
            )
        }
    }
}

@Composable
fun WifiIcon(size: Dp = 16.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val cx = this.size.width / 2f
        val cy = this.size.height * 0.7f
        val col = Color.White.copy(alpha = 0.8f)
        listOf(0.9f, 0.6f, 0.3f).forEachIndexed { i, scale ->
            drawArc(
                color      = col.copy(alpha = col.alpha * (1f - i * 0.2f)),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter  = false,
                topLeft    = Offset(cx - this.size.width * scale / 2f, cy - this.size.height * scale / 2f),
                size       = Size(this.size.width * scale, this.size.height * scale),
                style      = Stroke(1.5f)
            )
        }
        drawCircle(col, radius = 2f, center = Offset(cx, cy))
    }
}

@Composable
fun SignalIcon(size: Dp = 16.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val col = Color.White.copy(alpha = 0.8f)
        val barW = this.size.width * 0.18f
        val maxH = this.size.height * 0.9f
        for (i in 0..3) {
            val h = maxH * (0.25f + i * 0.25f)
            val x = i * (barW + 2.dp.toPx())
            drawRoundRect(
                color       = col.copy(alpha = if (i < 3) col.alpha else col.alpha * 0.4f),
                topLeft     = Offset(x, this.size.height - h),
                size        = Size(barW, h),
                cornerRadius = CornerRadius(1f)
            )
        }
    }
}

@Composable
fun BatteryIcon(percent: Int, size: Dp = 24.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width * 0.72f
        val h = this.size.height * 0.45f
        val x = (this.size.width - w) / 2f
        val y = (this.size.height - h) / 2f
        val col = Color.White.copy(alpha = 0.8f)
        drawRoundRect(Color.Transparent, topLeft = Offset(x, y), size = Size(w, h),
            cornerRadius = CornerRadius(3f), style = Stroke(1.5f))
        drawRoundRect(col, topLeft = Offset(x, y), size = Size(w, h),
            cornerRadius = CornerRadius(3f), style = Stroke(1.5f))
        drawRoundRect(col, topLeft = Offset(x + 2f, y + 2f),
            size = Size((w - 4f) * percent / 100f, h - 4f),
            cornerRadius = CornerRadius(2f))
        // Nub
        drawRoundRect(col, topLeft = Offset(x + w + 1f, y + h * 0.3f),
            size = Size(3f, h * 0.4f), cornerRadius = CornerRadius(2f))
    }
}

@Composable
fun Icon24(name: String, dark: Boolean, sizeDp: Dp = 24.dp) {
    val color = if (dark) OnLightPrimary else OnDarkPrimary
    Canvas(modifier = Modifier.size(sizeDp)) {
        val sw = 2f
        val c  = Offset(this.size.width / 2f, this.size.height / 2f)
        val r  = this.size.minDimension * 0.35f
        when (name.lowercase()) {
            "phone"   -> {
                // Simple phone handset
                drawRoundRect(color, topLeft = Offset(c.x - r * 0.55f, c.y - r),
                    size = Size(r * 1.1f, r * 2f), cornerRadius = CornerRadius(r * 0.4f), style = Stroke(sw))
                drawLine(color, Offset(c.x - r * 0.55f, c.y + r * 0.55f), Offset(c.x + r * 0.55f, c.y + r * 0.55f), sw)
            }
            "message" -> {
                drawRoundRect(color, topLeft = Offset(c.x - r, c.y - r * 0.75f),
                    size = Size(r * 2f, r * 1.5f), cornerRadius = CornerRadius(r * 0.3f), style = Stroke(sw))
                drawLine(color, Offset(c.x - r * 0.3f, c.y + r * 0.75f), Offset(c.x - r, c.y + r * 1.2f), sw)
            }
            "camera"  -> {
                drawRoundRect(color, topLeft = Offset(c.x - r, c.y - r * 0.7f),
                    size = Size(r * 2f, r * 1.4f), cornerRadius = CornerRadius(r * 0.25f), style = Stroke(sw))
                drawCircle(color, radius = r * 0.45f, center = c, style = Stroke(sw))
                // Viewfinder bump
                drawRoundRect(color, topLeft = Offset(c.x - r * 0.3f, c.y - r * 0.85f),
                    size = Size(r * 0.6f, r * 0.2f), cornerRadius = CornerRadius(r * 0.1f))
            }
            "apps"    -> {
                // 3×3 grid
                for (row in 0..2) for (col in 0..2) {
                    drawCircle(color, radius = 2.dp.toPx(),
                        center = Offset(c.x + (col - 1) * r * 0.7f, c.y + (row - 1) * r * 0.7f))
                }
            }
            "torch"   -> {
                drawLine(color, c, Offset(c.x, c.y - r), strokeWidth = sw * 2)
                for (i in -2..2) {
                    val a = Math.toRadians(i * 30.0)
                    drawLine(color,
                        Offset((c.x + sin(a) * r * 0.5f).toFloat(), c.y - r * 0.5f),
                        Offset((c.x + sin(a) * r).toFloat(), c.y - r * 1.2f),
                        strokeWidth = sw)
                }
            }
            "glyph"   -> {
                drawCircle(color, radius = r, center = c, style = Stroke(sw))
                for (i in 0..5) {
                    val a = Math.toRadians(i * 60.0)
                    drawLine(color,
                        Offset((c.x + cos(a) * r * 0.4f).toFloat(), (c.y + sin(a) * r * 0.4f).toFloat()),
                        Offset((c.x + cos(a) * r).toFloat(), (c.y + sin(a) * r).toFloat()),
                        strokeWidth = sw)
                }
            }
            "rec"     -> {
                drawCircle(color, radius = r * 0.8f, center = c, style = Stroke(sw))
                drawCircle(color, radius = r * 0.3f, center = c)
            }
            "wifi"    -> {
                for (scale in listOf(0.9f, 0.6f, 0.3f)) {
                    drawArc(color, 200f, 140f, false,
                        topLeft = Offset(c.x - r * scale, c.y - r * scale),
                        size    = Size(r * scale * 2f, r * scale * 2f),
                        style   = Stroke(sw))
                }
                drawCircle(color, radius = 2f, center = c)
            }
            else      -> drawCircle(color, radius = r, center = c, style = Stroke(sw))
        }
    }
}
