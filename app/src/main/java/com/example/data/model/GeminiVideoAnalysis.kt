package com.example.data.model

data class GeminiVideoAnalysis(
    val summaryEn: String,
    val summaryFa: String,
    val keyTakeaways: List<String>,
    val chapters: List<VideoChapter>,
    val tags: List<String>,
    val suggestedPlaylistCategory: String,
    val sentiment: String,
    val rawJson: String? = null
)

data class VideoChapter(
    val timestamp: String,
    val title: String,
    val description: String
)
