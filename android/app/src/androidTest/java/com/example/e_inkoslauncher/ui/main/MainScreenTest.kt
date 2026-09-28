package com.example.e_inkoslauncher.ui.main

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test

/** Basic smoke test – verifies that the launcher screen composes without crash. */
class MainScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun launcherComposesSuccessfully() {
        // Nothing to assert – just verify no crash on composition
        composeRule.setContent {
            com.example.e_inkoslauncher.theme.NothingLauncherTheme {
                MainScreen()
            }
        }
    }
}
