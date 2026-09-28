package com.example.e_inkoslauncher.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.e_inkoslauncher.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

// =============================================================================
// Launcher ViewModel – single source of truth for all UI state
// =============================================================================

data class LauncherUiState(
    // Clock
    val hour:   Int = 10,
    val minute: Int = 25,
    val second: Int = 0,
    val dateLabel: String = "Fri, 25 Oct",

    // Widget data
    val weather: WeatherData  = WeatherData(),
    val earbuds: EarbudsData  = EarbudsData(),
    val event:   EventData    = EventData(),
    val focus:   FocusData    = FocusData(),

    // App drawer
    val apps:         List<AppInfo> = emptyList(),
    val filteredApps: List<AppInfo> = emptyList(),
    val searchQuery:  String        = "",
    val isDrawerOpen: Boolean       = false,

    // Navigation
    val currentPage: Int = 1,   // 0 = quick-tiles, 1 = home, 2 = productivity

    // Settings
    val clockStyle: ClockStyle = ClockStyle.DOT_MATRIX,
    val cardCornerRadius: Int  = 28,
    val isDarkTheme: Boolean   = true
)

enum class ClockStyle { DOT_MATRIX, GROTESK, SERIF }

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = DataRepository(application)

    private val _uiState = MutableStateFlow(LauncherUiState())
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    init {
        startClock()
        loadWidgetData()
        loadApps()
    }

    // -------------------------------------------------------------------------
    // Clock ticker – updates every second
    // -------------------------------------------------------------------------
    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                val cal = Calendar.getInstance()
                val h = cal.get(Calendar.HOUR_OF_DAY)
                val m = cal.get(Calendar.MINUTE)
                val s = cal.get(Calendar.SECOND)

                val days   = arrayOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
                val months = arrayOf("Jan","Feb","Mar","Apr","May","Jun",
                                     "Jul","Aug","Sep","Oct","Nov","Dec")
                val dateStr = "${days[cal.get(Calendar.DAY_OF_WEEK)-1]}, " +
                              "${cal.get(Calendar.DAY_OF_MONTH)} " +
                              months[cal.get(Calendar.MONTH)]

                _uiState.update { it.copy(
                    hour      = h,
                    minute    = m,
                    second    = s,
                    dateLabel = dateStr
                ) }
                delay(1_000)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Widget data loading
    // -------------------------------------------------------------------------
    private fun loadWidgetData() {
        _uiState.update { state ->
            state.copy(
                weather = repo.getWeather(),
                earbuds = repo.getEarbuds(),
                event   = repo.getEvent(),
                focus   = repo.getFocus()
            )
        }
    }

    private fun loadApps() {
        viewModelScope.launch {
            val apps = repo.loadInstalledApps()
            _uiState.update { it.copy(apps = apps, filteredApps = apps) }
        }
    }

    // -------------------------------------------------------------------------
    // UI events
    // -------------------------------------------------------------------------
    fun setPage(index: Int) {
        _uiState.update { it.copy(currentPage = index) }
    }

    fun openDrawer() {
        _uiState.update { it.copy(isDrawerOpen = true) }
    }

    fun closeDrawer() {
        _uiState.update { it.copy(isDrawerOpen = false, searchQuery = "") }
    }

    fun onSearchQueryChanged(query: String) {
        val q = query.lowercase().trim()
        _uiState.update { state ->
            state.copy(
                searchQuery  = query,
                filteredApps = if (q.isEmpty()) state.apps
                               else state.apps.filter { it.label.lowercase().contains(q) }
            )
        }
    }

    fun setClockStyle(style: ClockStyle) {
        _uiState.update { it.copy(clockStyle = style) }
    }

    fun setCardCornerRadius(radius: Int) {
        _uiState.update { it.copy(cardCornerRadius = radius) }
    }
}
