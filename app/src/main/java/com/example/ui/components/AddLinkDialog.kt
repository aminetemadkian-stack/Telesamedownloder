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
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.TelegramBlue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddLinkDialog(
    isPersian: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit
) {
    var linkInput by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Video/All, 1: Podcast, 2: PDF, 3: Instagram
    val clipboardManager = LocalClipboardManager.current

    val tabAccentColor = when (selectedTab) {
        1 -> AmberAccent
        2 -> Color(0xFFE53935)
        3 -> InstagramPink
        else -> TelegramBlue
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_link_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddLink,
                    contentDescription = null,
                    tint = tabAccentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPersian) "افزودن لینک (ویدیو، پادکست، PDF)" else "Add Media (Video, Podcast, PDF)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 4.dp
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = TelegramBlue, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isPersian) "ویدیو تلگرام" else "Videos", fontSize = 11.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Headphones, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isPersian) "پادکست و صوت" else "Podcasts", fontSize = 11.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isPersian) "کتاب و PDF" else "PDFs", fontSize = 11.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = InstagramPink, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isPersian) "اینستاگرام" else "Instagram", fontSize = 11.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val promptDescription = when (selectedTab) {
                    0 -> if (isPersian) "لینک پست یا چنل ویدیویی تلگرام (t.me/channel/123 یا t.me/ch/1-10):" else "Enter Telegram video or channel link:"
                    1 -> if (isPersian) "لینک پادکست، ویس یا فایل صوتی تلگرام (t.me/podcast_channel/45):" else "Enter Telegram Podcast / Audio track link:"
                    2 -> if (isPersian) "لینک فایل PDF، کتاب یا جزوه آموزشی تلگرام (t.me/books/88):" else "Enter Telegram PDF book or document link:"
                    else -> if (isPersian) "لینک ریلز یا پیج اینستاگرام (instagram.com/reel/...):" else "Enter Instagram Reel or profile URL:"
                }

                Text(
                    text = promptDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                val placeholderText = when (selectedTab) {
                    0 -> "https://t.me/channel/123\nیا t.me/channel/10-15"
                    1 -> "https://t.me/radio_dialog/45\nیا t.me/bplus_podcast/12"
                    2 -> "https://t.me/tech_books/72\nیا t.me/ai_persian/book.pdf"
                    else -> "https://instagram.com/reel/C8Kx99"
                }

                OutlinedTextField(
                    value = linkInput,
                    onValueChange = { linkInput = it },
                    placeholder = {
                        Text(placeholderText, fontSize = 12.sp)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val clip = clipboardManager.getText()?.text
                                if (!clip.isNullOrBlank()) {
                                    linkInput = clip
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                tint = tabAccentColor
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                        .testTag("telegram_link_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick presets for testing
                Text(
                    text = if (isPersian) "نمونه‌های آماده تست:" else "Quick Test Presets:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = tabAccentColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = when (selectedTab) {
                        0 -> listOf(
                            "https://t.me/tech_persian/105" to "کانال هوش مصنوعی",
                            "https://t.me/cinema_hub/90-93" to "سریال ۴ قسمتی سینما",
                            "https://t.me/durov" to "پاول دوروف"
                        )
                        1 -> listOf(
                            "https://t.me/radio_dialog/45" to "پادکست رادیو دیالوگ",
                            "https://t.me/bplus_podcast/88" to "پادکست خلاصه کتاب",
                            "https://t.me/tech_audio/12" to "پادکست فناوری و وب۳"
                        )
                        2 -> listOf(
                            "https://t.me/kotlin_persian/book.pdf" to "کتاب مرجع کاتلین PDF",
                            "https://t.me/ai_books/deep_learning.pdf" to "جزوه یادگیری عمیق PDF",
                            "https://t.me/tech_persian/report.pdf" to "گزارش سالانه تکنولوژی PDF"
                        )
                        else -> listOf(
                            "https://instagram.com/reel/C8Kx99" to "ریلز ادیت ویدیو",
                            "https://instagram.com/reel/C9AI88" to "ریلز هوش مصنوعی",
                            "https://instagram.com/tech_persian" to "پیج تکنولوژی"
                        )
                    }

                    presets.forEach { (url, label) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { linkInput = url }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = tabAccentColor.copy(alpha = 0.08f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = tabAccentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPersian) "فایل‌های ویدیویی، صوتی پادکست و کتاب‌های PDF به طور هوشمند تحلیل و ذخیره می‌شوند."
                            else "Videos, audio podcasts, and PDF documents will be analyzed and organized.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (linkInput.isNotBlank()) {
                        onSubmit(linkInput)
                    }
                },
                enabled = linkInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tabAccentColor
                ),
                modifier = Modifier.testTag("submit_telegram_link_button")
            ) {
                Text(
                    text = if (isPersian) "آنالیز و دریافت" else "Analyze & Add",
                    color = if (selectedTab == 1) Color.Black else Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(if (isPersian) "انصراف" else "Cancel")
            }
        }
    )
}
