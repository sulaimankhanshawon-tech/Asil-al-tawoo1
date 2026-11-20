package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GeminiStudioViewModel
import com.example.ui.viewmodel.SaudiFoodViewModel

enum class AppNavDestination(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
    MAPS("Maps & Reviews", Icons.Default.Explore, "nav_maps"),
    RESEARCH("Saudi Intel", Icons.Default.Analytics, "nav_research"),
    STUDIO("AI Studio", Icons.Default.AutoAwesome, "nav_studio"),
    CALENDAR("Schedule", Icons.Default.CalendarMonth, "nav_calendar"),
    CAMPAIGNS("Campaigns", Icons.Default.RocketLaunch, "nav_campaigns"),
    PLATFORMS("Channels", Icons.Default.Hub, "nav_platforms")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {

            MyApplicationTheme {
                SufraApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SufraApp(
    viewModel: SaudiFoodViewModel = viewModel(),
    geminiStudioViewModel: GeminiStudioViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(AppNavDestination.DASHBOARD) }
    var showAiCopilot by remember { mutableStateOf(false) }
    val isEmergencyPaused by viewModel.isEmergencyPaused.collectAsState()

    // Handle back button to return to dashboard
    BackHandler(enabled = currentScreen != AppNavDestination.DASHBOARD && !showAiCopilot) {
        currentScreen = AppNavDestination.DASHBOARD
    }

    if (showAiCopilot) {
        GeminiAssistantScreen(
            geminiViewModel = geminiStudioViewModel,
            onClose = { showAiCopilot = false },
            onNavigateToStudio = {
                showAiCopilot = false
                currentScreen = AppNavDestination.STUDIO
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "سفرة",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                        }
                        Column {
                            Text(
                                text = currentScreen.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Saudi Food Market AI Manager",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Quick launch for Multi-turn AI Chatbot & Live Voice Copilot
                    Surface(
                        onClick = { showAiCopilot = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFD4A017),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("topbar_ai_copilot_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VoiceChat,
                                contentDescription = "Open Gemini Copilot",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Copilot",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }

                    // Quick status pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isEmergencyPaused) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("topbar_status_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isEmergencyPaused) Color(0xFFC62828) else Color(0xFF2E7D32))
                            )
                            Text(
                                text = if (isEmergencyPaused) "PAUSED" else "5 POSTS/DAY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isEmergencyPaused) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                AppNavDestination.values().forEach { destination ->
                    val isSelected = currentScreen == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = destination },
                        alwaysShowLabel = false,
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppNavDestination.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToStudio = { currentScreen = AppNavDestination.STUDIO },
                    onNavigateToResearch = { currentScreen = AppNavDestination.RESEARCH },
                    onNavigateToCalendar = { currentScreen = AppNavDestination.CALENDAR },
                    onNavigateToPlatforms = { currentScreen = AppNavDestination.PLATFORMS },
                    onNavigateToMaps = { currentScreen = AppNavDestination.MAPS }
                )
                AppNavDestination.MAPS -> MapsAndReviewsScreen(
                    viewModel = viewModel,
                    geminiStudioViewModel = geminiStudioViewModel,
                    onNavigateToStudio = { currentScreen = AppNavDestination.STUDIO },
                    onNavigateToCalendar = { currentScreen = AppNavDestination.CALENDAR }
                )
                AppNavDestination.RESEARCH -> ResearchScreen(
                    viewModel = viewModel,
                    geminiStudioViewModel = geminiStudioViewModel,
                    onNavigateToStudio = { currentScreen = AppNavDestination.STUDIO }
                )
                AppNavDestination.STUDIO -> ContentStudioScreen(
                    viewModel = viewModel,
                    geminiStudioViewModel = geminiStudioViewModel,
                    onNavigateToCalendar = { currentScreen = AppNavDestination.CALENDAR }
                )
                AppNavDestination.CALENDAR -> CalendarScreen(
                    viewModel = viewModel
                )
                AppNavDestination.CAMPAIGNS -> CampaignsScreen(
                    viewModel = viewModel
                )
                AppNavDestination.PLATFORMS -> PlatformsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
