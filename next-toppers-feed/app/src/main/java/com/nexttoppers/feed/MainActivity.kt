package com.nexttoppers.feed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.nexttoppers.feed.navigation.NtfNavGraph
import com.nexttoppers.feed.ui.settings.SettingsViewModel
import com.nexttoppers.feed.ui.theme.NextToppersFeedTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            NextToppersFeedTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NtfNavGraph()
                }
            }
        }
    }
}
