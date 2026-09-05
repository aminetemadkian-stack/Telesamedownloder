package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.VideoItem
import java.io.File

object ForwardShareHelper {

    fun shareVideo(context: Context, video: VideoItem) {
        try {
            if (video.isDownloaded && video.localFilePath != null) {
                val file = File(video.localFilePath)
                if (file.exists()) {
                    val contentUri: Uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "video/mp4"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        putExtra(Intent.EXTRA_SUBJECT, video.title)
                        putExtra(Intent.EXTRA_TEXT, "ویدیو از کانال ${video.channelTitle}\nلینک تلگرام: ${video.telegramUrl}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    context.startActivity(Intent.createChooser(shareIntent, "فوروارد / اشتراک‌گذاری ویدیو به:"))
                    return
                }
            }

            // If not downloaded locally, share Telegram link & title
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, video.title)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "${video.title}\n\nکانال: ${video.channelTitle} (@${video.channelUsername})\nلینک تلگرام: ${video.telegramUrl}"
                )
            }
            context.startActivity(Intent.createChooser(shareIntent, "ارسال به تلگرام یا برنامه‌ها:"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "خطا در اشتراک‌گذاری: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openInTelegram(context: Context, telegramUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl)).apply {
                setPackage("org.telegram.messenger")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to normal browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
            context.startActivity(browserIntent)
        }
    }
}
