package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.VideoItem
import com.example.ui.theme.TelegramBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagManagementDialog(
    video: VideoItem,
    availableCategories: List<String>,
    isPersian: Boolean,
    onDismiss: () -> Unit,
    onSave: (tags: String, category: String) -> Unit
) {
    val tagList = remember {
        mutableStateListOf<String>().apply {
            addAll(video.tagList)
        }
    }
    var newTagInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(video.category) }
    var customCategoryInput by remember { mutableStateOf("") }
    var isAddingCustomCategory by remember { mutableStateOf(false) }

    val defaultCategories = listOf("عمومی", "تکنولوژی و فناوری", "آموزشی", "سینما و فیلم", "مستند و طبیعت", "پادکست")
    val combinedCategories = (defaultCategories + availableCategories).distinct()

    val suggestedTags = listOf("برنامه‌نویسی", "کاتلین", "کامپوز", "ریلز", "آموزشی", "سینما", "پادکست", "علمی", "موبایل", "اندروید")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("tag_management_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Label,
                    contentDescription = null,
                    tint = TelegramBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersian) "مدیریت برچسب‌ها و دسته‌بندی" else "Manage Tags & Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = TelegramBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPersian) "انتخاب دسته‌بندی ویدیو:" else "Select Category:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    combinedCategories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TelegramBlue,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Button to add custom category
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { isAddingCustomCategory = !isAddingCustomCategory }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPersian) "دسته جدید" else "+ New Category", fontSize = 11.sp)
                        }
                    }
                }

                if (isAddingCustomCategory) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customCategoryInput,
                            onValueChange = { customCategoryInput = it },
                            placeholder = { Text(if (isPersian) "نام دسته جدید..." else "New category name...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (customCategoryInput.isNotBlank()) {
                                    selectedCategory = customCategoryInput.trim()
                                    customCategoryInput = ""
                                    isAddingCustomCategory = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                        ) {
                            Text(if (isPersian) "تایید" else "Set", fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tags Section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = null,
                        tint = TelegramBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPersian) "برچسب‌های این ویدیو (تگ‌ها):" else "Video Tags:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Existing tags chips
                if (tagList.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tagList.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TelegramBlue.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 3.dp, bottom = 3.dp)
                                ) {
                                    Text(text = "#$tag", fontSize = 11.sp, color = TelegramBlue, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { tagList.remove(tag) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Add new tag input field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it },
                        placeholder = { Text(if (isPersian) "افزودن برچسب جدید..." else "Add new tag...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_tag_input_field"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            val clean = newTagInput.trim().removePrefix("#")
                            if (clean.isNotBlank() && !tagList.contains(clean)) {
                                tagList.add(clean)
                                newTagInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                        modifier = Modifier.testTag("add_tag_confirm_button")
                    ) {
                        Text(if (isPersian) "افزودن" else "Add", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Quick suggested tags
                Text(
                    text = if (isPersian) "برچسب‌های پیشنهادی پرکاربرد:" else "Suggested Tags:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    suggestedTags.forEach { sug ->
                        if (!tagList.contains(sug)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { tagList.add(sug) }
                            ) {
                                Text(
                                    text = "+ $sug",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(tagList.joinToString(","), selectedCategory)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                modifier = Modifier.testTag("save_tags_button")
            ) {
                Text(if (isPersian) "ذخیره تغییرات" else "Save Changes", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isPersian) "انصراف" else "Cancel")
            }
        }
    )
}
