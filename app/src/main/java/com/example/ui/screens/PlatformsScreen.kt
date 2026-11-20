package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.data.model.*
import com.example.ui.components.PlatformChip
import com.example.ui.viewmodel.SaudiFoodViewModel
import kotlinx.coroutines.launch

@Composable
fun PlatformsScreen(
    viewModel: SaudiFoodViewModel,
    modifier: Modifier = Modifier,
    initialTab: String = "CHANNELS"
) {
    val accounts by viewModel.accounts.collectAsState()
    val appLogs by viewModel.appLogs.collectAsState()
    val automationMode by viewModel.automationMode.collectAsState()
    val isEmergencyPaused by viewModel.isEmergencyPaused.collectAsState()
    val businessProfile by viewModel.businessProfile.collectAsState()
    val whatsAppConsents by viewModel.whatsAppConsents.collectAsState()
    val whatsAppCampaigns by viewModel.whatsAppCampaigns.collectAsState()
    val whatsAppTemplates = viewModel.whatsAppTemplates
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(initialTab) } // "CHANNELS", "WHATSAPP", "PROFILE", "LOGS", "SETTINGS", "SPECS"
    var logPlatformFilter by remember { mutableStateOf("ALL") } // "ALL", "FACEBOOK", "INSTAGRAM", "TIKTOK", "SNAPCHAT", "X"
    var showClearLogsDialog by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf<SocialAccount?>(null) }
    var showConnectModal by remember { mutableStateOf<SocialAccount?>(null) }
    var accountHandleInput by remember { mutableStateOf("@sufra_ksa") }

    var mapsLinkInput by remember(businessProfile) { mutableStateOf(businessProfile.savedGoogleMapsUri) }
    var whatsAppPhoneInput by remember(businessProfile) { mutableStateOf(businessProfile.whatsAppNumber) }
    var restaurantNameInput by remember(businessProfile) { mutableStateOf(businessProfile.restaurantName) }
    var autoAppendLinks by remember(businessProfile) { mutableStateOf(businessProfile.autoAppendLinksToCaptions) }

    var showAddConsentDialog by remember { mutableStateOf(false) }
    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerPhone by remember { mutableStateOf("+9665") }
    var newCustomerSource by remember { mutableStateOf(WhatsAppConsentSource.DINE_IN_QR) }
    var selectedTemplateId by remember { mutableStateOf(whatsAppTemplates.firstOrNull()?.id ?: "") }


    val filteredLogs = remember(appLogs, logPlatformFilter) {
        if (logPlatformFilter == "ALL") {
            appLogs
        } else {
            val targetPlatform = when (logPlatformFilter) {
                "FACEBOOK" -> SocialPlatform.FACEBOOK
                "INSTAGRAM" -> SocialPlatform.INSTAGRAM
                "TIKTOK" -> SocialPlatform.TIKTOK
                "SNAPCHAT" -> SocialPlatform.SNAPCHAT
                "X" -> SocialPlatform.X_TWITTER
                else -> null
            }
            appLogs.filter { it.platform == targetPlatform }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize().testTag("platforms_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Hub, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Social Media Platforms & API Hub",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Official API endpoints & OAuth 2.0 authorization for Meta (FB & IG), TikTok, Snapchat, and X. Zero scraping, 100% compliant with platform developer policies.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Tab Selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "CHANNELS" to "Channels (5)",
                        "WHATSAPP" to "WhatsApp Mktg",
                        "PROFILE" to "Saved Links",
                        "LOGS" to "Logs",
                        "SETTINGS" to "Safeguards",
                        "SPECS" to "Architecture"
                    ).forEach { (key, label) ->
                        val isSelected = selectedTab == key
                        Button(
                            onClick = { selectedTab = key },
                            colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.outlinedButtonColors(),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, maxLines = 1)
                        }
                    }
                }
            }


            // Content per Tab
            if (selectedTab == "CHANNELS") {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("owner_portal_notice"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFFC5A059).copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFFC5A059), modifier = Modifier.size(20.dp))
                                Text("Owner Business Channels (Single-Tenant)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Text(
                                text = "Dedicated integration for your own 5 business accounts: Facebook Page, Instagram Professional, TikTok Business, Snapchat Public Profile, and X. Uses official OAuth 2.0 PKCE authentication. Strictly zero passwords requested or stored, no fake connections, and zero automatic paid purchases.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Your Business Accounts (${accounts.count { it.isConnected }}/${accounts.size} Connected)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(accounts) { account ->
                    AccountCard(
                        account = account,
                        onToggleConnect = {
                            if (account.isConnected) {
                                viewModel.disconnectAccount(account.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("${account.platform.title} disconnected.")
                                }
                            } else {
                                accountHandleInput = "@sufra_${account.platform.title.lowercase().take(5)}"
                                showConnectModal = account
                            }
                        },
                        onInspectApi = { showAuthDialog = account }
                    )
                }
            } else if (selectedTab == "WHATSAPP") {
                // WHATSAPP MARKETING MODULE (Module 9)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("whatsapp_api_status_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366))
                                    Text("Meta WhatsApp Business Cloud API", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFEBEE)
                                ) {
                                    Text(
                                        text = "NOT CONFIGURED",
                                        color = Color(0xFFC62828),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Official Meta Cloud API v19.0 direct integration. Requires verified Phone Number ID, WhatsApp Business Account ID (WABA ID), and Permanent System User Access Token from Meta Developer Suite. Compliant with Meta Commerce Policy and Saudi CST Communications regulations. Zero automatic paid purchases or spam.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Customer Consent Register
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("whatsapp_consent_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Customer Consent Register", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        text = "${whatsAppConsents.count { it.isOptedIn }} / ${whatsAppConsents.size} Customers Opted-In",
                                        fontSize = 11.sp,
                                        color = Color(0xFF25D366),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Button(
                                    onClick = { showAddConsentDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("btn_add_customer_consent")
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Opt-In", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Text(
                                text = "Anti-Spam Safeguard: Meta and Saudi regulations strictly prohibit marketing messages without verifiable prior opt-in consent.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            whatsAppConsents.forEach { consent ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(consent.customerName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (consent.isOptedIn) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                                ) {
                                                    Text(
                                                        text = if (consent.isOptedIn) "OPTED-IN" else "OPTED-OUT",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (consent.isOptedIn) Color(0xFF2E7D32) else Color(0xFFC62828),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text("${consent.phoneNumber} • ${consent.source.labelEn}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.toggleCustomerConsent(consent.id) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(if (consent.isOptedIn) "Revoke" else "Re-Opt In", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Pre-Approved Meta Templates & Live Preview
                val activeTemplate = whatsAppTemplates.find { it.id == selectedTemplateId } ?: whatsAppTemplates.first()
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("whatsapp_template_preview_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Pre-Approved Message Templates (Meta Cloud)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                whatsAppTemplates.forEach { template ->
                                    val isSelected = selectedTemplateId == template.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedTemplateId = template.id },
                                        label = { Text(template.name, fontSize = 11.sp) }
                                    )
                                }
                            }

                            // Simulated WhatsApp Chat Bubble
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDCF8C6), // WhatsApp incoming bubble green
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Sufra Restaurant • Official", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF075E54))
                                        Text("11:00 AM", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    Text(
                                        text = activeTemplate.headlineAr,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = activeTemplate.bodyTemplateAr
                                            .replace("{{customer_name}}", "عبدالرحمن")
                                            .replace("{{maps_link}}", businessProfile.savedGoogleMapsUri),
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E272C),
                                        lineHeight = 17.sp
                                    )
                                    HorizontalDivider(color = Color(0xFF075E54).copy(alpha = 0.2f))
                                    Text(
                                        text = "English Version:\n" + activeTemplate.bodyTemplateEn
                                            .replace("{{customer_name}}", "Abdulrahman")
                                            .replace("{{maps_link}}", businessProfile.savedGoogleMapsUri),
                                        fontSize = 11.sp,
                                        color = Color(0xFF4A5568),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // WhatsApp Campaign Approval & Broadcast Dispatch Gate
                val currentCampaign = whatsAppCampaigns.firstOrNull()
                if (currentCampaign != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("whatsapp_campaign_dispatch_card"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Broadcast Campaign Gate", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(currentCampaign.title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(currentCampaign.status.colorHex).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = currentCampaign.status.label,
                                            color = Color(currentCampaign.status.colorHex),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Target Recipients: ${whatsAppConsents.count { it.isOptedIn }} opted-in customers • Slot: ${currentCampaign.scheduledTimeSlot}",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (currentCampaign.lastErrorReason != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFFFEBEE),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "⚠️ " + currentCampaign.lastErrorReason,
                                            color = Color(0xFFC62828),
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.approveWhatsAppCampaign(currentCampaign.id)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("✅ Campaign approved by owner.")
                                            }
                                        },
                                        modifier = Modifier.weight(1f).testTag("btn_approve_whatsapp_campaign"),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Approve Broadcast", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val (success, message) = viewModel.dispatchWhatsAppCampaign(currentCampaign.id, isMetaApiConfigured = false)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(if (success) "✅ $message" else "⚠️ $message")
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                        modifier = Modifier.weight(1f).testTag("btn_send_whatsapp_broadcast"),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Dispatch Broadcast", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == "PROFILE") {
                // BUSINESS PROFILE & SAVED LINKS (Modules 7 & 8)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("business_identity_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Business Profile & Direct Links", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text(
                                text = "Your restaurant's verified Google Maps location link and official WhatsApp click-to-chat link. These are verified and appended to marketing posts and broadcasts.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = restaurantNameInput,
                                onValueChange = {
                                    restaurantNameInput = it
                                    viewModel.updateBusinessProfile(businessProfile.copy(restaurantName = it))
                                },
                                label = { Text("Restaurant Name (الاسم التجاري)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }

                // Module 7: Saved Business Google Maps Link
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("saved_google_maps_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFFD4A017))
                                Text("Module 7: Saved Business Google Maps Link", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedTextField(
                                value = mapsLinkInput,
                                onValueChange = {
                                    mapsLinkInput = it
                                    viewModel.updateBusinessProfile(businessProfile.copy(savedGoogleMapsUri = it))
                                },
                                label = { Text("Saved Google Maps Location URL") },
                                placeholder = { Text("https://maps.app.goo.gl/...") },
                                modifier = Modifier.fillMaxWidth().testTag("input_saved_maps_link"),
                                singleLine = true
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val (isValid, message) = viewModel.testSavedGoogleMapsLink()
                                        if (isValid) {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(businessProfile.savedGoogleMapsUri))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                            scope.launch {
                                                snackbarHostState.showSnackbar("📍 Saved Maps link verified & launched: ${businessProfile.savedGoogleMapsUri}")
                                            }
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("⚠️ Maps Link Error: $message")
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("btn_test_saved_maps_link"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Launch, contentDescription = null, tint = Color(0xFFE5C07B))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Test Saved Google Maps Link", color = Color(0xFFE5C07B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Module 8: Business WhatsApp Link
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("business_whatsapp_link_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF25D366))
                                Text("Module 8: Business WhatsApp Direct Link", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedTextField(
                                value = whatsAppPhoneInput,
                                onValueChange = {
                                    whatsAppPhoneInput = it
                                    viewModel.updateBusinessProfile(businessProfile.copy(whatsAppNumber = it))
                                },
                                label = { Text("Saudi WhatsApp Number (+966 5X XXX XXXX)") },
                                placeholder = { Text("+966501234567") },
                                modifier = Modifier.fillMaxWidth().testTag("input_business_whatsapp"),
                                singleLine = true
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Direct wa.me link: ${businessProfile.directWhatsAppUri}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Button(
                                onClick = {
                                    val (isValid, message) = viewModel.testBusinessWhatsAppLink()
                                    if (isValid) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(businessProfile.directWhatsAppUri))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                        scope.launch {
                                            snackbarHostState.showSnackbar("💬 WhatsApp direct link verified & launched: ${businessProfile.directWhatsAppUri}")
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("⚠️ WhatsApp Error: $message")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("btn_test_whatsapp_link"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Business WhatsApp Link", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Auto-Append Caption Toggle
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Auto-Append Links to Posts", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Attaches Google Maps and WhatsApp ordering link to post captions.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = autoAppendLinks,
                                    onCheckedChange = {
                                        autoAppendLinks = it
                                        viewModel.updateBusinessProfile(businessProfile.copy(autoAppendLinksToCaptions = it))
                                    }
                                )
                            }
                        }
                    }
                }
            } else if (selectedTab == "LOGS") {
                // Header & Action Bar

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("logs_control_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Dns,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Real-Time API Publishing Telemetry",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${filteredLogs.size} events",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Text(
                                text = "Live event stream & API telemetry for all automated and manual actions across Saudi food market channels. Real-time timestamps in Asia/Riyadh.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Action Buttons: Refresh & Clear Logs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.refreshLogs()
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Activity logs refreshed (${appLogs.size} total events).")
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_refresh_logs")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Refresh", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { showClearLogsDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_clear_logs")
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear Logs", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                // Filter Chips Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Filter By Platform:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "ALL" to "All events",
                                "FACEBOOK" to "Facebook",
                                "INSTAGRAM" to "Instagram",
                                "TIKTOK" to "TikTok",
                                "SNAPCHAT" to "Snapchat",
                                "X" to "X"
                            ).forEach { (key, label) ->
                                val isSelected = logPlatformFilter == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { logPlatformFilter = key },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.testTag("filter_${key.lowercase()}")
                                )
                            }
                        }
                    }
                }

                // Table Header Bar
                if (filteredLogs.isNotEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("logs_table_header")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "DATE / TIME (AST)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1.3f)
                                )
                                Text(
                                    text = "PLATFORM",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(0.9f)
                                )
                                Text(
                                    text = "EVENT TYPE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(1.3f)
                                )
                                Text(
                                    text = "STATUS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.weight(0.9f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }

                // Empty State when no events exist
                if (filteredLogs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .testTag("logs_empty_state"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 36.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Text(
                                    text = "No activity recorded yet.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (logPlatformFilter == "ALL") {
                                        "Application events, AI content generation, post scheduling, and API publishing attempts will be recorded here automatically."
                                    } else {
                                        "No activity logged for this specific platform filter. Select 'All events' above to view the complete system audit trail."
                                    },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Log Items
                    items(filteredLogs) { log ->
                        AppLogItemCard(log = log)
                    }
                }
            } else if (selectedTab == "SETTINGS") {
                // SETTINGS / SAFEGUARDS
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Emergency Safety & Autopilot Safeguards",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            // Killswitch toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Pause All Scheduled Automated Publishing",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isEmergencyPaused) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Instantly freezes all 5 daily queues across all connected channels.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = isEmergencyPaused,
                                    onCheckedChange = { viewModel.toggleEmergencyPause() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFC62828), checkedTrackColor = Color(0xFFFFCDD2))
                                )
                            }

                            HorizontalDivider()

                            Text(
                                text = "Automation Operating Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            AutomationMode.values().forEach { mode ->
                                val isSelected = automationMode == mode
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.setAutomationMode(mode) }
                                        )
                                        Column {
                                            Text(text = mode.title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(text = mode.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }

                            HorizontalDivider()

                            Text(
                                text = "Saudi Publishing Timing & Retries",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "• Timezone: Asia/Riyadh (AST, UTC+3)\n• Default Daily Cadence: 5 posts per day (9:00 AM, 12:00 PM, 3:00 PM, 6:00 PM, 9:00 PM)\n• Auto-Retry: 3 attempts with exponential backoff on transient HTTP 429 / 503 errors\n• Rate Limits: Compliant with Meta 25 API calls/sec, X 100 tweets/24h, TikTok 50 video uploads/day",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                // ARCHITECTURE & DETAILED ROADMAP SPECS
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AccountTree, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("System Architecture & Specifications", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Comprehensive production design across Client, FastAPI Backend, PostgreSQL Schemas, Redis Scheduling, and AI Inference.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                // 1. Core Architecture Diagram Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("1. Multi-Tier High Level Architecture", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Mobile App (Compose) + Web (Next.js 15)\n   │  HTTPS / WSS (OAuth 2.0 Bearer JWT)\n   ▼\nAPI Gateway (Reverse Proxy / NGINX / Cloudflare)\n   ▼\nFastAPI Async Application Core (Python 3.11+)\n   ├── Auth / Multi-Tenant RBAC\n   ├── Saudi Market Research Engine\n   ├── Bilingual AI Studio (Arabic/English)\n   ├── 7 & 30-Day Campaign Manager\n   └── Publisher Dispatcher & Retry Worker\n   ▼\nData & Tasks Tier:\n   ├── PostgreSQL 16 (Tenants, Posts, Campaigns, Logs)\n   ├── Redis 7 + Celery (Asia/Riyadh TZ Scheduler)\n   └── S3 Bucket (High-res Food Photos & 9:16 Video)",
                                    modifier = Modifier.padding(10.dp),
                                    fontSize = 10.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 2. Database Schema Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("2. PostgreSQL Multi-Tenant Database Schema", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            val tables = listOf(
                                "tenants" to "id (UUID), business_name, brand_slug, primary_city, subscription_plan, created_at",
                                "users" to "id (UUID), tenant_id, email, password_hash, full_name, role (ADMIN/MARKETER/CHEF)",
                                "social_accounts" to "id, tenant_id, platform, account_handle, encrypted_access_token, token_expires_at, is_connected",
                                "market_research_reports" to "id, city, category, title_ar, title_en, trending_item, search_growth_percent, hashtags",
                                "marketing_campaigns" to "id, tenant_id, campaign_type (7D/30D), target_posts, published_posts, target_reach, weekly_themes (JSONB)",
                                "scheduled_posts" to "id, tenant_id, campaign_id, title, caption_ar, caption_en, target_platforms, slot_time, status",
                                "publishing_audit_logs" to "id, post_id, platform, http_status_code, platform_post_id, api_payload (JSONB), execution_status"
                            )
                            tables.forEach { (name, fields) ->
                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("• $name", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(fields, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // 3. 12-Week Roadmap Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("3. Detailed 12-Week Development Roadmap", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            val phases = listOf(
                                "Weeks 1–2: Foundation" to "PostgreSQL schemas, FastAPI template, Android client Compose UI, Meta Graph API sandbox.",
                                "Weeks 3–4: Saudi Research Engine" to "Google Trends KSA crawler, regional culinary knowledge base (Riyadh, Jeddah, etc.), dialect prompts.",
                                "Weeks 5–6: Visual AI Pipeline" to "Imagen 3 commercial photography integration, 9:16 vertical video reel rendering, draft editor.",
                                "Weeks 7–8: Autonomous Scheduler" to "Celery Asia/Riyadh 5-slot cron, approval workflow modes, emergency killswitch, retry backoff.",
                                "Weeks 9–10: Growth Campaigns" to "35-post & 150-post state machines, organic reach benchmarks, exportable client reports.",
                                "Weeks 11–12: Compliance & Pilot Launch" to "Official Meta/TikTok app review approval, penetration test, pilot across 5 Saudi restaurants."
                            )
                            phases.forEach { (phase, details) ->
                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text(phase, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text(details, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Inspect API dialog
    if (showAuthDialog != null) {
        val acc = showAuthDialog!!
        AlertDialog(
            onDismissRequest = { showAuthDialog = null },
            title = { Text(acc.platform.title + " API Configuration", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Official API: ${acc.platform.apiName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Supported Formats: ${acc.platform.supportedFormats}", fontSize = 11.sp)
                    Text("Character Limit: ${acc.platform.characterLimit} chars", fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Authorized Scopes:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    acc.platform.requiredScopes.forEach { scopeName ->
                        Text("• $scopeName", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Token Health: ${acc.statusText} (${acc.tokenExpiryDays} days remaining)", fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showAuthDialog = null }) {
                    Text("Done")
                }
            }
        )
    }

    // Connect Official OAuth Modal (PRIORITY 5 & OWNER CHANNELS)
    if (showConnectModal != null) {
        val target = showConnectModal!!
        AlertDialog(
            onDismissRequest = { showConnectModal = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlatformChip(target.platform)
                    Text("Connect ${target.platform.title} Account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Official API: ${target.platform.apiName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("1. Developer Credentials Required:", fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                            Text(target.platform.developerCredentials, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("2. Required OAuth 2.0 Permissions:", fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                            Text(target.platform.requiredScopes.joinToString(", "), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("3. Platform Approvals Required:", fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
                            Text(target.platform.requiredApprovals, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                            Text(
                                "Security Guarantee: Passwords are NEVER requested or stored. Zero paid services or automated purchases enabled.",
                                fontSize = 9.5.sp,
                                color = Color(0xFF1B5E20),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = accountHandleInput,
                        onValueChange = { accountHandleInput = it },
                        label = { Text("Your ${target.platform.title} Username / Page ID") },
                        placeholder = { Text("@your_restaurant_ksa") },
                        modifier = Modifier.fillMaxWidth().testTag("input_account_handle"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.connectAccount(target.id, accountHandleInput)
                        showConnectModal = null
                        scope.launch {
                            snackbarHostState.showSnackbar("✅ ${target.platform.title} authorized for $accountHandleInput!")
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_oauth_connect")
                ) {
                    Text("Authorize OAuth 2.0")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConnectModal = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Logs Confirmation Dialog
    if (showClearLogsDialog) {
        AlertDialog(
            onDismissRequest = { showClearLogsDialog = false },
            icon = {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Clear Activity Logs?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all recorded application events and API publishing telemetry? This action cannot be undone.",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearLogs()
                        showClearLogsDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("All activity logs cleared.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_clear_logs_button")
                ) {
                    Text("Clear Logs")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearLogsDialog = false },
                    modifier = Modifier.testTag("cancel_clear_logs_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Customer Consent Dialog
    if (showAddConsentDialog) {
        AlertDialog(
            onDismissRequest = { showAddConsentDialog = false },
            icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF25D366)) },
            title = { Text("Register Customer WhatsApp Opt-In") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Anti-Spam Safeguard: Record customer's explicit prior consent before sending promotional WhatsApp broadcasts.",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newCustomerName,
                        onValueChange = { newCustomerName = it },
                        label = { Text("Customer Name (e.g. نورة العتيبي)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_consent_name"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newCustomerPhone,
                        onValueChange = { newCustomerPhone = it },
                        label = { Text("WhatsApp Number (+966 5X XXX XXXX)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_consent_phone"),
                        singleLine = true
                    )
                    Text("Consent Source (مصدر الموافقة):", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        WhatsAppConsentSource.values().forEach { src ->
                            FilterChip(
                                selected = newCustomerSource == src,
                                onClick = { newCustomerSource = src },
                                label = { Text(src.labelEn, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCustomerName.isNotBlank() && newCustomerPhone.length >= 9) {
                            viewModel.registerCustomerConsent(newCustomerName, newCustomerPhone, newCustomerSource)
                            showAddConsentDialog = false
                            newCustomerName = ""
                            newCustomerPhone = "+9665"
                            scope.launch {
                                snackbarHostState.showSnackbar("✅ Verified customer opt-in recorded!")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier.testTag("btn_confirm_add_consent")
                ) {
                    Text("Save Opt-In", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddConsentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun AccountCard(
    account: SocialAccount,
    onToggleConnect: () -> Unit,
    onInspectApi: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("account_card_${account.platform.name.lowercase()}"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (account.isConnected) Color(0xFF2E7D32) else Color(0xFF9E9E9E))
                    )
                    Column {
                        Text(
                            text = account.accountName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (account.isConnected) "${account.handle} • Connected" else "Not connected (OAuth required)",
                            fontSize = 11.sp,
                            fontWeight = if (account.isConnected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (account.isConnected) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                PlatformChip(platform = account.platform)
            }

            // Status message
            Text(
                text = "Status: ${account.statusText}",
                fontSize = 11.sp,
                color = if (account.isConnected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            // Credentials and Approvals Breakdown
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Integration Requirements & Developer Approvals", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Text("• Developer Credentials: ${account.platform.developerCredentials}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("• Required Scopes: ${account.platform.requiredScopes.joinToString(", ")}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    Text("• Platform Review / Approval: ${account.platform.requiredApprovals}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("🔒 Passwords: Never requested or stored • Zero paid services or automated purchases enabled", fontSize = 9.5.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onInspectApi,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("API Scopes & Rate Limits", fontSize = 11.sp)
                }

                Button(
                    onClick = onToggleConnect,
                    colors = if (account.isConnected) {
                        ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    } else {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_connect_${account.platform.name.lowercase()}")
                ) {
                    Text(
                        text = if (account.isConnected) "Disconnect" else "Connect ${account.platform.title}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AppLogItemCard(
    log: AppLogEntry,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("log_item_${log.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Time (Asia/Riyadh AST) | Platform Pill | Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = log.timestampRiyadh,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (log.platform != null) {
                        PlatformChip(platform = log.platform)
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "System / Engine",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Status Indicator Badge
                    LogStatusIndicatorBadge(status = log.status)
                }
            }

            // Row 2: Event Type Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = log.eventType.displayTitle,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Row 3: Summary text
            Text(
                text = log.summary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Row 4: Error Details (when applicable)
            if (!log.errorDetails.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE),
                    border = BorderStroke(1.dp, Color(0xFFEF9A9A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_error_box_${log.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFFC62828)
                        )
                        Column {
                            Text(
                                text = "Error Details:",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Text(
                                text = log.errorDetails,
                                fontSize = 11.sp,
                                color = Color(0xFFB71C1C)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogStatusIndicatorBadge(status: LogStatus) {
    val (bgColor, textColor, icon, label) = when (status) {
        LogStatus.SUCCESS -> Quadruple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            Icons.Default.CheckCircle,
            "Success"
        )
        LogStatus.PENDING -> Quadruple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            Icons.Default.Schedule,
            "Pending"
        )
        LogStatus.FAILED -> Quadruple(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            Icons.Default.Cancel,
            "Failed"
        )
        LogStatus.WARNING -> Quadruple(
            Color(0xFFFFFDE7),
            Color(0xFFF57F17),
            Icons.Default.Warning,
            "Warning"
        )
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = textColor
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

