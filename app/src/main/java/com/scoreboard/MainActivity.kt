package com.scoreboard

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.scoreboard.ui.configure.ConfigureScreen
import com.scoreboard.ui.scoreboard.ScoreboardScreen
import com.scoreboard.ui.theme.ScoreboardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScoreboardTheme {
                ScoreboardApp()
            }
        }
    }
}

@Composable
fun ScoreboardApp() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // When orientation changes to portrait, unlock rotation
    LaunchedEffect(isLandscape) {
        if (!isLandscape) {
            (context as? MainActivity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }

    if (isLandscape) {
        // Landscape: Show scoreboard
        ScoreboardScreen()
    } else {
        // Portrait: Show configuration
        ConfigureScreen()
    }
}
