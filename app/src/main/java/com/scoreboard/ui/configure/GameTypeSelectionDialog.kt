package com.scoreboard.ui.configure

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scoreboard.R
import com.scoreboard.data.models.GameType
import com.scoreboard.viewmodel.ConfigureViewModel

@Composable
fun GameTypeSelectionDialog(
    currentGameType: GameType,
    viewModel: ConfigureViewModel,
    onDismiss: () -> Unit
) {
    var selectedGameType by remember { mutableStateOf(currentGameType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Game Type") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(GameType.values()) { gameType ->
                    ListItem(
                        headlineContent = { Text(gameType.displayName) },
                        trailingContent = {
                            if (selectedGameType == gameType) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            selectedGameType = gameType
                            viewModel.setGameType(gameType)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
