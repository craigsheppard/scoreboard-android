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
import com.craigsheppard.scoreboard.R
import com.scoreboard.data.models.GameType
import com.scoreboard.data.models.TeamConfiguration
import com.scoreboard.viewmodel.ConfigureViewModel

@Composable
fun ConfigureTeamComponent(
    team: TeamConfiguration,
    isHomeTeam: Boolean,
    gameType: GameType,
    viewModel: ConfigureViewModel,
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Extended color palette organized by hue
                val colorPalette = listOf(
                    // Reds
                    listOf(
                        Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFEF9A9A), Color(0xFFE57373),
                        Color(0xFFEF5350), Color(0xFFF44336), Color(0xFFE53935), Color(0xFFD32F2F),
                        Color(0xFFC62828), Color(0xFFB71C1C)
                    ),
                    // Pinks
                    listOf(
                        Color(0xFFFCE4EC), Color(0xFFF8BBD0), Color(0xFFF48FB1), Color(0xFFF06292),
                        Color(0xFFEC407A), Color(0xFFE91E63), Color(0xFFD81B60), Color(0xFFC2185B),
                        Color(0xFFAD1457), Color(0xFF880E4F)
                    ),
                    // Purples
                    listOf(
                        Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFCE93D8), Color(0xFFBA68C8),
                        Color(0xFFAB47BC), Color(0xFF9C27B0), Color(0xFF8E24AA), Color(0xFF7B1FA2),
                        Color(0xFF6A1B9A), Color(0xFF4A148C)
                    ),
                    // Blues
                    listOf(
                        Color(0xFFE3F2FD), Color(0xFFBBDEFB), Color(0xFF90CAF9), Color(0xFF64B5F6),
                        Color(0xFF42A5F5), Color(0xFF2196F3), Color(0xFF1E88E5), Color(0xFF1976D2),
                        Color(0xFF1565C0), Color(0xFF0D47A1)
                    ),
                    // Cyans
                    listOf(
                        Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFF80DEEA), Color(0xFF4DD0E1),
                        Color(0xFF26C6DA), Color(0xFF00BCD4), Color(0xFF00ACC1), Color(0xFF0097A7),
                        Color(0xFF00838F), Color(0xFF006064)
                    ),
                    // Greens
                    listOf(
                        Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFFA5D6A7), Color(0xFF81C784),
                        Color(0xFF66BB6A), Color(0xFF4CAF50), Color(0xFF43A047), Color(0xFF388E3C),
                        Color(0xFF2E7D32), Color(0xFF1B5E20)
                    ),
                    // Yellows/Oranges
                    listOf(
                        Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFFF59D), Color(0xFFFFF176),
                        Color(0xFFFFEE58), Color(0xFFFFEB3B), Color(0xFFFDD835), Color(0xFFFBC02D),
                        Color(0xFFF9A825), Color(0xFFF57F17)
                    ),
                    // Oranges
                    listOf(
                        Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFFFCC80), Color(0xFFFFB74D),
                        Color(0xFFFFA726), Color(0xFFFF9800), Color(0xFFFB8C00), Color(0xFFF57C00),
                        Color(0xFFEF6C00), Color(0xFFE65100)
                    ),
                    // Browns/Greys
                    listOf(
                        Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFFBCAAA4), Color(0xFFA1887F),
                        Color(0xFF8D6E63), Color(0xFF795548), Color(0xFF6D4C41), Color(0xFF5D4037),
                        Color(0xFF4E342E), Color(0xFF3E2723)
                    ),
                    // Greys/Black/White
                    listOf(
                        Color.White, Color(0xFFF5F5F5), Color(0xFFEEEEEE), Color(0xFFE0E0E0),
                        Color(0xFFBDBDBD), Color(0xFF9E9E9E), Color(0xFF757575), Color(0xFF616161),
                        Color(0xFF424242), Color.Black
                    )
                )

                Text("Color Palette", style = MaterialTheme.typography.labelMedium)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    colorPalette.forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            rowColors.forEach { color ->
                                Button(
                                    onClick = { selectedColor = color },
                                    colors = ButtonDefaults.buttonColors(containerColor = color),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    if (selectedColor == color) {
                                        Text(
                                            "✓",
                                            color = if (color.luminance() > 0.5f) Color.Black else Color.White,
                                            style = MaterialTheme.typography.labelSmall
                                        )
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
