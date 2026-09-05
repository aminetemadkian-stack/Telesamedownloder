package com.example.data.repository

import com.example.data.local.PlaylistDao
import com.example.data.local.VideoDao
import com.example.data.model.Playlist
import com.example.data.model.QualityLevel
import com.example.data.model.VideoItem
import com.example.data.remote.InstagramScraperService
import com.example.data.remote.TelegramScraperService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class VideoRepository(
    private val videoDao: VideoDao,
    private val playlistDao: PlaylistDao,
    private val proxyRepository: ProxyRepository,
    private val downloadManager: DownloadManager,
    val storageManager: StorageManager
) {
    val allVideos: Flow<List<VideoItem>> = videoDao.getAllVideos()
    val favoriteVideos: Flow<List<VideoItem>> = videoDao.getFavoriteVideos()
    val downloadedVideos: Flow<List<VideoItem>> = videoDao.getDownloadedVideos()
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()
    val favoritePlaylists: Flow<List<Playlist>> = playlistDao.getFavoritePlaylists()
    val allChannels: Flow<List<String>> = videoDao.getAllChannels()
    val allCategories: Flow<List<String>> = videoDao.getAllCategories()

    suspend fun initializeSeedDataIfEmpty() = withContext(Dispatchers.IO) {
        val existingVideos = allVideos.firstOrNull()
        if (existingVideos.isNullOrEmpty()) {
            // Seed Default Playlists
            val techPlaylistId = playlistDao.insertPlaylist(
                Playlist(
                    name = "برنامه‌نویسی و توسعه نرم‌افزار",
                    description = "مجموعه ویدیوهای آموزشی تخصصی توسعه نرم‌افزار، کاتلین و کامپوز از کانال‌های تلگرام",
                    channelUsername = "tech_persian",
                    isFavorite = true,
                    colorHex = "#2AABEE",
                    iconName = "code",
                    sourcePlatform = "TELEGRAM"
                )
            )

            val instaPlaylistId = playlistDao.insertPlaylist(
                Playlist(
                    name = "پلی‌لیست ریلز اینستاگرام",
                    description = "برترین ریلزهای تکنولوژی، عکاسی، ایده‌پردازی و خلاقیت از اینستاگرام",
                    channelUsername = "tech_persian",
                    isFavorite = true,
                    colorHex = "#E1306C",
                    iconName = "instagram",
                    sourcePlatform = "INSTAGRAM"
                )
            )

            val documentaryPlaylistId = playlistDao.insertPlaylist(
                Playlist(
                    name = "مستندهای علمی و کیهان‌شناسی",
                    description = "شگفتی‌های کیهان، فیزیک کوانتوم و فناوری‌های نوین",
                    channelUsername = "science_doc",
                    isFavorite = false,
                    colorHex = "#00E676",
                    iconName = "science",
                    sourcePlatform = "TELEGRAM"
                )
            )

            val podcastPlaylistId = playlistDao.insertPlaylist(
                Playlist(
                    name = "پادکست‌ها و رادیو دیالوگ تلگرام",
                    description = "مجموعه برنامه‌های صوتی تحلیلی، گفتگو با کارآفرینان و خلاصه کتاب صوتی",
                    channelUsername = "radio_dialog",
                    isFavorite = true,
                    colorHex = "#FFB300",
                    iconName = "mic",
                    sourcePlatform = "TELEGRAM"
                )
            )

            val pdfPlaylistId = playlistDao.insertPlaylist(
                Playlist(
                    name = "کتاب‌ها و جزوات تخصصی PDF تلگرام",
                    description = "کتاب‌های مرجع برنامه‌نویسی، معماری نرم‌افزار و مستندات PDF برای مطالعه آفلاین",
                    channelUsername = "tech_books",
                    isFavorite = true,
                    colorHex = "#E53935",
                    iconName = "menu_book",
                    sourcePlatform = "TELEGRAM"
                )
            )

            val cinemaPlaylistId = playlistDao.insertPlaylist(
                Playlist(
                    name = "نقد و تحلیل سینمای جهان",
                    description = "بررسی شاهکارهای سینما، جلوه‌های ویژه و تکنیک‌های فیلم‌سازی",
                    channelUsername = "cinema_hub",
                    isFavorite = false,
                    colorHex = "#9C27B0",
                    iconName = "movie",
                    sourcePlatform = "TELEGRAM"
                )
            )

            // Seed High Quality Video, Podcast and PDF Items
            val sampleVideos = listOf(
                VideoItem(
                    title = "کتاب مرجع کاتلین و معماری مدرن جت‌پک کامپوز (PDF)",
                    channelTitle = "کتابخانه تخصصی توسعه نرم‌افزار",
                    channelUsername = "tech_books",
                    telegramUrl = "https://t.me/tech_books/101.pdf",
                    streamUrl = "https://raw.githubusercontent.com/mozilla/pdf.js/ba2edeae/examples/learning/helloworld.pdf",
                    thumbnailUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80",
                    durationSeconds = 0,
                    pageCount = 64,
                    fileSizeBytes = 8_450_000L,
                    caption = "کتاب جامع آموزش مفاهیم پیشرفته معماری در کاتلین، مدیریت وضعیت در کامپوز، الگوهای MVI و کار با پایگاه‌داده روم با کیفیت کامل برای مطالعه و ذخیره.",
                    category = "آموزشی",
                    playlistId = pdfPlaylistId,
                    isFavorite = true,
                    sourcePlatform = "TELEGRAM",
                    customTags = "کتاب, PDF, کاتلین, کامپوز",
                    mediaType = "PDF"
                ),
                VideoItem(
                    title = "پادکست رادیو تکنولوژی: انقلابی به نام هوش مصنوعی ژنراتیو",
                    channelTitle = "رادیو دیالوگ و پادکست",
                    channelUsername = "radio_dialog",
                    telegramUrl = "https://t.me/radio_dialog/45",
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                    thumbnailUrl = "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=600&auto=format&fit=crop&q=80",
                    durationSeconds = 1420,
                    fileSizeBytes = 28_600_000L,
                    caption = "در این اپیزود صوتی از پادکست رادیو دیالوگ به بررسی چشم‌انداز مدل‌های زبانی بزرگ، تغییر بازار کار برنامه‌نویسی و مسیر آینده توسعه فناوری می‌پردازیم.",
                    category = "پادکست",
                    playlistId = podcastPlaylistId,
                    isFavorite = true,
                    sourcePlatform = "TELEGRAM",
                    customTags = "پادکست, هوش مصنوعی, صوتی",
                    mediaType = "PODCAST"
                ),
                VideoItem(
                    title = "آموزش پیشرفته معماری و ساختارهای مدرن کاتلین در سال جدید",
                    channelTitle = "دنیای تکنولوژی و برنامه‌نویسی",
                    channelUsername = "tech_persian",
                    telegramUrl = "https://t.me/tech_persian/101",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    thumbnailUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop&q=80",
                    durationSeconds = 185,
                    fileSizeBytes = 24_500_000L,
                    caption = "در این قسمت تخصصی به بررسی ساختارها، کوروتین‌ها و تحولات شگرف در پردازش محتوا می‌پردازیم.",
                    category = "تکنولوژی و فناوری",
                    playlistId = techPlaylistId,
                    isFavorite = true,
                    sourcePlatform = "TELEGRAM",
                    customTags = "برنامه‌نویسی, کاتلین, توسعه نرم‌افزار",
                    mediaType = "VIDEO"
                ),
                VideoItem(
                    title = "جزوه راهنمای الگوریتم‌ها و شبکه‌های عصبی عمیق (PDF)",
                    channelTitle = "کتابخانه تخصصی توسعه نرم‌افزار",
                    channelUsername = "tech_books",
                    telegramUrl = "https://t.me/tech_books/72.pdf",
                    streamUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
                    thumbnailUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=600&auto=format&fit=crop&q=80",
                    durationSeconds = 0,
                    pageCount = 38,
                    fileSizeBytes = 5_200_000L,
                    caption = "جزوه خلاصه و کاربردی ساختارهای داده، تحلیل پیچیدگی زمانی الگوریتم‌ها و شبکه‌های یادگیری عمیق همراه با نمودارها.",
                    category = "آموزشی",
                    playlistId = pdfPlaylistId,
                    isFavorite = false,
                    sourcePlatform = "TELEGRAM",
                    customTags = "الگوریتم, PDF, جزوه آموزشی",
                    mediaType = "PDF"
                ),
                VideoItem(
                    title = "پادکست خلاصه کتاب: تفکر سریع و کند (قسمت ویژه)",
                    channelTitle = "رادیو دیالوگ و پادکست",
                    channelUsername = "radio_dialog",
                    telegramUrl = "https://t.me/radio_dialog/88",
                    streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                    thumbnailUrl = "https://images.unsplash.com/photo-1478737270239-2f02b77fc618?w=600&auto=format&fit=crop&q=80",
                    durationSeconds = 1860,
                    fileSizeBytes = 36_400_000L,
                    caption = "بررسی کتاب پرفروش دانیل کانمن و نحوه تصمیم‌گیری شهودی در برابر منطقی در موقعیت‌های حساس کاری و فردی.",
                    category = "پادکست",
                    playlistId = podcastPlaylistId,
                    isFavorite = false,
                    sourcePlatform = "TELEGRAM",
                    customTags = "پادکست, کتاب صوتی, روانشناسی",
                    mediaType = "PODCAST"
                ),
                VideoItem(
                    title = "آموزش ادیت ریلز سینمایی با موبایل در ۳۰ ثانیه 🎬",
                    channelTitle = "دنیای تکنولوژی (اینستاگرام)",
                    channelUsername = "tech_persian",
                    telegramUrl = "https://instagram.com/reel/C8Kx99",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    thumbnailUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&auto=format&fit=crop&q=80",
                    durationSeconds = 45,
                    fileSizeBytes = 12_800_000L,
                    caption = "آموزش ترفندهای وایرال شدن ریلز، کات‌های ریتمیک و اصلاح رنگ با اپلیکیشن گوشی #اینستاگرام #ریلز #ادیت",
                    category = "آموزشی",
                    playlistId = instaPlaylistId,
                    isFavorite = true,
                    sourcePlatform = "INSTAGRAM",
                    customTags = "اینستاگرام, ریلز, ادیت حرفه‌ای, اکسپلور",
                    mediaType = "VIDEO"
                ),
                VideoItem(
                    title = "آموزش معماری مدرن Compose و بهینه‌سازی کدهای اندروید",
                    channelTitle = "دنیای تکنولوژی و برنامه‌نویسی",
                    channelUsername = "tech_persian",
                    telegramUrl = "https://t.me/tech_persian/102",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    thumbnailUrl = "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&auto=format&fit=crop&q=80",
                    durationSeconds = 240,
                    fileSizeBytes = 31_200_000L,
                    caption = "بررسی الگوهای استاندارد MVVM، مدیریت حافظه در جت‌پک کامپوز و اتصال پایگاه‌داده محلی روم.",
                    category = "آموزشی",
                    playlistId = techPlaylistId,
                    isFavorite = false,
                    sourcePlatform = "TELEGRAM",
                    customTags = "کاتلین, کامپوز, روم",
                    mediaType = "VIDEO"
                ),
                VideoItem(
                    title = "سفر به اعماق سیاهچاله‌ها و افق رویداد کیهانی",
                    channelTitle = "مستندهای علمی و طبیعت",
                    channelUsername = "science_doc",
                    telegramUrl = "https://t.me/science_doc/45",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
                    thumbnailUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&auto=format&fit=crop&q=80",
                    durationSeconds = 310,
                    fileSizeBytes = 42_800_000L,
                    caption = "تصاویر شگفت‌انگیز تلسکوپ جیمز وب و رازهای ناگفته تولد و مرگ ستارگان در کهکشان‌های دوردست.",
                    category = "مستند و طبیعت",
                    playlistId = documentaryPlaylistId,
                    isFavorite = false,
                    sourcePlatform = "TELEGRAM",
                    customTags = "نجوم, فضا, کیهان",
                    mediaType = "VIDEO"
                ),
                VideoItem(
                    title = "کالبدشکافی فیلم‌نامه‌نویسی و کارگردانی در آثار کریستوفر نولان",
                    channelTitle = "سینما و فیلم‌های برتر",
                    channelUsername = "cinema_hub",
                    telegramUrl = "https://t.me/cinema_hub/88",
                    streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    thumbnailUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=600&auto=format&fit=crop&q=80",
                    durationSeconds = 150,
                    fileSizeBytes = 19_600_000L,
                    caption = "تحلیل فرم و مفهوم در سینمای مدرن، نحوه روایت غیرخطی داستان و استفاده از موسیقی متن برای القای تنش روانی.",
                    category = "سینما و فیلم",
                    playlistId = cinemaPlaylistId,
                    isFavorite = false,
                    sourcePlatform = "TELEGRAM",
                    customTags = "نولان, سینما, فیلمنامه",
                    mediaType = "VIDEO"
                )
            )
            videoDao.insertVideos(sampleVideos)
        }
    }

    suspend fun addMediaLinks(rawInput: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val lines = rawInput.split("\n", ",").map { it.trim() }.filter { it.isNotBlank() }
            if (lines.isEmpty()) return@withContext Result.failure(Exception("لینکی وارد نشده است"))

            val activeProxy = proxyRepository.getActiveProxy()
            var addedCount = 0

            for (line in lines) {
                // Check if Instagram link
                val parsedInstagram = InstagramScraperService.parseInstagramUrl(line)
                if (parsedInstagram != null) {
                    val scrapedVideos = InstagramScraperService.fetchInstagramVideos(parsedInstagram, activeProxy)
                    if (scrapedVideos.isNotEmpty()) {
                        // Check or create Instagram Playlist
                        var instaPlaylist = playlistDao.getPlaylistByChannel(parsedInstagram.username)
                        if (instaPlaylist == null) {
                            val playlistName = if (parsedInstagram.isProfileCollection) {
                                "پلی‌لیست پیج @${parsedInstagram.username}"
                            } else {
                                "مجموعه ریلزهای @${parsedInstagram.username}"
                            }
                            val newPlId = playlistDao.insertPlaylist(
                                Playlist(
                                    name = playlistName,
                                    description = "ویدیوها و ریلزهای استخراج شده از اینستاگرام @${parsedInstagram.username}",
                                    channelUsername = parsedInstagram.username,
                                    isFavorite = false,
                                    colorHex = "#E1306C",
                                    iconName = "instagram",
                                    sourcePlatform = "INSTAGRAM"
                                )
                            )
                            instaPlaylist = playlistDao.getPlaylistById(newPlId)
                        }

                        val preparedVideos = scrapedVideos.map { video ->
                            video.copy(playlistId = instaPlaylist?.id, sourcePlatform = "INSTAGRAM")
                        }

                        videoDao.insertVideos(preparedVideos)
                        addedCount += preparedVideos.size
                    }
                    continue
                }

                // Check if Telegram link
                val parsedTelegram = TelegramScraperService.parseTelegramUrl(line)
                if (parsedTelegram != null) {
                    val scrapedVideos = TelegramScraperService.fetchTelegramVideo(parsedTelegram, activeProxy)
                    if (scrapedVideos.isNotEmpty()) {
                        // Check if a playlist for this channel already exists, or create one
                        var channelPlaylist = playlistDao.getPlaylistByChannel(parsedTelegram.channelUsername)
                        if (channelPlaylist == null) {
                            val newPlId = playlistDao.insertPlaylist(
                                Playlist(
                                    name = "پلی‌لیست کانال @${parsedTelegram.channelUsername}",
                                    description = "تمام ویدیوهای استخراج شده از کانال تلگرام @${parsedTelegram.channelUsername}",
                                    channelUsername = parsedTelegram.channelUsername,
                                    isFavorite = false,
                                    colorHex = "#2AABEE",
                                    iconName = "playlist",
                                    sourcePlatform = "TELEGRAM"
                                )
                            )
                            channelPlaylist = playlistDao.getPlaylistById(newPlId)
                        }

                        val preparedVideos = scrapedVideos.map { video ->
                            video.copy(playlistId = channelPlaylist?.id, sourcePlatform = "TELEGRAM")
                        }

                        videoDao.insertVideos(preparedVideos)
                        addedCount += preparedVideos.size
                    }
                    continue
                }

                // Direct video link
                if (line.startsWith("http://") || line.startsWith("https://")) {
                    val directVideo = VideoItem(
                        title = "ویدیوی لینک مستقیم (${line.takeLast(25)})",
                        channelTitle = "لینک مستقیم",
                        channelUsername = "direct_link",
                        telegramUrl = line,
                        streamUrl = line,
                        thumbnailUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=600&auto=format&fit=crop&q=80",
                        durationSeconds = 120,
                        fileSizeBytes = 20_000_000L,
                        caption = "ویدیوی اضافه شده از طریق لینک مستقیم: $line",
                        category = "عمومی",
                        sourcePlatform = "DIRECT",
                        customTags = "لینک مستقیم"
                    )
                    videoDao.insertVideo(directVideo)
                    addedCount++
                }
            }

            if (addedCount > 0) {
                Result.success(addedCount)
            } else {
                Result.failure(Exception("فرمت لینک نامعتبر است. نمونه تلگرام: t.me/channel/123 یا اینستاگرام: instagram.com/reel/xxx"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVideoTagsAndCategory(videoId: Long, customTags: String, category: String) = withContext(Dispatchers.IO) {
        videoDao.updateTagsAndCategory(videoId, customTags.trim(), category.trim())
    }

    suspend fun updateLastPlayed(videoId: Long) = withContext(Dispatchers.IO) {
        videoDao.updateLastPlayed(videoId, System.currentTimeMillis())
    }

    suspend fun startSmartDownload(video: VideoItem, quality: QualityLevel) = withContext(Dispatchers.IO) {
        val activeProxy = proxyRepository.getActiveProxy()
        downloadManager.startDownload(video, quality, activeProxy)
    }

    suspend fun toggleFavoriteVideo(videoId: Long, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        videoDao.setFavorite(videoId, !currentFavorite)
    }

    suspend fun toggleFavoriteChannelPlaylist(channelUsername: String, makeFavorite: Boolean) = withContext(Dispatchers.IO) {
        playlistDao.setChannelPlaylistFavorite(channelUsername, makeFavorite)
        videoDao.setChannelVideosFavorite(channelUsername, makeFavorite)
    }

    suspend fun toggleFavoritePlaylist(playlistId: Long, makeFavorite: Boolean) = withContext(Dispatchers.IO) {
        playlistDao.setFavorite(playlistId, makeFavorite)
        videoDao.setPlaylistVideosFavorite(playlistId, makeFavorite)
    }

    suspend fun createPlaylist(
        name: String,
        description: String,
        channelUsername: String? = null,
        sourcePlatform: String = "CUSTOM",
        colorHex: String = "#2AABEE"
    ): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(
            Playlist(
                name = name.ifBlank { "پلی‌لیست جدید" },
                description = description,
                channelUsername = channelUsername,
                isFavorite = false,
                colorHex = colorHex,
                sourcePlatform = sourcePlatform
            )
        )
    }

    suspend fun deletePlaylist(playlistId: Long) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylistById(playlistId)
    }

    suspend fun setVideoPlaylist(videoId: Long, playlistId: Long?) = withContext(Dispatchers.IO) {
        videoDao.setVideoPlaylist(videoId, playlistId)
    }

    suspend fun deleteVideo(video: VideoItem) = withContext(Dispatchers.IO) {
        downloadManager.deleteLocalFile(video)
        videoDao.deleteVideoById(video.id)
    }

    suspend fun deleteDownloadedFileOnly(video: VideoItem) = withContext(Dispatchers.IO) {
        downloadManager.deleteLocalFile(video)
    }

    suspend fun performSmartStorageClean(targetBytesToFree: Long = 0L): Pair<Int, Long> = withContext(Dispatchers.IO) {
        storageManager.smartCleanStorage(targetBytesToFree)
    }
}
