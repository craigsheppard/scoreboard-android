package com.scoreboard.ui.configure

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scoreboard.R
import com.scoreboard.viewmodel.ScoreboardViewModel

@Composable
fun ConfigureScreen(viewModel: ScoreboardViewModel) {
    var config by remember { mutableStateOf(viewModel.appConfig) }

    var showNewGameDialog by remember { mutableStateOf(false) }
    var showGameTypeDialog by remember { mutableStateOf(false) }

    // Update config when viewModel changes
    LaunchedEffect(viewModel.appConfig) {
        config = viewModel.appConfig
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.todays_game),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 40.dp)
            )

            TextButton(onClick = { showGameTypeDialog = true }) {
                Text(text = config.currentGameType.displayName)
            }
        }

        // Home Team Configuration
        ConfigureTeamComponent(
            team = config.homeTeam,
            isHomeTeam = true,
            gameType = config.currentGameType,
            viewModel = viewModel,
            onTeamUpdated = { updatedTeam ->
                viewModel.updateHomeTeam(updatedTeam)
                config = viewModel.appConfig
            }
        )

        // Swap Teams Button
        TextButton(
            onClick = {
                viewModel.swapTeams()
                config = viewModel.appConfig
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("⇅ " + stringResource(R.string.swap_sides))
        }

        // Away Team Configuration
        ConfigureTeamComponent(
            team = config.awayTeam,
            isHomeTeam = false,
            gameType = config.currentGameType,
            viewModel = viewModel,
            onTeamUpdated = { updatedTeam ->
                viewModel.updateAwayTeam(updatedTeam)
                config = viewModel.appConfig
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // New Game button
        Button(
            onClick = { showNewGameDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.new_game))
        }

        // Go button
        Button(
            onClick = { /* Force landscape - handled by system */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(text = stringResource(R.string.go))
        }
    }

    // New Game Dialog
    if (showNewGameDialog) {
        AlertDialog(
            onDismissRequest = { showNewGameDialog = false },
            title = { Text(stringResource(R.string.new_game)) },
            text = { Text("Start a new game? This will reset the score.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.newGame()
                    config = viewModel.appConfig
                    showNewGameDialog = false
                }) {
                    Text("New Game")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewGameDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Game Type Selection Dialog
    if (showGameTypeDialog) {
        GameTypeSelectionDialog(
            currentGameType = config.currentGameType,
            viewModel = viewModel,
            onDismiss = {
                showGameTypeDialog = false
                config = viewModel.appConfig
            }
        )
    }
}
