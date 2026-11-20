package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R

enum class SocialPlatform(
    val title: String,
    val brandColorHex: Long,
    val characterLimit: Int,
    val supportedFormats: String,
    val apiName: String,
    val requiredScopes: List<String>,
    val developerCredentials: String,
    val requiredApprovals: String
) {
    INSTAGRAM(
        title = "Instagram",
        brandColorHex = 0xFFE1306C,
        characterLimit = 2200,
        supportedFormats = "Reels (9:16), Feed (1:1, 4:5), Stories",
        apiName = "Instagram Graph API v19.0",
        requiredScopes = listOf("instagram_basic", "instagram_content_publish", "pages_read_engagement"),
        developerCredentials = "Meta Developer App (App ID & App Secret configured on backend)",
        requiredApprovals = "Meta App Review (instagram_content_publish) & Business Verification"
    ),
    FACEBOOK(
        title = "Facebook",
        brandColorHex = 0xFF1877F2,
        characterLimit = 63206,
        supportedFormats = "Image Posts, Video (16:9, 1:1), Stories",
        apiName = "Meta Graph API v19.0",
        requiredScopes = listOf("pages_manage_posts", "pages_read_engagement", "pages_show_list"),
        developerCredentials = "Meta Developer App (App ID & App Secret configured on backend)",
        requiredApprovals = "Meta App Review (pages_manage_posts) & Page Admin Role"
    ),
    TIKTOK(
        title = "TikTok",
        brandColorHex = 0xFF000000,
        characterLimit = 4000,
        supportedFormats = "Vertical Video 9:16 (15-60s), Carousel",
        apiName = "TikTok Content Posting API",
        requiredScopes = listOf("video.upload", "video.publish", "user.info.basic"),
        developerCredentials = "TikTok for Developers App (Client Key & Client Secret on backend)",
        requiredApprovals = "TikTok Commercial Developer Approval (Content Posting API)"
    ),
    SNAPCHAT(
        title = "Snapchat",
        brandColorHex = 0xFFFFFC00,
        characterLimit = 250,
        supportedFormats = "Spotlight (9:16), Brand Story, Public Profile",
        apiName = "Snapchat Marketing API",
        requiredScopes = listOf("snapchat-marketing-api", "public_profile_publish"),
        developerCredentials = "Snapchat Business Manager (OAuth Client ID & Client Secret on backend)",
        requiredApprovals = "Snapchat Marketing Partner Approval (Public Profiles API)"
    ),
    X_TWITTER(
        title = "X (Twitter)",
        brandColorHex = 0xFF14171A,
        characterLimit = 280,
        supportedFormats = "Images (16:9), Video clips (<140s), Threads",
        apiName = "X API v2 (OAuth 2.0 PKCE)",
        requiredScopes = listOf("tweet.read", "tweet.write", "users.read"),
        developerCredentials = "X Developer Portal App (OAuth 2.0 Client ID with PKCE)",
        requiredApprovals = "X Developer Portal v2 Write Access Approval"
    )
}

enum class SaudiCity(val englishName: String, val arabicName: String, val province: String) {
    RIYADH("Riyadh", "الرياض", "Central Province"),
    JEDDAH("Jeddah", "جدة", "Western Province"),
    MAKKAH("Makkah", "مكة المكرمة", "Western Province"),
    MADINAH("Madinah", "المدينة المنورة", "Western Province"),
    DAMMAM("Dammam", "الدمام", "Eastern Province"),
    KHOBAR("Khobar", "الخبر", "Eastern Province"),
    TAIF("Taif", "الطائف", "Western Province"),
    ABHA("Abha", "أبها", "Asir / Southern")
}

enum class FoodCategory(val englishName: String, val arabicName: String) {
    TRENDING("Trending Now", "الأكثر رواجاً"),
    TRADITIONAL_SAUDI("Traditional Saudi", "المطبخ السعودي الأصيل"),
    CAFES_COFFEE("Cafés & Specialty Coffee", "مقاهي وقهوة مختصة"),
    SMASH_BURGERS("Smash Burgers & Fast Food", "برجر وسريع التحضير"),
    HEALTHY_CLEAN("Healthy & Clean Eating", "صحي ودايت"),
    STREET_FOOD("Saudi Street Food", "أكلات شوارع وأكشاك"),
    SWEETS_DESSERTS("Desserts & Beverages", "حلويات ومشروبات"),
    DELIVERY_TRENDS("Delivery & Cloud Kitchens", "توصيل ومطابخ سحابية"),
    SEASONAL_CAMPAIGNS("National & Seasonal Events", "مواسم وفعاليات وطنية")
}

enum class PostContentType(val label: String, val isVideo: Boolean, val recommendedDuration: String) {
    FOOD_PHOTOGRAPHY("Food Photography Post", false, "1:1 / 4:5"),
    RESTAURANT_PROMO("Restaurant Promotional Offer", false, "1:1 / Stories"),
    FOOD_FACTS("Saudi Culinary Trivia & Facts", false, "Carousel"),
    SAUDI_FOOD_CULTURE("Saudi Food Heritage & Culture", false, "1:1 / 16:9"),
    RECIPE_SPOTLIGHT("Recipe & Secret Spices Spotlight", false, "Carousel"),
    SHORT_VIDEO_REEL("Short Viral Reel / TikTok Video", true, "15–30 sec 9:16"),
    CHEF_RECOMMENDATIONS("Chef's Signature Recommendation", false, "1:1"),
    EDUCATIONAL_STORY("Nutritional & Quality Insights", true, "15 sec 9:16")
}

enum class PostStatus(val displayName: String, val colorHex: Long) {
    DRAFT("Draft", 0xFF757575),
    AWAITING_APPROVAL("Awaiting Approval", 0xFFE65100),
    SCHEDULED("Scheduled", 0xFF1565C0),
    PUBLISHED("Published", 0xFF2E7D32),
    FAILED_RETRYING("Retry Queued", 0xFFC62828),
    REJECTED("Rejected", 0xFF8D6E63)
}

enum class AutomationMode(val title: String, val subtitle: String) {
    MANUAL("Manual Mode", "AI creates content, user reviews and publishes manually"),
    APPROVAL("Approval Mode", "AI generates & queues content; publishes upon user tap"),
    FULL_AUTOMATION("Full Automation Mode", "AI autonomously researches, generates, schedules & publishes")
}

data class SocialAccount(
    val id: String,
    val platform: SocialPlatform,
    val accountName: String,
    val handle: String,
    val isConnected: Boolean = false,
    val followerCount: Int = 0,
    val statusText: String = "Not connected",
    val autoPublishEnabled: Boolean = false,
    val tokenExpiryDays: Int = 0,
    val lastVerifiedTimestamp: String? = null
)

data class MarketResearchReport(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val city: SaudiCity,
    val category: FoodCategory,
    val trendingItem: String,
    val hashtags: List<String>,
    val audienceInterest: String,
    val competitorObservations: String,
    val marketingOpportunity: String,
    val searchGrowthPercent: Int,
    val sourceUrl: String,
    val researchDate: String
)

data class SocialPost(
    val id: String,
    val title: String,
    var captionAr: String,
    var captionEn: String,
    var hashtags: List<String>,
    var targetPlatforms: Set<SocialPlatform>,
    var scheduledTimeSlot: String, // e.g. "09:00 AM", "12:00 PM"
    var scheduledDate: String,     // e.g. "Today", "Tomorrow"
    var status: PostStatus,
    val contentType: PostContentType,
    @DrawableRes var imageResId: Int? = null,
    var customImageUri: String? = null,
    var imagePrompt: String = "",
    val isVideo: Boolean = false,
    val videoDurationSec: Int? = null,
    var engagementLikes: Int = 0,
    var engagementShares: Int = 0,
    var engagementComments: Int = 0,
    var retryAttempts: Int = 0,
    var lastApiPayload: String? = null,
    var lastErrorMessage: String? = null
)

data class MarketingCampaign(
    val id: String,
    val name: String,
    val typeName: String, // "7-Day Growth Campaign" or "30-Day Growth Campaign"
    val durationDays: Int,
    val targetPosts: Int,
    val publishedPosts: Int = 0,
    val currentDay: Int = 1,
    val targetReach: Int,
    val currentReach: Int = 0,
    val targetEngagementRate: Double,
    val currentEngagementRate: Double = 0.0,
    val targetFollowersGain: Int,
    val currentFollowersGain: Int = 0,
    val weeklyThemes: List<String>,
    val activeOpportunities: String
)

data class PublishingLog(
    val id: String,
    val timestamp: String,
    val platform: SocialPlatform,
    val postTitle: String,
    val status: String,
    val httpStatusCode: Int,
    val responseMessage: String
)

enum class LogStatus(val label: String) {
    SUCCESS("Success"),
    PENDING("Pending"),
    FAILED("Failed"),
    WARNING("Warning")
}

enum class AppLogEventType(val displayTitle: String) {
    APPLICATION_STARTED("Application started"),
    AI_CONTENT_GENERATED("AI content generated"),
    POST_SAVED("Post saved"),
    POST_SCHEDULED("Post scheduled"),
    POST_APPROVED("Post approved"),
    PUBLISHING_ATTEMPTED("Publishing attempted"),
    PUBLISHING_SUCCESSFUL("Publishing successful"),
    PUBLISHING_FAILED("Publishing failed"),
    ACCOUNT_CONNECTED("Social account connected"),
    ACCOUNT_DISCONNECTED("Social account disconnected"),
    SAVED_MAPS_LINK_TESTED("Saved Google Maps link tested"),
    WHATSAPP_LINK_TESTED("Business WhatsApp link tested"),
    WHATSAPP_CONSENT_REGISTERED("Customer WhatsApp consent recorded"),
    WHATSAPP_CAMPAIGN_PREPARED("WhatsApp campaign prepared"),
    WHATSAPP_CAMPAIGN_APPROVED("WhatsApp campaign approved"),
    WHATSAPP_CAMPAIGN_DISPATCHED("WhatsApp campaign dispatch attempted")
}

data class AppLogEntry(
    val id: String,
    val timestampRiyadh: String,
    val platform: SocialPlatform?,
    val eventType: AppLogEventType,
    val status: LogStatus,
    val summary: String,
    val errorDetails: String? = null
)

data class BusinessProfile(
    val restaurantName: String = "مطعم سفرة النعيمي الأصيل",
    val primaryCity: SaudiCity = SaudiCity.RIYADH,
    val savedGoogleMapsUri: String = "https://maps.app.goo.gl/SufraRiyadhSample",
    val whatsAppNumber: String = "+966501234567",
    val autoAppendLinksToCaptions: Boolean = true,
    val crNumber: String = "1010XXXXXX",
    val taxNumber: String = "300XXXXXXXXXXXX"
) {
    val cleanInternationalWhatsAppNumber: String
        get() {
            val digits = whatsAppNumber.filter { it.isDigit() }
            return when {
                digits.startsWith("966") -> digits
                digits.startsWith("05") -> "966" + digits.substring(1)
                digits.startsWith("5") -> "966$digits"
                else -> digits
            }
        }

    val directWhatsAppUri: String
        get() = "https://wa.me/$cleanInternationalWhatsAppNumber"
}

enum class WhatsAppConsentSource(val labelAr: String, val labelEn: String) {
    DINE_IN_QR("مسح QR الطاولة في المطعم", "Dine-In Table QR Scan"),
    WEBSITE_RESERVATION("حجز الطاولة عبر الموقع", "Website Reservation Form"),
    COUNTER_CHECKOUT("نقطة البيع عند المحاسب", "POS Counter Checkout"),
    DELIVERY_ORDER("طلب التوصيل المباشر", "Direct Delivery Order")
}

data class WhatsAppConsentRecord(
    val id: String,
    val customerName: String,
    val phoneNumber: String,
    val consentTimestamp: String,
    val source: WhatsAppConsentSource,
    val isOptedIn: Boolean = true,
    val notes: String = "Explicit marketing opt-in verified"
)

data class WhatsAppMarketingTemplate(
    val id: String,
    val name: String,
    val metaTemplateName: String,
    val category: String,
    val headlineAr: String,
    val headlineEn: String,
    val bodyTemplateAr: String,
    val bodyTemplateEn: String,
    val callToActionAr: String,
    val callToActionEn: String
)

enum class WhatsAppCampaignStatus(val label: String, val colorHex: Long) {
    DRAFT("Draft", 0xFF757575),
    AWAITING_APPROVAL("Awaiting Approval", 0xFFE65100),
    APPROVED("Approved by Owner", 0xFF1565C0),
    DISPATCHED("Dispatched via Meta", 0xFF2E7D32),
    FAILED("Dispatch Failed", 0xFFC62828)
}

data class WhatsAppCampaign(
    val id: String,
    val title: String,
    val templateId: String,
    val scheduledTimeSlot: String,
    val scheduledDate: String,
    var status: WhatsAppCampaignStatus,
    val targetRecipientCount: Int,
    var previewAr: String,
    var previewEn: String,
    var isApprovedByOwner: Boolean = false,
    var dispatchedTimestamp: String? = null,
    var lastErrorReason: String? = null
)


