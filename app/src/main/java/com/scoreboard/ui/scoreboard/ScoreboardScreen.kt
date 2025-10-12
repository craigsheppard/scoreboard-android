package com.scoreboard.ui.scoreboard

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.scoreboard.data.models.ScoreSide
import com.scoreboard.viewmodel.ScoreboardViewModel

@Composable
fun ScoreboardScreen(viewModel: ScoreboardViewModel) {
    val config = viewModel.appConfig

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
