/*
 * Copyright 2026 Vincent Tsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pawmatch.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.pawmatch.app.ui.screens.MainScreen
import com.pawmatch.app.ui.screens.AuthScreen
import com.pawmatch.app.ui.theme.PawMatchTheme
import com.pawmatch.app.ui.theme.LocalThemeManager
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setupSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            val isDarkThemeState = remember { mutableStateOf(false) }

            CompositionLocalProvider(LocalThemeManager provides isDarkThemeState) {
                PawMatchTheme(darkTheme = isDarkThemeState.value, dynamicColor = false) {
                    var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }

                    if (isLoggedIn) {
                    MainScreen()
                } else {
                    AuthScreen(
                        onAuthSuccess = { isLoggedIn = true }
                    )
                }
            }
        }
    }

    private fun setupSplashScreen() {
        var keepSplashScreenOn = true
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                delay(2000)
                keepSplashScreenOn = false
            }
        }

        installSplashScreen().setKeepOnScreenCondition {
            keepSplashScreenOn
        }
    }
}
