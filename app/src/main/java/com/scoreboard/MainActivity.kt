package com.scoreboard

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import com.scoreboard.ui.configure.ConfigureScreen
import com.scoreboard.ui.scoreboard.ScoreboardScreen
import com.scoreboard.ui.theme.ScoreboardTheme
import com.scoreboard.viewmodel.ScoreboardViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ScoreboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ScoreboardTheme {
                ScoreboardApp(viewModel)
            }
        }
    }
}

@Composable
fun ScoreboardApp(viewModel: ScoreboardViewModel) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Landscape: Show scoreboard
        ScoreboardScreen(viewModel = viewModel)
    } else {
        // Portrait: Show configuration
        ConfigureScreen(viewModel = viewModel)
    }
}
