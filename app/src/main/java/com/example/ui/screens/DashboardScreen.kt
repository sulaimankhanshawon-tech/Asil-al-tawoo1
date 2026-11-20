package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow


import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.components.MetricCard
import com.example.ui.components.PlatformChip
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.SaudiFoodViewModel

@Composable
fun DashboardScreen(
    viewModel: SaudiFoodViewModel,
    onNavigateToStudio: () -> Unit,
    onNavigateToResearch: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToPlatforms: () -> Unit,
    onNavigateToMaps: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val reports by viewModel.researchReports.collectAsState()
    val campaigns by viewModel.campaigns.collectAsState()
    val automationMode by viewModel.automationMode.collectAsState()
    val isEmergencyPaused by viewModel.isEmergencyPaused.collectAsState()
    val businessProfile by viewModel.businessProfile.collectAsState()
    val context = LocalContext.current

    var showPostDetailDialog by remember { mutableStateOf<SocialPost?>(null) }


    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Hero Branding & Riyadh Time Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0D472B),
                                    Color(0xFF145D39),
                                    Color(0xFF082B1B)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isEmergencyPaused) Color(0xFFFF5252) else Color(0xFF69F0AE))
                                )
                                Text(
                                    text = if (isEmergencyPaused) "EMERGENCY PAUSED" else "AI AUTOPILOT ACTIVE",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            // Saudi Flag & Time indicator
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🇸🇦", fontSize = 12.sp)
                                    Text(
                                        text = "Asia/Riyadh (UTC+3)",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "سفرة AI • Social Media Manager",
                            color = Color(0xFFFFD54F),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Autonomous Marketing Engine for the Saudi Food & Restaurant Market",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onNavigateToStudio,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A017)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("btn_create_post_hero")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New AI Post", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = onNavigateToPlatforms,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.6f), Color.White.copy(alpha = 0.6f)))),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("btn_manage_channels")
                            ) {
                                Icon(Icons.Default.Sensors, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connected Channels (${summary.connectedAccountsCount}/5)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 2. Emergency Pause Banner if triggered
        if (isEmergencyPaused) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828))
                            Column {
                                Text(
                                    text = "Automated Publishing Paused",
                                    color = Color(0xFFC62828),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Scheduled queue is temporarily frozen.",
                                    color = Color(0xFF5D1010),
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.toggleEmergencyPause() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Resume", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 3. Automation Mode Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Publishing Automation Mode",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AutomationMode.values().forEach { mode ->
                            val isSelected = automationMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setAutomationMode(mode) },
                                label = {
                                    Text(
                                        text = mode.title.replace(" Mode", ""),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chip_mode_${mode.name}")
                            )
                        }
                    }
                }
            }
        }

        // 3b. Google Maps & AI Review Intelligence Spotlight Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMaps() }
                    .testTag("card_dash_maps_intelligence"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B4D3E)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0D472B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = Color(0xFFE5C07B),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Google Maps & Review Intelligence",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE5C07B),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Places API (New) • 8 Saudi Cities • Gemini Sentiment",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                    Button(
                        onClick = onNavigateToMaps,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC5A059),
                            contentColor = Color(0xFF0D472B)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Open Map", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // 3c. Saved Google Maps & Business WhatsApp Quick Actions (Modules 7 & 8)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("card_dash_saved_links"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text(businessProfile.restaurantName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = businessProfile.primaryCity.englishName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Saved Links: 📍 Maps (${if (businessProfile.savedGoogleMapsUri.contains("maps")) "Configured" else "Not set"}) • 💬 WhatsApp (${businessProfile.whatsAppNumber})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val (isValid, _) = viewModel.testSavedGoogleMapsLink()
                                if (isValid) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(businessProfile.savedGoogleMapsUri))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("dash_btn_test_saved_maps"),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFFD4A017), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Maps Link", fontSize = 11.sp, maxLines = 1)
                        }

                        Button(
                            onClick = {
                                val (isValid, _) = viewModel.testBusinessWhatsAppLink()
                                if (isValid) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(businessProfile.directWhatsAppUri))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("dash_btn_test_whatsapp"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }

        // 4. Performance Metrics 2x2 (Real Application Data)
        item {

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Estimated Reach",
                        value = summary.reachDisplay,
                        subtitle = if (summary.connectedAccountsCount == 0) "Connect channels to measure" else "${summary.connectedAccountsCount} active channels",
                        icon = Icons.Default.TrendingUp,
                        iconTint = Color(0xFF2E7D32),
                        modifier = Modifier.weight(1f).testTag("metric_reach")
                    )
                    MetricCard(
                        title = "Engagement Rate",
                        value = summary.engagementRateDisplay,
                        subtitle = if (summary.todayPublished == 0) "Requires published posts" else "Calculated from live posts",
                        icon = Icons.Default.Favorite,
                        iconTint = Color(0xFFE91E63),
                        modifier = Modifier.weight(1f).testTag("metric_engagement")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Connected Channels",
                        value = "${summary.connectedAccountsCount} / ${summary.totalAccountsCount}",
                        subtitle = if (summary.connectedAccountsCount == 0) "No platforms authorized" else "Meta, TikTok, Snap, X",
                        icon = Icons.Default.Public,
                        iconTint = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f).testTag("metric_channels")
                    )
                    MetricCard(
                        title = "Today's Posts",
                        value = "${summary.todayTotalPosts} Total",
                        subtitle = "${summary.todayPublished} live • ${summary.todayScheduled} sched",
                        icon = Icons.Default.Schedule,
                        iconTint = Color(0xFFD4A017),
                        modifier = Modifier.weight(1f).testTag("metric_today_posts")
                    )
                }

                // Post Pipeline Breakdown Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Today's Post Pipeline Breakdown",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PipelineCountPill(label = "Drafts", count = summary.todayDrafts, color = Color(0xFF757575))
                            PipelineCountPill(label = "Awaiting", count = summary.todayAwaitingApproval, color = Color(0xFFE65100))
                            PipelineCountPill(label = "Scheduled", count = summary.todayScheduled, color = Color(0xFF1565C0))
                            PipelineCountPill(label = "Published", count = summary.todayPublished, color = Color(0xFF2E7D32))
                            if (summary.todayFailed > 0) {
                                PipelineCountPill(label = "Failed", count = summary.todayFailed, color = Color(0xFFC62828))
                            }
                        }
                    }
                }
            }
        }

        // 5. Growth Campaigns Progress (7-Day & 30-Day)
        item {
            SectionHeader(title = "Active Marketing Campaigns")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                campaigns.forEach { campaign ->
                    val progressFraction = campaign.publishedPosts.toFloat() / campaign.targetPosts.toFloat()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = campaign.typeName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = campaign.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "Day ${campaign.currentDay}/${campaign.durationDays}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { progressFraction.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${campaign.publishedPosts} of ${campaign.targetPosts} posts published",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Reach: ${campaign.currentReach / 1000}K / ${campaign.targetReach / 1000}K",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Today's 5-Post Daily Schedule Carousel
        item {
            SectionHeader(
                title = "Today's Schedule (5 Daily Posts)",
                actionText = "Calendar",
                onActionClick = onNavigateToCalendar
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(posts) { post ->
                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .clickable { showPostDetailDialog = post },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            // Thumbnail
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                            ) {
                                val resId = post.imageResId
                                if (resId != null) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = post.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                // Slot Time Badge
                                Surface(
                                    shape = RoundedCornerShape(bottomEnd = 10.dp),
                                    color = Color.Black.copy(alpha = 0.75f),
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        Text(text = post.scheduledTimeSlot, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Status badge
                                Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                                    StatusBadge(status = post.status)
                                }
                            }

                            // Info
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = post.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = post.captionAr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    post.targetPlatforms.take(3).forEach { platform ->
                                        PlatformChip(platform = platform)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (post.status == PostStatus.AWAITING_APPROVAL) {
                                        Button(
                                            onClick = { viewModel.approvePost(post.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D472B)),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("Approve", fontSize = 11.sp)
                                        }
                                    } else if (post.status == PostStatus.SCHEDULED) {
                                        OutlinedButton(
                                            onClick = { viewModel.publishNow(post.id) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Publish Now", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Saudi Food Market Intelligence Highlights
        item {
            SectionHeader(
                title = "Saudi Market Trends (AI Intel)",
                actionText = "Explore All",
                onActionClick = onNavigateToResearch
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                reports.take(3).forEach { report ->
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${report.searchGrowthPercent}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(text = report.city.arabicName, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(text = report.category.arabicName, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = report.titleAr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = report.marketingOpportunity,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { viewModel.generatePostFromTrend(report) },
                                modifier = Modifier.testTag("btn_quick_gen_${report.id}")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Generate", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Post Detail Modal
    if (showPostDetailDialog != null) {
        val selected = showPostDetailDialog!!
        AlertDialog(
            onDismissRequest = { showPostDetailDialog = null },
            title = {
                Text(text = selected.title, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Arabic Caption:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = selected.captionAr, fontSize = 12.sp)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(text = "English Caption:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(text = selected.captionEn, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Target Platforms:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        selected.targetPlatforms.forEach { PlatformChip(it) }
                    }
                    if (selected.imagePrompt.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "AI Image Prompt:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = selected.imagePrompt,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (selected.lastApiPayload != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "API Payload Details:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            text = selected.lastApiPayload ?: "",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selected.status == PostStatus.DRAFT || selected.status == PostStatus.AWAITING_APPROVAL) {
                        Button(
                            onClick = {
                                viewModel.approvePost(selected.id)
                                showPostDetailDialog = null
                            }
                        ) {
                            Text("Approve & Schedule")
                        }
                    } else if (selected.status == PostStatus.SCHEDULED) {
                        Button(
                            onClick = {
                                viewModel.publishNow(selected.id)
                                showPostDetailDialog = null
                            }
                        ) {
                            Text("Publish via API")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostDetailDialog = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PipelineCountPill(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

