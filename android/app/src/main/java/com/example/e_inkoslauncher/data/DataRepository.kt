package com.example.e_inkoslauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.UserHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// =============================================================================
// Data Models
// =============================================================================

/** One installed application entry for the App Drawer */
data class AppInfo(
    val label:       String,
    val packageName: String,
    val launchIntent: Intent?
)

/** Live weather state – swap the stub with an API call */
data class WeatherData(
    val tempDisplay: String   = "28°",
    val city:        String   = "New Delhi",
    val condition:   String   = "Sunny"
)

/** Earbuds battery state */
data class EarbudsData(
    val batteryPercent: Int    = 90,
    val modelName:      String = "Ear (a)",
    val brand:          String = "Nothing"
)

/** Flight / calendar event */
data class EventData(
    val flightCode: String = "Flight BA995",
    val destination:String = "to London",
    val dateTime:   String = "4 Jun 2025, 19:50"
)

/** Focus session state */
data class FocusData(
    val durationLabel: String = "3h 19m",
    val progressFraction: Float = 0.55f
)

// =============================================================================
// Data Repository – single source of truth
// =============================================================================

class DataRepository(private val context: Context) {

    // -------------------------------------------------------------------------
    // App list – queried from PackageManager
    // -------------------------------------------------------------------------
    suspend fun loadInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val launchIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        pm.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)
            .map { info ->
                AppInfo(
                    label        = info.loadLabel(pm).toString(),
                    packageName  = info.activityInfo.packageName,
                    launchIntent = pm.getLaunchIntentForPackage(info.activityInfo.packageName)
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    // -------------------------------------------------------------------------
    // Stub data sources – replace with real API / ContentProvider calls
    // -------------------------------------------------------------------------
    fun getWeather():  WeatherData  = WeatherData()
    fun getEarbuds():  EarbudsData  = EarbudsData()
    fun getEvent():    EventData    = EventData()
    fun getFocus():    FocusData    = FocusData()
}
