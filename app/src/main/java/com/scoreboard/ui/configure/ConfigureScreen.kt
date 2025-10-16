package com.scoreboard.ui.configure

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.craigsheppard.scoreboard.R
import com.scoreboard.viewmodel.ConfigureViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ConfigureScreen(
    viewModel: ConfigureViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val config = uiState.appConfig

    var showNewGameDialog by remember { mutableStateOf(false) }
    var showGameTypeDialog by remember { mutableStateOf(false) }

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
            }
        )

        // Swap Teams Button
        TextButton(
            onClick = {
                viewModel.swapTeams()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.swap_icon) + " " + stringResource(R.string.swap_sides))
        }

        // Away Team Configuration
        ConfigureTeamComponent(
            team = config.awayTeam,
            isHomeTeam = false,
            gameType = config.currentGameType,
            viewModel = viewModel,
            onTeamUpdated = { updatedTeam ->
                viewModel.updateAwayTeam(updatedTeam)
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
            onClick = {
                // Rotate to landscape - ScoreboardScreen will lock it
                (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            },
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
            text = { Text(stringResource(R.string.new_game_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.newGame()
                    showNewGameDialog = false
                }) {
                    Text(stringResource(R.string.new_game_dialog_confirm))
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
            onDismiss = { showGameTypeDialog = false }
        )
    }
}
