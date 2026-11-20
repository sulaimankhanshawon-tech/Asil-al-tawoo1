package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.SaudiMapCity
import com.example.ui.components.GroundingSourcesSection
import com.example.ui.viewmodel.*
import kotlinx.coroutines.launch

enum class AssistantTab(val title: String) {
    CHATBOT("AI Chatbot"),
    LIVE_VOICE("Live Voice")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiAssistantScreen(
    geminiViewModel: GeminiStudioViewModel,
    onClose: () -> Unit,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    var activeTab by remember { mutableStateOf(AssistantTab.CHATBOT) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("btn_close_ai_copilot")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to App"
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = "Sufra Gemini Copilot",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Multi-turn Chat • Grounding • Live Voice API",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    if (activeTab == AssistantTab.CHATBOT) {
                        IconButton(
                            onClick = { geminiViewModel.clearChatHistory() },
                            modifier = Modifier.testTag("btn_clear_chat")
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat History")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize().testTag("gemini_assistant_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                AssistantTab.values().forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        text = {
                            Text(
                                tab.title,
                                fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (tab == AssistantTab.CHATBOT) Icons.Default.Forum else Icons.Default.GraphicEq,
                                contentDescription = tab.title,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            when (activeTab) {
                AssistantTab.CHATBOT -> MultiTurnChatbotPane(
                    geminiViewModel = geminiViewModel,
                    onShowToast = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } },
                    onNavigateToStudio = onNavigateToStudio
                )
                AssistantTab.LIVE_VOICE -> LiveVoiceConversationPane(
                    geminiViewModel = geminiViewModel,
                    onShowToast = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                )
            }
        }
    }
}

@Composable
private fun MultiTurnChatbotPane(
    geminiViewModel: GeminiStudioViewModel,
    onShowToast: (String) -> Unit,
    onNavigateToStudio: () -> Unit
) {
    val selectedModel by geminiViewModel.selectedChatModel.collectAsState()
    val selectedRole by geminiViewModel.selectedChatRole.collectAsState()
    val systemPrompt by geminiViewModel.customSystemInstruction.collectAsState()
    val useSearch by geminiViewModel.enableSearchGrounding.collectAsState()
    val useMaps by geminiViewModel.enableMapsGrounding.collectAsState()
    val cityContext by geminiViewModel.chatCityContext.collectAsState()
    val messages by geminiViewModel.chatMessages.collectAsState()
    val isSending by geminiViewModel.isChatSending.collectAsState()

    var showConfigPanel by remember { mutableStateOf(true) }
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Collapsible Model, Role & Grounding Config Header
        Surface(
            color = Color(0xFF0F2B22),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedModel.badge} • ${selectedRole.title}",
                        color = Color(0xFFD4A017),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(
                        onClick = { showConfigPanel = !showConfigPanel },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = if (showConfigPanel) "Hide Setup" else "Configure AI",
                            color = Color.White,
                            fontSize = 10.5.sp
                        )
                        Icon(
                            imageVector = if (showConfigPanel) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = showConfigPanel) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        // Model Tier Picker
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(AiChatModelTier.values()) { tier ->
                                FilterChip(
                                    selected = selectedModel == tier,
                                    onClick = { geminiViewModel.selectChatModel(tier) },
                                    label = { Text(tier.label, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFD4A017),
                                        selectedLabelColor = Color.Black,
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }

                        // System Role Preset Picker
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(AiChatRolePreset.values()) { role ->
                                FilterChip(
                                    selected = selectedRole == role,
                                    onClick = { geminiViewModel.selectChatRole(role) },
                                    label = { Text(role.title, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF1B4D3E),
                                        selectedLabelColor = Color(0xFFD4A017),
                                        labelColor = Color(0xFFE0E0E0)
                                    )
                                )
                            }
                        }

                        if (selectedRole == AiChatRolePreset.CUSTOM) {
                            OutlinedTextField(
                                value = systemPrompt,
                                onValueChange = { geminiViewModel.updateCustomSystemInstruction(it) },
                                label = { Text("System Instruction", color = Color(0xFFD4A017), fontSize = 10.sp) },
                                minLines = 2,
                                maxLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFD4A017),
                                    unfocusedBorderColor = Color(0xFF2E6B56)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Grounding Tools + City Context
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = useSearch,
                                onClick = { geminiViewModel.toggleSearchGrounding(!useSearch) },
                                label = { Text("Google Search", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(13.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2E7D32),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color.LightGray
                                )
                            )
                            FilterChip(
                                selected = useMaps,
                                onClick = { geminiViewModel.toggleMapsGrounding(!useMaps) },
                                label = { Text("Google Maps", fontSize = 10.sp) },
                                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(13.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2E7D32),
                                    selectedLabelColor = Color.White,
                                    labelColor = Color.LightGray
                                )
                            )

                            var cityExpanded by remember { mutableStateOf(false) }
                            Box {
                                OutlinedButton(
                                    onClick = { cityExpanded = true },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD4A017))
                                ) {
                                    Text(cityContext.englishName, fontSize = 10.sp)
                                }
                                DropdownMenu(expanded = cityExpanded, onDismissRequest = { cityExpanded = false }) {
                                    SaudiMapCity.values().forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text("${c.englishName} (${c.arabicName})") },
                                            onClick = {
                                                geminiViewModel.setChatCityContext(c)
                                                cityExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Scrollable Multi-Turn Conversation Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_message_list"),
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Ask Sufra AI Copilot",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Maintains multi-turn conversation memory with ${selectedRole.title}. Tap a prompt starter or ask anything:",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val starters = listOf(
                                "أعطني خطة محتوى 7 أيام لمطعم كبسة في الرياض",
                                "What are the top specialty coffee spots in Al Khobar right now?",
                                "Compare weekend family dinner promo ideas for Jeddah vs Riyadh"
                            )
                            starters.forEach { starter ->
                                OutlinedButton(
                                    onClick = { geminiViewModel.sendChatMessage(starter) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(starter, fontSize = 11.sp, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatBubbleItem(
                    message = msg,
                    onCopy = {
                        clipboard.setText(AnnotatedString(msg.text))
                        onShowToast("Message copied")
                    },
                    onOpenStudio = onNavigateToStudio
                )
            }
        }

        // Message Composer Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask in Arabic or English...", fontSize = 12.sp) },
                    maxLines = 4,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_chat_message")
                )
                FilledIconButton(
                    onClick = {
                        val toSend = inputText
                        inputText = ""
                        geminiViewModel.sendChatMessage(toSend)
                    },
                    enabled = inputText.isNotBlank() && !isSending,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF0D472B),
                        contentColor = Color(0xFFD4A017)
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_send_chat")
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFFD4A017))
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Chat Message")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessageUi,
    onCopy: () -> Unit,
    onOpenStudio: () -> Unit
) {
    val isUser = message.isUser
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (isUser) 14.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 14.dp
            ),
            color = if (isUser) Color(0xFF0D472B) else MaterialTheme.colorScheme.surface,
            border = if (isUser) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "You" else "Sufra AI • ${message.modelBadge ?: "Gemini"}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) Color(0xFFD4A017) else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = message.timestampRiyadh,
                        fontSize = 9.5.sp,
                        color = if (isUser) Color(0xFFB7D3C6) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (message.errorMessage != null) {
                    Text(
                        text = "Error: ${message.errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = message.text.ifBlank { if (message.isStreaming) "Thinking..." else "" },
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )
                }

                if (!isUser && !message.isStreaming && message.errorMessage == null) {
                    GroundingSourcesSection(
                        sources = message.sources,
                        searchWidgetHtml = message.searchWidgetHtml
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onCopy,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text("Copy", fontSize = 10.5.sp)
                        }
                        TextButton(
                            onClick = onOpenStudio,
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text("AI Studio", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun LiveVoiceConversationPane(
    geminiViewModel: GeminiStudioViewModel,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    val liveState by geminiViewModel.liveVoiceState.collectAsState()
    val systemRole by geminiViewModel.customSystemInstruction.collectAsState()
    var selectedVoice by remember { mutableStateOf(GeminiLiveVoiceOption.KORE) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            geminiViewModel.startLiveVoiceConversation(selectedVoice, systemRole)
        } else {
            onShowToast("Microphone permission is required for Gemini Live voice conversations.")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("live_voice_pane"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B22))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(
                                when (liveState) {
                                    is LiveVoiceState.Active -> Color(0xFF2E7D32)
                                    is LiveVoiceState.Connecting -> Color(0xFFD4A017)
                                    is LiveVoiceState.Error -> Color(0xFFC62828)
                                    is LiveVoiceState.Disconnected -> Color(0xFF1B4D3E)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (liveState) {
                                is LiveVoiceState.Active -> Icons.Default.GraphicEq
                                is LiveVoiceState.Error -> Icons.Default.ErrorOutline
                                else -> Icons.Default.RecordVoiceOver
                            },
                            contentDescription = "Gemini Live Voice Status",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Text(
                        text = "Gemini Live API • Bidirectional Voice",
                        color = Color(0xFFD4A017),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Model: gemini-3.1-flash-live-preview (Real-time audio in & out)",
                        color = Color(0xFFB7D3C6),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                    when (val s = liveState) {
                        is LiveVoiceState.Disconnected -> {
                            Text(
                                text = "Tap Start Voice Session to converse hands-free with Sufra AI about campaigns, captions, and restaurant ideas.",
                                color = Color.White,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        is LiveVoiceState.Connecting -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFFD4A017))
                                Text("Connecting WebSocket Live session...", color = Color.White, fontSize = 12.sp)
                            }
                        }
                        is LiveVoiceState.Active -> {
                            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF1B4D3E)) {
                                Text(
                                    text = "LIVE ACTIVE • ${s.voiceName} • Started ${s.startedAtRiyadh}",
                                    color = Color(0xFFD4A017),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        is LiveVoiceState.Error -> {
                            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF4A1010)) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(s.message, color = Color.White, fontSize = 11.5.sp, textAlign = TextAlign.Center)
                                    TextButton(onClick = { geminiViewModel.dismissLiveVoiceError() }) {
                                        Text("Dismiss", color = Color(0xFFD4A017), fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Voice Selection Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(GeminiLiveVoiceOption.values()) { voiceOpt ->
                            FilterChip(
                                selected = selectedVoice == voiceOpt,
                                onClick = { selectedVoice = voiceOpt },
                                enabled = liveState !is LiveVoiceState.Active,
                                label = { Text(voiceOpt.label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD4A017),
                                    selectedLabelColor = Color.Black,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }

                    if (liveState is LiveVoiceState.Active) {
                        Button(
                            onClick = { geminiViewModel.stopLiveVoiceConversation() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_stop_live_voice")
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("End Live Conversation", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    geminiViewModel.startLiveVoiceConversation(selectedVoice, systemRole)
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            enabled = liveState !is LiveVoiceState.Connecting,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A017)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_start_live_voice")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Live Voice Session", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (liveState is LiveVoiceState.Active) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Send Quick Topic Cue to Live Session", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        val cues = listOf(
                            "Brainstorm 3 Najdi Arabic captions for a Ramadan Kabsa offer",
                            "Suggest peak Asia/Riyadh posting times for a Jeddah specialty cafe",
                            "Give me a 15-second TikTok food hook in Saudi dialect"
                        )
                        cues.forEach { cue ->
                            OutlinedButton(
                                onClick = {
                                    geminiViewModel.sendLivePromptHint(cue)
                                    onShowToast("Sent cue to Live voice session")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(cue, fontSize = 11.sp, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }
        }
    }
}
