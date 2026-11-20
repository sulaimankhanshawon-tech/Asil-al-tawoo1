package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.*
import com.example.data.repository.SaudiFoodRepository
import com.example.ui.viewmodel.SaudiFoodViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sufra AI", appName)
  }

  @Test
  fun `verify initial saudi food repository data`() {
    val repo = SaudiFoodRepository()
    val accounts = repo.accounts.value
    assertEquals(5, accounts.size)

    val posts = repo.posts.value
    assertEquals(5, posts.size)

    val reports = repo.researchReports.value
    assertTrue(reports.isNotEmpty())

    // Test approve action
    val awaitingPost = posts.first { it.status == PostStatus.AWAITING_APPROVAL }
    repo.approvePost(awaitingPost.id)
    val updatedPost = repo.posts.value.first { it.id == awaitingPost.id }
    assertEquals(PostStatus.SCHEDULED, updatedPost.status)

    // Test accounts start disconnected
    assertTrue(accounts.all { !it.isConnected })

    // Test automation mode change
    repo.setAutomationMode(AutomationMode.FULL_AUTOMATION)
    assertEquals(AutomationMode.FULL_AUTOMATION, repo.automationMode.value)
  }

  @Test
  fun `verify five post daily batch generator and scheduling`() {
    val repo = SaudiFoodRepository()
    val initialCount = repo.posts.value.size

    val createdPosts = repo.generateDailyFivePostBatch(city = SaudiCity.RIYADH, category = FoodCategory.TRADITIONAL_SAUDI)
    assertEquals(5, createdPosts.size)
    assertEquals(initialCount + 5, repo.posts.value.size)

    // Verify slots exist in the newly created posts
    val latestFive = repo.posts.value.take(5)
    assertTrue(latestFive.any { it.scheduledTimeSlot.contains("09:00") })
    assertTrue(latestFive.any { it.scheduledTimeSlot.contains("12:00") })
    assertTrue(latestFive.any { it.scheduledTimeSlot.contains("03:00") })
    assertTrue(latestFive.any { it.scheduledTimeSlot.contains("06:00") })
    assertTrue(latestFive.any { it.scheduledTimeSlot.contains("09:00 PM") || it.scheduledTimeSlot.contains("09:00") })

    // Test updating post time slot
    val targetPost = latestFive.first()
    repo.updatePostTime(targetPost.id, "23:00 AST (Suhoor Slot)")
    val timeUpdatedPost = repo.posts.value.first { it.id == targetPost.id }
    assertEquals("23:00 AST (Suhoor Slot)", timeUpdatedPost.scheduledTimeSlot)

    // Test connect and disconnect account
    repo.connectAccount("acc_ig_1", "@sufra_saudi_food")
    val connectedIg = repo.accounts.value.first { it.id == "acc_ig_1" }
    assertTrue(connectedIg.isConnected)
    assertEquals("@sufra_saudi_food", connectedIg.handle)

    repo.disconnectAccount("acc_ig_1")
    val disconnectedIg = repo.accounts.value.first { it.id == "acc_ig_1" }
    assertTrue(!disconnectedIg.isConnected)
  }

  @Test
  fun `verify real activity log system events and statuses`() {
    val repo = SaudiFoodRepository()
    val initialLogs = repo.appLogs.value

    // 1. Verify "Application started" event is logged at initialization
    assertTrue("Should have logged application started event", initialLogs.any { it.eventType == AppLogEventType.APPLICATION_STARTED })
    val startLog = initialLogs.first { it.eventType == AppLogEventType.APPLICATION_STARTED }
    assertEquals(LogStatus.SUCCESS, startLog.status)
    assertTrue(startLog.timestampRiyadh.contains("AST"))

    // 2. Generate daily batch -> AI content generated & Post saved
    repo.generateDailyFivePostBatch(city = SaudiCity.RIYADH, category = FoodCategory.TRENDING)
    val afterBatchLogs = repo.appLogs.value
    assertTrue("Should contain AI_CONTENT_GENERATED event", afterBatchLogs.any { it.eventType == AppLogEventType.AI_CONTENT_GENERATED })
    assertTrue("Should contain POST_SAVED event", afterBatchLogs.any { it.eventType == AppLogEventType.POST_SAVED })

    // 3. Connect account -> ACCOUNT_CONNECTED
    repo.connectAccount("acc_ig_1", "@sufra_riyadh")
    val afterConnectLogs = repo.appLogs.value
    val connectLog = afterConnectLogs.first { it.eventType == AppLogEventType.ACCOUNT_CONNECTED }
    assertEquals(LogStatus.SUCCESS, connectLog.status)
    assertEquals(SocialPlatform.INSTAGRAM, connectLog.platform)
    assertTrue(connectLog.summary.contains("@sufra_riyadh"))

    // 4. Approve post -> POST_APPROVED and POST_SCHEDULED
    val draftPost = repo.posts.value.first()
    repo.approvePost(draftPost.id)
    val afterApproveLogs = repo.appLogs.value
    assertTrue("Should contain POST_APPROVED event", afterApproveLogs.any { it.eventType == AppLogEventType.POST_APPROVED })
    assertTrue("Should contain POST_SCHEDULED event", afterApproveLogs.any { it.eventType == AppLogEventType.POST_SCHEDULED })

    // 5. Publish post with connected account vs disconnected accounts
    val approvedPost = repo.posts.value.first { it.id == draftPost.id }
    repo.publishPostNow(approvedPost.id)
    val afterPublishLogs = repo.appLogs.value
    assertTrue("Should contain PUBLISHING_ATTEMPTED event", afterPublishLogs.any { it.eventType == AppLogEventType.PUBLISHING_ATTEMPTED })
    
    // Target platforms include Instagram (which is connected) and others (which are not connected)
    if (approvedPost.targetPlatforms.contains(SocialPlatform.INSTAGRAM)) {
      assertTrue("Connected platform should succeed", afterPublishLogs.any {
        it.eventType == AppLogEventType.PUBLISHING_SUCCESSFUL && it.platform == SocialPlatform.INSTAGRAM
      })
    }
    assertTrue("Disconnected platforms should fail with 401 error details", afterPublishLogs.any {
      it.eventType == AppLogEventType.PUBLISHING_FAILED && it.status == LogStatus.FAILED && it.errorDetails != null
    })

    // 6. Disconnect account -> ACCOUNT_DISCONNECTED with WARNING status
    repo.disconnectAccount("acc_ig_1")
    val afterDisconnectLogs = repo.appLogs.value
    val disconnectLog = afterDisconnectLogs.first { it.eventType == AppLogEventType.ACCOUNT_DISCONNECTED }
    assertEquals(LogStatus.WARNING, disconnectLog.status)

    // 7. Test Clear Logs
    repo.clearLogs()
    assertTrue("App logs should be empty after clear", repo.appLogs.value.isEmpty())
  }

  @Test
  fun `verify google maps places search across saudi cities and categories`() = kotlinx.coroutines.test.runTest {
    val repo = com.example.data.repository.RestaurantIntelligenceRepository()

    // 1. Test Riyadh Saudi Restaurant Search
    val riyadhResults = repo.searchRestaurants(
      query = "",
      city = SaudiMapCity.RIYADH,
      category = MapFoodCategory.SAUDI
    )
    assertTrue("Should find Saudi restaurants in Riyadh", riyadhResults.isNotEmpty())
    val najdVillage = riyadhResults.first { it.id == "riyadh_najd_village" }
    assertEquals("Najd Village (قرية نجد)", najdVillage.name)
    assertEquals(4.6, najdVillage.rating, 0.01)
    assertTrue("Places API policy: up to 5 reviews", najdVillage.reviews.size <= 5)

    // 2. Test Jeddah Seafood Search
    val jeddahSeafood = repo.searchRestaurants(
      query = "Twina",
      city = SaudiMapCity.JEDDAH,
      category = MapFoodCategory.SEAFOOD
    )
    assertTrue("Should find Twina in Jeddah", jeddahSeafood.any { it.name.contains("Twina") })

    // 3. Test Abha and Taif regional coverage
    val abhaResults = repo.searchRestaurants(city = SaudiMapCity.ABHA)
    assertTrue("Should find restaurants in Abha", abhaResults.isNotEmpty())

    val taifResults = repo.searchRestaurants(city = SaudiMapCity.TAIF)
    assertTrue("Should find restaurants in Taif", taifResults.isNotEmpty())
  }

  @Test
  fun `verify ai review sentiment analysis and quotes vs interpretations distinction`() = kotlinx.coroutines.test.runTest {
    val repo = com.example.data.repository.RestaurantIntelligenceRepository()
    val riyadhVenue = repo.getVerifiedSaudiRestaurants(city = SaudiMapCity.RIYADH).first()

    val report = repo.analyzeReviewsWithAi(riyadhVenue)
    assertNotNull(report)
    assertEquals(riyadhVenue.id, report.restaurantId)
    assertTrue(report.positiveScorePercent >= 0)
    assertTrue(report.negativeScorePercent >= 0)
    assertTrue("Total percent should equal or approximate 100%", report.positiveScorePercent + report.negativeScorePercent + report.neutralScorePercent <= 100)
    assertTrue(report.reviewsAnalyzedCount <= 5)
    assertTrue(report.disclaimer.contains("Places API policy"))

    // Verify classified review items have explicit quotes and AI interpretations
    assertTrue("Should contain classified review items", report.classifiedReviewItems.isNotEmpty())
    report.classifiedReviewItems.forEach { item ->
      assertTrue("Quote must not be blank", item.actualCustomerQuote.isNotBlank())
      assertTrue("AI interpretation must be clearly prefixed", item.aiInterpretation.contains("AI Interpretation"))
      assertNotNull(item.polarity)
      assertNotNull(item.matchedTheme)
    }

    // Verify business recommendations derived from report
    val businessRecs = repo.generateBusinessRecommendations(riyadhVenue, report)
    assertTrue(businessRecs.serviceImprovements.isNotEmpty())
    assertTrue(businessRecs.deliveryImprovements.isNotEmpty())
    assertTrue(businessRecs.marketingContentIdeas.isNotEmpty())
  }

  @Test
  fun `verify competitor intelligence and social media campaign export`() = kotlinx.coroutines.test.runTest {
    val restaurantRepo = com.example.data.repository.RestaurantIntelligenceRepository()
    val saudiFoodRepo = SaudiFoodRepository()
    val viewModel = SaudiFoodViewModel(saudiFoodRepo, restaurantRepo)

    val venues = restaurantRepo.getVerifiedSaudiRestaurants(city = SaudiMapCity.RIYADH).take(3)
    val competitorReport = restaurantRepo.compareCompetitors(venues)

    assertEquals(3, competitorReport.comparedRestaurants.size)
    assertTrue("Disclaimer must clarify higher ratings don't prove better food", competitorReport.disclaimer.contains("do not definitively prove"))
    assertTrue(competitorReport.strategicMarketingAdvice.isNotEmpty())

    // Test Social Campaign Generation and Export to Studio
    val singleVenue = venues.first()
    val reviewReport = restaurantRepo.analyzeReviewsWithAi(singleVenue)
    val socialCampaign = restaurantRepo.generateSocialMediaCampaign(singleVenue, reviewReport)

    assertTrue("Arabic caption must contain restaurant name", socialCampaign.arabicCaption.contains(singleVenue.name))
    assertTrue("English caption must contain restaurant name", socialCampaign.englishCaption.contains(singleVenue.name))
    assertTrue(socialCampaign.hashtags.isNotEmpty())
    assertTrue(socialCampaign.photographyPrompt.contains("8k"))
    assertTrue(socialCampaign.shortVideoConcept.contains("TikTok") || socialCampaign.shortVideoConcept.contains("Reel"))

    // Export to AI Studio
    val initialPostsCount = saudiFoodRepo.posts.value.size
    val exportedPost = viewModel.exportReviewCampaignToStudio(socialCampaign)
    assertEquals(initialPostsCount + 1, saudiFoodRepo.posts.value.size)
    assertEquals(PostStatus.DRAFT, exportedPost.status)
    assertTrue(exportedPost.title.contains(singleVenue.name))

    // Export to Content Calendar
    val scheduledPost = viewModel.exportReviewCampaignToCalendar(socialCampaign, "07:00 PM")
    assertEquals(initialPostsCount + 2, saudiFoodRepo.posts.value.size)
    assertEquals("07:00 PM", scheduledPost.scheduledTimeSlot)
  }

  @Test
  fun `verify jeddah restaurant search and generic term matching`() = kotlinx.coroutines.test.runTest {
    val restaurantRepo = com.example.data.repository.RestaurantIntelligenceRepository()

    // 1. Verify searching "restaurant" in Jeddah does NOT return 0 restaurants
    val jeddahGeneric = restaurantRepo.searchRestaurants(
      query = "restaurant",
      city = SaudiMapCity.JEDDAH
    )
    assertTrue("Searching 'restaurant' in Jeddah must return venues", jeddahGeneric.isNotEmpty())
    assertTrue("Should include iconic Jeddah venues", jeddahGeneric.any { it.name.contains("Baik") || it.name.contains("Abu Zaid") || it.name.contains("Twina") })

    // 2. Verify all 10 categories have verified venues in Jeddah
    MapFoodCategory.values().forEach { cat ->
      val inCategory = restaurantRepo.getVerifiedSaudiRestaurants(city = SaudiMapCity.JEDDAH, category = cat)
      assertTrue("Jeddah must have at least one venue in ${cat.englishName}", inCategory.isNotEmpty())
    }

    // 3. Verify specific queries in Jeddah
    val alBaik = restaurantRepo.searchRestaurants(query = "Al Baik", city = SaudiMapCity.JEDDAH)
    assertTrue("Should find Al Baik in Jeddah", alBaik.any { it.name.contains("Al Baik") })

    val abuZaid = restaurantRepo.searchRestaurants(query = "Abu Zaid", city = SaudiMapCity.JEDDAH)
    assertTrue("Should find Abu Zaid in Jeddah", abuZaid.any { it.name.contains("Abu Zaid") })

    val burger = restaurantRepo.searchRestaurants(query = "burger", city = SaudiMapCity.JEDDAH)
    assertTrue("Should find burger venue in Jeddah", burger.any { it.name.contains("Section-B") })

    // 4. Verify searchPlacesWithStatus state structure for Jeddah
    val searchState = restaurantRepo.searchPlacesWithStatus(
      query = "restaurant",
      city = SaudiMapCity.JEDDAH
    )
    assertTrue("Search state must return results for Jeddah", searchState.results.isNotEmpty())
    assertNotNull(searchState.status)
    assertEquals(SaudiMapCity.JEDDAH, searchState.citySearched)
    assertTrue(searchState.statusBannerText.isNotBlank())
  }

  @Test
  fun `verify saved business google maps link validation and testing`() {
    val repo = SaudiFoodRepository()

    // 1. Initial valid Google Maps link
    val initialResult = repo.testSavedGoogleMapsLink()
    assertTrue("Default link should be valid", initialResult.first)
    assertTrue("Message should confirm valid format", initialResult.second.contains("maps.app.goo.gl"))

    val initialLogs = repo.appLogs.value
    assertTrue("Must log SAVED_MAPS_LINK_TESTED event", initialLogs.any { it.eventType == AppLogEventType.SAVED_MAPS_LINK_TESTED && it.status == LogStatus.SUCCESS })

    // 2. Test invalid URL format
    repo.updateBusinessProfile(repo.businessProfile.value.copy(savedGoogleMapsUri = "invalid_link_without_protocol"))
    val invalidResult = repo.testSavedGoogleMapsLink()
    assertFalse("Invalid URL must fail validation", invalidResult.first)
    assertTrue(invalidResult.second.contains("Invalid URL protocol"))

    val updatedLogs = repo.appLogs.value
    assertTrue("Must log failed test event", updatedLogs.any { it.eventType == AppLogEventType.SAVED_MAPS_LINK_TESTED && it.status == LogStatus.FAILED })
  }

  @Test
  fun `verify business whatsapp link generation and validation`() {
    val repo = SaudiFoodRepository()

    // 1. Default valid Saudi mobile number
    val defaultResult = repo.testBusinessWhatsAppLink()
    assertTrue("Default Saudi number (+966501234567) should be valid", defaultResult.first)
    assertEquals("https://wa.me/966501234567", repo.businessProfile.value.directWhatsAppUri)
    assertTrue("Log must record WHATSAPP_LINK_TESTED", repo.appLogs.value.any { it.eventType == AppLogEventType.WHATSAPP_LINK_TESTED && it.status == LogStatus.SUCCESS })

    // 2. Local 05 format normalization to international 9665
    repo.updateBusinessProfile(repo.businessProfile.value.copy(whatsAppNumber = "0555987654"))
    assertEquals("966555987654", repo.businessProfile.value.cleanInternationalWhatsAppNumber)
    assertEquals("https://wa.me/966555987654", repo.businessProfile.value.directWhatsAppUri)
    val localResult = repo.testBusinessWhatsAppLink()
    assertTrue("Local 05 format must be valid", localResult.first)

    // 3. Invalid phone number format
    repo.updateBusinessProfile(repo.businessProfile.value.copy(whatsAppNumber = "12345"))
    val badResult = repo.testBusinessWhatsAppLink()
    assertFalse("Invalid short number must fail", badResult.first)
    assertTrue(badResult.second.contains("Expected Saudi mobile format"))
  }

  @Test
  fun `verify whatsapp marketing customer consent campaign approval and dispatch safeguards`() {
    val repo = SaudiFoodRepository()

    // 1. Verify initial customer consent records exist with opt-in status
    val initialConsents = repo.whatsAppConsents.value
    assertTrue("Initial customer consents must be populated", initialConsents.isNotEmpty())
    assertTrue("Default customers must be opted-in", initialConsents.all { it.isOptedIn })

    // 2. Register new customer consent
    val newConsent = repo.registerCustomerConsent(
      name = "فاطمة الدوسري",
      phone = "+966509988776",
      source = WhatsAppConsentSource.WEBSITE_RESERVATION
    )
    assertTrue("Consents list must now include newly added customer", repo.whatsAppConsents.value.any { it.id == newConsent.id })
    assertTrue("Must log WHATSAPP_CONSENT_REGISTERED", repo.appLogs.value.any { it.eventType == AppLogEventType.WHATSAPP_CONSENT_REGISTERED })

    // 3. Toggle consent (Opt-out safeguard)
    repo.toggleCustomerConsent(newConsent.id)
    val updatedRecord = repo.whatsAppConsents.value.first { it.id == newConsent.id }
    assertFalse("Customer must now be marked opted out", updatedRecord.isOptedIn)

    // 4. Verify templates
    assertTrue("Must have pre-approved templates", repo.whatsAppTemplates.isNotEmpty())
    val fridayTemplate = repo.whatsAppTemplates.first { it.id == "whatsapp_tpl_friday_feast" }
    assertTrue("Template must contain Arabic copy", fridayTemplate.headlineAr.contains("الجمعة"))

    // 5. Create new WhatsApp Campaign
    val campaign = repo.createWhatsAppCampaign(
      title = "Eid Hospitality Special",
      templateId = fridayTemplate.id,
      scheduledSlot = "10:30 AM AST"
    )
    assertEquals(WhatsAppCampaignStatus.AWAITING_APPROVAL, campaign.status)
    assertFalse("Must not be approved by owner initially", campaign.isApprovedByOwner)

    // 6. Safeguard: Dispatch without approval must be blocked
    val blockedResult = repo.dispatchWhatsAppCampaign(campaign.id, isMetaApiConfigured = false)
    assertFalse("Dispatch without approval must be blocked", blockedResult.first)
    assertTrue(blockedResult.second.contains("Manager Approval Required"))

    // 7. Approve campaign
    repo.approveWhatsAppCampaign(campaign.id)
    val approvedCampaign = repo.whatsAppCampaigns.value.first { it.id == campaign.id }
    assertEquals(WhatsAppCampaignStatus.APPROVED, approvedCampaign.status)
    assertTrue(approvedCampaign.isApprovedByOwner)
    assertTrue("Must log WHATSAPP_CAMPAIGN_APPROVED", repo.appLogs.value.any { it.eventType == AppLogEventType.WHATSAPP_CAMPAIGN_APPROVED })

    // 8. Safeguard: Dispatch without Meta credentials returns clear diagnostic failure (HTTP 401)
    val dispatchFailedResult = repo.dispatchWhatsAppCampaign(campaign.id, isMetaApiConfigured = false)
    assertFalse("Dispatch without credentials must fail gracefully", dispatchFailedResult.first)
    assertTrue("Must explain missing Meta credentials", dispatchFailedResult.second.contains("HTTP 401 Unauthorized") && dispatchFailedResult.second.contains("WABA ID"))
    val failedCamp = repo.whatsAppCampaigns.value.first { it.id == campaign.id }
    assertEquals(WhatsAppCampaignStatus.FAILED, failedCamp.status)

    // 9. When credentials are simulated as configured, dispatch succeeds
    repo.approveWhatsAppCampaign(campaign.id) // Re-approve
    val dispatchSuccessResult = repo.dispatchWhatsAppCampaign(campaign.id, isMetaApiConfigured = true)
    assertTrue("Dispatch with credentials should succeed", dispatchSuccessResult.first)
    assertTrue(dispatchSuccessResult.second.contains("Broadcast dispatched successfully"))
    val dispatchedCamp = repo.whatsAppCampaigns.value.first { it.id == campaign.id }
    assertEquals(WhatsAppCampaignStatus.DISPATCHED, dispatchedCamp.status)
    assertNotNull(dispatchedCamp.dispatchedTimestamp)
  }

  @Test
  fun `verify saudi food ai content template model and conversion`() {
    val aiViewModel = com.example.ui.viewmodel.SaudiFoodAiContentViewModel()
    assertEquals(com.example.ui.viewmodel.SaudiFoodAiState.Idle, aiViewModel.uiState.value)

    val template = com.example.ui.viewmodel.SaudiFoodContentTemplate(
      templateTitleAr = "سماش برجر بالترفل والبريوش",
      templateTitleEn = "Truffle Smash Burger on Brioche",
      saudiDialect = "لهجة نجدية",
      targetCity = "الرياض",
      foodTrendCategory = "سماش برجر",
      captionAr = "يا هلا بالزين في الرياض! 🇸🇦🍔 جربتوا سماش برجر الترفل الفاخر مع صوص خاص؟ طعم يدخل بالقلب مباشرة!",
      captionEn = "Craving perfection in Riyadh? Taste our signature truffle smash burger served on warm toasted brioche.",
      hashtags = listOf("#مطاعم_الرياض", "#سماش_برجر", "#ترند_الرياض", "#سفرة_AI"),
      callToAction = "اطلب عبر الرابط في البايو",
      photographyPrompt = "Gourmet juicy double smash burger dripping with melted cheddar and truffle aioli, toasted brioche bun, atmospheric dining lighting 8k resolution",
      recommendedPostingTimeSlot = "08:30 PM AST",
      targetPlatforms = listOf("Instagram", "TikTok"),
      marketingHook = "ترند الترفل الأول في الرياض"
    )

    // Convert template to SocialPost for Sufra scheduling
    val socialPost = aiViewModel.convertTemplateToSocialPost(
      template = template,
      targetPlatforms = setOf(SocialPlatform.INSTAGRAM, SocialPlatform.TIKTOK)
    )

    assertNotNull(socialPost.id)
    assertEquals("سماش برجر بالترفل والبريوش", socialPost.title)
    assertTrue(socialPost.captionAr.contains("يا هلا بالزين في الرياض!"))
    assertEquals("08:30 PM AST", socialPost.scheduledTimeSlot)
    assertEquals(PostStatus.DRAFT, socialPost.status)
    assertTrue(socialPost.hashtags.contains("#مطاعم_الرياض"))
    assertTrue(socialPost.isVideo) // Since TikTok was targeted
    assertEquals(R.drawable.trendy_burger_1793398409429, socialPost.imageResId)

    // Verify resetState returns to Idle
    aiViewModel.resetState()
    assertEquals(com.example.ui.viewmodel.SaudiFoodAiState.Idle, aiViewModel.uiState.value)
  }

  @Test
  fun `verify gemini studio viewmodel configuration and state transitions`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val studioVm = com.example.ui.viewmodel.GeminiStudioViewModel()

    // Initial states
    assertEquals(com.example.ui.viewmodel.ImageGenUiState.Idle, studioVm.imageGenState.value)
    assertEquals(com.example.ui.viewmodel.AudioTranscribeState.Idle, studioVm.audioState.value)
    assertEquals(com.example.ui.viewmodel.LiveVoiceState.Disconnected, studioVm.liveVoiceState.value)
    assertEquals(com.example.ui.viewmodel.AiChatModelTier.GENERAL, studioVm.selectedChatModel.value)
    assertEquals(com.example.ui.viewmodel.AiChatRolePreset.SAUDI_STRATEGIST, studioVm.selectedChatRole.value)
    assertTrue(studioVm.enableSearchGrounding.value)
    assertTrue(studioVm.enableMapsGrounding.value)

    // Switch model tiers & system instruction roles
    studioVm.selectChatModel(com.example.ui.viewmodel.AiChatModelTier.REASONING)
    assertEquals("gemini-4-argon", studioVm.selectedChatModel.value.modelName)
    studioVm.selectChatModel(com.example.ui.viewmodel.AiChatModelTier.FAST)
    assertEquals("gemini-3.5-flash-lite", studioVm.selectedChatModel.value.modelName)

    studioVm.selectChatRole(com.example.ui.viewmodel.AiChatRolePreset.BILINGUAL_COPYWRITER)
    assertTrue(studioVm.customSystemInstruction.value.contains("Saudi dialects"))
    studioVm.selectChatRole(com.example.ui.viewmodel.AiChatRolePreset.CUSTOM)
    studioVm.updateCustomSystemInstruction("You are a Riyadh specialty coffee brand strategist.")
    assertEquals("You are a Riyadh specialty coffee brand strategist.", studioVm.customSystemInstruction.value)

    // Toggle grounding & city context
    studioVm.toggleSearchGrounding(false)
    studioVm.toggleMapsGrounding(true)
    studioVm.setChatCityContext(SaudiMapCity.JEDDAH)
    assertFalse(studioVm.enableSearchGrounding.value)
    assertTrue(studioVm.enableMapsGrounding.value)
    assertEquals(SaudiMapCity.JEDDAH, studioVm.chatCityContext.value)

    // Blank image prompt validation surfaces error transparently
    studioVm.generateFoodImage(prompt = "   ")
    assertTrue(studioVm.imageGenState.value is com.example.ui.viewmodel.ImageGenUiState.Error)
    studioVm.resetImageGenState()
    assertEquals(com.example.ui.viewmodel.ImageGenUiState.Idle, studioVm.imageGenState.value)

    // Save generated bitmap to app storage
    val bmp = android.graphics.Bitmap.createBitmap(64, 64, android.graphics.Bitmap.Config.ARGB_8888)
    val savedUri = kotlinx.coroutines.runBlocking { studioVm.saveGeneratedBitmapToFile(context, bmp) }
    assertNotNull(savedUri)
    assertTrue(java.io.File(savedUri!!.path!!).exists())
  }
}


