package com.scoreboard

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Landscape: Show scoreboard
        ScoreboardScreen()
    } else {
        // Portrait: Show configuration
        ConfigureScreen()
    }
}
