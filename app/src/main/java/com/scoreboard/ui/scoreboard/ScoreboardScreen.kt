package com.scoreboard.ui.scoreboard

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.view.OrientationEventListener
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.scoreboard.data.models.ScoreSide
import com.scoreboard.viewmodel.ScoreboardViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ScoreboardScreen(
    viewModel: ScoreboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val config = uiState.appConfig

    var hasBeenLandscape by remember { mutableStateOf(false) }
    var orientationUnlocked by remember { mutableStateOf(false) }

    // Lock to landscape initially, then monitor device orientation
    // Unlock only after device has been physically rotated to landscape and back to portrait
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED

        val orientationListener = object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                // Landscape range: 45-135 (left) or 225-315 (right)
                val isLandscape = (orientation >= 45 && orientation <= 135) ||
                                 (orientation >= 225 && orientation <= 315)

                if (isLandscape) {
                    hasBeenLandscape = true
                }

                // Once device has been in landscape and is now in portrait (315-45), unlock
                if (hasBeenLandscape && !isLandscape && !orientationUnlocked) {
                    orientationUnlocked = true
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                }
            }
        }

        if (orientationListener.canDetectOrientation()) {
            orientationListener.enable()
        }

        onDispose {
            orientationListener.disable()
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Home team (left side)
        ScoreView(
            team = config.homeTeam,
            side = ScoreSide.LEFT,
            gameType = config.currentGameType,
            onScoreChange = { newScore ->
                viewModel.updateHomeTeamScore(newScore)
            },
            modifier = Modifier.weight(1f)
        )

        // Away team (right side)
        ScoreView(
            team = config.awayTeam,
            side = ScoreSide.RIGHT,
            gameType = config.currentGameType,
            onScoreChange = { newScore ->
                viewModel.updateAwayTeamScore(newScore)
            },
            modifier = Modifier.weight(1f)
        )
    }
}
