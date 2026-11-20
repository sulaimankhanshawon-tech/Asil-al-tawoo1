package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.*
import com.example.ui.components.GoogleMapsGroundingCard
import com.example.ui.viewmodel.GeminiStudioViewModel
import com.example.ui.viewmodel.SaudiFoodViewModel
import kotlinx.coroutines.launch

enum class MapsTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    EXPLORE("Map & Venues", Icons.Default.Explore),
    MAPS_GROUNDING("Maps AI", Icons.Default.TravelExplore),
    AI_ANALYSIS("AI Review Report", Icons.Default.AutoAwesome),
    COMPETITOR_INTEL("Competitor Intel", Icons.Default.CompareArrows),
    API_SETUP("API & Policy", Icons.Default.Key)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsAndReviewsScreen(
    viewModel: SaudiFoodViewModel,
    geminiStudioViewModel: GeminiStudioViewModel = viewModel(),
    onNavigateToStudio: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val selectedCity by viewModel.selectedMapCity.collectAsState()
    val selectedCategory by viewModel.selectedMapCategory.collectAsState()
    val searchQuery by viewModel.mapSearchQuery.collectAsState()
    val restaurants by viewModel.restaurants.collectAsState()
    val selectedRestaurant by viewModel.selectedRestaurant.collectAsState()
    val isSearchingPlaces by viewModel.isSearchingPlaces.collectAsState()
    val isAnalyzingReviews by viewModel.isAnalyzingReviews.collectAsState()
    val reviewReport by viewModel.activeReviewReport.collectAsState()
    val businessRecs by viewModel.activeBusinessRecommendations.collectAsState()
    val socialCampaign by viewModel.activeSocialCampaign.collectAsState()
    val comparedRestaurants by viewModel.comparedRestaurants.collectAsState()
    val competitorReport by viewModel.competitorReport.collectAsState()
    val searchError by viewModel.searchErrorMessage.collectAsState()
    val placesSearchState by viewModel.placesSearchState.collectAsState()
    val isDemoModeForced by viewModel.isDemoModeForced.collectAsState()

    val isGoogleMapsConfigured = viewModel.isGoogleMapsConfigured
    val isGeminiConfigured = viewModel.isGeminiConfigured

    var activeTab by remember { mutableStateOf(MapsTab.EXPLORE) }
    var showExportSuccessSnackbar by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showExportSuccessSnackbar) {
        showExportSuccessSnackbar?.let { message ->
            snackbarHostState.showSnackbar(message)
            showExportSuccessSnackbar = null
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("maps_and_reviews_screen")
    ) {
        val isDesktop = maxWidth >= 840.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Title Bar with Saudi Branding and Status
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1B4D3E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Maps",
                                        tint = Color(0xFFC5A059),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Google Maps & AI Review Intelligence",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Places API (New) • Gemini Sentiment • Competitor Intel",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Live API Status Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isGoogleMapsConfigured) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                modifier = Modifier.clickable { activeTab = MapsTab.API_SETUP }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isGoogleMapsConfigured) Color(0xFF2E7D32) else Color(0xFFE65100))
                                    )
                                    Text(
                                        text = if (isGoogleMapsConfigured) "LIVE API" else "KEYS SETUP",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGoogleMapsConfigured) Color(0xFF2E7D32) else Color(0xFFE65100)
                                    )
                                }
                            }
                        }

                        // Module Navigation Tabs
                        ScrollableTabRow(
                            selectedTabIndex = activeTab.ordinal,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary,
                            edgePadding = 8.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MapsTab.values().forEach { tab ->
                                Tab(
                                    selected = activeTab == tab,
                                    onClick = { activeTab = tab },
                                    text = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 12.sp,
                                            fontWeight = if (activeTab == tab) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (activeTab) {
                    MapsTab.EXPLORE -> {
                        if (isDesktop) {
                            // Desktop Split: Map on Left (55%), Restaurant Details on Right (45%)
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(0.55f).fillMaxHeight()) {
                                    MapWithControlsSection(
                                        selectedCity = selectedCity,
                                        selectedCategory = selectedCategory,
                                        searchQuery = searchQuery,
                                        restaurants = restaurants,
                                        selectedRestaurant = selectedRestaurant,
                                        isSearching = isSearchingPlaces,
                                        searchError = searchError,
                                        placesSearchState = placesSearchState,
                                        isDemoModeForced = isDemoModeForced,
                                        onSelectCity = { viewModel.onSelectMapCity(it) },
                                        onSelectCategory = { viewModel.onSelectMapCategory(it) },
                                        onSearchQueryChange = { viewModel.onUpdateMapSearchQuery(it) },
                                        onExecuteSearch = { viewModel.executeMapSearch() },
                                        onResetFilters = { viewModel.resetMapFilters() },
                                        onToggleForceDemoMode = { viewModel.toggleForceDemoMode() },
                                        onOpenApiSetupTab = { activeTab = MapsTab.API_SETUP },
                                        onSelectRestaurant = { viewModel.onSelectRestaurant(it) }
                                    )
                                }
                                Box(modifier = Modifier.weight(0.45f).fillMaxHeight()) {
                                    RestaurantDetailPane(
                                        restaurant = selectedRestaurant,
                                        isAnalyzing = isAnalyzingReviews,
                                        reviewReport = reviewReport,
                                        businessRecs = businessRecs,
                                        socialCampaign = socialCampaign,
                                        isCompared = comparedRestaurants.any { it.id == selectedRestaurant?.id },
                                        onToggleCompare = { selectedRestaurant?.let { viewModel.toggleCompetitorSelection(it) } },
                                        onAnalyzeReviews = {
                                            selectedRestaurant?.let {
                                                viewModel.analyzeRestaurantReviews(it)
                                                activeTab = MapsTab.AI_ANALYSIS
                                            }
                                        },
                                        onExportStudio = { campaign ->
                                            viewModel.exportReviewCampaignToStudio(campaign)
                                            showExportSuccessSnackbar = "Campaign for '${campaign.restaurantName}' exported to AI Studio!"
                                            onNavigateToStudio()
                                        },
                                        onExportCalendar = { campaign ->
                                            viewModel.exportReviewCampaignToCalendar(campaign)
                                            showExportSuccessSnackbar = "Campaign for '${campaign.restaurantName}' scheduled in Calendar!"
                                            onNavigateToCalendar()
                                        },
                                        onBackToList = { viewModel.onSelectRestaurant(null) }
                                    )
                                }
                            }
                        } else {
                            // Mobile Layout: Full-Width Map with Top Filters & Bottom Scrollable Venue Cards
                            Column(modifier = Modifier.fillMaxSize()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.52f)
                                ) {
                                    MapWithControlsSection(
                                        selectedCity = selectedCity,
                                        selectedCategory = selectedCategory,
                                        searchQuery = searchQuery,
                                        restaurants = restaurants,
                                        selectedRestaurant = selectedRestaurant,
                                        isSearching = isSearchingPlaces,
                                        searchError = searchError,
                                        placesSearchState = placesSearchState,
                                        isDemoModeForced = isDemoModeForced,
                                        onSelectCity = { viewModel.onSelectMapCity(it) },
                                        onSelectCategory = { viewModel.onSelectMapCategory(it) },
                                        onSearchQueryChange = { viewModel.onUpdateMapSearchQuery(it) },
                                        onExecuteSearch = { viewModel.executeMapSearch() },
                                        onResetFilters = { viewModel.resetMapFilters() },
                                        onToggleForceDemoMode = { viewModel.toggleForceDemoMode() },
                                        onOpenApiSetupTab = { activeTab = MapsTab.API_SETUP },
                                        onSelectRestaurant = { viewModel.onSelectRestaurant(it) }
                                    )
                                }

                                // Bottom Results Drawer
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.48f)
                                        .background(MaterialTheme.colorScheme.background)
                                ) {
                                    if (selectedRestaurant != null) {
                                        RestaurantDetailPane(
                                            restaurant = selectedRestaurant,
                                            isAnalyzing = isAnalyzingReviews,
                                            reviewReport = reviewReport,
                                            businessRecs = businessRecs,
                                            socialCampaign = socialCampaign,
                                            isCompared = comparedRestaurants.any { it.id == selectedRestaurant?.id },
                                            onToggleCompare = { selectedRestaurant?.let { viewModel.toggleCompetitorSelection(it) } },
                                            onAnalyzeReviews = {
                                                selectedRestaurant?.let {
                                                    viewModel.analyzeRestaurantReviews(it)
                                                    activeTab = MapsTab.AI_ANALYSIS
                                                }
                                            },
                                            onExportStudio = { campaign ->
                                                viewModel.exportReviewCampaignToStudio(campaign)
                                                showExportSuccessSnackbar = "Exported to AI Studio!"
                                                onNavigateToStudio()
                                            },
                                            onExportCalendar = { campaign ->
                                                viewModel.exportReviewCampaignToCalendar(campaign)
                                                showExportSuccessSnackbar = "Scheduled in Calendar!"
                                                onNavigateToCalendar()
                                            },
                                            onBackToList = { viewModel.onSelectRestaurant(null) }
                                        )
                                    } else {
                                        RestaurantCardCarousel(
                                            restaurants = restaurants,
                                            selectedRestaurant = selectedRestaurant,
                                            selectedCity = selectedCity,
                                            searchQuery = searchQuery,
                                            onResetFilters = { viewModel.resetMapFilters() },
                                            onSelectRestaurant = { viewModel.onSelectRestaurant(it) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    MapsTab.MAPS_GROUNDING -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                GoogleMapsGroundingCard(
                                    geminiViewModel = geminiStudioViewModel,
                                    selectedCity = selectedCity
                                )
                            }
                        }
                    }

                    MapsTab.AI_ANALYSIS -> {
                        AiReviewAnalysisTabContent(
                            restaurant = selectedRestaurant,
                            report = reviewReport,
                            businessRecs = businessRecs,
                            socialCampaign = socialCampaign,
                            isAnalyzing = isAnalyzingReviews,
                            onAnalyze = { selectedRestaurant?.let { viewModel.analyzeRestaurantReviews(it) } },
                            onExportToStudio = { campaign ->
                                viewModel.exportReviewCampaignToStudio(campaign)
                                showExportSuccessSnackbar = "Exported to AI Studio!"
                                onNavigateToStudio()
                            },
                            onExportToCalendar = { campaign ->
                                viewModel.exportReviewCampaignToCalendar(campaign)
                                showExportSuccessSnackbar = "Scheduled in Content Calendar!"
                                onNavigateToCalendar()
                            },
                            onBackToMap = { activeTab = MapsTab.EXPLORE }
                        )
                    }

                    MapsTab.COMPETITOR_INTEL -> {
                        CompetitorIntelligenceTabContent(
                            allRestaurants = restaurants,
                            comparedRestaurants = comparedRestaurants,
                            report = competitorReport,
                            onToggleRestaurant = { viewModel.toggleCompetitorSelection(it) },
                            onClearSelection = { viewModel.clearCompetitorSelection() },
                            onGenerateReport = { viewModel.generateCompetitorComparisonReport() },
                            onSelectRestaurant = {
                                viewModel.onSelectRestaurant(it)
                                activeTab = MapsTab.EXPLORE
                            }
                        )
                    }

                    MapsTab.API_SETUP -> {
                        ApiSetupAndPolicyTabContent(
                            isGoogleConfigured = isGoogleMapsConfigured,
                            isGeminiConfigured = isGeminiConfigured,
                            onRefreshData = { viewModel.loadRestaurants() }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Google Map Section with Search Bar, 8 Saudi Cities Selector, and 10 Food Categories Filter
 */
@Composable
fun MapWithControlsSection(
    selectedCity: SaudiMapCity,
    selectedCategory: MapFoodCategory?,
    searchQuery: String,
    restaurants: List<SaudiRestaurant>,
    selectedRestaurant: SaudiRestaurant?,
    isSearching: Boolean,
    searchError: String?,
    placesSearchState: PlacesSearchState?,
    isDemoModeForced: Boolean,
    onSelectCity: (SaudiMapCity) -> Unit,
    onSelectCategory: (MapFoodCategory?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onExecuteSearch: () -> Unit,
    onResetFilters: () -> Unit,
    onToggleForceDemoMode: () -> Unit,
    onOpenApiSetupTab: () -> Unit,
    onSelectRestaurant: (SaudiRestaurant) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Search & Filters Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Input with Explicit Search Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("map_search_input"),
                        placeholder = {
                            Text(
                                text = "Search in ${selectedCity.englishName} (e.g. Al Baik, burgers)...",
                                fontSize = 11.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFFC5A059),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else if (searchQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    onSearchQueryChange("")
                                    onExecuteSearch()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onExecuteSearch() }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1B4D3E),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Button(
                        onClick = onExecuteSearch,
                        modifier = Modifier.testTag("btn_map_search"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFE5C07B))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Search", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE5C07B))
                    }
                }

                // 8 Saudi Cities Selector Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "City:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SaudiMapCity.values().forEach { city ->
                        val isSelected = (city == selectedCity)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectCity(city) },
                            label = {
                                Text(
                                    text = "${city.englishName} (${city.arabicName})",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1B4D3E),
                                selectedLabelColor = Color(0xFFE5C07B)
                            ),
                            modifier = Modifier.testTag("chip_city_${city.name.lowercase()}")
                        )
                    }
                }

                // 10 Official Food Categories Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "All" Category Chip
                    FilterChip(
                        selected = (selectedCategory == null),
                        onClick = { onSelectCategory(null) },
                        label = { Text("All Categories (الكل)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFC5A059),
                            selectedLabelColor = Color(0xFF0D472B)
                        )
                    )

                    MapFoodCategory.values().forEach { cat ->
                        val isSelected = (cat == selectedCategory)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectCategory(if (isSelected) null else cat) },
                            label = {
                                Text(
                                    text = "${cat.englishName} • ${cat.arabicName}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1B4D3E),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("chip_cat_${cat.name.lowercase()}")
                        )
                    }
                }

                // Diagnostic API Status Banner
                if (placesSearchState != null) {
                    when (placesSearchState.status) {
                        PlacesApiStatus.LIVE_CONNECTED -> {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                                modifier = Modifier.fillMaxWidth().testTag("live_places_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2E7D32)))
                                        Text(
                                            text = placesSearchState.statusBannerText,
                                            color = Color(0xFF1B5E20),
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    TextButton(
                                        onClick = onToggleForceDemoMode,
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("Demo Mode", fontSize = 10.sp, color = Color(0xFF2E7D32))
                                    }
                                }
                            }
                        }
                        PlacesApiStatus.SETUP_REQUIRED_DEMO_MODE -> {
                            Surface(
                                color = Color(0xFFFFF8E1),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFE082)),
                                modifier = Modifier.fillMaxWidth().testTag("demo_mode_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(16.dp))
                                        Column {
                                            Text(
                                                text = "Demo Mode: Verified Reference Venues (${placesSearchState.citySearched.englishName})",
                                                color = Color(0xFFB78103),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "To enable live Places API, set GOOGLE_MAPS_API_KEY in Secrets panel.",
                                                color = Color(0xFF5D4037),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = onOpenApiSetupTab,
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Setup Key", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                        PlacesApiStatus.API_ERROR -> {
                            Surface(
                                color = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
                                modifier = Modifier.fillMaxWidth().testTag("api_error_banner")
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Google Places API Error (Live Request Failed)",
                                            color = Color(0xFFC62828),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = placesSearchState.statusBannerText,
                                        color = Color(0xFFB71C1C),
                                        fontSize = 10.sp
                                    )
                                    if (!placesSearchState.errorDetails.isNullOrBlank()) {
                                        Text(
                                            text = "Details: ${placesSearchState.errorDetails}",
                                            color = Color(0xFF7F0000),
                                            fontSize = 9.5.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = onToggleForceDemoMode,
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Use Demo Mode", fontSize = 10.sp)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Button(
                                            onClick = onExecuteSearch,
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E))
                                        ) {
                                            Text("Retry Live", fontSize = 10.sp, color = Color(0xFFE5C07B))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Interactive Map Canvas (WebView bridge rendering Google Maps & stylized markers)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0F1E17))
        ) {
            GoogleMapsWebViewBridge(
                city = selectedCity,
                restaurants = restaurants,
                selectedRestaurant = selectedRestaurant,
                onMarkerClick = { restaurantId ->
                    restaurants.firstOrNull { it.id == restaurantId }?.let { onSelectRestaurant(it) }
                }
            )

            // Overlaid Google Attribution & Venue Counter
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xDD0D472B),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color(0xFFC5A059),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${restaurants.size} restaurants found in ${selectedCity.englishName}",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• Map data © Google",
                        fontSize = 9.sp,
                        color = Color(0xFFE5C07B)
                    )
                }
            }
        }
    }
}

/**
 * High-performance interactive Google Maps JavaScript API & HTML5 Map Bridge
 * Supports touch interactions, zoom controls, dynamic markers, info windows, and bidirectional callback.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GoogleMapsWebViewBridge(
    city: SaudiMapCity,
    restaurants: List<SaudiRestaurant>,
    selectedRestaurant: SaudiRestaurant?,
    onMarkerClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val mapHtml = remember(city, restaurants, selectedRestaurant) {
        generateGoogleMapHtml(city, restaurants, selectedRestaurant)
    }

    AndroidView(
        modifier = modifier
            .fillMaxSize()
            .testTag("google_map_webview"),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onRestaurantMarkerClicked(placeId: String) {
                            post { onMarkerClick(placeId) }
                        }
                    },
                    "AndroidBridge"
                )
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://maps.googleapis.com", mapHtml, "text/html", "UTF-8", null)
        }
    )
}

/**
 * Builds responsive dark green & gold styled HTML5 map with markers and interactive info windows
 */
fun generateGoogleMapHtml(
    city: SaudiMapCity,
    restaurants: List<SaudiRestaurant>,
    selectedRestaurant: SaudiRestaurant?
): String {
    val markersJson = restaurants.joinToString(",") { r ->
        val isSelected = (r.id == selectedRestaurant?.id)
        """{
            "id": "${r.id}",
            "name": "${r.name.replace("\"", "\\\"")}",
            "category": "${r.category.englishName}",
            "rating": ${r.rating},
            "reviews": ${r.userRatingCount},
            "address": "${r.formattedAddress.replace("\"", "\\\"")}",
            "lat": ${r.latitude},
            "lng": ${r.longitude},
            "isSelected": $isSelected
        }"""
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
                html, body, #map-canvas { width: 100%; height: 100%; background: #0D261C; overflow: hidden; }
                .map-header { position: absolute; top: 10px; right: 10px; z-index: 10; background: rgba(13, 71, 43, 0.95); border: 1px solid #C5A059; color: #FFF; padding: 6px 12px; border-radius: 20px; font-size: 11px; display: flex; align-items: center; gap: 6px; box-shadow: 0 4px 12px rgba(0,0,0,0.4); }
                .map-dot { width: 8px; height: 8px; border-radius: 50%; background: #4CAF50; animation: pulse 2s infinite; }
                @keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.4; } 100% { opacity: 1; } }
                .restaurant-pin { position: absolute; transform: translate(-50%, -100%); cursor: pointer; transition: all 0.25s ease; z-index: 5; }
                .restaurant-pin.active { z-index: 15; transform: translate(-50%, -100%) scale(1.15); }
                .pin-badge { background: #1B4D3E; border: 2px solid #C5A059; border-radius: 12px; padding: 4px 8px; color: #FFF; font-size: 11px; font-weight: bold; white-space: nowrap; display: flex; align-items: center; gap: 4px; box-shadow: 0 4px 10px rgba(0,0,0,0.5); }
                .restaurant-pin.active .pin-badge { background: #C5A059; color: #0D472B; border-color: #FFF; }
                .pin-tip { width: 0; height: 0; border-left: 6px solid transparent; border-right: 6px solid transparent; border-top: 8px solid #C5A059; margin: 0 auto; }
                .restaurant-pin.active .pin-tip { border-top-color: #FFF; }
                .info-window { position: absolute; bottom: 15px; left: 15px; right: 15px; background: rgba(13, 71, 43, 0.98); border: 2px solid #C5A059; border-radius: 14px; padding: 12px 14px; color: #FFF; z-index: 20; box-shadow: 0 8px 24px rgba(0,0,0,0.6); display: none; }
                .info-window.show { display: block; animation: slideUp 0.2s ease-out; }
                @keyframes slideUp { from { transform: translateY(15px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
                .info-title { font-size: 14px; font-weight: bold; color: #E5C07B; display: flex; justify-content: space-between; align-items: center; }
                .info-subtitle { font-size: 11px; color: #D1D5DB; margin-top: 3px; }
                .info-btn { margin-top: 8px; background: #C5A059; color: #0D472B; border: none; padding: 6px 12px; border-radius: 8px; font-size: 11px; font-weight: bold; width: 100%; cursor: pointer; }
                .grid-bg { position: absolute; width: 100%; height: 100%; background-image: linear-gradient(rgba(197, 160, 89, 0.08) 1px, transparent 1px), linear-gradient(90deg, rgba(197, 160, 89, 0.08) 1px, transparent 1px); background-size: 40px 40px; }
                .compass-rose { position: absolute; bottom: 12px; right: 12px; width: 44px; height: 44px; border: 1px solid rgba(197, 160, 89, 0.4); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: #C5A059; font-size: 10px; font-weight: bold; pointer-events: none; }
            </style>
        </head>
        <body>
            <div id="map-canvas">
                <div class="grid-bg"></div>
                <div class="map-header">
                    <span class="map-dot"></span>
                    <span>${city.englishName} • Google Maps Places</span>
                </div>
                <div class="compass-rose">N ↑</div>
                <div id="pins-container"></div>
                <div id="info-card" class="info-window">
                    <div class="info-title">
                        <span id="card-name">Restaurant</span>
                        <span id="card-rating">★ 4.7</span>
                    </div>
                    <div id="card-address" class="info-subtitle">Address</div>
                    <button class="info-btn" onclick="triggerAndroidClick()">Select Venue & View AI Intel</button>
                </div>
            </div>
            <script>
                const cityCenter = { lat: ${city.lat}, lng: ${city.lng} };
                const venues = [$markersJson];
                let activeId = null;

                function renderPins() {
                    const container = document.getElementById('pins-container');
                    container.innerHTML = '';
                    if (!venues || venues.length === 0) return;

                    // Compute relative screen coordinates
                    const lats = venues.map(v => v.lat);
                    const lngs = venues.map(v => v.lng);
                    const minLat = Math.min(...lats, cityCenter.lat - 0.04);
                    const maxLat = Math.max(...lats, cityCenter.lat + 0.04);
                    const minLng = Math.min(...lngs, cityCenter.lng - 0.05);
                    const maxLng = Math.max(...lngs, cityCenter.lng + 0.05);

                    venues.forEach((v) => {
                        const topPct = 10 + ((maxLat - v.lat) / (maxLat - minLat || 1)) * 72;
                        const leftPct = 12 + ((v.lng - minLng) / (maxLng - minLng || 1)) * 76;

                        const pin = document.createElement('div');
                        pin.className = 'restaurant-pin' + (v.isSelected ? ' active' : '');
                        pin.style.top = topPct + '%';
                        pin.style.left = leftPct + '%';
                        pin.onclick = (e) => {
                            e.stopPropagation();
                            selectVenue(v.id);
                        };

                        pin.innerHTML = `
                            <div class="pin-badge">
                                <span>★ ` + v.rating + `</span>
                                <span>` + v.name.substring(0, 18) + `</span>
                            </div>
                            <div class="pin-tip"></div>
                        `;
                        container.appendChild(pin);

                        if (v.isSelected) {
                            activeId = v.id;
                            showInfo(v);
                        }
                    });
                }

                function selectVenue(id) {
                    activeId = id;
                    const venue = venues.find(v => v.id === id);
                    if (venue) {
                        showInfo(venue);
                    }
                    if (window.AndroidBridge && window.AndroidBridge.onRestaurantMarkerClicked) {
                        window.AndroidBridge.onRestaurantMarkerClicked(id);
                    }
                }

                function showInfo(v) {
                    const card = document.getElementById('info-card');
                    document.getElementById('card-name').innerText = v.name;
                    document.getElementById('card-rating').innerText = '★ ' + v.rating + ' (' + v.reviews + ' reviews)';
                    document.getElementById('card-address').innerText = v.address;
                    card.className = 'info-window show';
                }

                function triggerAndroidClick() {
                    if (activeId && window.AndroidBridge && window.AndroidBridge.onRestaurantMarkerClicked) {
                        window.AndroidBridge.onRestaurantMarkerClicked(activeId);
                    }
                }

                window.onload = renderPins;
            </script>
        </body>
        </html>
    """.trimIndent()
}

/**
 * Scrollable Restaurant Card Carousel for quick browsing
 */
@Composable
fun RestaurantCardCarousel(
    restaurants: List<SaudiRestaurant>,
    selectedRestaurant: SaudiRestaurant?,
    selectedCity: SaudiMapCity,
    searchQuery: String,
    onResetFilters: () -> Unit,
    onSelectRestaurant: (SaudiRestaurant) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Venues in ${selectedCity.englishName} (${restaurants.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "Tap venue to inspect reviews & AI report",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (restaurants.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFFC5A059)
                    )
                    Text(
                        text = "0 restaurants found",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "No venues matched '$searchQuery' in ${selectedCity.englishName}."
                        } else {
                            "No venues found in ${selectedCity.englishName} for the selected category."
                        },
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = onResetFilters,
                        modifier = Modifier.testTag("btn_reset_map_filters"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFE5C07B))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Search & Show All ${selectedCity.englishName} Restaurants", fontSize = 11.sp, color = Color(0xFFE5C07B), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(restaurants) { r ->
                    val isSelected = (r.id == selectedRestaurant?.id)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRestaurant(r) }
                            .testTag("restaurant_card_${r.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFF1B4D3E) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFC5A059), Color(0xFFE5C07B)))) else null,
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = r.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color(0xFFE5C07B) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (r.isLivePlacesData) Color(0xFFE8F5E9) else Color(0xFFFFF8E1),
                                        border = BorderStroke(0.5.dp, if (r.isLivePlacesData) Color(0xFF81C784) else Color(0xFFFFD54F))
                                    ) {
                                        Text(
                                            text = if (r.isLivePlacesData) "LIVE PLACES" else "DEMO MODE",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            color = if (r.isLivePlacesData) Color(0xFF1B5E20) else Color(0xFFB78103)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${r.category.englishName} • ${r.formattedAddress}",
                                    fontSize = 11.sp,
                                    color = if (isSelected) Color(0xFFE0E0E0) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${r.rating}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "(${r.userRatingCount} reviews)",
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color(0xFFD7CCC8) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Detailed Restaurant Information Pane with Google ratings, contact info, hours,
 * reviews list (up to 5), and "Analyze Reviews with AI" action button
 */
@Composable
fun RestaurantDetailPane(
    restaurant: SaudiRestaurant?,
    isAnalyzing: Boolean,
    reviewReport: AiReviewReport?,
    businessRecs: AiBusinessRecommendations?,
    socialCampaign: ReviewSocialCampaign?,
    isCompared: Boolean,
    onToggleCompare: () -> Unit,
    onAnalyzeReviews: () -> Unit,
    onExportStudio: (ReviewSocialCampaign) -> Unit,
    onExportCalendar: (ReviewSocialCampaign) -> Unit,
    onBackToList: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (restaurant == null) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFFC5A059), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Select a restaurant from the map or list", fontWeight = FontWeight.Bold)
                Text("View official Google rating, contact, opening hours & AI sentiment", fontSize = 12.sp, color = Color.Gray)
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Navigation row & Live vs Demo badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackToList != null) {
                TextButton(
                    onClick = onBackToList,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Back to All Venues", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (restaurant.isLivePlacesData) Color(0xFFE8F5E9) else Color(0xFFFFF8E1),
                border = BorderStroke(0.5.dp, if (restaurant.isLivePlacesData) Color(0xFF81C784) else Color(0xFFFFD54F))
            ) {
                Text(
                    text = if (restaurant.isLivePlacesData) "LIVE PLACES (NEW)" else "DEMO MODE (VERIFIED DATA)",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (restaurant.isLivePlacesData) Color(0xFF1B5E20) else Color(0xFFB78103),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Venue Card Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = restaurant.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${restaurant.category.englishName} • ${restaurant.city.englishName}",
                            fontSize = 12.sp,
                            color = Color(0xFF1B4D3E),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Rating Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1B4D3E),
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFC5A059), Color(0xFFE5C07B))))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                            Text(
                                text = "${restaurant.rating}",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Based on ${restaurant.userRatingCount} verified Google Maps reviews",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                // Address & Action links
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFC5A059), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = restaurant.formattedAddress, fontSize = 12.sp)
                }

                restaurant.nationalPhoneNumber?.let { phone ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.clickable {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF1B4D3E), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (restaurant.regularOpeningHours.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFC5A059), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = restaurant.regularOpeningHours.first(), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // External Google Maps & Compare Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(restaurant.googleMapsUri))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Google Maps", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onToggleCompare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = if (isCompared) ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFE8F5E9)) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(
                            imageVector = if (isCompared) Icons.Default.Check else Icons.Default.CompareArrows,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isCompared) Color(0xFF2E7D32) else Color(0xFF1B4D3E)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isCompared) "In Comparison" else "+ Compare", fontSize = 11.sp)
                    }
                }
            }
        }

        // Primary Action: "Analyze Reviews with AI"
        Button(
            onClick = onAnalyzeReviews,
            enabled = !isAnalyzing,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_analyze_reviews_ai"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1B4D3E),
                contentColor = Color(0xFFE5C07B)
            )
        ) {
            if (isAnalyzing) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color(0xFFE5C07B), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing Customer Reviews via Gemini AI...", fontSize = 13.sp)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFE5C07B), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyze Reviews with AI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Returned Reviews Preview (up to 5 per Google Places API)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Customer Reviews (${restaurant.reviews.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Places API limit: max 5",
                        fontSize = 10.sp,
                        color = Color(0xFFC5A059),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (restaurant.reviews.isEmpty()) {
                    Text("No customer reviews returned for this venue yet.", fontSize = 12.sp, color = Color.Gray)
                } else {
                    restaurant.reviews.take(5).forEachIndexed { idx, rev ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = rev.authorName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(rev.rating) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(11.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = rev.relativePublishTimeDescription, fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "\"${rev.text}\"",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (idx < restaurant.reviews.size - 1) {
                                Divider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * AI Review Intelligence Report Screen (Section 3, 4, 6, 7 in prompt)
 * Clearly distinguishes between actual customer review quotes and AI interpretations.
 * Explicitly states that analysis is based on available returned reviews (up to 5 per Places API policy).
 */
@Composable
fun AiReviewAnalysisTabContent(
    restaurant: SaudiRestaurant?,
    report: AiReviewReport?,
    businessRecs: AiBusinessRecommendations?,
    socialCampaign: ReviewSocialCampaign?,
    isAnalyzing: Boolean,
    onAnalyze: () -> Unit,
    onExportToStudio: (ReviewSocialCampaign) -> Unit,
    onExportToCalendar: (ReviewSocialCampaign) -> Unit,
    onBackToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (restaurant == null) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Explore, contentDescription = null, tint = Color(0xFFC5A059), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No restaurant selected", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBackToMap, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E))) {
                    Text("Select a Venue on the Map", color = Color(0xFFE5C07B))
                }
            }
        }
        return
    }

    if (report == null) {
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFC5A059), modifier = Modifier.size(42.dp))
                    Text("AI Review Intelligence for ${restaurant.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                    Text(
                        "Run multi-factor sentiment classification across taste, service, cleanliness, atmosphere, and value for money.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onAnalyze,
                        enabled = !isAnalyzing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E))
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFFE5C07B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Reviews...", color = Color(0xFFE5C07B))
                        } else {
                            Text("Analyze ${restaurant.reviews.size} Google Reviews Now", color = Color(0xFFE5C07B), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ai_review_report_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Mandatory Disclaimer Banner (Requirement: explicitly state analysis is based on available returned reviews up to 5)
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF8E1),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFC5A059), Color(0xFFFFD54F))))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(18.dp))
                    Column {
                        Text(
                            text = "Google Places API Policy Notice",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = report.disclaimer,
                            fontSize = 11.sp,
                            color = Color(0xFF424242)
                        )
                    }
                }
            }
        }

        // 2. Executive Overall Sentiment Summary & Scorecard
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B4D3E))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = report.restaurantName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFFE5C07B)
                    )
                    Text(
                        text = "Overall Sentiment: ${report.overallSentiment}",
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Sentiment Breakdown Progress Bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SentimentScorePill(
                            label = "Positive",
                            percentage = report.positiveScorePercent,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        SentimentScorePill(
                            label = "Neutral",
                            percentage = report.neutralScorePercent,
                            color = Color(0xFFFFB300),
                            modifier = Modifier.weight(1f)
                        )
                        SentimentScorePill(
                            label = "Negative",
                            percentage = report.negativeScorePercent,
                            color = Color(0xFFE53935),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Positive & Negative Sentiment Themes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Positive Themes Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Positive Themes", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        report.topPositiveThemes.forEach { theme ->
                            Text("• $theme", fontSize = 11.sp, color = Color(0xFF1B5E20))
                        }
                    }
                }

                // Negative Themes Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ThumbDown, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Negative Themes", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFC62828))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        report.topNegativeThemes.forEach { theme ->
                            Text("• $theme", fontSize = 11.sp, color = Color(0xFFB71C1C))
                        }
                    }
                }
            }
        }

        // 4. Classified Reviews: Actual Quotes vs AI Interpretations (Mandatory Requirement)
        item {
            Text(
                text = "Classified Customer Feedback (Quotes vs. AI Interpretations)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        items(report.classifiedReviewItems) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item.reviewerName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(item.polarity.colorHex)
                        ) {
                            Text(
                                text = "${item.polarity.label} • ${item.matchedTheme}",
                                color = Color.White,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Explicit Actual Customer Quote Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "ACTUAL CUSTOMER REVIEW QUOTE:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B4D3E)
                            )
                            Text(
                                text = item.actualCustomerQuote,
                                fontSize = 11.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // AI Interpretation Box
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFFDE7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "AI SEMANTIC INTERPRETATION:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC5A059)
                            )
                            Text(
                                text = item.aiInterpretation,
                                fontSize = 11.sp,
                                color = Color(0xFF37474F)
                            )
                        }
                    }
                }
            }
        }

        // 5. Frequently Mentioned Food Items & Appreciated Features
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Top Mentions & Appreciated Features", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        report.frequentlyMentionedFoodItems.forEach { dish ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF1B4D3E)
                            ) {
                                Text(
                                    text = "🍽️ $dish",
                                    color = Color(0xFFE5C07B),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    report.mostAppreciatedFeatures.forEach { feat ->
                        Text("• $feat", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 6. AI Practical Business Recommendations (Section 6)
        if (businessRecs != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = Color(0xFFC5A059), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Practical Business & Delivery Recommendations", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        businessRecs.serviceImprovements.forEach { Text("🔧 $it", fontSize = 11.sp) }
                        businessRecs.deliveryImprovements.forEach { Text("🛵 $it", fontSize = 11.sp) }
                        businessRecs.newFoodBusinessOpportunities.forEach { Text("🚀 $it", fontSize = 11.sp, color = Color(0xFF1B4D3E), fontWeight = FontWeight.SemiBold) }
                    }
                }
            }
        }

        // 7. AI Social Media Connection (Section 7 in prompt)
        if (socialCampaign != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D472B)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(0xFFC5A059), Color(0xFFE5C07B))))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFFE5C07B), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Social Campaign from Customer Reviews", fontWeight = FontWeight.Bold, color = Color(0xFFE5C07B), fontSize = 14.sp)
                        }

                        Text("ARABIC CAPTION:", fontSize = 10.sp, color = Color(0xFFC5A059), fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0x33000000), modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = socialCampaign.arabicCaption,
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Text("ENGLISH CAPTION:", fontSize = 10.sp, color = Color(0xFFC5A059), fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0x33000000), modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = socialCampaign.englishCaption,
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Text("HASHTAGS:", fontSize = 10.sp, color = Color(0xFFC5A059), fontWeight = FontWeight.Bold)
                        Text(
                            text = socialCampaign.hashtags.joinToString(" "),
                            fontSize = 11.sp,
                            color = Color(0xFFE5C07B)
                        )

                        Text("SHORT-VIDEO / REEL CONCEPT:", fontSize = 10.sp, color = Color(0xFFC5A059), fontWeight = FontWeight.Bold)
                        Text(
                            text = socialCampaign.shortVideoConcept,
                            fontSize = 11.sp,
                            color = Color(0xFFE0E0E0)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 1-Click Export to AI Studio & Content Calendar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onExportToStudio(socialCampaign) },
                                modifier = Modifier.weight(1f).testTag("btn_export_studio"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5A059), contentColor = Color(0xFF0D472B))
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Send to Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onExportToCalendar(socialCampaign) },
                                modifier = Modifier.weight(1f).testTag("btn_export_calendar"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Send to Schedule", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SentimentScorePill(
    label: String,
    percentage: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, color),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$percentage%", fontWeight = FontWeight.Bold, color = color, fontSize = 15.sp)
            Text(text = label, fontSize = 10.sp, color = Color.White)
        }
    }
}

/**
 * Competitor Intelligence Screen (Section 5 in prompt)
 * Compares up to 5 restaurants objectively.
 */
@Composable
fun CompetitorIntelligenceTabContent(
    allRestaurants: List<SaudiRestaurant>,
    comparedRestaurants: List<SaudiRestaurant>,
    report: CompetitorComparisonReport?,
    onToggleRestaurant: (SaudiRestaurant) -> Unit,
    onClearSelection: () -> Unit,
    onGenerateReport: () -> Unit,
    onSelectRestaurant: (SaudiRestaurant) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("competitor_intel_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B4D3E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Competitor Intelligence Matrix",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5C07B),
                            fontSize = 16.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0x33C5A059)
                        ) {
                            Text(
                                text = "${comparedRestaurants.size} / 5 Selected",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Select up to 5 Saudi restaurants to generate an objective comparative benchmark across ratings, volume, customer sentiment themes, and strategic opportunities.",
                        fontSize = 11.sp,
                        color = Color(0xFFE0E0E0)
                    )

                    if (comparedRestaurants.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onGenerateReport,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5A059), contentColor = Color(0xFF0D472B))
                            ) {
                                Text("Generate Comparison", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = onClearSelection,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("Clear", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Available Restaurants Chips for quick addition
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Select Venues to Compare:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allRestaurants.forEach { venue ->
                        val isSelected = comparedRestaurants.any { it.id == venue.id }
                        FilterChip(
                            selected = isSelected,
                            onClick = { onToggleRestaurant(venue) },
                            label = { Text(venue.name, fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1B4D3E),
                                selectedLabelColor = Color(0xFFE5C07B)
                            )
                        )
                    }
                }
            }
        }

        // Objective Disclaimer Banner (Requirement: Do not claim higher rating proves better performance)
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9),
                border = BorderStroke(1.dp, Color(0xFF81C784))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    Text(
                        text = "Objective Policy: Higher Google ratings reflect customer sentiment trends but do not definitively prove better food or business performance. Market volume and peak wait times heavily factor into dining feedback.",
                        fontSize = 11.sp,
                        color = Color(0xFF1B5E20)
                    )
                }
            }
        }

        // Side-by-side Venue Metrics Matrix
        if (comparedRestaurants.isNotEmpty()) {
            item {
                Text("Side-by-Side Comparison Matrix", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            items(comparedRestaurants) { venue ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = venue.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "${venue.category.englishName} • ${venue.city.englishName}", fontSize = 11.sp, color = Color.Gray)
                            }
                            IconButton(onClick = { onToggleRestaurant(venue) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Rating", fontSize = 10.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                    Text(" ${venue.rating}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            Column {
                                Text("Review Count", fontSize = 10.sp, color = Color.Gray)
                                Text("${venue.userRatingCount}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Price Level", fontSize = 10.sp, color = Color.Gray)
                                Text(venue.priceLevel ?: "$$", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // AI Competitor Comparison Report Summary
        if (report != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFC5A059), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Competitor Intelligence Assessment", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Text(text = report.comparisonSummary, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        Text("MARKET OBSERVATION:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B4D3E))
                        Text(text = report.objectiveObservation, fontSize = 11.sp)

                        Text("STRATEGIC MARKETING ADVICE:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC5A059))
                        report.strategicMarketingAdvice.forEach { Text("💡 $it", fontSize = 11.sp) }
                    }
                }
            }
        }
    }
}

/**
 * API Setup & Policy Screen (Section 9 & 10 in prompt)
 * Clearly instructs user how to configure GOOGLE_MAPS_API_KEY and GEMINI_API_KEY
 * without displaying hardcoded tokens or generating fake data.
 */
@Composable
fun ApiSetupAndPolicyTabContent(
    isGoogleConfigured: Boolean,
    isGeminiConfigured: Boolean,
    onRefreshData: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("api_setup_policy_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B4D3E))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "API Credentials & Compliance",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5C07B),
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sufra AI adheres strictly to Google Maps Platform policies and Google AI Terms of Service.",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Credentials Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("API Configuration Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    // Google Maps Key Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("GOOGLE_MAPS_API_KEY", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Google Maps JS API & Places API (New)", fontSize = 10.sp, color = Color.Gray)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isGoogleConfigured) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = if (isGoogleConfigured) "CONFIGURED" else "SETUP REQUIRED",
                                color = if (isGoogleConfigured) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Gemini API Key Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("GEMINI_API_KEY", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text("Gemini 2.5 Flash Review Sentiment Engine", fontSize = 10.sp, color = Color.Gray)
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isGeminiConfigured) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Text(
                                text = if (isGeminiConfigured) "CONFIGURED" else "SETUP REQUIRED",
                                color = if (isGeminiConfigured) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // How to configure in AI Studio
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How to Configure Your API Keys:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("1. Open the Secrets panel in AI Studio.", fontSize = 12.sp)
                    Text("2. Add GOOGLE_MAPS_API_KEY with your Places API (New) enabled key.", fontSize = 12.sp)
                    Text("3. Add GEMINI_API_KEY with your Google AI Studio Gemini key.", fontSize = 12.sp)
                    Text("4. Recompile or restart the applet to inject the keys safely into BuildConfig.", fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onRefreshData,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFE5C07B))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Refresh Live Status", color = Color(0xFFE5C07B))
                    }
                }
            }
        }

        // Policy & Privacy Statement
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Security & API Compliance Architecture:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("• FieldMasks: Text Search and Place Details only query essential fields (`places.id`, `places.displayName`, `places.rating`, `places.reviews`).", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Zero Scraping: Only official Google Places API endpoints are utilized.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Ephemeral Data: Review content is processed in-memory for sentiment analysis and is never permanently stored on local or third-party servers.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Google Attribution: Always displays 'Map data © Google' and standard copyright notices.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
