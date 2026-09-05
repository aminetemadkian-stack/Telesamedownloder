package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Playlist
import com.example.data.model.VideoItem
import com.example.ui.theme.TelegramBlue

@Composable
fun PlaylistSelectorDialog(
    video: VideoItem,
    playlists: List<Playlist>,
    isPersian: Boolean,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Long?) -> Unit,
    onCreateNewPlaylist: (String, String) -> Unit
) {
    var selectedPlId by remember { mutableStateOf(video.playlistId) }
    var isCreatingNew by remember { mutableStateOf(false) }
    var newPlName by remember { mutableStateOf("") }
    var newPlDesc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("playlist_selector_dialog"),
        title = {
            Text(
                text = if (isPersian) "افزودن به پلی‌لیست" else "Add to Playlist",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!isCreatingNew) {
                    Text(
                        text = if (isPersian) "پلی‌لیست مورد نظر را برای این ویدیو انتخاب کنید:"
                        else "Choose a playlist for this video:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedPlId == null) TelegramBlue.copy(alpha = 0.15f) else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPlId = null }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedPlId == null,
                                        onClick = { selectedPlId = null },
                                        colors = RadioButtonDefaults.colors(selectedColor = TelegramBlue)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isPersian) "بدون پلی‌لیست (عمومی)" else "No Playlist (General)",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        items(playlists) { pl ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedPlId == pl.id) TelegramBlue.copy(alpha = 0.15f) else Color.Transparent,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPlId = pl.id }
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedPlId == pl.id,
                                        onClick = { selectedPlId = pl.id },
                                        colors = RadioButtonDefaults.colors(selectedColor = TelegramBlue)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = pl.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (pl.channelUsername != null) {
                                            Text(
                                                text = "@${pl.channelUsername}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TelegramBlue,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { isCreatingNew = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isPersian) "ایجاد پلی‌لیست جدید" else "Create New Playlist")
                    }
                } else {
                    // Create New Playlist Form
                    Text(
                        text = if (isPersian) "مشخصات پلی‌لیست جدید:" else "New Playlist Details:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPlName,
                        onValueChange = { newPlName = it },
                        label = { Text(if (isPersian) "نام پلی‌لیست" else "Playlist Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPlDesc,
                        onValueChange = { newPlDesc = it },
                        label = { Text(if (isPersian) "توضیحات (اختیاری)" else "Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { isCreatingNew = false }) {
                            Text(if (isPersian) "بازگشت" else "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newPlName.isNotBlank()) {
                                    onCreateNewPlaylist(newPlName, newPlDesc)
                                    isCreatingNew = false
                                }
                            },
                            enabled = newPlName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                        ) {
                            Text(if (isPersian) "ذخیره" else "Save", color = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isCreatingNew) {
                Button(
                    onClick = {
                        onSelectPlaylist(selectedPlId)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text(if (isPersian) "تایید" else "Apply", color = Color.White)
                }
            }
        },
        dismissButton = {
            if (!isCreatingNew) {
                OutlinedButton(onClick = onDismiss) {
                    Text(if (isPersian) "انصراف" else "Cancel")
                }
            }
        }
    )
}
