package com.example.data.model

/**
 * Supported Saudi cities for Google Maps & AI Review Intelligence.
 */
enum class SaudiMapCity(
    val englishName: String,
    val arabicName: String,
    val lat: Double,
    val lng: Double,
    val zoomLevel: Float = 12.0f
) {
    JEDDAH("Jeddah", "جدة", 21.4858, 39.1925, 12.0f),
    RIYADH("Riyadh", "الرياض", 24.7136, 46.6753, 11.5f),
    MAKKAH("Makkah", "مكة المكرمة", 21.3891, 39.8579, 12.5f),
    MADINAH("Madinah", "المدينة المنورة", 24.5247, 39.5692, 12.5f),
    DAMMAM("Dammam", "الدمام", 26.4207, 50.0888, 12.0f),
    KHOBAR("Khobar", "الخبر", 26.2172, 50.1971, 12.5f),
    TAIF("Taif", "الطائف", 21.4373, 40.5127, 12.0f),
    ABHA("Abha", "أبها", 18.2164, 42.5053, 12.0f)
}

/**
 * 10 Official Food Categories specified by user requirement.
 */
enum class MapFoodCategory(
    val englishName: String,
    val arabicName: String,
    val searchKeyword: String
) {
    SAUDI("Saudi restaurants", "مطاعم سعودية", "Saudi restaurant"),
    ARABIC("Arabic restaurants", "مطاعم عربية", "Arabic restaurant"),
    FAST_FOOD("Fast food", "وجبات سريعة", "Fast food restaurant"),
    CAFES("Cafes", "مقاهي وكافيهات", "Cafe specialty coffee"),
    BAKERIES("Bakeries", "مخابز ومعجنات", "Bakery pastry"),
    SEAFOOD("Seafood", "مأكولات بحرية", "Seafood restaurant"),
    DESSERTS("Desserts", "حلويات", "Dessert shop"),
    PIZZA("Pizza", "بيتزا", "Pizza restaurant"),
    BURGERS("Burgers", "برجر", "Burger restaurant"),
    HEALTHY_FOOD("Healthy food", "أكل صحي", "Healthy organic restaurant")
}

/**
 * Google Places API (New) Restaurant Entity
 */
data class SaudiRestaurant(
    val id: String, // Places place_id
    val name: String,
    val city: SaudiMapCity,
    val category: MapFoodCategory,
    val rating: Double,
    val userRatingCount: Int,
    val formattedAddress: String,
    val latitude: Double,
    val longitude: Double,
    val websiteUri: String? = null,
    val nationalPhoneNumber: String? = null,
    val regularOpeningHours: List<String> = emptyList(),
    val googleMapsUri: String = "https://maps.google.com/?cid=$id",
    val priceLevel: String? = "$$",
    val reviews: List<RestaurantReview> = emptyList(),
    val isVerifiedGoogleListing: Boolean = true,
    val isLivePlacesData: Boolean = false
)

/**
 * Google Places Review (maximum 5 reviews per Google Places API policy)
 */
data class RestaurantReview(
    val id: String,
    val authorName: String,
    val rating: Int,
    val relativePublishTimeDescription: String,
    val text: String,
    val languageCode: String? = "ar",
    val authorPhotoUrl: String? = null
)

/**
 * Sentiment Classification Categories
 */
enum class SentimentPolarity(val label: String, val colorHex: Long) {
    POSITIVE("Positive", 0xFF2E7D32),
    NEGATIVE("Negative", 0xFFC62828),
    NEUTRAL("Neutral", 0xFFF57C00)
}

enum class PositiveSentimentTheme(val displayName: String) {
    FOOD_QUALITY("Food Quality"),
    TASTE("Taste"),
    CUSTOMER_SERVICE("Customer Service"),
    CLEANLINESS("Cleanliness"),
    ATMOSPHERE("Atmosphere"),
    VALUE_FOR_MONEY("Value for Money")
}

enum class NegativeSentimentTheme(val displayName: String) {
    POOR_FOOD_QUALITY("Poor Food Quality"),
    SLOW_SERVICE("Slow Service"),
    HIGH_PRICES("High Prices"),
    LONG_WAITING_TIMES("Long Waiting Times"),
    CLEANLINESS_COMPLAINTS("Cleanliness Complaints"),
    DELIVERY_PROBLEMS("Delivery Problems"),
    CUSTOMER_SERVICE_COMPLAINTS("Customer Service Complaints")
}

enum class NeutralSentimentTheme(val displayName: String) {
    GENERAL_OBSERVATIONS("General Observations"),
    MIXED_OPINIONS("Mixed Opinions"),
    SUGGESTIONS("Suggestions")
}

/**
 * Represents a single classified review snippet with explicit distinction between
 * actual customer quote and AI interpretation.
 */
data class ClassifiedReviewItem(
    val actualCustomerQuote: String,
    val reviewerName: String,
    val rating: Int,
    val polarity: SentimentPolarity,
    val matchedTheme: String,
    val aiInterpretation: String
)

/**
 * Complete AI Review Intelligence Report for a single restaurant
 */
data class AiReviewReport(
    val restaurantId: String,
    val restaurantName: String,
    val overallSentiment: String, // e.g. "Highly Positive (80% Positive, 10% Neutral, 10% Negative)"
    val positiveScorePercent: Int,
    val negativeScorePercent: Int,
    val neutralScorePercent: Int,
    val reviewsAnalyzedCount: Int, // Max 5 as per Google Places API
    val topPositiveThemes: List<String>,
    val topNegativeThemes: List<String>,
    val commonCustomerComplaints: List<String>,
    val mostAppreciatedFeatures: List<String>,
    val frequentlyMentionedFoodItems: List<String>,
    val customerSatisfactionOpportunities: List<String>,
    val businessImprovementRecommendations: List<String>,
    val classifiedReviewItems: List<ClassifiedReviewItem>,
    val rawGeminiAnalysisSummary: String? = null,
    val disclaimer: String = "Analysis is based strictly on available returned reviews (up to 5 per Google Places API policy). Actual customer quotes are strictly distinguished from AI interpretations."
)

/**
 * Practical Business Recommendations derived from customer review sentiment
 */
data class AiBusinessRecommendations(
    val serviceImprovements: List<String>,
    val deliveryImprovements: List<String>,
    val competitiveOutperformances: List<String>,
    val marketingContentIdeas: List<String>,
    val newFoodBusinessOpportunities: List<String>
)

/**
 * Competitor Comparison Analysis (up to 5 restaurants)
 */
data class CompetitorComparisonReport(
    val comparedRestaurants: List<SaudiRestaurant>,
    val comparisonSummary: String,
    val relativeStrengths: Map<String, List<String>>, // RestaurantId -> Strengths
    val relativeChallenges: Map<String, List<String>>, // RestaurantId -> Challenges
    val objectiveObservation: String,
    val strategicMarketingAdvice: List<String>,
    val disclaimer: String = "Objective comparison based on Google Places data and customer reviews. Higher ratings do not definitively prove better food or business performance."
)

/**
 * Generated Social Media Content ready to export to Studio and Calendar
 */
data class ReviewSocialCampaign(
    val restaurantName: String,
    val highlightedDish: String,
    val actualCustomerQuote: String,
    val arabicCaption: String,
    val englishCaption: String,
    val hashtags: List<String>,
    val photographyPrompt: String,
    val shortVideoConcept: String,
    val recommendedTimeSlot: String = "01:00 PM",
    val targetAudience: String
)

/**
 * Places API Status state representing live API connection vs missing setup / error
 */
enum class PlacesApiStatus {
    LIVE_CONNECTED,
    API_ERROR,
    SETUP_REQUIRED_DEMO_MODE
}

data class PlacesSearchState(
    val results: List<SaudiRestaurant> = emptyList(),
    val status: PlacesApiStatus = PlacesApiStatus.SETUP_REQUIRED_DEMO_MODE,
    val isLiveApi: Boolean = false,
    val statusBannerText: String = "",
    val errorDetails: String? = null,
    val httpStatusCode: Int? = null,
    val querySearched: String = "",
    val citySearched: SaudiMapCity = SaudiMapCity.RIYADH
)
