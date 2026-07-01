package com.nexttoppers.feed

import android.content.Intent
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ── Read deep-link route from notification tap ─────────────────────
        val initialRoute = intent?.getStringExtra(EXTRA_NOTIFICATION_ROUTE)
        if (initialRoute != null) _pendingRoute.value = initialRoute

        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState()
            NextToppersFeedTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NtfNavGraph()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val route = intent.getStringExtra(EXTRA_NOTIFICATION_ROUTE)
        if (!route.isNullOrEmpty()) {
            _pendingRoute.value = route
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ROUTE = "ntf_notification_route"

        private val _pendingRoute = MutableStateFlow<String?>(null)
        val pendingRoute: StateFlow<String?> = _pendingRoute.asStateFlow()

        fun consumePendingRoute(): String? {
            val route = _pendingRoute.value
            _pendingRoute.value = null
            return route
        }
    }
}
