package com.scoreboard.ui.scoreboard

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.input.pointer.pointerInput
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

    var orientationLocked by remember { mutableStateOf(true) }

    // Lock/unlock orientation based on state
    DisposableEffect(orientationLocked) {
        val activity = context as? Activity
        if (orientationLocked) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
        onDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // Double-tap anywhere to unlock rotation
                        orientationLocked = false
                    }
                )
            }
    ) {
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
}
