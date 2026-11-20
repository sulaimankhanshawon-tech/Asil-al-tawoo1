package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import com.google.firebase.ai.type.AspectRatio

enum class AiImageAspectOption(
    val label: String,
    val aspectRatio: AspectRatio
) {
    SQUARE("1:1 Feed", AspectRatio.SQUARE_1x1),
    PORTRAIT_FEED("4:5 IG", AspectRatio.PORTRAIT_4x5),
    VERTICAL_REEL("9:16 Story", AspectRatio.PORTRAIT_9x16),
    LANDSCAPE("16:9 Banner", AspectRatio.LANDSCAPE_16x9)
}

sealed interface ImageGenUiState {
    data object Idle : ImageGenUiState
    data class Loading(val statusLabel: String) : ImageGenUiState
    data class Success(
        val bitmap: Bitmap,
        val promptUsed: String,
        val isEdit: Boolean,
        val modelUsed: String,
        val savedUri: Uri? = null
    ) : ImageGenUiState
    data class Error(val message: String) : ImageGenUiState
}

sealed interface AudioTranscribeState {
    data object Idle : AudioTranscribeState
    data class Recording(val elapsedSeconds: Int) : AudioTranscribeState
    data object Transcribing : AudioTranscribeState
    data class Success(
        val transcript: String,
        val suggestedCaptionAr: String,
        val suggestedCaptionEn: String
    ) : AudioTranscribeState
    data class Error(val message: String) : AudioTranscribeState
}

enum class AiChatModelTier(
    val label: String,
    val modelName: String,
    val badge: String,
    val description: String
) {
    FAST(
        label = "Fast Mode",
        modelName = "gemini-3.5-flash-lite",
        badge = "Flash-Lite",
        description = "Ultra-fast responses for quick captions, hashtags & replies"
    ),
    GENERAL(
        label = "General & Grounded",
        modelName = "gemini-3.8-flash",
        badge = "3.8 Flash",
        description = "Balanced intelligence with Google Search & Google Maps Grounding"
    ),
    REASONING(
        label = "Deep Reasoning",
        modelName = "gemini-4-argon",
        badge = "4 Argon",
        description = "Deep campaign strategy, pricing & competitor intelligence (Blaze)"
    )
}

enum class AiChatRolePreset(
    val title: String,
    val arabicBadge: String,
    val systemInstruction: String
) {
    SAUDI_STRATEGIST(
        title = "Saudi Culinary Strategist",
        arabicBadge = "خبير التسويق",
        systemInstruction = "You are Sufra AI, a senior Saudi Arabian food & restaurant marketing strategist. Give practical, culturally authentic advice tailored to Riyadh, Jeddah, Makkah, Madinah, Dammam, Khobar, Taif, and Abha. Use Asia/Riyadh peak mealtime schedules, Vision 2030 hospitality trends, and natural Saudi Arabic alongside crisp English."
    ),
    BILINGUAL_COPYWRITER(
        title = "Bilingual Copywriter",
        arabicBadge = "كاتب محتوى",
        systemInstruction = "You are an award-winning Saudi social media copywriter for restaurants, specialty cafes, and cloud kitchens. Craft high-converting Arabic captions in authentic Saudi dialects (Najdi, Hijazi, Sharqawi) paired with polished English hospitality captions, trending KSA hashtags, and 8K food photography prompts."
    ),
    REVIEW_ANALYST(
        title = "Competitor & Ops Analyst",
        arabicBadge = "محلل المنافسين",
        systemInstruction = "You are a Saudi hospitality operations and customer experience analyst. Evaluate restaurant positioning, dining experience, delivery friction, pricing perception, and competitor differentiation objectively without fabricating metrics."
    ),
    CUSTOM(
        title = "Custom System Role",
        arabicBadge = "دور مخصص",
        systemInstruction = "You are a helpful AI assistant for a Saudi restaurant social media manager."
    )
}

enum class GroundingSourceType {
    WEB,
    GOOGLE_MAPS
}

data class GroundingSource(
    val title: String,
    val uri: String,
    val type: GroundingSourceType,
    val subtitle: String? = null,
    val placeId: String? = null
)

data class ChatMessageUi(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestampRiyadh: String,
    val isStreaming: Boolean = false,
    val modelBadge: String? = null,
    val sources: List<GroundingSource> = emptyList(),
    val searchWidgetHtml: String? = null,
    val errorMessage: String? = null
)

enum class GeminiLiveVoiceOption(
    val voiceName: String,
    val label: String
) {
    KORE("Kore", "Kore (Warm & Clear)"),
    PUCK("Puck", "Puck (Upbeat)"),
    CHARON("Charon", "Charon (Calm & Deep)"),
    FENRIR("Fenrir", "Fenrir (Bold Studio)"),
    AOEDE("Aoede", "Aoede (Conversational)")
}

sealed interface LiveVoiceState {
    data object Disconnected : LiveVoiceState
    data object Connecting : LiveVoiceState
    data class Active(val voiceName: String, val startedAtRiyadh: String) : LiveVoiceState
    data class Error(val message: String) : LiveVoiceState
}

sealed interface GroundedIntelUiState {
    data object Idle : GroundedIntelUiState
    data object Loading : GroundedIntelUiState
    data class Success(
        val query: String,
        val summaryText: String,
        val sources: List<GroundingSource>,
        val searchWidgetHtml: String? = null,
        val generatedAt: String
    ) : GroundedIntelUiState
    data class Error(val message: String) : GroundedIntelUiState
}
