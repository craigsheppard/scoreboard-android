package com.scoreboard.ui.configure

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scoreboard.R
import com.scoreboard.data.models.GameType
import com.scoreboard.data.models.SavedTeam
import com.scoreboard.data.models.TeamConfiguration
import com.scoreboard.viewmodel.ScoreboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamSelectionDialog(
    team: TeamConfiguration,
    isHomeTeam: Boolean,
    gameType: GameType,
    viewModel: ScoreboardViewModel,
    onDismiss: () -> Unit,
    onTeamSelected: (TeamConfiguration) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var teamToDelete by remember { mutableStateOf<SavedTeam?>(null) }
    var showUnsavedChangesDialog by remember { mutableStateOf(false) }
    var pendingTeam by remember { mutableStateOf<SavedTeam?>(null) }

    val availableTeams = viewModel.getTeamsForCurrentGameType()
    val hasUnsavedChanges = team.hasUnsavedChanges()

    fun applyTeam(savedTeam: SavedTeam?) {
        if (hasUnsavedChanges) {
            pendingTeam = savedTeam
            showUnsavedChangesDialog = true
        } else {
            if (savedTeam != null) {
                val teamConfig = savedTeam.toTeamConfiguration()
                teamConfig.updateLastSavedState()
                onTeamSelected(teamConfig)
            } else {
                // Create new team
                val newTeam = TeamConfiguration(
                    teamName = "New Team",
                    primaryColor = Color.Red,
                    secondaryColor = Color.Blue,
                    fontColor = Color.White,
                    savedTeamId = null
                )
                newTeam.updateLastSavedState()
                onTeamSelected(newTeam)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.8f)
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Title
                Text(
                    text = if (isHomeTeam)
                        stringResource(R.string.select_home_team)
                    else
                        stringResource(R.string.select_away_team),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // New Team button
                    item {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.new_team),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New team",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable { applyTeam(null) }
                        )
                        HorizontalDivider()
                    }

                    // Saved teams
                    if (availableTeams.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_saved_teams),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(availableTeams) { savedTeam ->
                            ListItem(
                                headlineContent = { Text(savedTeam.name) },
                                trailingContent = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Color indicators
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(savedTeam.primaryColor))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(savedTeam.secondaryColor))
                                        )

                                        IconButton(onClick = {
                                            teamToDelete = savedTeam
                                            showDeleteDialog = true
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete team",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.clickable { applyTeam(savedTeam) }
                            )
                        }
                    }
                }

                // Cancel button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && teamToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_team)) },
            text = { Text("Are you sure you want to delete '${teamToDelete?.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    teamToDelete?.let { viewModel.deleteTeam(it) }
                    showDeleteDialog = false
                    teamToDelete = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    teamToDelete = null
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Unsaved changes dialog
    if (showUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog = false },
            title = { Text(stringResource(R.string.unsaved_changes)) },
            text = { Text("You have unsaved changes to the current team. What would you like to do?") },
            confirmButton = {
                TextButton(onClick = {
                    // Save current team first
                    viewModel.saveTeam(team)

                    // Then apply new team
                    if (pendingTeam != null) {
                        val teamConfig = pendingTeam!!.toTeamConfiguration()
                        teamConfig.updateLastSavedState()
                        onTeamSelected(teamConfig)
                    } else {
                        val newTeam = TeamConfiguration(
                            teamName = "New Team",
                            primaryColor = Color.Red,
                            secondaryColor = Color.Blue,
                            fontColor = Color.White,
                            savedTeamId = null
                        )
                        newTeam.updateLastSavedState()
                        onTeamSelected(newTeam)
                    }
                    showUnsavedChangesDialog = false
                }) {
                    Text("Save Current Team")
                }
            },
            dismissButton = {
                Column {
                    TextButton(onClick = {
                        // Discard changes and apply new team
                        if (pendingTeam != null) {
                            val teamConfig = pendingTeam!!.toTeamConfiguration()
                            teamConfig.updateLastSavedState()
                            onTeamSelected(teamConfig)
                        } else {
                            val newTeam = TeamConfiguration(
                                teamName = "New Team",
                                primaryColor = Color.Red,
                                secondaryColor = Color.Blue,
                                fontColor = Color.White,
                                savedTeamId = null
                            )
                            newTeam.updateLastSavedState()
                            onTeamSelected(newTeam)
                        }
                        showUnsavedChangesDialog = false
                    }) {
                        Text(
                            stringResource(R.string.discard_changes),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    TextButton(onClick = { showUnsavedChangesDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        )
    }
}
