package com.pawmatch.app

import MainView
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity

import com.pawmatch.app.ui.navigation.AppNavigation
import com.pawmatch.app.ui.theme.PawMatchTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PawMatchTheme {
                AppNavigation()
            }
        }
    }
}