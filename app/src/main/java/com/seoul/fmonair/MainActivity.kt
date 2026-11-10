package com.seoul.fmonair

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RadioPlaybackService.resetExitSignal()

        lifecycleScope.launch {
            RadioPlaybackService.shouldExitAppFlow.collect { shouldExit ->
                if (shouldExit) {
                    RadioPlaybackService.resetExitSignal()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        finishAndRemoveTask()
                    } else {
                        finishAffinity()
                    }
                }
            }
        }

        setContent {
            val darkGlassColorScheme = darkColorScheme(
                primary = Color(0xFF38BDF8),
                secondary = Color(0xFF818CF8),
                background = Color(0xFF0B1120),
                surface = Color(0xFF111827),
                onBackground = Color(0xFFF8FAFC),
                onSurface = Color(0xFFE2E8F0)
            )
            MaterialTheme(colorScheme = darkGlassColorScheme) {
                SeoulRadioAppScreen()
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (RadioPlaybackService.isPlayingFlow.value) {
            val ensureIntent = Intent(this, RadioPlaybackService::class.java).apply {
                action = RadioPlaybackService.ACTION_ENSURE_NOTIFICATION
            }
            startService(ensureIntent)
            Toast.makeText(
                this,
                "상단 알림창에서 라디오 재생 중지 및 앱 종료가 가능합니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
fun SeoulRadioAppScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var stations by remember { mutableStateOf(StationRepository.loadStations(context)) }
    // ★ 즐겨찾기 ID를 순서가 있는 List<String>으로 관리
    var favoriteIds by remember { mutableStateOf(StationRepository.getFavoriteIds(context)) }
    var liveProgramMap by remember { mutableStateOf(StationRepository.loadCachedProgramMap(context)) }

    val currentStation by RadioPlaybackService.currentStationFlow.collectAsState()
    val currentProgram by RadioPlaybackService.currentProgramFlow.collectAsState()
    val isPlaying by RadioPlaybackService.isPlayingFlow.collectAsState()
    val streamStatus by RadioPlaybackService.streamStatusFlow.collectAsState()
    val liveIcyTitle by RadioPlaybackService.liveMetadataTitleFlow.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCategory by remember {
        mutableStateOf(
            if (StationRepository.getFavoriteIds(context).isNotEmpty()) {
                StationCategory.FAVORITE
            } else {
                StationCategory.ALL
            }
        )
    }
    var channelSearchQuery by remember { mutableStateOf("") }

    var isHeroCompact by remember { mutableStateOf(false) }

    val maxVol = remember { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
    var volumeLevel by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVol)
    }

    var selectedSleepOption by remember { mutableStateOf(SleepTimerOption.OFF) }
    var remainingSleepSeconds by remember { mutableIntStateOf(0) }

    var showSleepTimerModal by remember { mutableStateOf(false) }
    var showCustomUrlModal by remember { mutableStateOf(false) }
    var showApkGuideModal by remember { mutableStateOf(false) }

    var showMelonWebModal by remember { mutableStateOf(false) }
    var melonSearchKeyword by remember { mutableStateOf("") }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && isPlaying) {
            val ensureIntent = Intent(context, RadioPlaybackService::class.java).apply {
                action = RadioPlaybackService.ACTION_ENSURE_NOTIFICATION
            }
            context.startService(ensureIntent)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        // 앱 시작 시 사용자 커스텀 프로그램명은 유지하면서 나머지 즐겨찾기 프로그램 정보 최신화
        val refreshed = StationRepository.updateFavoriteStationsPrograms(
            context = context,
            currentStationId = currentStation.id,
            overwriteUserCustom = false
        )
        liveProgramMap = refreshed
        refreshed[currentStation.id]?.let { latest ->
            RadioPlaybackService.refreshProgramTitleNow(context, latest)
        }
    }

    val defaultLyricLines = remember {
        listOf(
            "재생 중에는 화면 최상단에 대형 [🎵 BGM 노래 인식] 버튼이 항상 고정됩니다",
            "즐겨찾기 채널 카드의 [◀ / ▶] 또는 [설정]에서 즐겨찾기 순서를 조정할 수 있습니다",
            "우측 상단 [설정]에서 즐겨찾기 방송의 프로그램 이름을 직접 수정하거나 업데이트할 수 있습니다",
            "ACRCloud 정식 키가 기본 내장되어 있으며 설정에서 언제든 수정 가능합니다"
        )
    }

    var currentSong by remember {
        mutableStateOf(
            RecognizedSong(
                id = "init_song",
                title = "BGM 인식 또는 가사 검색을 실행해 주세요",
                artist = "수도권 온에어 FM 라디오",
                album = "ACRCloud / AudD 핑거프린팅 준비 완료",
                stationName = currentStation.name,
                frequencyText = currentStation.frequencyText,
                recognizedAt = "대기 중",
                plainLyrics = defaultLyricLines.joinToString(separator = "\n"),
                syncedLyrics = defaultLyricLines.mapIndexed { idx, line -> LyricLine(idx * 3.5f, line) },
                sourceMethod = "ACRCloud 정밀 핑거프린팅"
            )
        )
    }
    var isRecognizingAudio by remember { mutableStateOf(false) }
    var lyricsSearchQuery by remember { mutableStateOf("") }
    var lrcSearchResults by remember { mutableStateOf<List<RecognizedSong>>(emptyList()) }
    var isSearchingLyrics by remember { mutableStateOf(false) }
    var isLyricsFocusMode by remember { mutableStateOf(false) }
    var activeLyricLineIndex by remember { mutableIntStateOf(0) }

    var savedSongsHistory by remember {
        mutableStateOf<List<RecognizedSong>>(SongAndLyricsService.loadSavedSongs(context))
    }

    fun buildCleanMelonKeyword(song: RecognizedSong): String {
        val isPlaceholder = song.id == "init_song" ||
            song.title.startsWith("현재 곡 미감지") ||
            song.title.startsWith("BGM 인식")
        if (isPlaceholder) {
            return lyricsSearchQuery.trim()
        }
        return "${song.title} ${song.artist}".trim()
    }

    fun copySongTitleAndArtist(song: RecognizedSong, showToast: Boolean = true) {
        val isPlaceholder = song.id == "init_song" ||
            song.title.startsWith("현재 곡 미감지") ||
            song.title.startsWith("BGM 인식")
        val copyText = if (isPlaceholder) {
            lyricsSearchQuery.trim().ifBlank { "${song.title} - ${song.artist}" }
        } else {
            "${song.title} - ${song.artist}"
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("song_title_artist", copyText))
        if (showToast) {
            Toast.makeText(context, "복사 완료: $copyText", Toast.LENGTH_SHORT).show()
        }
    }

    fun openMelonSearchModal(customKeyword: String? = null) {
        val keyword = (customKeyword ?: buildCleanMelonKeyword(currentSong)).trim()
        melonSearchKeyword = keyword
        if (keyword.isNotBlank()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("melon_search", keyword))
        }
        showMelonWebModal = true
    }

    LaunchedEffect(liveIcyTitle) {
        val icy = liveIcyTitle
        if (!icy.isNullOrBlank() && icy.contains("-")) {
            val recognized = SongAndLyricsService.recognizeCurrentRadioSong(
                context = context,
                station = currentStation,
                icyMetadataTitle = icy
            )
            if (!recognized.title.startsWith("현재 곡 미감지")) {
                currentSong = recognized
                lyricsSearchQuery = "${recognized.title} ${recognized.artist}".trim()
                savedSongsHistory = SongAndLyricsService.saveSongToHistory(context, recognized)
            }
        }
    }

    LaunchedEffect(currentSong.id, isPlaying) {
        activeLyricLineIndex = 0
        while (true) {
            delay(3500)
            if (currentSong.syncedLyrics.isNotEmpty()) {
                activeLyricLineIndex = (activeLyricLineIndex + 1) % currentSong.syncedLyrics.size
            }
        }
    }

    LaunchedEffect(remainingSleepSeconds) {
        if (remainingSleepSeconds > 0) {
            delay(1000)
            remainingSleepSeconds -= 1
            if (remainingSleepSeconds == 0) {
                selectedSleepOption = SleepTimerOption.OFF
                if (isPlaying) {
                    val stopIntent = Intent(context, RadioPlaybackService::class.java).apply {
                        action = RadioPlaybackService.ACTION_STOP_PLAYBACK
                    }
                    context.startService(stopIntent)
                }
                Toast.makeText(context, "취침예약 시간이 종료되어 라디오를 자동으로 정지했습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun playStation(station: RadioStation) {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_PLAY_STATION
            putExtra(RadioPlaybackService.EXTRA_STATION_ID, station.id)
        }
        context.startService(intent)
    }

    fun togglePlayPause() {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_TOGGLE_PLAY
        }
        context.startService(intent)
    }

    fun stopPlayback() {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_STOP_PLAYBACK
        }
        context.startService(intent)
    }

    fun exitProgram() {
        val intent = Intent(context, RadioPlaybackService::class.java).apply {
            action = RadioPlaybackService.ACTION_EXIT_APP
        }
        context.startService(intent)
    }

    fun runSongRecognition() {
        if (isRecognizingAudio) return
        isRecognizingAudio = true
        Toast.makeText(context, "5.5초 고감도 녹음 및 ACRCloud 핑거프린팅 분석 중...", Toast.LENGTH_SHORT).show()
        coroutineScope.launch {
            val recognized = SongAndLyricsService.recognizeCurrentRadioSong(
                context = context,
                station = currentStation,
                icyMetadataTitle = liveIcyTitle
            )
            currentSong = recognized
            if (!recognized.title.startsWith("현재 곡 미감지")) {
                lyricsSearchQuery = "${recognized.title} ${recognized.artist}".trim()
                savedSongsHistory = SongAndLyricsService.saveSongToHistory(context, recognized)
                Toast.makeText(context, "곡 인식 성공: ${recognized.title} - ${recognized.artist}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "음원이 감지되지 않아 가사/멜론 검색 탭으로 이동합니다.", Toast.LENGTH_SHORT).show()
            }
            isRecognizingAudio = false
            selectedTabIndex = 1
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        runSongRecognition()
    }

    fun triggerAudioRecognitionWithPermission() {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            runSongRecognition()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // ★ 사용자가 지정한 즐겨찾기 순서(favoriteIds.indexOf)대로 정확히 정렬
    val filteredStations = stations
        .filter { station ->
            val matchesCategory = when (selectedCategory) {
                StationCategory.ALL -> true
                StationCategory.FAVORITE -> favoriteIds.contains(station.id)
                else -> station.category == selectedCategory
            }
            val q = channelSearchQuery.trim()
            val latestProg = liveProgramMap[station.id] ?: StationRepository.getLatestProgramTitle(context, station)
            val matchesSearch = q.isEmpty() ||
                station.name.contains(q, ignoreCase = true) ||
                station.frequencyText.contains(q, ignoreCase = true) ||
                station.defaultProgram.contains(q, ignoreCase = true) ||
                latestProg.contains(q, ignoreCase = true) ||
                station.broadcaster.contains(q, ignoreCase = true)

            matchesCategory && matchesSearch
        }
        .sortedWith(
            compareBy<RadioStation> { station ->
                val favIndex = favoriteIds.indexOf(station.id)
                if (favIndex >= 0) favIndex else Int.MAX_VALUE
            }
        )

    val orderedCategories = remember {
        listOf(
            StationCategory.FAVORITE,
            StationCategory.ALL,
            StationCategory.KBS,
            StationCategory.MBC,
            StationCategory.SBS,
            StationCategory.NEWS_EDU,
            StationCategory.MUSIC_RELIGION
        )
    }

    val activeHeroProgram = liveProgramMap[currentStation.id]?.takeIf { it.isNotBlank() }
        ?: currentProgram.ifBlank { StationRepository.getLatestProgramTitle(context, currentStation) }

    Scaffold(
        containerColor = Color(0xFF0B1120),
        topBar = {
            TopHeaderBar(
                isPlaying = isPlaying,
                currentStation = currentStation,
                currentProgram = activeHeroProgram,
                streamStatus = streamStatus,
                remainingSleepSeconds = remainingSleepSeconds,
                isRecognizingAudio = isRecognizingAudio,
                onTogglePlay = { togglePlayPause() },
                onStopPlay = { stopPlayback() },
                onExitApp = { exitProgram() },
                onRecognizeAudio5Sec = { triggerAudioRecognitionWithPermission() },
                onOpenMelonSearch = { openMelonSearchModal() },
                onOpenSleepModal = { showSleepTimerModal = true },
                onOpenCustomUrlModal = { showCustomUrlModal = true },
                onOpenApkGuideModal = { showApkGuideModal = true }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0B1120), Color(0xFF111827), Color(0xFF0F172A))
                    )
                )
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 36.dp)
        ) {
            if (!isLyricsFocusMode || selectedTabIndex != 1) {
                item {
                    NowPlayingHeroCard(
                        station = currentStation,
                        currentProgram = activeHeroProgram,
                        isPlaying = isPlaying,
                        streamStatus = streamStatus,
                        isFavorite = favoriteIds.contains(currentStation.id),
                        volumeLevel = volumeLevel,
                        isRecognizingAudio = isRecognizingAudio,
                        isCompact = isHeroCompact,
                        onToggleCompact = { isHeroCompact = !isHeroCompact },
                        onTogglePlay = { togglePlayPause() },
                        onStopPlay = { stopPlayback() },
                        onPrevChannel = {
                            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                                action = RadioPlaybackService.ACTION_PREV_STATION
                            }
                            context.startService(intent)
                        },
                        onNextChannel = {
                            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                                action = RadioPlaybackService.ACTION_NEXT_STATION
                            }
                            context.startService(intent)
                        },
                        onToggleFavorite = {
                            favoriteIds = StationRepository.toggleFavorite(context, currentStation.id)
                        },
                        onVolumeChange = { newVol ->
                            volumeLevel = newVol
                            val targetVol = (newVol * maxVol).toInt()
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, 0)
                        },
                        onJumpToLyrics = {
                            selectedTabIndex = 1
                        },
                        onRecognizeAudio5Sec = {
                            triggerAudioRecognitionWithPermission()
                        },
                        onOpenMelonSearch = {
                            openMelonSearchModal()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color(0xFF111827).copy(alpha = 0.95f),
                    contentColor = Color(0xFF38BDF8),
                    indicator = { tabPositions ->
                        SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Color(0xFF38BDF8)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = {
                            Text(
                                "수도권 채널 (${stations.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        icon = { Icon(Icons.Default.Radio, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = {
                            Text(
                                "가사·멜론검색",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        icon = { Icon(Icons.Default.Lyrics, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = {
                            Text(
                                "노래 보관함 (${savedSongsHistory.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (selectedTabIndex) {
                0 -> {
                    item {
                        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                orderedCategories.forEach { category ->
                                    val labelText = if (category == StationCategory.FAVORITE) {
                                        "★ ${category.displayName} (${favoriteIds.size})"
                                    } else {
                                        category.displayName
                                    }
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = { selectedCategory = category },
                                        label = { Text(labelText, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF2563EB),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color(0xFF1E293B),
                                            labelColor = Color(0xFFCBD5E1)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = channelSearchQuery,
                                onValueChange = { channelSearchQuery = it },
                                placeholder = { Text("채널명, 주파수(MHz), 프로그램명 검색...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedContainerColor = Color(0xFF111827),
                                    unfocusedContainerColor = Color(0xFF111827),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    if (selectedCategory == StationCategory.FAVORITE && filteredStations.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 36.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "즐겨찾기에 등록된 방송이 없습니다.",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { selectedCategory = StationCategory.ALL },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                    ) {
                                        Text("전체 방송 보기", color = Color.White)
                                    }
                                }
                            }
                        }
                    } else {
                        val rows = filteredStations.chunked(2)
                        items(rows) { rowPair ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowPair.forEach { station ->
                                    val isCurrent = station.id == currentStation.id
                                    val favIdx = favoriteIds.indexOf(station.id)
                                    val isFav = favIdx >= 0
                                    val displayProgram = liveProgramMap[station.id]?.takeIf { it.isNotBlank() }
                                        ?: if (isCurrent && currentProgram.isNotBlank()) {
                                            currentProgram
                                        } else {
                                            StationRepository.getLatestProgramTitle(context, station)
                                        }
                                    Box(modifier = Modifier.weight(1f)) {
                                        StationGridCard(
                                            station = station,
                                            programTitle = displayProgram,
                                            isSelected = isCurrent,
                                            isPlaying = isPlaying,
                                            isFav = isFav,
                                            favoriteOrderNumber = if (isFav) favIdx + 1 else null,
                                            canMoveUp = isFav && favIdx > 0,
                                            canMoveDown = isFav && favIdx < favoriteIds.size - 1,
                                            onSelectStation = { playStation(station) },
                                            onToggleFavorite = {
                                                favoriteIds = StationRepository.toggleFavorite(context, station.id)
                                            },
                                            onMoveFavoriteUp = {
                                                favoriteIds = StationRepository.moveFavoriteStation(context, station.id, moveUp = true)
                                            },
                                            onMoveFavoriteDown = {
                                                favoriteIds = StationRepository.moveFavoriteStation(context, station.id, moveUp = false)
                                            }
                                        )
                                    }
                                }
                                if (rowPair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                1 -> {
                    item {
                        RealtimeLyricsSection(
                            currentStation = currentStation,
                            currentProgram = activeHeroProgram,
                            currentSong = currentSong,
                            activeLyricIndex = activeLyricLineIndex,
                            isFocusMode = isLyricsFocusMode,
                            searchQuery = lyricsSearchQuery,
                            isSearching = isSearchingLyrics,
                            searchResults = lrcSearchResults,
                            onToggleFocusMode = { isLyricsFocusMode = !isLyricsFocusMode },
                            onSearchQueryChange = { lyricsSearchQuery = it },
                            onExecuteLrcSearch = {
                                if (lyricsSearchQuery.isNotBlank()) {
                                    isSearchingLyrics = true
                                    coroutineScope.launch {
                                        lrcSearchResults = SongAndLyricsService.searchLyricsFromLrcLib(
                                            query = lyricsSearchQuery,
                                            stationName = currentStation.name,
                                            frequencyText = currentStation.frequencyText
                                        )
                                        isSearchingLyrics = false
                                    }
                                }
                            },
                            onOpenMelonWithQuery = { keyword ->
                                openMelonSearchModal(keyword)
                            },
                            onCopySongTitleArtist = {
                                copySongTitleAndArtist(currentSong, showToast = true)
                            },
                            onSelectSearchedSong = { chosen ->
                                currentSong = chosen
                                lyricsSearchQuery = "${chosen.title} ${chosen.artist}".trim()
                                savedSongsHistory = SongAndLyricsService.saveSongToHistory(context, chosen)
                                lrcSearchResults = emptyList()
                            }
                        )
                    }
                }

                2 -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "라디오에서 찾은 노래 보관함 (${savedSongsHistory.size}곡)",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (savedSongsHistory.isNotEmpty()) {
                                TextButton(onClick = {
                                    savedSongsHistory = SongAndLyricsService.clearAllHistory(context)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("전체 삭제", color = Color(0xFFEF4444), fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (savedSongsHistory.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp, horizontal = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "아직 저장된 노래 히스토리가 없습니다. 상단 'BGM 인식' 또는 가사 검색을 실행하면 한국어 제목으로 자동 저장됩니다.",
                                    color = Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        items(savedSongsHistory, key = { it.id }) { song ->
                            Surface(
                                color = Color(0xFF1E293B).copy(alpha = 0.75f),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 5.dp)
                                    .clickable {
                                        currentSong = song
                                        lyricsSearchQuery = "${song.title} ${song.artist}".trim()
                                        selectedTabIndex = 1
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${song.title} - ${song.artist}",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${song.stationName} (${song.frequencyText}) · 저장 시각: ${song.recognizedAt}",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "클릭하여 가사 보기 · ${song.sourceMethod}",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { copySongTitleAndArtist(song, showToast = true) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "노래 제목 - 가수 복사",
                                                tint = Color(0xFFCBD5E1),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        Surface(
                                            color = Color(0xFF00CD3C),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.clickable {
                                                openMelonSearchModal("${song.title} ${song.artist}")
                                            }
                                        ) {
                                            Text(
                                                text = "🍈 멜론",
                                                color = Color.Black,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                            )
                                        }

                                        IconButton(onClick = {
                                            savedSongsHistory = SongAndLyricsService.deleteSongFromHistory(context, song.id)
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "삭제", tint = Color(0xFF94A3B8))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSleepTimerModal) {
        SleepTimerDialog(
            currentOption = selectedSleepOption,
            remainingSeconds = remainingSleepSeconds,
            onSelectOption = { option ->
                selectedSleepOption = option
                remainingSleepSeconds = option.minutes * 60
                showSleepTimerModal = false
            },
            onDismiss = { showSleepTimerModal = false }
        )
    }

    if (showCustomUrlModal) {
        CustomStreamUrlDialog(
            context = context,
            station = currentStation,
            allStations = stations,
            favoriteIds = favoriteIds,
            currentProgramMap = liveProgramMap,
            onChangeFavoriteOrder = { newOrderedIds ->
                favoriteIds = StationRepository.saveFavoriteOrder(context, newOrderedIds)
            },
            onUpdateFavoritePrograms = { updatedMap ->
                liveProgramMap = updatedMap
                updatedMap[currentStation.id]?.let { latestTitle ->
                    RadioPlaybackService.refreshProgramTitleNow(context, latestTitle)
                }
            },
            onSaveSettings = { newUrl, newAuddToken, acrHost, acrKey, acrSecret, customEditedPrograms ->
                StationRepository.saveCustomStreamUrl(context, currentStation.id, newUrl)
                SongAndLyricsService.saveAuddApiToken(context, newAuddToken)
                SongAndLyricsService.saveAcrCloudConfig(context, acrHost, acrKey, acrSecret)
                val updatedMap = StationRepository.saveCustomProgramTitles(context, customEditedPrograms)
                liveProgramMap = updatedMap
                updatedMap[currentStation.id]?.let { latestTitle ->
                    RadioPlaybackService.refreshProgramTitleNow(context, latestTitle)
                }
                stations = StationRepository.loadStations(context)
                val updated = stations.find { it.id == currentStation.id } ?: currentStation
                playStation(updated)
                showCustomUrlModal = false
                Toast.makeText(context, "즐겨찾기 순서·프로그램 정보 및 ACRCloud 설정이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCustomUrlModal = false }
        )
    }

    if (showApkGuideModal) {
        ApkBuildGuideDialog(onDismiss = { showApkGuideModal = false })
    }

    if (showMelonWebModal) {
        MelonSearchWebDialog(
            initialKeyword = melonSearchKeyword,
            onDismiss = { showMelonWebModal = false }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MelonSearchWebDialog(
    initialKeyword: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchInput by remember(initialKeyword) { mutableStateOf(initialKeyword) }
    var mainWebViewRef by remember { mutableStateOf<WebView?>(null) }
    var popupWebViewRef by remember { mutableStateOf<WebView?>(null) }
    var containerRef by remember { mutableStateOf<FrameLayout?>(null) }

    fun buildMelonSearchUrl(keyword: String): String {
        val trimmed = keyword.trim()
        return if (trimmed.isNotBlank()) {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            "https://m2.melon.com/search/searchMcom.htm?q=$encoded"
        } else {
            "https://m2.melon.com/"
        }
    }

    fun injectKeywordIntoMelonSearchBox(webView: WebView?, keyword: String, autoSubmit: Boolean) {
        val clean = keyword.trim()
        if (webView == null || clean.isBlank()) return
        val escapedJsLiteral = JSONObject.quote(clean)
        val jsCode = """
            (function() {
                var kw = $escapedJsLiteral;
                var selectors = [
                    'input[type="search"]',
                    'input#top_search',
                    'input[name="searchKeyword"]',
                    'input[name="q"]',
                    'input[name="schText"]',
                    '.search_box input',
                    '.search_input input',
                    'input.input_text',
                    'input[placeholder*="검색"]',
                    'input[title*="검색"]'
                ];
                var targetInput = null;
                for (var i = 0; i < selectors.length; i++) {
                    var el = document.querySelector(selectors[i]);
                    if (el) {
                        targetInput = el;
                        break;
                    }
                }
                if (targetInput) {
                    targetInput.value = kw;
                    targetInput.setAttribute('value', kw);
                    targetInput.dispatchEvent(new Event('input', { bubbles: true }));
                    targetInput.dispatchEvent(new Event('change', { bubbles: true }));
                    if ($autoSubmit) {
                        var btn = document.querySelector('button.btn_search, .btn_search, button[type="submit"], a.btn_search');
                        if (btn && !window.__melonAutoSearched) {
                            window.__melonAutoSearched = true;
                            btn.click();
                        } else if (targetInput.form && !window.__melonAutoSearched) {
                            window.__melonAutoSearched = true;
                            targetInput.form.submit();
                        }
                    }
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(jsCode, null)
    }

    fun handleCustomSchemeUrl(ctx: Context, view: WebView?, url: String): Boolean {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return false
        }
        return try {
            if (url.startsWith("intent://")) {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                try {
                    ctx.startActivity(intent)
                } catch (_: Exception) {
                    val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                    if (!fallbackUrl.isNullOrBlank()) {
                        view?.loadUrl(fallbackUrl)
                    } else {
                        val pkg = intent.`package`
                        if (!pkg.isNullOrBlank()) {
                            ctx.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
                            )
                        }
                    }
                }
            } else {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
            true
        } catch (_: Exception) {
            true
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun configureMelonWebView(webView: WebView, ctx: Context, isPopup: Boolean = false) {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "Mozilla/5.0 (Linux; Android 14; SM-S928N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleCustomSchemeUrl(ctx, view, url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                CookieManager.getInstance().flush()
                if (!isPopup && url != null) {
                    if ((url == "https://m2.melon.com/" || url == "https://m.melon.com/") && searchInput.isNotBlank()) {
                        view?.loadUrl(buildMelonSearchUrl(searchInput))
                    } else if (searchInput.isNotBlank()) {
                        injectKeywordIntoMelonSearchBox(view, searchInput, autoSubmit = true)
                        view?.postDelayed({
                            injectKeywordIntoMelonSearchBox(view, searchInput, autoSubmit = false)
                        }, 600L)
                    }
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: Message?
            ): Boolean {
                val container = containerRef ?: return false
                val popup = WebView(ctx)
                configureMelonWebView(popup, ctx, isPopup = true)
                popup.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                container.addView(popup)
                popupWebViewRef = popup

                val transport = resultMsg?.obj as? WebView.WebViewTransport
                transport?.webView = popup
                resultMsg?.sendToTarget()
                return true
            }

            override fun onCloseWindow(window: WebView?) {
                super.onCloseWindow(window)
                window?.let { w ->
                    containerRef?.removeView(w)
                    w.destroy()
                }
                popupWebViewRef = null
                CookieManager.getInstance().flush()
                if (searchInput.isNotBlank()) {
                    mainWebViewRef?.loadUrl(buildMelonSearchUrl(searchInput))
                } else {
                    mainWebViewRef?.reload()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .border(1.5.dp, Color(0xFF00CD3C), RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0B1120)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111827))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (popupWebViewRef != null) {
                            IconButton(
                                onClick = {
                                    popupWebViewRef?.let { p ->
                                        containerRef?.removeView(p)
                                        p.destroy()
                                    }
                                    popupWebViewRef = null
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "팝업 닫기", tint = Color(0xFFFACC15))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Surface(
                            color = Color(0xFF00CD3C),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🍈 MELON",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (popupWebViewRef != null) "카카오/멜론 인증 중" else "멜론 검색 & 로그인",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFFEE500),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.clickable {
                                popupWebViewRef?.let { p ->
                                    containerRef?.removeView(p)
                                    p.destroy()
                                    popupWebViewRef = null
                                }
                                val retUrl = URLEncoder.encode(buildMelonSearchUrl(searchInput), "UTF-8")
                                mainWebViewRef?.loadUrl("https://member.melon.com/muid/mobile/login/login_inform.htm?returnPage=$retUrl")
                            }
                        ) {
                            Text(
                                text = "카카오·멜론 로그인",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        TextButton(
                            onClick = {
                                val q = searchInput.trim()
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(buildMelonSearchUrl(q))))
                                } catch (_: Exception) {
                                }
                            }
                        ) {
                            Text("멜론앱 열기", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "닫기", tint = Color.White)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111827))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        placeholder = { Text("노래 제목 가수 (자동 입력됨)", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00CD3C),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF0F172A),
                            unfocusedContainerColor = Color(0xFF0F172A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            popupWebViewRef?.let { p ->
                                containerRef?.removeView(p)
                                p.destroy()
                                popupWebViewRef = null
                            }
                            if (searchInput.isNotBlank()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("melon_search", searchInput.trim()))
                                mainWebViewRef?.loadUrl(buildMelonSearchUrl(searchInput))
                                injectKeywordIntoMelonSearchBox(mainWebViewRef, searchInput, autoSubmit = true)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00CD3C)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("검색창 자동입력·검색", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }
                }

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            val mainWv = WebView(ctx)
                            configureMelonWebView(mainWv, ctx, isPopup = false)
                            mainWv.layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            addView(mainWv)
                            mainWv.loadUrl(buildMelonSearchUrl(initialKeyword))

                            mainWebViewRef = mainWv
                            containerRef = this
                        }
                    }
                )
            }
        }
    }
}

/**
 * ★ 채널 그리드 카드: 즐겨찾기된 방송은 순서 번호(#1, #2...)와 함께 ◀ / ▶ 버튼으로 순서를 바로 조정 가능
 */
@Composable
fun StationGridCard(
    station: RadioStation,
    programTitle: String,
    isSelected: Boolean,
    isPlaying: Boolean,
    isFav: Boolean,
    favoriteOrderNumber: Int? = null,
    canMoveUp: Boolean = false,
    canMoveDown: Boolean = false,
    onSelectStation: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoveFavoriteUp: () -> Unit = {},
    onMoveFavoriteDown: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF1E3A8A).copy(alpha = 0.65f) else Color(0xFF1E293B).copy(alpha = 0.65f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelectStation() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(station.badgeColorHex),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = station.broadcaster,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (favoriteOrderNumber != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "#$favoriteOrderNumber",
                                color = Color(0xFFFACC15),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (isSelected && isPlaying) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = Color(0xFF16A34A),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "재생중",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFav) {
                        Text(
                            text = "◀",
                            color = if (canMoveUp) Color(0xFF38BDF8) else Color(0xFF475569),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .clickable(enabled = canMoveUp) { onMoveFavoriteUp() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "▶",
                            color = if (canMoveDown) Color(0xFF38BDF8) else Color(0xFF475569),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .clickable(enabled = canMoveDown) { onMoveFavoriteDown() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }

                    Icon(
                        imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "즐겨찾기",
                        tint = if (isFav) Color(0xFFFACC15) else Color(0xFF64748B),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onToggleFavorite() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = station.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = station.frequencyText,
                color = Color(0xFF38BDF8),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "🎙️ $programTitle",
                color = if (isSelected) Color(0xFFFDE047) else Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (station.resolverType) {
                    StreamResolverType.STATIC_HLS -> "고정 HLS (.m3u8)"
                    else -> "실시간 토큰 Resolver"
                },
                color = if (isSelected && isPlaying) Color(0xFF4ADE80) else Color(0xFF64748B),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun TopHeaderBar(
    isPlaying: Boolean,
    currentStation: RadioStation,
    currentProgram: String,
    streamStatus: String,
    remainingSleepSeconds: Int,
    isRecognizingAudio: Boolean,
    onTogglePlay: () -> Unit,
    onStopPlay: () -> Unit,
    onExitApp: () -> Unit,
    onRecognizeAudio5Sec: () -> Unit,
    onOpenMelonSearch: () -> Unit,
    onOpenSleepModal: () -> Unit,
    onOpenCustomUrlModal: () -> Unit,
    onOpenApkGuideModal: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "topLivePulse")
    val dotAlpha by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    val isBuffering = streamStatus.contains("해석") || streamStatus.contains("버퍼링") || streamStatus.contains("전환")

    Surface(
        color = Color(0xFF0B1120).copy(alpha = 0.98f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(30.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "앱 아이콘",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "수도권 FM",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = when {
                            isPlaying -> Color(0xFF065F46)
                            isBuffering -> Color(0xFF1E3A8A)
                            else -> Color(0xFF1E293B)
                        },
                        modifier = Modifier.border(
                            width = 1.dp,
                            color = when {
                                isPlaying -> Color(0xFF4ADE80)
                                isBuffering -> Color(0xFF38BDF8)
                                else -> Color(0xFF334155)
                            },
                            shape = RoundedCornerShape(50)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .alpha(if (isPlaying || isBuffering) dotAlpha else 1f)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isPlaying -> Color(0xFF4ADE80)
                                            isBuffering -> Color(0xFF38BDF8)
                                            else -> Color(0xFF64748B)
                                        }
                                    )
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = when {
                                    isPlaying -> "LIVE 재생중"
                                    isBuffering -> "연결중..."
                                    else -> "정지됨"
                                },
                                color = when {
                                    isPlaying -> Color(0xFF4ADE80)
                                    isBuffering -> Color(0xFF38BDF8)
                                    else -> Color(0xFF94A3B8)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (remainingSleepSeconds > 0) Color(0xFF1D4ED8) else Color(0xFF1E293B),
                        modifier = Modifier
                            .clickable { onOpenSleepModal() }
                            .padding(end = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = "취침예약",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (remainingSleepSeconds > 0) {
                                    String.format("%02d:%02d", remainingSleepSeconds / 60, remainingSleepSeconds % 60)
                                } else {
                                    "취침"
                                },
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenCustomUrlModal,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "설정 및 즐겨찾기 순서·프로그램 편집",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenApkGuideModal,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = "사용 가이드",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    IconButton(
                        onClick = onExitApp,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "프로그램 종료",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isPlaying || isBuffering || isRecognizingAudio) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPlaying) Color(0xFF064E3B).copy(alpha = 0.55f) else Color(0xFF1E293B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .border(
                                width = 1.dp,
                                color = if (isPlaying) Color(0xFF10B981).copy(alpha = 0.6f) else Color(0xFF38BDF8).copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onTogglePlay() }
                            ) {
                                FiveBarAudioEqualizer(isPlaying = isPlaying)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (isPlaying) Color(0xFFEF4444) else Color(0xFF2563EB),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isPlaying) "ON-AIR" else "BUFFER",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${currentStation.name} (${currentStation.frequencyText}) · ${currentProgram.ifBlank { currentStation.defaultProgram }}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.clickable { onStopPlay() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = "재생 중지",
                                            tint = Color(0xFFFACC15),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "중지",
                                            color = Color(0xFFFACC15),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(5.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF7F1D1D),
                                    modifier = Modifier.clickable { onExitApp() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "앱 종료",
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "종료",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onRecognizeAudio5Sec,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .border(
                                    width = 1.5.dp,
                                    color = Color(0xFF38BDF8),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRecognizingAudio) Color(0xFF1E3A8A) else Color(0xFF0284C7)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            if (isRecognizingAudio) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "라디오 음원 분석 중 (5.5초)...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "🎵 지금 나오는 노래(BGM) 인식",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Button(
                            onClick = onOpenMelonSearch,
                            modifier = Modifier.height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00CD3C)),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp)
                        ) {
                            Text(
                                text = "🍈 멜론",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NowPlayingHeroCard(
    station: RadioStation,
    currentProgram: String,
    isPlaying: Boolean,
    streamStatus: String,
    isFavorite: Boolean,
    volumeLevel: Float,
    isRecognizingAudio: Boolean,
    isCompact: Boolean,
    onToggleCompact: () -> Unit,
    onTogglePlay: () -> Unit,
    onStopPlay: () -> Unit,
    onPrevChannel: () -> Unit,
    onNextChannel: () -> Unit,
    onToggleFavorite: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onJumpToLyrics: () -> Unit,
    onRecognizeAudio5Sec: () -> Unit,
    onOpenMelonSearch: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .border(
                width = if (isPlaying) 1.5.dp else 1.dp,
                color = if (isPlaying) Color(0xFF38BDF8).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(22.dp)
            ),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF1E293B).copy(alpha = 0.72f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(station.badgeColorHex),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = station.broadcaster,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = station.scheduleHours,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format("%.1f", station.frequencyMhz),
                            color = Color(0xFF38BDF8),
                            fontSize = if (isCompact) 22.sp else 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MHz",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = station.name,
                            color = Color.White,
                            fontSize = if (isCompact) 16.sp else 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isPlaying) Color(0xFF38BDF8).copy(alpha = 0.45f) else Color(0xFF334155),
                                shape = RoundedCornerShape(10.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (isPlaying) Color(0xFFEF4444) else Color(0xFF475569),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isPlaying) "ON-AIR 방송" else "편성 프로그램",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentProgram.ifBlank { station.defaultProgram },
                                color = Color(0xFFFDE047),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isPlaying) "● 실시간 스트리밍 재생 중 ($streamStatus)" else "상태: $streamStatus",
                        color = if (isPlaying) Color(0xFF4ADE80) else Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FiveBarAudioEqualizer(isPlaying = isPlaying)
                        IconButton(onClick = onToggleFavorite, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "즐겨찾기",
                                tint = if (isFavorite) Color(0xFFFACC15) else Color(0xFF94A3B8)
                            )
                        }
                        IconButton(onClick = onToggleCompact, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (isCompact) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                contentDescription = "접기/펼치기",
                                tint = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = !isCompact) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    AnalogFmFrequencyDial(currentFrequencyMhz = station.frequencyMhz)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPrevChannel,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "이전 채널", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    if (isPlaying) {
                                        listOf(Color(0xFF059669), Color(0xFF0284C7))
                                    } else {
                                        listOf(Color(0xFF2563EB), Color(0xFF06B6D4))
                                    }
                                )
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "재생/일시정지",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onStopPlay,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "재생 중지", tint = Color(0xFFFACC15))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onNextChannel,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A))
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "다음 채널", tint = Color.White)
                    }
                }

                Button(
                    onClick = onJumpToLyrics,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        Icons.Default.Lyrics,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("가사 탭", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRecognizeAudio5Sec,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .border(
                            width = 1.5.dp,
                            color = Color(0xFF38BDF8),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecognizingAudio) Color(0xFF1E3A8A) else Color(0xFF2563EB)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (isRecognizingAudio) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "음원 핑거프린팅 분석 중...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color(0xFFFDE047),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🎵 BGM 노래 인식 (클릭)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Button(
                    onClick = onOpenMelonSearch,
                    modifier = Modifier.height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00CD3C)),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(
                        text = "🍈 멜론 검색",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            AnimatedVisibility(visible = !isCompact) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "볼륨",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = volumeLevel,
                        onValueChange = onVolumeChange,
                        modifier = Modifier
                            .weight(1f)
                            .height(22.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF38BDF8),
                            activeTrackColor = Color(0xFF38BDF8),
                            inactiveTrackColor = Color(0xFF0F172A)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(volumeLevel * 100).toInt()}%",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AnalogFmFrequencyDial(currentFrequencyMhz: Float) {
    val clamped = currentFrequencyMhz.coerceIn(88.0f, 108.0f)
    val fraction by animateFloatAsState(
        targetValue = (clamped - 88.0f) / 20.0f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "dialPointer"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF090D16))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("88", "92", "96", "100", "104", "108 MHz").forEach { label ->
                Text(
                    text = label,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
        ) {
            val width = size.width
            val height = size.height
            val tickCount = 40

            for (i in 0..tickCount) {
                val x = (i.toFloat() / tickCount) * width
                val isMajor = i % 4 == 0
                val tickTop = if (isMajor) 2f else height * 0.35f
                drawLine(
                    color = if (isMajor) Color(0xFF94A3B8) else Color(0xFF475569),
                    start = Offset(x, tickTop),
                    end = Offset(x, height - 2f),
                    strokeWidth = if (isMajor) 2f else 1f
                )
            }

            val needleX = fraction * width
            drawLine(
                color = Color(0xFFEF4444),
                start = Offset(needleX, 0f),
                end = Offset(needleX, height),
                strokeWidth = 4.5f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color(0xFFFACC15),
                radius = 5f,
                center = Offset(needleX, height / 2f)
            )
        }
    }
}

@Composable
fun FiveBarAudioEqualizer(isPlaying: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "eqTransition")
    val durations = listOf(380, 520, 310, 460, 350)

    Row(
        modifier = Modifier
            .height(22.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        durations.forEachIndexed { idx, dur ->
            val animRatio by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = dur, delayMillis = idx * 70),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$idx"
            )
            val barFraction = if (isPlaying) animRatio else 0.18f
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((20 * barFraction).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isPlaying) Color(0xFF4ADE80) else Color(0xFF475569)
                    )
            )
        }
    }
}

@Composable
fun RealtimeLyricsSection(
    currentStation: RadioStation,
    currentProgram: String,
    currentSong: RecognizedSong,
    activeLyricIndex: Int,
    isFocusMode: Boolean,
    searchQuery: String,
    isSearching: Boolean,
    searchResults: List<RecognizedSong>,
    onToggleFocusMode: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onExecuteLrcSearch: () -> Unit,
    onOpenMelonWithQuery: (String) -> Unit,
    onCopySongTitleArtist: () -> Unit,
    onSelectSearchedSong: (RecognizedSong) -> Unit
) {
    val context = LocalContext.current

    fun currentSongKeyword(): String {
        return if (currentSong.id != "init_song" &&
            !currentSong.title.startsWith("현재 곡 미감지") &&
            !currentSong.title.startsWith("BGM 인식")
        ) {
            "${currentSong.title} ${currentSong.artist}".trim()
        } else {
            searchQuery.trim()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        if (!isFocusMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("한국어 노래 제목·가수·가사 입력", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedContainerColor = Color(0xFF111827),
                        unfocusedContainerColor = Color(0xFF111827),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onExecuteLrcSearch,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("가사 검색", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        onOpenMelonWithQuery(searchQuery.ifBlank { currentSongKeyword() })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00CD3C)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("🍈 멜론 검색", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }

            if (searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        searchResults.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectSearchedSong(item) }
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${item.artist} · ${item.album}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                                Text("선택·가사 보기", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color(0xFF064E3B),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .border(1.dp, Color(0xFF00CD3C), RoundedCornerShape(50))
                        .clickable {
                            onOpenMelonWithQuery(currentSongKeyword())
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🍈 멜론 로그인·즉시검색", color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                val quickLinks = listOf(
                    "${currentStation.name} 선곡표" to currentStation.playlistWebUrl,
                    "KBS 콩 선곡표" to "https://program.kbs.co.kr/2fm/radio/main.html",
                    "MBC 미니 선곡표" to "https://www.imbc.com/broad/radio/",
                    "SBS 고릴라 선곡표" to "https://www.sbs.co.kr/radio",
                    "CBS 레인보우 선곡표" to "https://www.cbs.co.kr/radio"
                )
                quickLinks.forEach { (label, url) ->
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = Color(0xFFCBD5E1), fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Surface(
            color = Color(0xFF1E293B).copy(alpha = 0.85f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF9333EA)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LibraryMusic, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentSong.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${currentSong.artist} · ${currentSong.stationName} ($currentProgram)",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "방식: ${currentSong.sourceMethod}",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF00CD3C),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                onOpenMelonWithQuery(currentSongKeyword())
                            }
                        ) {
                            Text(
                                text = "🍈 멜론에서 보기",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = onCopySongTitleArtist
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "노래 제목 - 가수 복사",
                                tint = Color(0xFFCBD5E1)
                            )
                        }

                        TextButton(onClick = onToggleFocusMode) {
                            Text(
                                text = if (isFocusMode) "집중모드 해제" else "집중 모드",
                                color = Color(0xFFFACC15),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = Color(0xFF090D16),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                currentSong.syncedLyrics.forEachIndexed { index, line ->
                    val isHighlighted = index == activeLyricIndex
                    Text(
                        text = line.text,
                        color = if (isHighlighted) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                        fontSize = if (isHighlighted) 18.sp else 15.sp,
                        fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isHighlighted) Color(0xFF1E293B).copy(alpha = 0.65f) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SleepTimerDialog(
    currentOption: SleepTimerOption,
    remainingSeconds: Int,
    onSelectOption: (SleepTimerOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text("취침예약 타이머 설정", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (remainingSeconds > 0) {
                    Text(
                        text = "현재 남은 시간: ${remainingSeconds / 60}분 ${remainingSeconds % 60}초 후 자동 종료",
                        color = Color(0xFF38BDF8),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                SleepTimerOption.entries.forEach { option ->
                    Button(
                        onClick = { onSelectOption(option) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentOption == option) Color(0xFF2563EB) else Color(0xFF0F172A)
                        )
                    ) {
                        Text(option.label, color = Color.White)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기", color = Color(0xFF38BDF8))
            }
        }
    )
}

/**
 * ★ 통합 설정 다이얼로그:
 * 1) 즐겨찾기 방송국 순서(▲/▼) 조정 + 각 방송국 프로그램 정보 사용자 직접 수정 + 실시간 업데이트
 * 2) ACRCloud 기본 키(f6abd26110a9254f8256a0ccff81e72a) 및 Secret 내장 + 사용자 자유 수정
 * 3) AudD 보조 토큰 및 커스텀 스트림 URL 설정
 */
@Composable
fun CustomStreamUrlDialog(
    context: Context,
    station: RadioStation,
    allStations: List<RadioStation>,
    favoriteIds: List<String>,
    currentProgramMap: Map<String, String>,
    onChangeFavoriteOrder: (List<String>) -> Unit,
    onUpdateFavoritePrograms: (Map<String, String>) -> Unit,
    onSaveSettings: (String?, String, String, String, String, Map<String, String>) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val acrConfig = remember { SongAndLyricsService.getAcrCloudConfig(context) }

    var inputUrl by remember { mutableStateOf(station.customStreamUrl ?: "") }
    var inputAcrHost by remember { mutableStateOf(acrConfig.host) }
    var inputAcrKey by remember { mutableStateOf(acrConfig.accessKey) }
    var inputAcrSecret by remember { mutableStateOf(acrConfig.accessSecret) }
    var inputAuddToken by remember { mutableStateOf(SongAndLyricsService.getAuddApiToken(context)) }

    var isUpdatingPrograms by remember { mutableStateOf(false) }
    var lastUpdatedText by remember { mutableStateOf(StationRepository.getLastProgramUpdateTimeText(context)) }
    var orderedFavIds by remember(favoriteIds) { mutableStateOf(favoriteIds) }

    // 사용자가 직접 수정할 수 있는 방송국별 프로그램 이름 상태 맵
    val editablePrograms = remember {
        mutableStateMapOf<String, String>().apply {
            allStations.forEach { st ->
                val initialProg = currentProgramMap[st.id]
                    ?: StationRepository.getLatestProgramTitle(context, st)
                put(st.id, initialProg)
            }
        }
    }

    val favoriteStations = remember(allStations, orderedFavIds) {
        orderedFavIds.mapNotNull { id -> allStations.find { it.id == id } }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text("즐겨찾기 순서·프로그램 편집 & ACRCloud 설정", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // ★ 1. 즐겨찾기 방송 순서 조정 및 프로그램 정보 직접 수정/업데이트 영역
                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "1. 즐겨찾기 순서 조정 & 방송 프로그램 정보 수정 (${favoriteStations.size}개)",
                            color = Color(0xFFFDE047),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "▲/▼ 버튼으로 순서를 바꾸고, 입력란에서 프로그램 이름을 직접 수정할 수 있습니다.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "최근 상태: $lastUpdatedText",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                if (!isUpdatingPrograms) {
                                    isUpdatingPrograms = true
                                    coroutineScope.launch {
                                        val refreshed = StationRepository.updateFavoriteStationsPrograms(
                                            context = context,
                                            currentStationId = station.id,
                                            overwriteUserCustom = true
                                        )
                                        refreshed.forEach { (id, title) ->
                                            editablePrograms[id] = title
                                        }
                                        lastUpdatedText = StationRepository.getLastProgramUpdateTimeText(context)
                                        onUpdateFavoritePrograms(refreshed)
                                        isUpdatingPrograms = false
                                        Toast.makeText(
                                            context,
                                            "즐겨찾기 ${favoriteStations.size}개 방송국의 프로그램 정보를 최신 편성으로 불러왔습니다!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isUpdatingPrograms) Color(0xFF1E3A8A) else Color(0xFF2563EB)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isUpdatingPrograms) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("최신 온에어 프로그램 정보 수신 중...", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "실시간 최신 방송 프로그램 정보로 자동 업데이트",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        if (favoriteStations.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                favoriteStations.forEachIndexed { idx, favStation ->
                                    Surface(
                                        color = Color(0xFF1E293B),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${idx + 1}위 · ${favStation.name} (${favStation.frequencyText})",
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    IconButton(
                                                        onClick = {
                                                            if (idx > 0) {
                                                                val mutable = orderedFavIds.toMutableList()
                                                                val item = mutable.removeAt(idx)
                                                                mutable.add(idx - 1, item)
                                                                orderedFavIds = mutable
                                                                onChangeFavoriteOrder(mutable)
                                                            }
                                                        },
                                                        enabled = idx > 0,
                                                        modifier = Modifier.size(26.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.KeyboardArrowUp,
                                                            contentDescription = "위로 이동",
                                                            tint = if (idx > 0) Color(0xFFFACC15) else Color(0xFF475569)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            if (idx < favoriteStations.size - 1) {
                                                                val mutable = orderedFavIds.toMutableList()
                                                                val item = mutable.removeAt(idx)
                                                                mutable.add(idx + 1, item)
                                                                orderedFavIds = mutable
                                                                onChangeFavoriteOrder(mutable)
                                                            }
                                                        },
                                                        enabled = idx < favoriteStations.size - 1,
                                                        modifier = Modifier.size(26.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.KeyboardArrowDown,
                                                            contentDescription = "아래로 이동",
                                                            tint = if (idx < favoriteStations.size - 1) Color(0xFFFACC15) else Color(0xFF475569)
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            // ★ 사용자가 해당 방송국의 프로그램 정보를 직접 수정할 수 있는 입력란
                                            OutlinedTextField(
                                                value = editablePrograms[favStation.id] ?: "",
                                                onValueChange = { newText ->
                                                    editablePrograms[favStation.id] = newText
                                                },
                                                placeholder = {
                                                    Text(
                                                        text = "방송 프로그램 이름 입력",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White,
                                                    focusedContainerColor = Color(0xFF0F172A),
                                                    unfocusedContainerColor = Color(0xFF0F172A),
                                                    focusedBorderColor = Color(0xFFFDE047),
                                                    unfocusedBorderColor = Color(0xFF334155)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "즐겨찾기에 등록된 채널이 없습니다. 채널 카드 우측 상단 별(★)을 눌러 등록해 주세요.",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. ACRCloud 오디오 핑거프린팅 설정",
                        color = Color(0xFF4ADE80),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = {
                            inputAcrHost = SongAndLyricsService.DEFAULT_ACR_HOST
                            inputAcrKey = SongAndLyricsService.DEFAULT_ACR_ACCESS_KEY
                            inputAcrSecret = SongAndLyricsService.DEFAULT_ACR_ACCESS_SECRET
                        }
                    ) {
                        Text("기본키 복원", color = Color(0xFFFACC15), fontSize = 11.sp)
                    }
                }
                Text(
                    text = "기본 Access Key 및 Secret이 입력되어 있으며, 필요 시 아래에서 직접 수정할 수 있습니다.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = inputAcrHost,
                    onValueChange = { inputAcrHost = it },
                    label = { Text("ACRCloud Host", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4ADE80),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = inputAcrKey,
                    onValueChange = { inputAcrKey = it },
                    label = { Text("ACRCloud Access Key (수정 가능)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4ADE80),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = inputAcrSecret,
                    onValueChange = { inputAcrSecret = it },
                    label = { Text("ACRCloud Access Secret (수정 가능)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4ADE80),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "3. AudD 오디오 핑거프린팅 API 토큰 (보조)",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = inputAuddToken,
                    onValueChange = { inputAuddToken = it },
                    placeholder = { Text("test", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "4. ${station.name} 커스텀 스트림 URL",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    placeholder = { Text("비워두면 기본 온에어 주소 사용", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color(0xFF94A3B8))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val customMap = editablePrograms.toMap()
                    onSaveSettings(
                        inputUrl,
                        inputAuddToken,
                        inputAcrHost,
                        inputAcrKey,
                        inputAcrSecret,
                        customMap
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("저장·적용", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ApkBuildGuideDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Text("앱 주요 기능 안내", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "• 즐겨찾기 채널 카드 상단의 [◀ / ▶] 버튼이나 우측 상단 [설정]의 [▲ / ▼] 버튼으로 즐겨찾기 순서를 원하는 대로 조정할 수 있습니다.",
                    color = Color(0xFFFDE047),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "• [설정]에서 즐겨찾기된 각 방송국의 프로그램 이름을 직접 수정하거나 실시간 편성 정보로 자동 업데이트할 수 있습니다.",
                    color = Color(0xFF4ADE80),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "• ACRCloud Access Key와 Secret이 기본 입력되어 있으며 [설정]에서 언제든 수정 가능합니다.",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인", color = Color(0xFF38BDF8))
            }
        }
    )
}