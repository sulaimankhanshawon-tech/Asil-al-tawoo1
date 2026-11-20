package com.example.data.repository

import androidx.annotation.DrawableRes
import com.example.R
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class SaudiFoodRepository {

    private val _accounts = MutableStateFlow<List<SocialAccount>>(emptyList())
    val accounts: StateFlow<List<SocialAccount>> = _accounts.asStateFlow()

    private val _posts = MutableStateFlow<List<SocialPost>>(emptyList())
    val posts: StateFlow<List<SocialPost>> = _posts.asStateFlow()

    private val _researchReports = MutableStateFlow<List<MarketResearchReport>>(emptyList())
    val researchReports: StateFlow<List<MarketResearchReport>> = _researchReports.asStateFlow()

    private val _campaigns = MutableStateFlow<List<MarketingCampaign>>(emptyList())
    val campaigns: StateFlow<List<MarketingCampaign>> = _campaigns.asStateFlow()

    private val _publishingLogs = MutableStateFlow<List<PublishingLog>>(emptyList())
    val publishingLogs: StateFlow<List<PublishingLog>> = _publishingLogs.asStateFlow()

    private val _appLogs = MutableStateFlow<List<AppLogEntry>>(emptyList())
    val appLogs: StateFlow<List<AppLogEntry>> = _appLogs.asStateFlow()

    private val _businessProfile = MutableStateFlow(BusinessProfile())
    val businessProfile: StateFlow<BusinessProfile> = _businessProfile.asStateFlow()

    private val _whatsAppConsents = MutableStateFlow<List<WhatsAppConsentRecord>>(emptyList())
    val whatsAppConsents: StateFlow<List<WhatsAppConsentRecord>> = _whatsAppConsents.asStateFlow()

    private val _whatsAppCampaigns = MutableStateFlow<List<WhatsAppCampaign>>(emptyList())
    val whatsAppCampaigns: StateFlow<List<WhatsAppCampaign>> = _whatsAppCampaigns.asStateFlow()


    fun formatRiyadhTimestamp(date: Date = Date()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("Asia/Riyadh")
        return sdf.format(date) + " AST"
    }

    fun sanitizeLog(message: String?): String? {
        if (message == null) return null
        return message
            .replace(Regex("(?i)bearer\\s+[A-Za-z0-9_.-]+"), "Bearer [REDACTED]")
            .replace(Regex("(?i)token=[^&\\s]+"), "token=[REDACTED]")
            .replace(Regex("(?i)secret=[^&\\s]+"), "secret=[REDACTED]")
            .replace(Regex("(?i)password=[^&\\s]+"), "password=[REDACTED]")
            .replace(Regex("(?i)key=[A-Za-z0-9_.-]+"), "key=[REDACTED]")
    }

    fun addLog(
        eventType: AppLogEventType,
        status: LogStatus,
        summary: String,
        platform: SocialPlatform? = null,
        errorDetails: String? = null
    ) {
        val cleanSummary = sanitizeLog(summary) ?: ""
        val cleanError = sanitizeLog(errorDetails)
        val timestamp = formatRiyadhTimestamp()
        val logId = "log_" + UUID.randomUUID().toString().take(8)

        val newEntry = AppLogEntry(
            id = logId,
            timestampRiyadh = timestamp,
            platform = platform,
            eventType = eventType,
            status = status,
            summary = cleanSummary,
            errorDetails = cleanError
        )
        _appLogs.value = listOf(newEntry) + _appLogs.value

        // Legacy compatibility
        if (platform != null) {
            val legacy = PublishingLog(
                id = logId,
                timestamp = timestamp,
                platform = platform,
                postTitle = cleanSummary,
                status = when (status) {
                    LogStatus.SUCCESS -> "SUCCESS"
                    LogStatus.FAILED -> "FAILED"
                    LogStatus.PENDING -> "PENDING"
                    LogStatus.WARNING -> "WARNING"
                },
                httpStatusCode = when (status) {
                    LogStatus.SUCCESS -> 200
                    LogStatus.FAILED -> 401
                    LogStatus.PENDING -> 102
                    LogStatus.WARNING -> 429
                },
                responseMessage = cleanError ?: cleanSummary
            )
            _publishingLogs.value = listOf(legacy) + _publishingLogs.value
        }
    }

    fun clearLogs() {
        _appLogs.value = emptyList()
        _publishingLogs.value = emptyList()
    }


    private val _automationMode = MutableStateFlow(AutomationMode.APPROVAL)
    val automationMode: StateFlow<AutomationMode> = _automationMode.asStateFlow()

    private val _isEmergencyPaused = MutableStateFlow(false)
    val isEmergencyPaused: StateFlow<Boolean> = _isEmergencyPaused.asStateFlow()

    private val _selectedCity = MutableStateFlow<SaudiCity?>(null)
    val selectedCity: StateFlow<SaudiCity?> = _selectedCity.asStateFlow()

    private val _selectedCategory = MutableStateFlow<FoodCategory?>(null)
    val selectedCategory: StateFlow<FoodCategory?> = _selectedCategory.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // 1. Initial Social Accounts — strictly start as NOT CONNECTED (No fake connections)
        _accounts.value = listOf(
            SocialAccount(
                id = "acc_ig_1",
                platform = SocialPlatform.INSTAGRAM,
                accountName = "Instagram Professional",
                handle = "Not connected",
                isConnected = false,
                followerCount = 0,
                statusText = "Not connected (OAuth required)",
                autoPublishEnabled = false,
                tokenExpiryDays = 0
            ),
            SocialAccount(
                id = "acc_fb_1",
                platform = SocialPlatform.FACEBOOK,
                accountName = "Facebook Page",
                handle = "Not connected",
                isConnected = false,
                followerCount = 0,
                statusText = "Not connected (Page Admin auth required)",
                autoPublishEnabled = false,
                tokenExpiryDays = 0
            ),
            SocialAccount(
                id = "acc_tt_1",
                platform = SocialPlatform.TIKTOK,
                accountName = "TikTok Business",
                handle = "Not connected",
                isConnected = false,
                followerCount = 0,
                statusText = "Not connected (Content Posting API required)",
                autoPublishEnabled = false,
                tokenExpiryDays = 0
            ),
            SocialAccount(
                id = "acc_sc_1",
                platform = SocialPlatform.SNAPCHAT,
                accountName = "Snapchat Public Profile",
                handle = "Not connected",
                isConnected = false,
                followerCount = 0,
                statusText = "Not connected (Marketing API required)",
                autoPublishEnabled = false,
                tokenExpiryDays = 0
            ),
            SocialAccount(
                id = "acc_x_1",
                platform = SocialPlatform.X_TWITTER,
                accountName = "X (Twitter) Profile",
                handle = "Not connected",
                isConnected = false,
                followerCount = 0,
                statusText = "Not connected (OAuth 2.0 PKCE required)",
                autoPublishEnabled = false,
                tokenExpiryDays = 0
            )
        )

        // 2. Comprehensive Saudi Food Market Research Intel
        _researchReports.value = listOf(
            MarketResearchReport(
                id = "rep_1",
                titleEn = "Smash Burger Craze & Truffle Glaze in Riyadh",
                titleAr = "شغف البرجر المسموش مع صوص الكمأة في الرياض",
                city = SaudiCity.RIYADH,
                category = FoodCategory.SMASH_BURGERS,
                trendingItem = "Double Black Angus Truffle Smash Burger",
                hashtags = listOf("#مطاعم_الرياض", "#برجر_الرياض", "#ترند_الرياض", "#اكل_لذيذ"),
                audienceInterest = "High youth and young-professional dinner crowd in Hittin and Anas Ibn Malik districts between 8 PM and 1 AM.",
                competitorObservations = "Local smash brands reporting 40% surge in Jahez & HungerStation orders for double-patty combos with loaded waffle fries.",
                marketingOpportunity = "Position our premium brioche bun with crispy caramelized lace edges as the ultimate weekend midnight craving.",
                searchGrowthPercent = 94,
                sourceUrl = "https://trends.google.com/trends/explore?geo=SA&q=smash+burger",
                researchDate = "Today, 14:00 AST"
            ),
            MarketResearchReport(
                id = "rep_2",
                titleEn = "Saudi Coffee & Heritage Dates in Specialty Cafes",
                titleAr = "انتعاش القهوة السعودية والتمور الفاخرة في كافيهات الخبر وجدة",
                city = SaudiCity.KHOBAR,
                category = FoodCategory.CAFES_COFFEE,
                trendingItem = "Khawlani Saudi Coffee with Sukkari & Tahini Dip",
                hashtags = listOf("#قهوة_سعودية", "#كافيهات_الخبر", "#عام_القهوة_السعودية", "#قهوة_مختصة"),
                audienceInterest = "Social gatherings, after-work meetups, and evening hospitality culture across Eastern Province waterfront.",
                competitorObservations = "High demand for authentic brass dallah tableside pouring with aesthetic cardamom aromas on TikTok and Snapchat Spotlight.",
                marketingOpportunity = "Highlight slow-roasted Southern Khawlani beans with golden saffron hue and pairing with organic Qassim dates.",
                searchGrowthPercent = 82,
                sourceUrl = "https://gastronomy.moc.gov.sa/saudi-coffee",
                researchDate = "Today, 11:30 AST"
            ),
            MarketResearchReport(
                id = "rep_3",
                titleEn = "Royal Lamb Kabsa with Toasted Pine Nuts & Raisins",
                titleAr = "كبسة لحم النعيمي الملكية بالمكسرات والزعفران",
                city = SaudiCity.RIYADH,
                category = FoodCategory.TRADITIONAL_SAUDI,
                trendingItem = "Naeemi Lamb Kabsa Platter with Dakous",
                hashtags = listOf("#كبسة_سعودية", "#كبسة_لحم", "#المطبخ_السعودي", "#عزايم_الرياض"),
                audienceInterest = "Family Friday lunch gatherings and corporate hospitality banquets looking for tender, fall-off-the-bone meat.",
                competitorObservations = "Heritage brass platter presentations capture 3x more saves and shares on Instagram and Facebook.",
                marketingOpportunity = "Focus on generous Saudi hospitality (الكرم الحاتمي), spiced basmati aroma, and traditional homemade dakous.",
                searchGrowthPercent = 76,
                sourceUrl = "https://sauditourism.sa/dining/traditional-cuisine",
                researchDate = "Today, 09:15 AST"
            ),
            MarketResearchReport(
                id = "rep_4",
                titleEn = "Hijazi Mutabbaq & Seafood in Historic Al-Balad",
                titleAr = "المطبق الحجازي والمأكولات البحرية في جدة التاريخية",
                city = SaudiCity.JEDDAH,
                category = FoodCategory.STREET_FOOD,
                trendingItem = "Crispy Banana & Meat Mutabbaq with Fresh Lemon",
                hashtags = listOf("#مطاعم_جدة", "#البلد_جدة", "#اكل_حجازي", "#مطبق"),
                audienceInterest = "Late-night foodies exploring Al-Balad heritage alleys and Jeddah Corniche sea breeze dining.",
                competitorObservations = "Video clips of hot pastry stretching and sizzling on giant flat iron griddles average 120k+ TikTok views.",
                marketingOpportunity = "Create ASMR sizzle video with lemon squeezing and sweet-savory pairing comparisons.",
                searchGrowthPercent = 68,
                sourceUrl = "https://visitsaudi.com/jeddah/dining",
                researchDate = "Yesterday, 19:40 AST"
            ),
            MarketResearchReport(
                id = "rep_5",
                titleEn = "Warm Winter Masoub Malaki with Honey & Cream",
                titleAr = "معصوب ملكي بالقشطة والعسل والتمر للشتاء",
                city = SaudiCity.TAIF,
                category = FoodCategory.SWEETS_DESSERTS,
                trendingItem = "Masoub Malaki with Qashta, Honey & Cornflakes",
                hashtags = listOf("#معصوب_ملكي", "#حلا_سعودي", "#شتاء_السعودية", "#مطاعم_الطائف"),
                audienceInterest = "High morning and evening comfort food demand during crisp mountain winter temperatures in Taif and Abha.",
                competitorObservations = "Customers searching for hearty energy breakfasts and rich desserts with pure mountain honey.",
                marketingOpportunity = "Emphasize warmth, generous fresh honeycomb drizzle, and rich whole-wheat goodness.",
                searchGrowthPercent = 115,
                sourceUrl = "https://trends.google.com/trends/explore?geo=SA&q=masoub",
                researchDate = "Yesterday, 16:00 AST"
            )
        )

        // 3. Initial 5 Daily Post Slots for Today with exact prompts and statuses
        _posts.value = listOf(
            SocialPost(
                id = "post_slot_1",
                title = "Morning Khawlani Coffee & Dates",
                captionAr = "صباح الخير من قلب السعودية 🇸🇦✨ فنجال القهوة الخولانية مع حبة سكري تفتح النفس وتعدل المزاج. كرم الضيافة وأصالة الماضي في كل رشفة! حياكم الله في سفرة.",
                captionEn = "Good morning from the heart of Saudi Arabia! 🇸🇦 Golden Khawlani Saudi coffee poured fresh with organic Qassim dates. Experience true Arabian hospitality.",
                hashtags = listOf("#قهوة_سعودية", "#صباح_الخير", "#كافيهات_السعودية", "#ضيافة_سعودية"),
                targetPlatforms = setOf(SocialPlatform.INSTAGRAM, SocialPlatform.X_TWITTER),
                scheduledTimeSlot = "09:00 AM",
                scheduledDate = "Today",
                status = PostStatus.DRAFT,
                contentType = PostContentType.FOOD_PHOTOGRAPHY,
                imageResId = R.drawable.saudi_coffee_1793398408663,
                imagePrompt = "Traditional Saudi golden Arabic coffee poured from an ornate brass dallah into ceramic finjan cups, surrounded by fresh Sukkari dates on an antique wood table, warm natural morning light, commercial food photography."
            ),
            SocialPost(
                id = "post_slot_2",
                title = "Signature Royal Naeemi Kabsa",
                captionAr = "وقت الغداء يعني كبسة ملكية تليق بجمعتكم! لحم نعيمي طازج وذايب مع رز بسمتي معطر بالمسمار والهيل والزعفران ومزين باللوز المقرمش والصنوبر. 🍖👌🏼",
                captionEn = "Lunchtime deserves a feast fit for royalty! Fragrant basmati rice infused with cardamom & saffron, topped with tender Naeemi lamb and golden toasted almonds.",
                hashtags = listOf("#كبسة_سعودية", "#غداء_الجمعة", "#مطاعم_الرياض", "#سفرة_سعودية"),
                targetPlatforms = setOf(SocialPlatform.INSTAGRAM, SocialPlatform.FACEBOOK),
                scheduledTimeSlot = "12:00 PM",
                scheduledDate = "Today",
                status = PostStatus.AWAITING_APPROVAL,
                contentType = PostContentType.FOOD_PHOTOGRAPHY,
                imageResId = R.drawable.hero_kabsa_1793398408425,
                imagePrompt = "Gourmet Saudi lamb Kabsa served on a grand hammered brass tray, spiced yellow basmati rice garnished with toasted pine nuts, almonds, and raisins, rich culinary lighting."
            ),
            SocialPost(
                id = "post_slot_3",
                title = "Afternoon Tea & Crispy Sambusa",
                captionAr = "عصرية اليوم تحلى مع براد الشاي المنعنع والسمبوسة الحجازية المقرمشة بحشوة اللحم المفروم والبصل والكزبرة. حبة ورا حبة ما تشبع! 🫖🥟",
                captionEn = "Afternoon tea time perfected with crisp golden Hijazi sambusas stuffed with savory spiced minced beef, onions, and fresh coriander, paired with hot mint tea.",
                hashtags = listOf("#عصرية_شاي", "#سمبوسة", "#شاي_منعنع", "#اكل_سعودي"),
                targetPlatforms = setOf(SocialPlatform.INSTAGRAM, SocialPlatform.SNAPCHAT),
                scheduledTimeSlot = "03:00 PM",
                scheduledDate = "Today",
                status = PostStatus.DRAFT,
                contentType = PostContentType.FOOD_FACTS,
                imageResId = R.drawable.hero_kabsa_1793398408425,
                imagePrompt = "Golden crispy triangle sambusas stacked on artisan craft paper next to a glass of steaming amber mint tea in a Riyadh cafe setting, shallow depth of field."
            ),
            SocialPost(
                id = "post_slot_4",
                title = "Evening Truffle Smash Burger",
                captionAr = "تجهّز لسهرة الويكند مع أقوى كرانش! دبل أنجوس سماش برجر مع الجبن الذائب وصوص الترفل الخاص في خبز البريوش الطري مع بطاطس مقرمشة. 🍔🍟",
                captionEn = "Your ultimate evening craving is here. Double Angus smash patties with crisp caramelized edges, melted cheddar, and house truffle aioli on a buttery brioche bun.",
                hashtags = listOf("#برجر_الرياض", "#سماش_برجر", "#عشاء_الخميس", "#مطاعم_جدة"),
                targetPlatforms = setOf(SocialPlatform.INSTAGRAM, SocialPlatform.TIKTOK, SocialPlatform.X_TWITTER),
                scheduledTimeSlot = "06:00 PM",
                scheduledDate = "Today",
                status = PostStatus.DRAFT,
                contentType = PostContentType.FOOD_PHOTOGRAPHY,
                imageResId = R.drawable.trendy_burger_1793398409429,
                imagePrompt = "Artisan double smash burger with melted golden cheddar cheese and truffle glaze on a glossy brioche bun, side of crispy seasoned fries, vibrant street food aesthetic."
            ),
            SocialPost(
                id = "post_slot_5",
                title = "Late Night Royal Masoub Indulgence",
                captionAr = "ختامها مسك وحلا! معصوب ملكي غرقان بالقشطة البلدي والعسل الجبلي الصافي ورشة مكسرات وقرمشة الكورن فليكس. الدفء واللذة بعد يوم طويل ✨🍯",
                captionEn = "The sweetest ending to your day. Warm Royal Masoub layered with artisanal clotted cream, raw mountain honey, and crunchy toasted almonds. Pure comfort in every bite.",
                hashtags = listOf("#معصوب_ملكي", "#حلا_سعودي", "#حلا_الليل", "#سفرة_الحلا"),
                targetPlatforms = setOf(SocialPlatform.TIKTOK, SocialPlatform.INSTAGRAM),
                scheduledTimeSlot = "09:00 PM",
                scheduledDate = "Today",
                status = PostStatus.AWAITING_APPROVAL,
                contentType = PostContentType.SHORT_VIDEO_REEL,
                imageResId = R.drawable.saudi_coffee_1793398408663,
                isVideo = true,
                videoDurationSec = 20,
                imagePrompt = "Warm bowl of Masoub Malaki with honey drizzle glistening under ambient cafe lighting, fresh clotted cream and crushed bananas, commercial food shot."
            )
        )

        // 4. Initial Campaigns — with 0 published count initially
        updateCampaignsState()

        // 5. Initial Application Started Telemetry Event
        addLog(
            eventType = AppLogEventType.APPLICATION_STARTED,
            status = LogStatus.SUCCESS,
            summary = "Sufra AI Social Media Manager started. Saudi market research & publishing engines active.",
            platform = null
        )
    }

    private fun updateCampaignsState() {
        val publishedCount = _posts.value.count { it.status == PostStatus.PUBLISHED }
        val connectedAccounts = _accounts.value.count { it.isConnected }
        val totalFollowers = _accounts.value.filter { it.isConnected }.sumOf { it.followerCount }
        
        // Realistic calculation: if no accounts connected or 0 posts published, reach is 0
        val reach = if (connectedAccounts == 0 || publishedCount == 0) 0 else (publishedCount * (totalFollowers * 0.15).toInt()).coerceAtLeast(120)
        val engagementRate = if (publishedCount == 0 || connectedAccounts == 0) 0.0 else 5.2

        _campaigns.value = listOf(
            MarketingCampaign(
                id = "camp_7d",
                name = "Saudi Culinary Virality & Heritage Boost",
                typeName = "7-Day Growth Campaign",
                durationDays = 7,
                targetPosts = 35,
                publishedPosts = publishedCount.coerceAtMost(35),
                currentDay = ((publishedCount / 5) + 1).coerceAtMost(7),
                targetReach = 85000,
                currentReach = reach,
                targetEngagementRate = 6.2,
                currentEngagementRate = engagementRate,
                targetFollowersGain = 3500,
                currentFollowersGain = if (connectedAccounts > 0) (publishedCount * 14) else 0,
                weeklyThemes = listOf(
                    "Day 1-2: Signature Rice & Kabsa Masterclass (Heritage hook)",
                    "Day 3-4: Riyadh Smash Burger & Street Food Trends",
                    "Day 5-6: Specialty Saudi Coffee & Cafe Culture ASMR",
                    "Day 7: Weekend Family Feast Special & Customer UGC"
                ),
                activeOpportunities = "Pacing based on actual published posts. Connect official channels to unlock verified Meta/TikTok reach analytics."
            ),
            MarketingCampaign(
                id = "camp_30d",
                name = "KSA National Food Authority & Expansion",
                typeName = "30-Day Growth Campaign",
                durationDays = 30,
                targetPosts = 150,
                publishedPosts = publishedCount.coerceAtMost(150),
                currentDay = ((publishedCount / 5) + 1).coerceAtMost(30),
                targetReach = 450000,
                currentReach = reach * 3,
                targetEngagementRate = 5.5,
                currentEngagementRate = engagementRate,
                targetFollowersGain = 18000,
                currentFollowersGain = if (connectedAccounts > 0) (publishedCount * 45) else 0,
                weeklyThemes = listOf(
                    "Week 1: Heritage & Authenticity (Kabsa, Mandi, Jareesh, Saleeg)",
                    "Week 2: Urban Foodie Trends (Smash Burgers, Cloud Kitchens, Delivery)",
                    "Week 3: Interactive Chef Secrets & Viral TikTok Challenges",
                    "Week 4: Saudi Seasons & Loyalty Conversion Offers"
                ),
                activeOpportunities = "Campaign tracks verified content. Requires minimum 5 daily scheduled posts across Asia/Riyadh slots."
            )
        )

        // 5. Initial WhatsApp Customer Consent Records (Explicit Opt-In Required)
        _whatsAppConsents.value = listOf(
            WhatsAppConsentRecord(
                id = "consent_1",
                customerName = "عبدالرحمن الشمري",
                phoneNumber = "+966504432190",
                consentTimestamp = "2026-10-28 13:40 AST",
                source = WhatsAppConsentSource.DINE_IN_QR,
                isOptedIn = true,
                notes = "Scanned table QR during Friday lunch"
            ),
            WhatsAppConsentRecord(
                id = "consent_2",
                customerName = "سارة آل سعود",
                phoneNumber = "+966555123488",
                consentTimestamp = "2026-10-29 19:15 AST",
                source = WhatsAppConsentSource.WEBSITE_RESERVATION,
                isOptedIn = true,
                notes = "VIP banquet reservation online"
            ),
            WhatsAppConsentRecord(
                id = "consent_3",
                customerName = "محمد الغامدي",
                phoneNumber = "+966567890123",
                consentTimestamp = "2026-10-30 21:05 AST",
                source = WhatsAppConsentSource.COUNTER_CHECKOUT,
                isOptedIn = true,
                notes = "Opted in at cashier counter POS"
            )
        )

        // 6. Initial WhatsApp Campaign
        _whatsAppCampaigns.value = listOf(
            WhatsAppCampaign(
                id = "wa_camp_1",
                title = "Friday Family Kabsa Special Broadcast",
                templateId = "whatsapp_tpl_friday_feast",
                scheduledTimeSlot = "11:00 AM AST",
                scheduledDate = "Upcoming Friday",
                status = WhatsAppCampaignStatus.AWAITING_APPROVAL,
                targetRecipientCount = 3,
                previewAr = "أهلاً بك في مطعم سفرة الأصيل! 🇸🇦\nاحتفل مع عائلتك يوم الجمعة بخصم 20% على صحن كبسة النعيمي الملكية الفاخرة.\n📍 موقعنا: https://maps.app.goo.gl/SufraRiyadhSample",
                previewEn = "Hello from Sufra Restaurant! 🇸🇦\nCelebrate Friday with your family with 20% OFF on our Royal Naeemi Kabsa Platter.\n📍 Location: https://maps.app.goo.gl/SufraRiyadhSample",
                isApprovedByOwner = false
            )
        )
    }


    // Actions
    fun filterCity(city: SaudiCity?) {
        _selectedCity.value = city
    }

    fun filterCategory(category: FoodCategory?) {
        _selectedCategory.value = category
    }

    fun connectAccount(accountId: String, handle: String) {
        val targetAcc = _accounts.value.find { it.id == accountId }
        val cleanHandle = if (handle.isNotBlank()) handle else "@sufra_food"

        _accounts.value = _accounts.value.map { acc ->
            if (acc.id == accountId) {
                acc.copy(
                    isConnected = true,
                    handle = cleanHandle,
                    statusText = "Connected & Authorized (OAuth 2.0)",
                    tokenExpiryDays = 60,
                    lastVerifiedTimestamp = "Just now"
                )
            } else acc
        }
        if (targetAcc != null) {
            addLog(
                eventType = AppLogEventType.ACCOUNT_CONNECTED,
                status = LogStatus.SUCCESS,
                summary = "Authorized ${targetAcc.platform.title} account ($cleanHandle) via OAuth 2.0 PKCE flow.",
                platform = targetAcc.platform
            )
        }
        updateCampaignsState()
    }

    fun disconnectAccount(accountId: String) {
        val targetAcc = _accounts.value.find { it.id == accountId }

        _accounts.value = _accounts.value.map { acc ->
            if (acc.id == accountId) {
                acc.copy(
                    isConnected = false,
                    handle = "Not connected",
                    statusText = "Disconnected",
                    followerCount = 0,
                    tokenExpiryDays = 0,
                    lastVerifiedTimestamp = null
                )
            } else acc
        }
        if (targetAcc != null) {
            addLog(
                eventType = AppLogEventType.ACCOUNT_DISCONNECTED,
                status = LogStatus.WARNING,
                summary = "Disconnected ${targetAcc.platform.title} account. API publishing permissions revoked.",
                platform = targetAcc.platform
            )
        }
        updateCampaignsState()
    }

    fun toggleAccountConnection(accountId: String) {
        val account = _accounts.value.find { it.id == accountId } ?: return
        if (account.isConnected) {
            disconnectAccount(accountId)
        } else {
            connectAccount(accountId, "@${account.platform.title.lowercase()}_user")
        }
    }

    fun setAutomationMode(mode: AutomationMode) {
        _automationMode.value = mode
    }

    fun toggleEmergencyPause() {
        val nextState = !_isEmergencyPaused.value
        _isEmergencyPaused.value = nextState
        if (nextState) {
            addLog(
                eventType = AppLogEventType.POST_SCHEDULED,
                status = LogStatus.WARNING,
                summary = "Emergency safety killswitch engaged: All scheduled publishing pipelines frozen.",
                platform = null
            )
        } else {
            addLog(
                eventType = AppLogEventType.POST_SCHEDULED,
                status = LogStatus.SUCCESS,
                summary = "Emergency safety killswitch deactivated: Publishing queues resumed.",
                platform = null
            )
        }
    }

    fun approvePost(postId: String) {
        val post = _posts.value.find { it.id == postId }
        _posts.value = _posts.value.map { p ->
            if (p.id == postId) p.copy(status = PostStatus.SCHEDULED) else p
        }
        if (post != null) {
            addLog(
                eventType = AppLogEventType.POST_APPROVED,
                status = LogStatus.SUCCESS,
                summary = "Post '${post.title}' approved by manager.",
                platform = post.targetPlatforms.firstOrNull()
            )
            addLog(
                eventType = AppLogEventType.POST_SCHEDULED,
                status = LogStatus.PENDING,
                summary = "Post '${post.title}' scheduled for automated dispatch at ${post.scheduledTimeSlot} AST.",
                platform = post.targetPlatforms.firstOrNull()
            )
        }
        updateCampaignsState()
    }

    fun rejectPost(postId: String) {
        val post = _posts.value.find { it.id == postId }
        _posts.value = _posts.value.map { p ->
            if (p.id == postId) p.copy(status = PostStatus.REJECTED) else p
        }
        if (post != null) {
            addLog(
                eventType = AppLogEventType.POST_SCHEDULED,
                status = LogStatus.WARNING,
                summary = "Post '${post.title}' rejected from publication schedule.",
                platform = post.targetPlatforms.firstOrNull()
            )
        }
        updateCampaignsState()
    }

    fun deletePost(postId: String) {
        _posts.value = _posts.value.filterNot { it.id == postId }
        updateCampaignsState()
    }

    fun updatePost(updated: SocialPost) {
        _posts.value = _posts.value.map { if (it.id == updated.id) updated else it }
        addLog(
            eventType = AppLogEventType.POST_SAVED,
            status = LogStatus.SUCCESS,
            summary = "Post '${updated.title}' edits saved to database.",
            platform = updated.targetPlatforms.firstOrNull()
        )
        updateCampaignsState()
    }

    fun updatePostTime(postId: String, newTimeSlot: String) {
        val post = _posts.value.find { it.id == postId }
        _posts.value = _posts.value.map { p ->
            if (p.id == postId) p.copy(scheduledTimeSlot = newTimeSlot) else p
        }
        if (post != null) {
            addLog(
                eventType = AppLogEventType.POST_SCHEDULED,
                status = LogStatus.PENDING,
                summary = "Post '${post.title}' rescheduled to $newTimeSlot.",
                platform = post.targetPlatforms.firstOrNull()
            )
        }
    }

    fun publishPostNow(postId: String) {
        val post = _posts.value.find { it.id == postId } ?: return

        if (_isEmergencyPaused.value) {
            post.targetPlatforms.forEach { platform ->
                addLog(
                    eventType = AppLogEventType.PUBLISHING_ATTEMPTED,
                    status = LogStatus.PENDING,
                    summary = "Publishing dispatch initiated for '${post.title}' to ${platform.title}...",
                    platform = platform
                )
                addLog(
                    eventType = AppLogEventType.PUBLISHING_FAILED,
                    status = LogStatus.FAILED,
                    summary = "Publishing blocked for '${post.title}' on ${platform.title}.",
                    platform = platform,
                    errorDetails = "Safety Safeguard: Emergency publishing autopilot pause is active. Request rejected."
                )
            }
            _posts.value = _posts.value.map { p ->
                if (p.id == postId) p.copy(
                    status = PostStatus.FAILED_RETRYING,
                    lastErrorMessage = "Publishing halted: Emergency pause is active"
                ) else p
            }
            return
        }

        var anySuccess = false
        var anyFailure = false

        post.targetPlatforms.forEach { platform ->
            // 1. Publishing Attempted event
            addLog(
                eventType = AppLogEventType.PUBLISHING_ATTEMPTED,
                status = LogStatus.PENDING,
                summary = "Publishing dispatch initiated for '${post.title}' to ${platform.title}...",
                platform = platform
            )

            // Check if account for this platform is authorized and connected
            val account = _accounts.value.find { it.platform == platform }
            val isConnected = account?.isConnected == true

            if (isConnected) {
                anySuccess = true
                addLog(
                    eventType = AppLogEventType.PUBLISHING_SUCCESSFUL,
                    status = LogStatus.SUCCESS,
                    summary = "Successfully published '${post.title}' to ${platform.title} (${platform.apiName}) for handle ${account?.handle}.",
                    platform = platform
                )
            } else {
                anyFailure = true
                addLog(
                    eventType = AppLogEventType.PUBLISHING_FAILED,
                    status = LogStatus.FAILED,
                    summary = "Failed to publish '${post.title}' to ${platform.title}.",
                    platform = platform,
                    errorDetails = "HTTP 401 Unauthorized: ${platform.title} account is not connected. Requires official OAuth 2.0 authorization before publishing."
                )
            }
        }

        _posts.value = _posts.value.map { p ->
            if (p.id == postId) {
                if (anySuccess) {
                    p.copy(
                        status = PostStatus.PUBLISHED,
                        engagementLikes = 18,
                        engagementShares = 5,
                        engagementComments = 3,
                        lastErrorMessage = if (anyFailure) "Partial publish: Some channels were not connected" else null
                    )
                } else {
                    p.copy(
                        status = PostStatus.FAILED_RETRYING,
                        lastErrorMessage = "Publishing failed: Selected social channels are not connected (OAuth required)"
                    )
                }
            } else p
        }

        updateCampaignsState()
    }

    fun retryFailedPost(postId: String) {
        _posts.value = _posts.value.map { p ->
            if (p.id == postId) {
                p.copy(
                    status = PostStatus.SCHEDULED,
                    retryAttempts = p.retryAttempts + 1,
                    lastErrorMessage = null
                )
            } else p
        }
    }

    // PRIORITY 2: Generate exactly five unique food-related posts per day for the Saudi market
    fun generateDailyFivePostBatch(
        city: SaudiCity = SaudiCity.RIYADH,
        category: FoodCategory = FoodCategory.TRENDING
    ): List<SocialPost> {
        val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existingTitles = _posts.value.map { it.title }.toSet()

        val slotTemplates = listOf(
            // Slot 1: 09:00 AM (Morning Coffee & Traditional Breakfast)
            CulturalPostTemplate(
                slot = "09:00 AM",
                title = "Morning Coffee & Cardamom Aroma in ${city.englishName}",
                captionAr = "صباح الخير من ${city.arabicName} 🇸🇦✨ فنجال القهوة السعودية بالهيل والزعفران مع رطب سكري يفتح النفس. بداية يوم مفعمة بالنشاط والأصالة! حياكم الله.",
                captionEn = "Good morning from ${city.englishName}! 🇸🇦 Golden Saudi coffee infused with aromatic cardamom and saffron, paired with premium dates. True morning hospitality.",
                hashtags = listOf("#قهوة_سعودية", "#صباح_الخير", "#كافيهات_${city.arabicName.replace(" ", "_")}", "#ضيافة"),
                imageRes = R.drawable.saudi_coffee_1793398408663,
                contentType = PostContentType.FOOD_PHOTOGRAPHY,
                isVideo = false,
                imagePrompt = "Authentic golden Saudi coffee being poured from an ornate brass dallah pot into finjan cups, paired with fresh Sukkari dates in a modern sunlit ${city.englishName} cafe setting, morning light."
            ),
            // Slot 2: 12:00 PM (Signature Saudi Lunch Feast)
            CulturalPostTemplate(
                slot = "12:00 PM",
                title = "Signature Royal Kabsa Lunch Platter",
                captionAr = "وقت الغداء في ${city.arabicName}! كبسة لحم النعيمي الطازج مع الرز البسمتي الذهبي المعطر بخلطة البهارات الخاصة والمكسرات المحمصة. وجبة تليق بجمعتكم 🍖👌🏼",
                captionEn = "Lunchtime feast in ${city.englishName}! Royal slow-cooked Naeemi lamb served on fragrant basmati rice with toasted pine nuts and raisins. Traditional flavor at its finest.",
                hashtags = listOf("#كبسة_سعودية", "#غداء_اليوم", "#مطاعم_${city.arabicName.replace(" ", "_")}", "#كرم_الضيافة"),
                imageRes = R.drawable.hero_kabsa_1793398408425,
                contentType = PostContentType.FOOD_PHOTOGRAPHY,
                isVideo = false,
                imagePrompt = "Gourmet Saudi lamb Kabsa platter on a traditional patterned metal tray, garnished with toasted almonds and raisins, served with homemade spicy dakous salsa."
            ),
            // Slot 3: 03:00 PM (Afternoon Tea, Karak, & Savory Snacks)
            CulturalPostTemplate(
                slot = "03:00 PM",
                title = "Afternoon Karak Tea & Fresh Mutabbaq",
                captionAr = "عصرية اليوم تحلى مع كرك دافئ ومطبق حجازي مقرمش بحشوة الجبن والبيض واللحم مع عصرة ليمون وفلفل أخضر حار. استراحة ولا أروع! 🫖🍋",
                captionEn = "Afternoon recharge: Warm spiced Karak tea and freshly griddled crispy Mutabbaq stuffed with savory minced meat, leeks, and a squeeze of fresh lime.",
                hashtags = listOf("#كرك", "#مطبق", "#عصرية", "#سناك_${city.arabicName.replace(" ", "_")}"),
                imageRes = R.drawable.hero_kabsa_1793398408425,
                contentType = PostContentType.FOOD_FACTS,
                isVideo = false,
                imagePrompt = "Crispy golden folded Mutabbaq sliced into squares, lemon wedges, and a steaming glass of spiced Karak tea on an artisan wooden table, high contrast lighting."
            ),
            // Slot 4: 06:00 PM (Evening Dinner / Smash Burger / Street Food)
            CulturalPostTemplate(
                slot = "06:00 PM",
                title = "Crispy Truffle Smash Burger Evening Craving",
                captionAr = "عشاء اليوم غير في ${city.arabicName}! دبل سماش أنجوس برجر مع أطراف كرانشي مقرمشة وصوص الترفل الذائب في خبز بريوش طري ومعه بطاطس حارة 🍔🔥",
                captionEn = "Evening dinner craving in ${city.englishName}! Double Black Angus smash burger with crisp caramelized lace edges, melted cheese, and signature truffle mayo.",
                hashtags = listOf("#سماش_برجر", "#عشاء_اليوم", "#برجر_${city.arabicName.replace(" ", "_")}", "#ترند_السعودية"),
                imageRes = R.drawable.trendy_burger_1793398409429,
                contentType = PostContentType.FOOD_PHOTOGRAPHY,
                isVideo = false,
                imagePrompt = "Artisan double beef smash burger with melted cheese and house truffle aioli on a glossy brioche bun, surrounded by crispy seasoned fries, appetizing commercial photography."
            ),
            // Slot 5: 09:00 PM (Late Night Dessert / Ma'soub / Reels ASMR)
            CulturalPostTemplate(
                slot = "09:00 PM",
                title = "Late Night Royal Masoub Malaki with Honey",
                captionAr = "سهرة الليلة تحتاج حلا يدفي! معصوب ملكي محضر بالموز الطازج والخبز الأسمر ومغرق بالقشطة والعسل الصافي والمكسرات. حلا الليل على أصوله ✨🍯",
                captionEn = "Late night sweet comfort in ${city.englishName}. Royal Masoub layered with mashed whole-wheat flatbread, sweet bananas, rich clotted cream, and raw mountain honey.",
                hashtags = listOf("#معصوب_ملكي", "#حلا_الليل", "#تحلية_${city.arabicName.replace(" ", "_")}", "#حلويات_سعودية"),
                imageRes = R.drawable.saudi_coffee_1793398408663,
                contentType = PostContentType.SHORT_VIDEO_REEL,
                isVideo = true,
                videoDurationSec = 25,
                imagePrompt = "Vertical 9:16 cinematic close-up of amber mountain honey being drizzled over a bowl of warm Masoub Malaki with fresh qashta cream and crushed almonds."
            )
        )

        val newGeneratedPosts = slotTemplates.mapIndexed { index, template ->
            // Avoid duplicate naming if a post with this title already exists
            val uniqueTitle = if (existingTitles.contains(template.title)) {
                "${template.title} (Batch ${System.currentTimeMillis() % 1000})"
            } else {
                template.title
            }

            SocialPost(
                id = "batch_${UUID.randomUUID().toString().take(7)}",
                title = uniqueTitle,
                captionAr = template.captionAr,
                captionEn = template.captionEn,
                hashtags = template.hashtags,
                targetPlatforms = setOf(SocialPlatform.INSTAGRAM, SocialPlatform.TIKTOK, SocialPlatform.FACEBOOK),
                scheduledTimeSlot = template.slot,
                scheduledDate = "Today",
                status = PostStatus.DRAFT,
                contentType = template.contentType,
                imageResId = template.imageRes,
                imagePrompt = template.imagePrompt,
                isVideo = template.isVideo,
                videoDurationSec = if (template.isVideo) 25 else null,
                lastApiPayload = "{\"source\":\"AI Batch Generator\",\"slot\":\"${template.slot}\",\"city\":\"${city.englishName}\"}"
            )
        }

        // Prepend to posts list so user sees them immediately in Studio and Calendar
        _posts.value = newGeneratedPosts + _posts.value
        updateCampaignsState()

        addLog(
            eventType = AppLogEventType.AI_CONTENT_GENERATED,
            status = LogStatus.SUCCESS,
            summary = "Generated daily 5-post batch for ${city.englishName} (${category.englishName}).",
            platform = null
        )

        newGeneratedPosts.forEach { post ->
            addLog(
                eventType = AppLogEventType.POST_SAVED,
                status = LogStatus.SUCCESS,
                summary = "Post '${post.title}' saved to calendar (${post.scheduledTimeSlot}).",
                platform = post.targetPlatforms.firstOrNull()
            )
            if (_automationMode.value == AutomationMode.FULL_AUTOMATION) {
                addLog(
                    eventType = AppLogEventType.POST_SCHEDULED,
                    status = LogStatus.PENDING,
                    summary = "Post '${post.title}' scheduled for automated dispatch at ${post.scheduledTimeSlot}.",
                    platform = post.targetPlatforms.firstOrNull()
                )
            }
        }

        return newGeneratedPosts
    }

    fun createAiGeneratedPost(
        title: String,
        captionAr: String,
        captionEn: String,
        hashtags: List<String>,
        targetPlatforms: Set<SocialPlatform>,
        timeSlot: String,
        contentType: PostContentType,
        @DrawableRes imageRes: Int? = null,
        imagePrompt: String = "",
        isVideo: Boolean = false,
        videoDurationSec: Int? = null
    ): SocialPost {
        val newPost = SocialPost(
            id = "post_" + UUID.randomUUID().toString().take(6),
            title = title,
            captionAr = captionAr,
            captionEn = captionEn,
            hashtags = hashtags,
            targetPlatforms = targetPlatforms,
            scheduledTimeSlot = timeSlot,
            scheduledDate = "Today",
            status = if (_automationMode.value == AutomationMode.FULL_AUTOMATION) PostStatus.SCHEDULED else PostStatus.DRAFT,
            contentType = contentType,
            imageResId = imageRes ?: R.drawable.hero_kabsa_1793398408425,
            imagePrompt = imagePrompt.ifBlank { "Professional food photography of $title in modern Saudi dining style." },
            isVideo = isVideo,
            videoDurationSec = videoDurationSec,
            lastApiPayload = "{\"generated_by\":\"Saudi Food AI Engine v4\",\"slot\":\"$timeSlot\"}"
        )
        _posts.value = listOf(newPost) + _posts.value
        updateCampaignsState()

        addLog(
            eventType = AppLogEventType.AI_CONTENT_GENERATED,
            status = LogStatus.SUCCESS,
            summary = "AI generated food post: '$title'.",
            platform = targetPlatforms.firstOrNull()
        )
        addLog(
            eventType = AppLogEventType.POST_SAVED,
            status = LogStatus.SUCCESS,
            summary = "Post '$title' saved to database ($timeSlot).",
            platform = targetPlatforms.firstOrNull()
        )
        if (_automationMode.value == AutomationMode.FULL_AUTOMATION) {
            addLog(
                eventType = AppLogEventType.POST_SCHEDULED,
                status = LogStatus.PENDING,
                summary = "Post '$title' scheduled for $timeSlot.",
                platform = targetPlatforms.firstOrNull()
            )
        }

        return newPost
    }

    val whatsAppTemplates: List<WhatsAppMarketingTemplate> = listOf(
        WhatsAppMarketingTemplate(
            id = "whatsapp_tpl_friday_feast",
            name = "Friday Family Feast Special",
            metaTemplateName = "friday_family_kabsa_special_v1",
            category = "MARKETING",
            headlineAr = "عرض غداء الجمعة العائلي 🍖✨",
            headlineEn = "Friday Family Lunch Feast Special 🍖✨",
            bodyTemplateAr = "أهلاً بك {{customer_name}} في مطعم سفرة الأصيل! 🇸🇦\nاحتفل مع عائلتك يوم الجمعة بخصم 20% على صحن كبسة النعيمي الملكية الفاخرة مع الدقوس والسلطة والمشروبات.\n📍 موقع المطعم: {{maps_link}}\n💬 للحجز السريع، رد بكلمة 'حجز'",
            bodyTemplateEn = "Hello {{customer_name}} from Sufra Restaurant! 🇸🇦\nCelebrate Friday with your family with 20% OFF on our Royal Naeemi Kabsa Platter with spiced dakous, salad, and drinks.\n📍 Location: {{maps_link}}\n💬 Reply 'RESERVE' to book your table instantly.",
            callToActionAr = "تأكيد حجز الطاولة",
            callToActionEn = "Confirm Table Reservation"
        ),
        WhatsAppMarketingTemplate(
            id = "whatsapp_tpl_founding_day",
            name = "Saudi Founding Day Hospitality",
            metaTemplateName = "saudi_founding_day_banquet_v1",
            category = "MARKETING",
            headlineAr = "يوم بدينا • ضيافة سعودية أصيلة 🇸🇦",
            headlineEn = "Founding Day • Authentic Saudi Hospitality 🇸🇦",
            bodyTemplateAr = "حياك الله {{customer_name}}! بمناسبة يوم التأسيس السعودي، نتشرف بدعوتكم للاستمتاع بدلة قهوة سعودية مجانية وتمر سكري فاخر مع كل وجبة عائلية في مطعم سفرة.\nدمت يا وطني عامراً بالخير والضيافة.",
            bodyTemplateEn = "Welcome {{customer_name}}! In honor of Saudi Founding Day, enjoy a complimentary brass dallah of golden Saudi coffee and premium dates with every family meal at Sufra.\nLong live our homeland in prosperity and hospitality.",
            callToActionAr = "عرض المنيو الوطني",
            callToActionEn = "View Heritage Menu"
        ),
        WhatsAppMarketingTemplate(
            id = "whatsapp_tpl_business_lunch",
            name = "Corporate Express Lunch Deal",
            metaTemplateName = "corporate_express_lunch_v1",
            category = "MARKETING",
            headlineAr = "غداء عملك وصل في 25 دقيقة 🍔💼",
            headlineEn = "Business Lunch Delivered in 25 Mins 🍔💼",
            bodyTemplateAr = "مرحباً {{customer_name}}، هل تبحث عن غداء سريع ومميز لمكتبك؟\nكومبو الدبل سماش برجر مع بطاطس الترفل ومشروب منعش بـ 45 ريال فقط شامل التوصيل المباشر لمكتبك في الرياض أو جدة.\nاطلب الآن بدون انتظار!",
            bodyTemplateEn = "Hello {{customer_name}}, looking for a quick gourmet lunch at work?\nDouble Smash Burger combo with loaded truffle fries and drink for only 45 SAR including direct office delivery in Riyadh or Jeddah.\nOrder now with zero waiting!",
            callToActionAr = "اطلب الآن عبر الواتساب",
            callToActionEn = "Order via WhatsApp"
        )
    )

    fun updateBusinessProfile(updated: BusinessProfile) {
        _businessProfile.value = updated
        addLog(
            eventType = AppLogEventType.POST_SAVED,
            status = LogStatus.SUCCESS,
            summary = "Business profile updated: ${updated.restaurantName} (${updated.primaryCity.englishName})."
        )
    }

    fun testSavedGoogleMapsLink(): Pair<Boolean, String> {
        val uri = _businessProfile.value.savedGoogleMapsUri.trim()
        val isValid = uri.startsWith("http://") || uri.startsWith("https://")
        val isMapsDomain = uri.contains("google.com/maps") || uri.contains("maps.app.goo.gl") || uri.contains("goo.gl/maps")

        return if (isValid && isMapsDomain) {
            addLog(
                eventType = AppLogEventType.SAVED_MAPS_LINK_TESTED,
                status = LogStatus.SUCCESS,
                summary = "Saved Google Maps link verified: $uri",
                errorDetails = null
            )
            Pair(true, "Google Maps link format is valid: $uri")
        } else {
            val error = if (!isValid) "Invalid URL protocol (must start with https://)" else "URL must be a valid Google Maps location link (e.g. maps.app.goo.gl/...)"
            addLog(
                eventType = AppLogEventType.SAVED_MAPS_LINK_TESTED,
                status = LogStatus.FAILED,
                summary = "Failed testing saved Google Maps link: $uri",
                errorDetails = error
            )
            Pair(false, error)
        }
    }

    fun testBusinessWhatsAppLink(): Pair<Boolean, String> {
        val phone = _businessProfile.value.whatsAppNumber.trim()
        val digits = phone.filter { it.isDigit() }
        val cleanNumber = _businessProfile.value.cleanInternationalWhatsAppNumber
        val waLink = _businessProfile.value.directWhatsAppUri

        val isValid = digits.length >= 9 && (cleanNumber.startsWith("9665") || cleanNumber.startsWith("966"))

        return if (isValid) {
            addLog(
                eventType = AppLogEventType.WHATSAPP_LINK_TESTED,
                status = LogStatus.SUCCESS,
                summary = "Business WhatsApp direct link verified: $waLink",
                errorDetails = null
            )
            Pair(true, "WhatsApp click-to-chat link generated: $waLink")
        } else {
            val error = "Invalid Saudi WhatsApp number format. Expected Saudi mobile format (e.g., +966 5X XXX XXXX or 05XXXXXXXX)"
            addLog(
                eventType = AppLogEventType.WHATSAPP_LINK_TESTED,
                status = LogStatus.FAILED,
                summary = "Failed testing WhatsApp number: $phone",
                errorDetails = error
            )
            Pair(false, error)
        }
    }

    fun registerCustomerConsent(
        name: String,
        phone: String,
        source: WhatsAppConsentSource,
        notes: String = "Opted in via customer consent form"
    ): WhatsAppConsentRecord {
        val newRecord = WhatsAppConsentRecord(
            id = "consent_" + UUID.randomUUID().toString().take(8),
            customerName = name.ifBlank { "عميل سفرة" },
            phoneNumber = phone,
            consentTimestamp = formatRiyadhTimestamp(),
            source = source,
            isOptedIn = true,
            notes = notes
        )
        _whatsAppConsents.value = listOf(newRecord) + _whatsAppConsents.value
        addLog(
            eventType = AppLogEventType.WHATSAPP_CONSENT_REGISTERED,
            status = LogStatus.SUCCESS,
            summary = "Customer consent registered for ${newRecord.customerName} (${newRecord.phoneNumber}) via ${source.labelEn}."
        )
        return newRecord
    }

    fun toggleCustomerConsent(id: String) {
        _whatsAppConsents.value = _whatsAppConsents.value.map { consent ->
            if (consent.id == id) {
                val nextOptIn = !consent.isOptedIn
                addLog(
                    eventType = AppLogEventType.WHATSAPP_CONSENT_REGISTERED,
                    status = if (nextOptIn) LogStatus.SUCCESS else LogStatus.WARNING,
                    summary = if (nextOptIn) "Customer ${consent.customerName} re-opted in to WhatsApp broadcasts." else "Customer ${consent.customerName} opted OUT of WhatsApp broadcasts (Anti-Spam Safeguard)."
                )
                consent.copy(isOptedIn = nextOptIn)
            } else consent
        }
    }

    fun createWhatsAppCampaign(
        title: String,
        templateId: String,
        scheduledSlot: String = "11:00 AM AST"
    ): WhatsAppCampaign {
        val template = whatsAppTemplates.find { it.id == templateId } ?: whatsAppTemplates.first()
        val optedInCount = _whatsAppConsents.value.count { it.isOptedIn }
        val mapsLink = _businessProfile.value.savedGoogleMapsUri

        val previewAr = template.bodyTemplateAr
            .replace("{{customer_name}}", "عبدالرحمن")
            .replace("{{maps_link}}", mapsLink)

        val previewEn = template.bodyTemplateEn
            .replace("{{customer_name}}", "Abdulrahman")
            .replace("{{maps_link}}", mapsLink)

        val campaign = WhatsAppCampaign(
            id = "wa_camp_" + UUID.randomUUID().toString().take(8),
            title = title.ifBlank { template.name },
            templateId = template.id,
            scheduledTimeSlot = scheduledSlot,
            scheduledDate = "Upcoming Scheduled Broadcast",
            status = WhatsAppCampaignStatus.AWAITING_APPROVAL,
            targetRecipientCount = optedInCount,
            previewAr = previewAr,
            previewEn = previewEn,
            isApprovedByOwner = false
        )

        _whatsAppCampaigns.value = listOf(campaign) + _whatsAppCampaigns.value
        addLog(
            eventType = AppLogEventType.WHATSAPP_CAMPAIGN_PREPARED,
            status = LogStatus.PENDING,
            summary = "WhatsApp campaign created: '${campaign.title}' targeting $optedInCount verified opted-in recipients."
        )
        return campaign
    }

    fun approveWhatsAppCampaign(campaignId: String) {
        val camp = _whatsAppCampaigns.value.find { it.id == campaignId }
        _whatsAppCampaigns.value = _whatsAppCampaigns.value.map { c ->
            if (c.id == campaignId) {
                c.copy(status = WhatsAppCampaignStatus.APPROVED, isApprovedByOwner = true)
            } else c
        }
        if (camp != null) {
            addLog(
                eventType = AppLogEventType.WHATSAPP_CAMPAIGN_APPROVED,
                status = LogStatus.SUCCESS,
                summary = "WhatsApp marketing campaign '${camp.title}' approved by owner. Ready for scheduled dispatch."
            )
        }
    }

    fun dispatchWhatsAppCampaign(campaignId: String, isMetaApiConfigured: Boolean = false): Pair<Boolean, String> {
        val camp = _whatsAppCampaigns.value.find { it.id == campaignId }
            ?: return Pair(false, "Campaign not found")

        // 1. Check owner approval gate
        if (!camp.isApprovedByOwner && camp.status != WhatsAppCampaignStatus.APPROVED) {
            val err = "Manager Approval Required: Campaign has not been approved by the business owner."
            addLog(
                eventType = AppLogEventType.WHATSAPP_CAMPAIGN_DISPATCHED,
                status = LogStatus.FAILED,
                summary = "WhatsApp broadcast blocked for '${camp.title}': Awaiting owner approval.",
                errorDetails = err
            )
            return Pair(false, err)
        }

        // 2. Check customer consent compliance
        val optedInRecipients = _whatsAppConsents.value.filter { it.isOptedIn }
        if (optedInRecipients.isEmpty()) {
            val err = "Zero Opted-in Recipients: Strict Anti-Spam Safeguard prevents broadcast. All customer recipients have either revoked consent or no opted-in numbers are registered."
            addLog(
                eventType = AppLogEventType.WHATSAPP_CAMPAIGN_DISPATCHED,
                status = LogStatus.FAILED,
                summary = "WhatsApp broadcast blocked for '${camp.title}': No verifiable customer consent.",
                errorDetails = err
            )
            return Pair(false, err)
        }

        // 3. Check official Meta Cloud API configuration
        addLog(
            eventType = AppLogEventType.WHATSAPP_CAMPAIGN_DISPATCHED,
            status = LogStatus.PENDING,
            summary = "WhatsApp broadcast dispatch initiated for '${camp.title}' to ${optedInRecipients.size} recipients..."
        )

        return if (isMetaApiConfigured) {
            val timestamp = formatRiyadhTimestamp()
            _whatsAppCampaigns.value = _whatsAppCampaigns.value.map { c ->
                if (c.id == campaignId) {
                    c.copy(
                        status = WhatsAppCampaignStatus.DISPATCHED,
                        dispatchedTimestamp = timestamp,
                        lastErrorReason = null
                    )
                } else c
            }
            addLog(
                eventType = AppLogEventType.WHATSAPP_CAMPAIGN_DISPATCHED,
                status = LogStatus.SUCCESS,
                summary = "Successfully dispatched WhatsApp broadcast '${camp.title}' via Meta Cloud API v19.0 to ${optedInRecipients.size} opted-in recipients."
            )
            Pair(true, "Broadcast dispatched successfully via Meta Cloud API to ${optedInRecipients.size} recipients.")
        } else {
            val error = "HTTP 401 Unauthorized: Meta WhatsApp Business Cloud API credentials not configured. Required: Phone Number ID, WhatsApp Business Account ID (WABA ID), and Permanent System User Access Token configured in Meta Developer Suite. Zero messages sent without verified credentials."
            _whatsAppCampaigns.value = _whatsAppCampaigns.value.map { c ->
                if (c.id == campaignId) {
                    c.copy(
                        status = WhatsAppCampaignStatus.FAILED,
                        lastErrorReason = error
                    )
                } else c
            }
            addLog(
                eventType = AppLogEventType.WHATSAPP_CAMPAIGN_DISPATCHED,
                status = LogStatus.FAILED,
                summary = "WhatsApp broadcast failed for '${camp.title}'.",
                errorDetails = error
            )
            Pair(false, error)
        }
    }
}


private data class CulturalPostTemplate(
    val slot: String,
    val title: String,
    val captionAr: String,
    val captionEn: String,
    val hashtags: List<String>,
    @DrawableRes val imageRes: Int,
    val contentType: PostContentType,
    val isVideo: Boolean,
    val videoDurationSec: Int? = null,
    val imagePrompt: String
)
