package com.example.ui.viewmodel

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.SaudiCity
import com.example.data.model.SaudiMapCity
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.GroundingMetadata
import com.google.firebase.ai.type.ImageConfig
import com.google.firebase.ai.type.ImagePart
import com.google.firebase.ai.type.ImageSize
import com.google.firebase.ai.type.LatLng
import com.google.firebase.ai.type.LiveSession
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.RetrievalConfig
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.SpeechConfig
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.ToolConfig
import com.google.firebase.ai.type.Voice
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

private const val TAG = "GeminiStudioViewModel"

class GeminiStudioViewModel : ViewModel() {

    // ------------------------------------------------------------
    // 1. IMAGE CREATE & EDIT (gemini-nano-banana-2.1 / gemini-3-pro-image)
    // ------------------------------------------------------------
    private val _imageGenState = MutableStateFlow<ImageGenUiState>(ImageGenUiState.Idle)
    val imageGenState: StateFlow<ImageGenUiState> = _imageGenState.asStateFlow()

    fun generateFoodImage(
        prompt: String,
        aspectOption: AiImageAspectOption = AiImageAspectOption.SQUARE,
        useProModel: Boolean = false
    ) {
        if (prompt.isBlank()) {
            _imageGenState.value = ImageGenUiState.Error("Please enter a visual prompt first.")
            return
        }
        viewModelScope.launch {
            val modelName = if (useProModel) "gemini-3-pro-image" else "gemini-nano-banana-2.1"
            _imageGenState.value = ImageGenUiState.Loading("Synthesizing with $modelName (30–60s)...")
            try {
                val config = generationConfig {
                    responseModalities = listOf(ResponseModality.IMAGE)
                    imageConfig = ImageConfig.builder()
                        .setAspectRatio(aspectOption.aspectRatio)
                        .setImageSize(ImageSize.SIZE_1K)
                        .build()
                }
                val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel(modelName = modelName, generationConfig = config)

                val response = withTimeout(60_000L) {
                    model.generateContent(prompt)
                }
                val bitmap = response.candidates.firstOrNull()
                    ?.content?.parts?.filterIsInstance<ImagePart>()
                    ?.firstOrNull()?.image

                if (bitmap != null) {
                    _imageGenState.value = ImageGenUiState.Success(
                        bitmap = bitmap,
                        promptUsed = prompt,
                        isEdit = false,
                        modelUsed = modelName
                    )
                } else {
                    _imageGenState.value = ImageGenUiState.Error("No image was returned by $modelName.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Image generation timed out", e)
                _imageGenState.value = ImageGenUiState.Error("Image generation timed out after 60 seconds.")
            } catch (e: QuotaExceededException) {
                Log.e(TAG, "Image generation quota exceeded", e)
                _imageGenState.value = ImageGenUiState.Error("Quota exceeded or Blaze plan required for $modelName: ${e.localizedMessage}")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Image generation failed", e)
                _imageGenState.value = ImageGenUiState.Error(e.localizedMessage ?: "Image generation failed.")
            }
        }
    }

    fun editFoodImage(
        inputBitmap: Bitmap,
        editInstruction: String,
        aspectOption: AiImageAspectOption = AiImageAspectOption.SQUARE,
        useProModel: Boolean = false
    ) {
        if (editInstruction.isBlank()) {
            _imageGenState.value = ImageGenUiState.Error("Please enter an edit instruction for the selected image.")
            return
        }
        viewModelScope.launch {
            val modelName = if (useProModel) "gemini-3-pro-image" else "gemini-nano-banana-2.1"
            _imageGenState.value = ImageGenUiState.Loading("Editing image with $modelName (30–60s)...")
            try {
                val config = generationConfig {
                    responseModalities = listOf(ResponseModality.IMAGE)
                    imageConfig = ImageConfig.builder()
                        .setAspectRatio(aspectOption.aspectRatio)
                        .setImageSize(ImageSize.SIZE_1K)
                        .build()
                }
                val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .generativeModel(modelName = modelName, generationConfig = config)

                val response = withTimeout(60_000L) {
                    model.generateContent(
                        content {
                            image(inputBitmap)
                            text(editInstruction)
                        }
                    )
                }
                val bitmap = response.candidates.firstOrNull()
                    ?.content?.parts?.filterIsInstance<ImagePart>()
                    ?.firstOrNull()?.image

                if (bitmap != null) {
                    _imageGenState.value = ImageGenUiState.Success(
                        bitmap = bitmap,
                        promptUsed = editInstruction,
                        isEdit = true,
                        modelUsed = modelName
                    )
                } else {
                    _imageGenState.value = ImageGenUiState.Error("No edited image was returned by $modelName.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Image edit timed out", e)
                _imageGenState.value = ImageGenUiState.Error("Image editing timed out after 60 seconds.")
            } catch (e: QuotaExceededException) {
                Log.e(TAG, "Image edit quota exceeded", e)
                _imageGenState.value = ImageGenUiState.Error("Quota exceeded or Blaze plan required for $modelName: ${e.localizedMessage}")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Image edit failed", e)
                _imageGenState.value = ImageGenUiState.Error(e.localizedMessage ?: "Image editing failed.")
            }
        }
    }

    suspend fun saveGeneratedBitmapToFile(context: Context, bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        try {
            val outFile = File(context.filesDir, "sufra_ai_media_${System.currentTimeMillis()}.png")
            FileOutputStream(outFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
            }
            val uri = Uri.fromFile(outFile)
            val current = _imageGenState.value
            if (current is ImageGenUiState.Success) {
                _imageGenState.value = current.copy(savedUri = uri)
            }
            uri
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist generated bitmap", e)
            null
        }
    }

    fun resetImageGenState() {
        _imageGenState.value = ImageGenUiState.Idle
    }

    // ------------------------------------------------------------
    // 2. AUDIO TRANSCRIPTION FROM MICROPHONE (gemini-3.8-flash)
    // ------------------------------------------------------------
    private val _audioState = MutableStateFlow<AudioTranscribeState>(AudioTranscribeState.Idle)
    val audioState: StateFlow<AudioTranscribeState> = _audioState.asStateFlow()

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var recordingTimerJob: Job? = null

    private val transcriptionSchema = Schema.obj(
        properties = mapOf(
            "transcript" to Schema.string(description = "Accurate verbatim transcription of the recorded speech in its spoken language (Arabic and/or English)"),
            "suggestedCaptionAr" to Schema.string(description = "Polished Saudi dialect social media food caption derived from the voice note"),
            "suggestedCaptionEn" to Schema.string(description = "Polished English hospitality caption derived from the voice note")
        )
    )

    private val audioTranscriptionModel by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = transcriptionSchema
            }
        )
    }

    fun startMicrophoneRecording(context: Context) {
        stopAndReleaseRecorder()
        try {
            val outFile = File(context.cacheDir, "voice_note_${System.currentTimeMillis()}.m4a")
            recordingFile = outFile
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(16000)
                setAudioEncodingBitRate(64000)
                setOutputFile(outFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            _audioState.value = AudioTranscribeState.Recording(0)
            recordingTimerJob?.cancel()
            recordingTimerJob = viewModelScope.launch {
                var sec = 0
                while (true) {
                    delay(1000L)
                    sec++
                    _audioState.value = AudioTranscribeState.Recording(sec)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio recording", e)
            stopAndReleaseRecorder()
            _audioState.value = AudioTranscribeState.Error("Microphone unavailable: ${e.localizedMessage ?: "Recording error"}")
        }
    }

    fun stopAndTranscribeAudio() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        val file = recordingFile
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping MediaRecorder", e)
        } finally {
            mediaRecorder = null
        }

        if (file == null || !file.exists() || file.length() == 0L) {
            _audioState.value = AudioTranscribeState.Error("No audio was recorded. Hold or tap Record and speak clearly.")
            return
        }
        if (file.length() > 18 * 1024 * 1024) {
            _audioState.value = AudioTranscribeState.Error("Recording exceeds 18MB inline audio limit.")
            return
        }

        viewModelScope.launch {
            _audioState.value = AudioTranscribeState.Transcribing
            try {
                val bytes = withContext(Dispatchers.IO) { file.readBytes() }
                val prompt = content {
                    inlineData(bytes, "audio/mp4")
                    text(
                        "Transcribe verbatim what is spoken in this Saudi restaurant/food voice memo (Arabic or English), " +
                            "and also craft a localized Saudi Arabic food social media caption and an English counterpart."
                    )
                }
                val response = withTimeout(60_000L) {
                    audioTranscriptionModel.generateContent(prompt)
                }
                val rawJson = response.text
                if (rawJson.isNullOrBlank()) {
                    _audioState.value = AudioTranscribeState.Error("Transcription model returned empty response.")
                } else {
                    val json = JSONObject(rawJson)
                    _audioState.value = AudioTranscribeState.Success(
                        transcript = json.optString("transcript", ""),
                        suggestedCaptionAr = json.optString("suggestedCaptionAr", ""),
                        suggestedCaptionEn = json.optString("suggestedCaptionEn", "")
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Audio transcription timed out", e)
                _audioState.value = AudioTranscribeState.Error("Audio transcription timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Audio transcription failed", e)
                _audioState.value = AudioTranscribeState.Error(e.localizedMessage ?: "Audio transcription failed.")
            } finally {
                runCatching { file.delete() }
            }
        }
    }

    fun resetAudioState() {
        stopAndReleaseRecorder()
        _audioState.value = AudioTranscribeState.Idle
    }

    private fun stopAndReleaseRecorder() {
        recordingTimerJob?.cancel()
        recordingTimerJob = null
        runCatching { mediaRecorder?.stop() }
        runCatching { mediaRecorder?.release() }
        mediaRecorder = null
    }

    // ------------------------------------------------------------
    // 3. MULTI-TURN GEMINI CHATBOT + SEARCH & MAPS GROUNDING
    // ------------------------------------------------------------
    private val _selectedChatModel = MutableStateFlow(AiChatModelTier.GENERAL)
    val selectedChatModel: StateFlow<AiChatModelTier> = _selectedChatModel.asStateFlow()

    private val _selectedChatRole = MutableStateFlow(AiChatRolePreset.SAUDI_STRATEGIST)
    val selectedChatRole: StateFlow<AiChatRolePreset> = _selectedChatRole.asStateFlow()

    private val _customSystemInstruction = MutableStateFlow(AiChatRolePreset.SAUDI_STRATEGIST.systemInstruction)
    val customSystemInstruction: StateFlow<String> = _customSystemInstruction.asStateFlow()

    private val _enableSearchGrounding = MutableStateFlow(true)
    val enableSearchGrounding: StateFlow<Boolean> = _enableSearchGrounding.asStateFlow()

    private val _enableMapsGrounding = MutableStateFlow(true)
    val enableMapsGrounding: StateFlow<Boolean> = _enableMapsGrounding.asStateFlow()

    private val _chatCityContext = MutableStateFlow(SaudiMapCity.RIYADH)
    val chatCityContext: StateFlow<SaudiMapCity> = _chatCityContext.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessageUi>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessageUi>> = _chatMessages.asStateFlow()

    private val _isChatSending = MutableStateFlow(false)
    val isChatSending: StateFlow<Boolean> = _isChatSending.asStateFlow()

    private var activeChatSession: Chat? = null
    private var activeSessionConfigKey: String? = null

    fun selectChatModel(tier: AiChatModelTier) {
        _selectedChatModel.value = tier
        invalidateChatSessionKeepHistory()
    }

    fun selectChatRole(role: AiChatRolePreset) {
        _selectedChatRole.value = role
        if (role != AiChatRolePreset.CUSTOM) {
            _customSystemInstruction.value = role.systemInstruction
        }
        invalidateChatSessionKeepHistory()
    }

    fun updateCustomSystemInstruction(text: String) {
        _customSystemInstruction.value = text
        invalidateChatSessionKeepHistory()
    }

    fun toggleSearchGrounding(enabled: Boolean) {
        _enableSearchGrounding.value = enabled
        invalidateChatSessionKeepHistory()
    }

    fun toggleMapsGrounding(enabled: Boolean) {
        _enableMapsGrounding.value = enabled
        invalidateChatSessionKeepHistory()
    }

    fun setChatCityContext(city: SaudiMapCity) {
        _chatCityContext.value = city
        invalidateChatSessionKeepHistory()
    }

    fun clearChatHistory() {
        _chatMessages.value = emptyList()
        activeChatSession = null
        activeSessionConfigKey = null
    }

    private fun invalidateChatSessionKeepHistory() {
        activeChatSession = null
        activeSessionConfigKey = null
    }

    private fun getOrCreateChatSession(): Chat {
        val tier = _selectedChatModel.value
        val sysPrompt = _customSystemInstruction.value.ifBlank { AiChatRolePreset.SAUDI_STRATEGIST.systemInstruction }
        val useSearch = _enableSearchGrounding.value
        val useMaps = _enableMapsGrounding.value
        val city = _chatCityContext.value
        val key = "${tier.modelName}|$sysPrompt|$useSearch|$useMaps|${city.name}"

        val existing = activeChatSession
        if (existing != null && activeSessionConfigKey == key) {
            return existing
        }

        val toolsList = buildList {
            if (useSearch) add(Tool.googleSearch())
            if (useMaps) add(Tool.googleMaps())
        }

        val toolConfig = if (useMaps) {
            ToolConfig(
                retrievalConfig = RetrievalConfig.builder()
                    .setLatLng(LatLng(latitude = city.lat, longitude = city.lng))
                    .setLanguageCode("en-US")
                    .build()
            )
        } else null

        val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = tier.modelName,
            tools = toolsList.ifEmpty { null },
            toolConfig = toolConfig,
            systemInstruction = content { text(sysPrompt) }
        )

        val rebuiltHistory = _chatMessages.value
            .filter { it.errorMessage == null && it.text.isNotBlank() && !it.isStreaming }
            .map { msg -> content(if (msg.isUser) "user" else "model") { text(msg.text) } }

        val chat = model.startChat(history = rebuiltHistory)
        activeChatSession = chat
        activeSessionConfigKey = key
        return chat
    }

    fun sendChatMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isChatSending.value) return

        val userMsg = ChatMessageUi(
            id = "u_${UUID.randomUUID().toString().take(8)}",
            isUser = true,
            text = trimmed,
            timestampRiyadh = formatRiyadhTimeShort()
        )
        val assistantMsgId = "m_${UUID.randomUUID().toString().take(8)}"
        val tier = _selectedChatModel.value
        val placeholderMsg = ChatMessageUi(
            id = assistantMsgId,
            isUser = false,
            text = "",
            timestampRiyadh = formatRiyadhTimeShort(),
            isStreaming = true,
            modelBadge = tier.badge
        )
        _chatMessages.value = _chatMessages.value + userMsg + placeholderMsg

        viewModelScope.launch {
            _isChatSending.value = true
            val accumulated = StringBuilder()
            var latestMetadata: GroundingMetadata? = null

            try {
                val chat = getOrCreateChatSession()
                withTimeout(60_000L) {
                    chat.sendMessageStream(trimmed).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        accumulated.append(piece)
                        val meta = chunk.candidates.firstOrNull()?.groundingMetadata
                        if (meta != null) {
                            latestMetadata = meta
                        }
                        updateChatMessage(assistantMsgId) {
                            it.copy(text = accumulated.toString(), isStreaming = true)
                        }
                    }
                }
                val finalText = accumulated.toString().ifBlank { "Model returned an empty response." }
                val sources = extractGroundingSources(latestMetadata)
                val widgetHtml = latestMetadata?.searchEntryPoint?.renderedContent
                updateChatMessage(assistantMsgId) {
                    it.copy(
                        text = finalText,
                        isStreaming = false,
                        sources = sources,
                        searchWidgetHtml = widgetHtml
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Chat timed out", e)
                updateChatMessage(assistantMsgId) {
                    it.copy(isStreaming = false, errorMessage = "Request timed out after 60 seconds.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Chat error", e)
                updateChatMessage(assistantMsgId) {
                    it.copy(isStreaming = false, errorMessage = e.localizedMessage ?: "Failed to get response.")
                }
            } finally {
                _isChatSending.value = false
            }
        }
    }

    private fun updateChatMessage(id: String, transform: (ChatMessageUi) -> ChatMessageUi) {
        _chatMessages.value = _chatMessages.value.map { if (it.id == id) transform(it) else it }
    }

    // ------------------------------------------------------------
    // 4. STANDALONE GOOGLE SEARCH & MAPS GROUNDING QUERIES
    // ------------------------------------------------------------
    private val _searchGroundingState = MutableStateFlow<GroundedIntelUiState>(GroundedIntelUiState.Idle)
    val searchGroundingState: StateFlow<GroundedIntelUiState> = _searchGroundingState.asStateFlow()

    private val _mapsGroundingState = MutableStateFlow<GroundedIntelUiState>(GroundedIntelUiState.Idle)
    val mapsGroundingState: StateFlow<GroundedIntelUiState> = _mapsGroundingState.asStateFlow()

    fun runSearchGroundedIntel(query: String, city: SaudiCity?) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return
        viewModelScope.launch {
            _searchGroundingState.value = GroundedIntelUiState.Loading
            try {
                val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                    modelName = "gemini-3.8-flash",
                    tools = listOf(Tool.googleSearch())
                )
                val citySuffix = city?.let { " in ${it.englishName} (${it.arabicName}), Saudi Arabia" } ?: " in Saudi Arabia"
                val prompt = "Provide up-to-date food & restaurant market intelligence for: $cleanQuery$citySuffix. Include actionable social media content angles for Saudi hospitality brands."
                val response = withTimeout(60_000L) { model.generateContent(prompt) }
                val text = response.text
                if (text.isNullOrBlank()) {
                    _searchGroundingState.value = GroundedIntelUiState.Error("Search grounding returned an empty response.")
                } else {
                    val meta = response.candidates.firstOrNull()?.groundingMetadata
                    _searchGroundingState.value = GroundedIntelUiState.Success(
                        query = cleanQuery,
                        summaryText = text,
                        sources = extractGroundingSources(meta),
                        searchWidgetHtml = meta?.searchEntryPoint?.renderedContent,
                        generatedAt = formatRiyadhTimeShort()
                    )
                }
            } catch (e: TimeoutCancellationException) {
                _searchGroundingState.value = GroundedIntelUiState.Error("Google Search grounding timed out after 60s.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Search grounding failed", e)
                _searchGroundingState.value = GroundedIntelUiState.Error(e.localizedMessage ?: "Search grounding failed.")
            }
        }
    }

    fun runMapsGroundedQuery(query: String, city: SaudiMapCity) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return
        viewModelScope.launch {
            _mapsGroundingState.value = GroundedIntelUiState.Loading
            try {
                val model = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
                    modelName = "gemini-3.8-flash",
                    tools = listOf(Tool.googleMaps()),
                    toolConfig = ToolConfig(
                        retrievalConfig = RetrievalConfig.builder()
                            .setLatLng(LatLng(latitude = city.lat, longitude = city.lng))
                            .setLanguageCode("en-US")
                            .build()
                    )
                )
                val prompt = "$cleanQuery in ${city.englishName}, Saudi Arabia. Highlight notable venues, dining vibe, and Google Maps details."
                val response = withTimeout(60_000L) { model.generateContent(prompt) }
                val text = response.text
                if (text.isNullOrBlank()) {
                    _mapsGroundingState.value = GroundedIntelUiState.Error("Google Maps grounding returned an empty response.")
                } else {
                    val meta = response.candidates.firstOrNull()?.groundingMetadata
                    _mapsGroundingState.value = GroundedIntelUiState.Success(
                        query = cleanQuery,
                        summaryText = text,
                        sources = extractGroundingSources(meta),
                        generatedAt = formatRiyadhTimeShort()
                    )
                }
            } catch (e: TimeoutCancellationException) {
                _mapsGroundingState.value = GroundedIntelUiState.Error("Google Maps grounding timed out after 60s.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Maps grounding failed", e)
                _mapsGroundingState.value = GroundedIntelUiState.Error(e.localizedMessage ?: "Maps grounding failed.")
            }
        }
    }

    // ------------------------------------------------------------
    // 5. REAL-TIME VOICE CONVERSATION (Gemini Live API)
    // ------------------------------------------------------------
    private val _liveVoiceState = MutableStateFlow<LiveVoiceState>(LiveVoiceState.Disconnected)
    val liveVoiceState: StateFlow<LiveVoiceState> = _liveVoiceState.asStateFlow()

    @OptIn(PublicPreviewAPI::class)
    private var liveSession: LiveSession? = null

    @OptIn(PublicPreviewAPI::class)
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startLiveVoiceConversation(
        voiceOption: GeminiLiveVoiceOption,
        systemRolePrompt: String
    ) {
        if (_liveVoiceState.value is LiveVoiceState.Connecting || _liveVoiceState.value is LiveVoiceState.Active) return
        viewModelScope.launch {
            _liveVoiceState.value = LiveVoiceState.Connecting
            try {
                val liveModel = Firebase.ai(backend = GenerativeBackend.googleAI()).liveModel(
                    modelName = "gemini-3.1-flash-live-preview",
                    generationConfig = liveGenerationConfig {
                        responseModality = ResponseModality.AUDIO
                        speechConfig = SpeechConfig(voice = Voice(voiceOption.voiceName))
                    },
                    systemInstruction = content { text(systemRolePrompt) }
                )
                val session = withTimeout(30_000L) { liveModel.connect() }
                liveSession = session
                session.startAudioConversation()
                _liveVoiceState.value = LiveVoiceState.Active(
                    voiceName = voiceOption.label,
                    startedAtRiyadh = formatRiyadhTimeShort()
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Live API connection timed out", e)
                stopLiveVoiceConversation()
                _liveVoiceState.value = LiveVoiceState.Error("Live API connection timed out.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Live API voice session failed", e)
                stopLiveVoiceConversation()
                _liveVoiceState.value = LiveVoiceState.Error(e.localizedMessage ?: "Live API failed to connect.")
            }
        }
    }

    @OptIn(PublicPreviewAPI::class)
    fun sendLivePromptHint(text: String) {
        val session = liveSession ?: return
        viewModelScope.launch {
            runCatching { session.sendTextRealtime(text) }
                .onFailure { Log.e(TAG, "Failed to send realtime hint", it) }
        }
    }

    @OptIn(PublicPreviewAPI::class)
    fun stopLiveVoiceConversation() {
        val session = liveSession
        liveSession = null
        viewModelScope.launch {
            runCatching { session?.stopAudioConversation() }
            runCatching { session?.close() }
            if (_liveVoiceState.value !is LiveVoiceState.Error) {
                _liveVoiceState.value = LiveVoiceState.Disconnected
            }
        }
    }

    fun dismissLiveVoiceError() {
        _liveVoiceState.value = LiveVoiceState.Disconnected
    }

    override fun onCleared() {
        super.onCleared()
        stopAndReleaseRecorder()
        @OptIn(PublicPreviewAPI::class)
        runCatching { liveSession?.stopAudioConversation() }
    }

    private fun extractGroundingSources(metadata: GroundingMetadata?): List<GroundingSource> {
        if (metadata == null) return emptyList()
        val list = mutableListOf<GroundingSource>()
        metadata.groundingChunks.forEach { chunk ->
            chunk.web?.let { web ->
                val uri = web.uri
                if (!uri.isNullOrBlank()) {
                    list.add(
                        GroundingSource(
                            title = web.title ?: web.domain ?: "Web Source",
                            uri = uri,
                            type = GroundingSourceType.WEB,
                            subtitle = web.domain
                        )
                    )
                }
            }
            chunk.maps?.let { maps ->
                val uri = maps.uri
                if (!uri.isNullOrBlank()) {
                    list.add(
                        GroundingSource(
                            title = maps.title ?: "Google Maps Place",
                            uri = uri,
                            type = GroundingSourceType.GOOGLE_MAPS,
                            placeId = maps.placeId
                        )
                    )
                }
            }
        }
        return list.distinctBy { it.uri }
    }

    private fun formatRiyadhTimeShort(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Riyadh")
        return sdf.format(Date()) + " AST"
    }
}
