package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.*
import com.example.ui.components.AiImageStudioCard
import com.example.ui.components.AudioTranscriptionBar
import com.example.ui.components.PlatformChip
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.GeminiStudioViewModel
import com.example.ui.viewmodel.SaudiFoodAiContentBatch
import com.example.ui.viewmodel.SaudiFoodAiContentViewModel
import com.example.ui.viewmodel.SaudiFoodAiState
import com.example.ui.viewmodel.SaudiFoodContentTemplate
import com.example.ui.viewmodel.SaudiFoodViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentStudioScreen(
    viewModel: SaudiFoodViewModel,
    aiContentViewModel: SaudiFoodAiContentViewModel = viewModel(),
    geminiStudioViewModel: GeminiStudioViewModel = viewModel(),
    onNavigateToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("Royal Lamb Kabsa Feast") }
    var dishTopic by remember { mutableStateOf("Naeemi Lamb Kabsa with Toasted Nuts") }
    var selectedCity by remember { mutableStateOf(SaudiCity.RIYADH) }
    var selectedCategory by remember { mutableStateOf(FoodCategory.TRADITIONAL_SAUDI) }
    var selectedContentType by remember { mutableStateOf(PostContentType.FOOD_PHOTOGRAPHY) }
    var timeSlot by remember { mutableStateOf("12:00 PM") }
    var isVideoFormat by remember { mutableStateOf(false) }

    var selectedPlatforms by remember {
        mutableStateOf(
            setOf(
                SocialPlatform.INSTAGRAM,
                SocialPlatform.FACEBOOK,
                SocialPlatform.TIKTOK,
                SocialPlatform.X_TWITTER
            )
        )
    }

    // Generated Post Preview State
    var generatedCaptionAr by remember {
        mutableStateOf(
            "كبسة النعيمي الملكية 🇸🇦🍖 نكهة الأصالة والضيافة في قلب الرياض! لحم نعيمي طازج وذايب مع رز بسمتي معطر بالهيل والمسمار والزعفران ولوز مقرمش. متعتكم لا تُقاوم."
        )
    }
    var generatedCaptionEn by remember {
        mutableStateOf(
            "Royal Naeemi Kabsa 🇸🇦🍖 Tender slow-cooked lamb served over fragrant spiced basmati rice infused with saffron, cardamom, and golden toasted nuts. A true Saudi culinary masterpiece."
        )
    }
    var generatedImagePrompt by remember {
        mutableStateOf(
            "Gourmet Naeemi lamb Kabsa served on a grand hammered brass tray, yellow basmati rice garnished with toasted pine nuts, almonds, and golden raisins, soft atmospheric dining lighting."
        )
    }

    var currentLanguageTab by remember { mutableStateOf("AR") } // "AR" or "EN"
    var previewImageRes by remember { mutableStateOf(R.drawable.hero_kabsa_1793398408425) }
    var customImageUri by remember { mutableStateOf<Uri?>(null) }

    // Android Photo & Video Picker for Media Upload (Priority 3)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customImageUri = uri
            scope.launch {
                snackbarHostState.showSnackbar("📷 Custom photo selected from gallery!")
            }
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            customImageUri = uri
            isVideoFormat = true
            scope.launch {
                snackbarHostState.showSnackbar("🎬 Custom 9:16 Video Reel selected from gallery!")
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize().testTag("content_studio_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card with 5-Post Batch Quick Generator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Saudi AI Content Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Synthesizes 5 unique Saudi food posts per day (9 AM, 12 PM, 3 PM, 6 PM, 9 PM) with authentic dialect captions, hashtags, and realistic photography prompts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            fontSize = 11.5.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // PRIORITY 2: Generate 5 Daily Unique Posts Button
                        Button(
                            onClick = {
                                viewModel.generateDailyBatch(selectedCity, selectedCategory)
                                scope.launch {
                                    snackbarHostState.showSnackbar("✨ Generated 5 unique Saudi food posts for today!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A017)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("btn_generate_5_daily_batch")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚡ Generate 5 Daily Posts (Today's Saudi Batch)",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Firebase AI Content Templates Generator Card (Gemini 3.8 Flash)
            item {
                val aiState by aiContentViewModel.uiState.collectAsState()
                var selectedDialect by remember { mutableStateOf("لهجة نجدية") }
                val dialects = listOf("لهجة نجدية", "لهجة حجازية", "لهجة شرقية", "لهجة جنوبية", "سعودي عام")

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("firebase_ai_generator_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B22)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = Color(0xFFD4A017))
                                Text(
                                    text = "Firebase AI • Saudi Trend Templates",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4A017)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF1B4D3E)
                            ) {
                                Text(
                                    text = "Gemini 3.8 Flash",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Synthesizes structured Saudi food marketing templates with authentic regional dialects, cultural hooks, and 8K photography prompts via Firebase AI Logic.",
                            color = Color(0xFFE0E0E0),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dialect Selection
                        Text(text = "اللهجة المستهدفة:", color = Color(0xFFD4A017), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            items(dialects) { dialect ->
                                FilterChip(
                                    selected = selectedDialect == dialect,
                                    onClick = { selectedDialect = dialect },
                                    label = { Text(dialect, fontSize = 10.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFD4A017),
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Buttons: Structured 5-Template Generation vs Live Brainstorm
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    aiContentViewModel.generateLocalizedTemplates(
                                        trendKeyword = dishTopic.ifBlank { "سماش برجر بالترفل والخبز البريوش" },
                                        targetCity = selectedCity.arabicName,
                                        preferredDialect = selectedDialect,
                                        category = selectedCategory.arabicName,
                                        templateCount = 5
                                    )
                                },
                                enabled = aiState !is SaudiFoodAiState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A017)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("btn_firebase_generate_templates")
                            ) {
                                if (aiState is SaudiFoodAiState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generating...", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("5 Trend Templates", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    aiContentViewModel.generateStreamingBrainstorm(
                                        restaurantConcept = dishTopic.ifBlank { "مطعم كبسة حاشي مع قهوة سعودية" },
                                        city = selectedCity.arabicName
                                    )
                                },
                                enabled = aiState !is SaudiFoodAiState.Loading,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.weight(1f).testTag("btn_firebase_stream_brainstorm")
                            ) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Live Brainstorm", fontSize = 11.sp)
                            }
                        }

                        // State Render: Streaming, Success, Error
                        when (val state = aiState) {
                            is SaudiFoodAiState.Streaming -> {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1B4D3E),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = Color(0xFFD4A017))
                                            Text("Streaming AI brainstorm...", fontSize = 11.sp, color = Color(0xFFD4A017), fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = state.accumulatedText, color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
                                    }
                                }
                            }
                            is SaudiFoodAiState.Error -> {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF4A1010),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFFCDD2))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Firebase AI Status", color = Color(0xFFFFCDD2), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text(state.message, color = Color.White, fontSize = 10.5.sp)
                                        }
                                        TextButton(onClick = { aiContentViewModel.resetState() }) {
                                            Text("Dismiss", color = Color(0xFFD4A017), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                            is SaudiFoodAiState.Success -> {
                                Spacer(modifier = Modifier.height(12.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Generated Batch: ${state.batch.templates.size} Templates",
                                            color = Color(0xFFD4A017),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = state.batch.generatedAt,
                                            color = Color.LightGray,
                                            fontSize = 9.sp
                                        )
                                    }

                                    state.batch.templates.forEachIndexed { index, tpl ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF1B4D3E),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "${index + 1}. ${tpl.templateTitleAr}",
                                                        color = Color(0xFFD4A017),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = tpl.recommendedPostingTimeSlot,
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = tpl.captionAr,
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    lineHeight = 15.sp
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = tpl.hashtags.joinToString(" "),
                                                    color = Color(0xFF81D4FA),
                                                    fontSize = 10.sp
                                                )

                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.End,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            clipboardManager.setText(AnnotatedString(tpl.captionAr + "\n\n" + tpl.hashtags.joinToString(" ")))
                                                            scope.launch { snackbarHostState.showSnackbar("📋 Caption copied!") }
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Copy", color = Color.White, fontSize = 10.sp)
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Button(
                                                        onClick = {
                                                            val socialPost = aiContentViewModel.convertTemplateToSocialPost(
                                                                template = tpl,
                                                                targetPlatforms = selectedPlatforms
                                                            )
                                                            viewModel.updatePost(socialPost)
                                                            scope.launch {
                                                                snackbarHostState.showSnackbar("📥 Template exported to Schedule Queue!")
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A017)),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text("Export to Schedule", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }

            // AI Image Create & Edit Studio (gemini-nano-banana-2.1)
            item {
                AiImageStudioCard(
                    geminiViewModel = geminiStudioViewModel,
                    initialPrompt = generatedImagePrompt,
                    onAttachGeneratedUri = { uri ->
                        customImageUri = uri
                        isVideoFormat = false
                    },
                    onShowMessage = { msg ->
                        scope.launch { snackbarHostState.showSnackbar(msg) }
                    }
                )
            }

            // Input Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Custom Post Generator Parameters",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        AudioTranscriptionBar(
                            geminiViewModel = geminiStudioViewModel,
                            onApplyArabicCaption = { generatedCaptionAr = it; currentLanguageTab = "AR" },
                            onApplyEnglishCaption = { generatedCaptionEn = it; currentLanguageTab = "EN" },
                            onApplyDishTopic = { dishTopic = it },
                            onShowMessage = { msg ->
                                scope.launch { snackbarHostState.showSnackbar(msg) }
                            }
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Post / Dish Title") },
                            modifier = Modifier.fillMaxWidth().testTag("input_title"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = dishTopic,
                            onValueChange = { dishTopic = it },
                            label = { Text("Dish / Culinary Theme (e.g. Hashi Kabsa, Truffle Burger, Saudi Coffee)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_dish"),
                            singleLine = true
                        )

                        // Target City selection
                        Text(text = "Target City / المنطقة:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(SaudiCity.values()) { city ->
                                FilterChip(
                                    selected = selectedCity == city,
                                    onClick = { selectedCity = city },
                                    label = { Text(city.arabicName, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Content Type Selection
                        Text(text = "Content Format:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(PostContentType.values()) { type ->
                                FilterChip(
                                    selected = selectedContentType == type,
                                    onClick = {
                                        selectedContentType = type
                                        isVideoFormat = type.isVideo
                                    },
                                    label = { Text(type.label, fontSize = 11.sp) },
                                    leadingIcon = {
                                        if (type.isVideo) Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                )
                            }
                        }

                        // Target Platforms Selector
                        Text(text = "Publish to Platforms:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SocialPlatform.values().forEach { platform ->
                                val isSelected = selectedPlatforms.contains(platform)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedPlatforms = if (isSelected) {
                                            if (selectedPlatforms.size > 1) selectedPlatforms - platform else selectedPlatforms
                                        } else {
                                            selectedPlatforms + platform
                                        }
                                    },
                                    label = { Text(platform.title, fontSize = 10.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Time Slot Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Saudi Schedule Slot (AST):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            val slots = listOf("09:00 AM", "12:00 PM", "03:00 PM", "06:00 PM", "09:00 PM")
                            var expanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(timeSlot, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    slots.forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text(s) },
                                            onClick = {
                                                timeSlot = s
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Generate Button
                        Button(
                            onClick = {
                                previewImageRes = when {
                                    dishTopic.contains("burger", ignoreCase = true) || dishTopic.contains("برجر") -> R.drawable.trendy_burger_1793398409429
                                    dishTopic.contains("coffee", ignoreCase = true) || dishTopic.contains("قهوة") || dishTopic.contains("حلا") || dishTopic.contains("معصوب") -> R.drawable.saudi_coffee_1793398408663
                                    else -> R.drawable.hero_kabsa_1793398408425
                                }

                                generatedCaptionAr = "✨ تجربة فاخرة في ${selectedCity.arabicName}! أشهى $dishTopic محضر بأسرار التتبيلة الخاصة والطهي البطيء. حياكم الله واستمتعوا بألذ مذاق مع عائلتكم وأصحابكم 🇸🇦🍴"
                                generatedCaptionEn = "✨ Pure culinary perfection in ${selectedCity.englishName}! Savor our signature $dishTopic cooked to perfection with traditional Saudi spices. Reserve your table or order online today! 🇸🇦"
                                generatedImagePrompt = "Commercial food photography of $dishTopic served in modern ${selectedCity.englishName} restaurant, warm cinematic lighting, authentic Saudi presentation, 8k resolution."

                                scope.launch {
                                    snackbarHostState.showSnackbar("✨ New AI Copy, Hashtags, and Photography Prompt synthesized!")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("btn_synthesize_post"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synthesize Custom AI Post", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Live Post Preview Card (PRIORITY 2 & 3: Image / Media Workflow)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Live Generated Preview & Media",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                selectedPlatforms.forEach { PlatformChip(it) }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Visual Asset Preview with Upload Option
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isVideoFormat) 260.dp else 210.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            if (customImageUri != null) {
                                AsyncImage(
                                    model = customImageUri,
                                    contentDescription = "Uploaded User Media",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = previewImageRes),
                                    contentDescription = "Generated Post Media",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            if (isVideoFormat) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.75f),
                                    modifier = Modifier.align(Alignment.Center)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                        Text("Reels / TikTok 9:16 (0:25)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Slot tag
                            Surface(
                                shape = RoundedCornerShape(bottomEnd = 8.dp),
                                color = Color.Black.copy(alpha = 0.75f),
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Text(
                                    text = "$timeSlot AST",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Media Upload buttons overlay
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.75f),
                                    modifier = Modifier.clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Upload Photo", tint = Color.White, modifier = Modifier.size(14.dp))
                                        Text("Photo", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFD4A017).copy(alpha = 0.9f),
                                    modifier = Modifier.clickable {
                                        videoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Videocam, contentDescription = "Upload Video", tint = Color.Black, modifier = Modifier.size(14.dp))
                                        Text("Video Reel", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // PRIORITY 3: AI Photography Prompt Card
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        Text("AI Photography Prompt (Imagen / Studio)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(generatedImagePrompt))
                                            scope.launch { snackbarHostState.showSnackbar("📋 Image prompt copied to clipboard!") }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Prompt", modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text(
                                    text = generatedImagePrompt,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Language Toggle Tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = currentLanguageTab == "AR",
                                onClick = { currentLanguageTab = "AR" },
                                label = { Text("Arabic (اللهجة السعودية)", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = currentLanguageTab == "EN",
                                onClick = { currentLanguageTab = "EN" },
                                label = { Text("English Hospitality", fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Caption Display / Edit
                        if (currentLanguageTab == "AR") {
                            OutlinedTextField(
                                value = generatedCaptionAr,
                                onValueChange = { generatedCaptionAr = it },
                                modifier = Modifier.fillMaxWidth().testTag("caption_ar_editor"),
                                minLines = 3,
                                maxLines = 5
                            )
                        } else {
                            OutlinedTextField(
                                value = generatedCaptionEn,
                                onValueChange = { generatedCaptionEn = it },
                                modifier = Modifier.fillMaxWidth().testTag("caption_en_editor"),
                                minLines = 3,
                                maxLines = 5
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "#مطاعم_${selectedCity.arabicName.replace(" ", "_")} #اكل_سعودي #سفرة_السعودية #ترند #foodie_ksa",
                            color = Color(0xFF1976D2),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions: Save Draft vs Approve & Schedule vs Direct Publish
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val post = viewModel.generateCustomPost(
                                        title = title,
                                        dishTopic = dishTopic,
                                        targetCity = selectedCity,
                                        category = selectedCategory,
                                        selectedPlatforms = selectedPlatforms,
                                        timeSlot = timeSlot,
                                        isReelVideo = isVideoFormat,
                                        customImagePrompt = generatedImagePrompt
                                    )
                                    if (customImageUri != null) {
                                        post.customImageUri = customImageUri.toString()
                                        viewModel.updatePost(post)
                                    }
                                    scope.launch {
                                        snackbarHostState.showSnackbar("💾 Saved as Draft for $timeSlot!")
                                    }
                                    onNavigateToCalendar()
                                },
                                modifier = Modifier.weight(1f).testTag("btn_save_draft"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Draft", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val post = viewModel.generateCustomPost(
                                        title = title,
                                        dishTopic = dishTopic,
                                        targetCity = selectedCity,
                                        category = selectedCategory,
                                        selectedPlatforms = selectedPlatforms,
                                        timeSlot = timeSlot,
                                        isReelVideo = isVideoFormat,
                                        customImagePrompt = generatedImagePrompt
                                    )
                                    post.status = PostStatus.SCHEDULED
                                    if (customImageUri != null) {
                                        post.customImageUri = customImageUri.toString()
                                    }
                                    viewModel.updatePost(post)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("✅ Approved & Scheduled for $timeSlot AST!")
                                    }
                                    onNavigateToCalendar()
                                },
                                modifier = Modifier.weight(1f).testTag("btn_approve_and_schedule"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Approve & Schedule", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Today's Generated Posts / Drafts List (Allowing quick Approve / Reject / Edit)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Queue & Drafts (${posts.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Asia/Riyadh Slots",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(posts) { post ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            if (post.customImageUri != null) {
                                AsyncImage(
                                    model = post.customImageUri,
                                    contentDescription = post.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (post.imageResId != null) {
                                Image(
                                    painter = painterResource(id = post.imageResId!!),
                                    contentDescription = post.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = post.scheduledTimeSlot, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                StatusBadge(status = post.status)
                            }
                            Text(text = post.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                            Text(text = post.captionAr, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)

                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (post.status == PostStatus.DRAFT || post.status == PostStatus.AWAITING_APPROVAL) {
                                    TextButton(
                                        onClick = {
                                            viewModel.rejectPost(post.id)
                                            scope.launch { snackbarHostState.showSnackbar("Post rejected.") }
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Reject", color = Color(0xFFC62828), fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.approvePost(post.id)
                                            scope.launch { snackbarHostState.showSnackbar("Approved & Scheduled!") }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Approve", fontSize = 10.sp)
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
