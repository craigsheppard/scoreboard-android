package com.scoreboard.ui.configure

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scoreboard.R
import com.scoreboard.data.models.GameType
import com.scoreboard.data.models.TeamConfiguration
import com.scoreboard.viewmodel.ScoreboardViewModel

@Composable
fun ConfigureTeamComponent(
    team: TeamConfiguration,
    isHomeTeam: Boolean,
    gameType: GameType,
    viewModel: ScoreboardViewModel,
    onTeamUpdated: (TeamConfiguration) -> Unit,
    modifier: Modifier = Modifier
) {
    var teamName by remember { mutableStateOf(team.teamName) }
    var primaryColor by remember { mutableStateOf(team.primaryColor) }
    var secondaryColor by remember { mutableStateOf(team.secondaryColor) }
    var fontColor by remember { mutableStateOf(team.fontColor) }
    var showTeamSelection by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    // Update team when colors change
    LaunchedEffect(teamName, primaryColor, secondaryColor, fontColor) {
        val updatedTeam = team.copy(
            teamName = teamName,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            fontColor = fontColor
        )
        onTeamUpdated(updatedTeam)
    }

    val hasUnsavedChanges = team.hasUnsavedChanges()
    val isExistingTeam = team.savedTeamId != null &&
        viewModel.getTeamsForCurrentGameType().any { it.id == team.savedTeamId }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Team name and selection button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = teamName,
                    onValueChange = { teamName = it },
                    label = { Text(stringResource(R.string.team_name)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                IconButton(onClick = { showTeamSelection = true }) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Select team"
                    )
                }
            }

            // Color pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ColorPickerButton(
                    label = stringResource(R.string.primary_color),
                    color = primaryColor,
                    onColorSelected = { primaryColor = it }
                )

                ColorPickerButton(
                    label = stringResource(R.string.secondary_color),
                    color = secondaryColor,
                    onColorSelected = { secondaryColor = it }
                )

                ColorPickerButton(
                    label = stringResource(R.string.font_color),
                    color = fontColor,
                    onColorSelected = { fontColor = it }
                )
            }

            // Save button (only show if there are unsaved changes)
            if (hasUnsavedChanges) {
                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (isExistingTeam)
                            stringResource(R.string.save_changes)
                        else
                            stringResource(R.string.save_team)
                    )
                }
            }
        }
    }

    // Team selection dialog
    if (showTeamSelection) {
        TeamSelectionDialog(
            team = team,
            isHomeTeam = isHomeTeam,
            gameType = gameType,
            viewModel = viewModel,
            onDismiss = { showTeamSelection = false },
            onTeamSelected = { selectedTeam ->
                teamName = selectedTeam.teamName
                primaryColor = selectedTeam.primaryColor
                secondaryColor = selectedTeam.secondaryColor
                fontColor = selectedTeam.fontColor
                onTeamUpdated(selectedTeam)
                showTeamSelection = false
            }
        )
    }

    // Save confirmation dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    if (isExistingTeam)
                        stringResource(R.string.save_changes)
                    else
                        stringResource(R.string.save_team)
                )
            },
            text = {
                Text(
                    if (isExistingTeam)
                        "Save changes to '$teamName'?"
                    else
                        "Save '$teamName' as a ${gameType.displayName} team?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.saveTeam(team)
                    showSaveDialog = false
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun ColorPickerButton(
    label: String,
    color: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )

        Button(
            onClick = { showColorPicker = true },
            colors = ButtonDefaults.buttonColors(containerColor = color),
            modifier = Modifier.size(60.dp),
            contentPadding = PaddingValues(0.dp)
        ) {}
    }

    if (showColorPicker) {
        ColorPickerDialog(
            initialColor = color,
            onColorSelected = {
                onColorSelected(it)
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(initialColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Color") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Predefined colors
                val predefinedColors = listOf(
                    Color.Red, Color.Blue, Color.Green, Color.Yellow,
                    Color.Cyan, Color.Magenta, Color.Black, Color.White,
                    Color(0xFFFF9800), // Orange
                    Color(0xFF9C27B0), // Purple
                    Color(0xFF795548), // Brown
                    Color(0xFF607D8B)  // Blue Grey
                )

                Text("Quick Colors", style = MaterialTheme.typography.labelMedium)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    predefinedColors.chunked(4).forEach { rowColors ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowColors.forEach { color ->
                                Button(
                                    onClick = { selectedColor = color },
                                    colors = ButtonDefaults.buttonColors(containerColor = color),
                                    modifier = Modifier.size(50.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    if (selectedColor == color) {
                                        Text("✓", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(selectedColor) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
