package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.QualityLevel
import com.example.data.model.VideoItem
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.TelegramBlue
import java.io.File

enum class ReadingTheme {
    DARK, LIGHT, SEPIA
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerDialog(
    video: VideoItem,
    isPersian: Boolean,
    onDismiss: () -> Unit,
    onDownloadPdf: ((VideoItem) -> Unit)? = null
) {
    val context = LocalContext.current
    var currentPage by remember { mutableIntStateOf(1) }
    val totalPages = if (video.pageCount > 0) video.pageCount else 24
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Reader / Preview, 1: Details & Summary, 2: Index / Pages
    var readingTheme by remember { mutableStateOf(ReadingTheme.DARK) }
    var fontScale by remember { mutableFloatStateOf(14f) }

    val themeBgColor = when (readingTheme) {
        ReadingTheme.DARK -> Color(0xFF1E2430)
        ReadingTheme.LIGHT -> Color(0xFFF8FAFC)
        ReadingTheme.SEPIA -> Color(0xFFFBF0D9)
    }

    val themeTextColor = when (readingTheme) {
        ReadingTheme.DARK -> Color(0xFFF1F5F9)
        ReadingTheme.LIGHT -> Color(0xFF0F172A)
        ReadingTheme.SEPIA -> Color(0xFF433422)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("pdf_viewer_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE53935).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = video.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${video.channelTitle} • ${video.fileSizeFormatted}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TelegramBlue,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Share link button
                            IconButton(
                                onClick = { ForwardShareHelper.shareVideo(context, video) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Close Button
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("close_pdf_dialog")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Tabs: Preview, Summary, Pages
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(if (isPersian) "مطالعه سند" else "Read PDF", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(if (isPersian) "خلاصه و مشخصات" else "Details & Summary", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text(if (isPersian) "فهرست صفحات" else "Page Index", fontSize = 12.sp) }
                    )
                }

                // Content View according to tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> {
                            // PDF Reader Canvas
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Reading toolbar: Font size, themes, page navigation
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Theme picker
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF1E2430),
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clickable { readingTheme = ReadingTheme.DARK }
                                            ) {}
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFF8FAFC),
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clickable { readingTheme = ReadingTheme.LIGHT }
                                            ) {}
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFFFBF0D9),
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clickable { readingTheme = ReadingTheme.SEPIA }
                                            ) {}
                                        }

                                        // Font Zoom Buttons
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = { if (fontScale > 11f) fontScale -= 1.5f },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Smaller", modifier = Modifier.size(16.dp))
                                            }
                                            Text("${fontScale.toInt()}pt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            IconButton(
                                                onClick = { if (fontScale < 24f) fontScale += 1.5f },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Larger", modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        // Page Counter
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface
                                        ) {
                                            Text(
                                                text = if (isPersian) "صفحه $currentPage از $totalPages" else "Page $currentPage of $totalPages",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // PDF Simulated Content Page
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(themeBgColor)
                                        .padding(16.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Column {
                                        // Header of page
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "📖 ${video.title.take(30)}...",
                                                fontSize = (fontScale - 3f).sp,
                                                color = themeTextColor.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = "#$currentPage",
                                                fontSize = (fontScale - 3f).sp,
                                                fontWeight = FontWeight.Bold,
                                                color = themeTextColor.copy(alpha = 0.6f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = if (currentPage == 1) "فصل اول: مقدمه و پیش‌زمینه مباحث"
                                            else "بخش $currentPage: تحلیل کاربردی و پیاده‌سازی نکات",
                                            fontSize = (fontScale + 3f).sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeTextColor
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Sample book content / Caption extract
                                        val sampleText = if (video.caption.isNotBlank()) {
                                            "${video.caption}\n\n" +
                                                    "در این بخش از کتاب الکترونیکی PDF ارائه‌شده توسط کانال ${video.channelTitle}، به بررسی دقیق تکنیک‌ها، الگوهای طراحی استاندارد و راهکارهای ارتقای بهره‌وری پرداخته شده است.\n\n" +
                                                    "• راهکارهای بهینه‌سازی پردازش و استفاده از حافظه کش\n" +
                                                    "• بررسی مثال‌های عملی و نکات کلیدی برای پیاده‌سازی پایدار\n" +
                                                    "• نحوه استفاده از کتابخانه‌ها در پلتفرم‌های مختلف و پشتیبانی کامل آفلاین\n\n" +
                                                    "فایل‌های PDF با قابلیت ذخیره‌سازی آفلاین، امکان مطالعه در هر زمان و مکان بدون نیاز به اتصال دائم به اینترنت را فراهم می‌کنند."
                                        } else {
                                            "این فایل PDF شامل مجموعه مقالات، منابع تخصصی و آموزش‌های جامع است که از تلگرام دریافت شده است.\n\n" +
                                                    "صفحه $currentPage شامل نمودارها، توضیحات مفهومی و کدهای نمونه برای درک بهتر مباحث می‌باشد."
                                        }

                                        Text(
                                            text = sampleText,
                                            fontSize = fontScale.sp,
                                            lineHeight = (fontScale * 1.6f).sp,
                                            color = themeTextColor,
                                            textAlign = TextAlign.Justify
                                        )

                                        Spacer(modifier = Modifier.height(24.dp))
                                    }
                                }

                                // Bottom Page Navigation Bar
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { if (currentPage > 1) currentPage-- },
                                            enabled = currentPage > 1
                                        ) {
                                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Previous")
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isPersian) "صفحه قبل" else "Previous", fontSize = 12.sp)
                                        }

                                        Slider(
                                            value = currentPage.toFloat(),
                                            onValueChange = { currentPage = it.toInt().coerceIn(1, totalPages) },
                                            valueRange = 1f..totalPages.toFloat(),
                                            steps = totalPages - 2,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 12.dp)
                                        )

                                        Button(
                                            onClick = { if (currentPage < totalPages) currentPage++ },
                                            enabled = currentPage < totalPages
                                        ) {
                                            Text(if (isPersian) "صفحه بعد" else "Next", fontSize = 12.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Next")
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // Details & Summary View
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = TelegramBlue)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (isPersian) "اطلاعات فایل PDF تلگرام" else "Telegram PDF Document Info",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(if (isPersian) "تعداد صفحات:" else "Total Pages:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$totalPages صفحه", fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(if (isPersian) "حجم فایل:" else "File Size:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(video.fileSizeFormatted, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(if (isPersian) "کانال منتشر کننده:" else "Telegram Channel:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("@${video.channelUsername}", color = TelegramBlue, fontWeight = FontWeight.Bold)
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(if (isPersian) "وضعیت ذخیره:" else "Storage Status:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = if (video.isDownloaded) "ذخیره در حافظه دستگاه ✅" else "آنلاین (نیازمند دانلود)",
                                                color = if (video.isDownloaded) EmeraldGreen else MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = if (isPersian) "توضیحات و خلاصه متن:" else "Description & Summary:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 2.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = video.caption.ifBlank { "این فایل سند الکترونیکی PDF به طور مستقیم از کانال @${video.channelUsername} دریافت شده است و حاوی محتوای کامل متن، راهنماها و مباحث آموزشی است." },
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(14.dp),
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }

                        2 -> {
                            // Index Grid View
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = if (isPersian) "فهرست و پرش به صفحه دلخواه:" else "Jump to specific page:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val pageNumbers = (1..totalPages).toList()
                                for (chunk in pageNumbers.chunked(4)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        chunk.forEach { page ->
                                            val isCurrent = page == currentPage
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isCurrent) TelegramBlue else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .clickable {
                                                        currentPage = page
                                                        selectedTab = 0
                                                    }
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "$page",
                                                        color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        // Fill empty spaces
                                        repeat(4 - chunk.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                // Bottom Fixed Download / Open Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (video.isDownloaded) {
                            Button(
                                onClick = {
                                    // Open in external PDF reader app
                                    video.localFilePath?.let { path ->
                                        val file = File(path)
                                        val uri = Uri.fromFile(file)
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, "application/pdf")
                                            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_external_pdf_button")
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPersian) "باز کردن در PDF Reader" else "Open in PDF Reader", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { onDownloadPdf?.invoke(video) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("download_pdf_button")
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPersian) "ذخیره و دانلود فایل PDF" else "Save PDF to Storage", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Open in Telegram
                        FilledTonalIconButton(
                            onClick = { ForwardShareHelper.openInTelegram(context, video.telegramUrl) },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Telegram")
                        }
                    }
                }
            }
        }
    }
}
