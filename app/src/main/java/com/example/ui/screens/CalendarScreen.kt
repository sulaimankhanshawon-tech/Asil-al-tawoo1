package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.PostStatus
import com.example.data.model.SocialPost
import com.example.ui.components.PlatformChip
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.SaudiFoodViewModel
import kotlinx.coroutines.launch

@Composable
fun CalendarScreen(
    viewModel: SaudiFoodViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    var selectedFilter by remember { mutableStateOf<PostStatus?>(null) }
    var selectedViewMode by remember { mutableStateOf("DAY") } // "DAY", "WEEK", "MONTH"
    var selectedPostDetails by remember { mutableStateOf<SocialPost?>(null) }
    var postToEditTime by remember { mutableStateOf<SocialPost?>(null) }
    var editedTimeText by remember { mutableStateOf("09:00 AM") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredPosts = if (selectedFilter == null) posts else posts.filter { it.status == selectedFilter }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize().testTag("calendar_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = "Smart Content Calendar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "Asia/Riyadh (UTC+3)",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Orchestrates 5 daily food posts aligned with Saudi dining rhythms: 09:00 AM Morning Coffee, 12:00 PM Lunch, 03:00 PM Tea, 06:00 PM Dinner, 09:00 PM Late Night Treats.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // View Mode Selector (Day, Week, Month)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("DAY" to "Today's 5 Slots", "WEEK" to "7-Day View", "MONTH" to "30-Day Strategy").forEach { (key, label) ->
                        val isSelected = selectedViewMode == key
                        Button(
                            onClick = { selectedViewMode = key },
                            modifier = Modifier.weight(1f),
                            colors = if (isSelected) {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            } else {
                                ButtonDefaults.outlinedButtonColors()
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Status Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${posts.size})", fontSize = 10.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    listOf(PostStatus.DRAFT, PostStatus.AWAITING_APPROVAL, PostStatus.SCHEDULED, PostStatus.PUBLISHED).forEach { status ->
                        val count = posts.count { it.status == status }
                        FilterChip(
                            selected = selectedFilter == status,
                            onClick = { selectedFilter = status },
                            label = { Text("${status.displayName.take(7)} ($count)", fontSize = 9.5.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Timeline Items
            items(filteredPosts) { post ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPostDetails = post },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Media thumbnail
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(10.dp))
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
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.RestaurantMenu, contentDescription = null)
                                }
                            }
                            if (post.isVideo) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.Black.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        // Post info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.clickable {
                                        postToEditTime = post
                                        editedTimeText = post.scheduledTimeSlot
                                    }
                                ) {
                                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                    Text(
                                        text = "${post.scheduledTimeSlot} ✏️",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                StatusBadge(status = post.status)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = post.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = post.captionAr,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    post.targetPlatforms.take(2).forEach { PlatformChip(it) }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (post.status == PostStatus.DRAFT || post.status == PostStatus.AWAITING_APPROVAL) {
                                        Button(
                                            onClick = {
                                                viewModel.approvePost(post.id)
                                                scope.launch { snackbarHostState.showSnackbar("Approved & Scheduled for ${post.scheduledTimeSlot}!") }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D472B)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Approve", fontSize = 10.sp)
                                        }
                                    } else if (post.status == PostStatus.SCHEDULED) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.publishNow(post.id)
                                                scope.launch { snackbarHostState.showSnackbar("Dispatched to APIs!") }
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Publish", fontSize = 10.sp)
                                        }
                                    } else if (post.status == PostStatus.FAILED_RETRYING) {
                                        Button(
                                            onClick = {
                                                viewModel.retryPost(post.id)
                                                scope.launch { snackbarHostState.showSnackbar("Retrying post dispatch...") }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Retry", fontSize = 10.sp)
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

    // Modal: Edit Scheduled Time (PRIORITY 4)
    if (postToEditTime != null) {
        val target = postToEditTime!!
        AlertDialog(
            onDismissRequest = { postToEditTime = null },
            title = { Text("Edit Scheduled Time Slot", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select or enter a new time slot for \"${target.title}\" (Asia/Riyadh timezone):", fontSize = 12.sp)
                    val quickSlots = listOf("09:00 AM", "12:00 PM", "03:00 PM", "06:00 PM", "09:00 PM", "10:30 PM")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        quickSlots.take(3).forEach { slot ->
                            FilterChip(
                                selected = editedTimeText == slot,
                                onClick = { editedTimeText = slot },
                                label = { Text(slot, fontSize = 9.sp) }
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        quickSlots.drop(3).forEach { slot ->
                            FilterChip(
                                selected = editedTimeText == slot,
                                onClick = { editedTimeText = slot },
                                label = { Text(slot, fontSize = 9.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = editedTimeText,
                        onValueChange = { editedTimeText = it },
                        label = { Text("Custom Time (AST)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updatePostTime(target.id, editedTimeText)
                        postToEditTime = null
                        scope.launch { snackbarHostState.showSnackbar("Time slot updated to $editedTimeText AST") }
                    }
                ) {
                    Text("Save Time")
                }
            },
            dismissButton = {
                TextButton(onClick = { postToEditTime = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Post Details
    if (selectedPostDetails != null) {
        val post = selectedPostDetails!!
        AlertDialog(
            onDismissRequest = { selectedPostDetails = null },
            title = { Text(post.title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Slot: ${post.scheduledTimeSlot} AST", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        StatusBadge(post.status)
                    }
                    Text("Arabic Copy:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text(post.captionAr, fontSize = 12.sp)
                    Text("English Copy:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text(post.captionEn, fontSize = 12.sp)
                    if (post.imagePrompt.isNotBlank()) {
                        Text("AI Image Prompt:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text(post.imagePrompt, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Hashtags:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text(post.hashtags.joinToString(" "), color = Color(0xFF1976D2), fontSize = 11.sp)
                    if (post.status == PostStatus.PUBLISHED) {
                        Text("Live Engagement: ❤️ ${post.engagementLikes}  🔁 ${post.engagementShares}  💬 ${post.engagementComments}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (post.status == PostStatus.DRAFT || post.status == PostStatus.AWAITING_APPROVAL) {
                        Button(onClick = {
                            viewModel.approvePost(post.id)
                            selectedPostDetails = null
                        }) {
                            Text("Approve & Schedule")
                        }
                    } else if (post.status == PostStatus.SCHEDULED) {
                        Button(onClick = {
                            viewModel.publishNow(post.id)
                            selectedPostDetails = null
                        }) {
                            Text("Publish Now")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPostDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}
