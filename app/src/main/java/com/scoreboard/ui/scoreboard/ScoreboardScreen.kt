package com.scoreboard.ui.scoreboard

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.scoreboard.data.models.ScoreSide
import com.scoreboard.viewmodel.ScoreboardViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ScoreboardScreen(viewModel: ScoreboardViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val config = uiState.appConfig

    // Keep landscape locked - user must physically rotate to landscape and back to portrait
    // This matches iOS behavior where orientation is "sticky"
    DisposableEffect(Unit) {
        (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            // Don't change orientation on dispose
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
