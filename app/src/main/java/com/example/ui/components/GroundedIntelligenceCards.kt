package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.SaudiCity
import com.example.data.model.SaudiMapCity
import com.example.ui.viewmodel.GeminiStudioViewModel
import com.example.ui.viewmodel.GroundedIntelUiState
import com.example.ui.viewmodel.GroundingSource
import com.example.ui.viewmodel.GroundingSourceType

@Composable
fun GroundingSourcesSection(
    sources: List<GroundingSource>,
    searchWidgetHtml: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (sources.isEmpty() && searchWidgetHtml.isNullOrBlank()) return

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val mapsSources = sources.filter { it.type == GroundingSourceType.GOOGLE_MAPS }
        val webSources = sources.filter { it.type == GroundingSourceType.WEB }

        if (mapsSources.isNotEmpty()) {
            // Compliant Google Maps text attribution (exact casing, single-line, 12sp-16sp, Sans-Serif)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFFD4A017), modifier = Modifier.size(14.dp))
                Text(
                    text = "Google Maps",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            mapsSources.forEach { src ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(src.uri)))
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = src.title,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Google Maps",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = Color(0xFF5E5E5E)
                            )
                        }
                        Icon(Icons.Default.OpenInNew, contentDescription = "Open in Google Maps", modifier = Modifier.size(15.dp))
                    }
                }
            }
        }

        if (webSources.isNotEmpty()) {
            Text(
                text = "Google Search Sources",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            webSources.forEach { src ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(src.uri)))
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(src.title, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            src.subtitle?.let {
                                Text(it, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                            }
                        }
                        Icon(Icons.Default.OpenInNew, contentDescription = "Open web source", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        if (!searchWidgetHtml.isNullOrBlank()) {
            SearchEntryPointView(html = searchWidgetHtml)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SearchEntryPointView(html: String) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        if (!url.isNullOrBlank()) {
                            runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                        }
                        return true
                    }
                }
                settings.javaScriptEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp, max = 96.dp)
            .clip(RoundedCornerShape(8.dp))
    )
}

@Composable
fun GoogleSearchGroundingCard(
    geminiViewModel: GeminiStudioViewModel,
    selectedCity: SaudiCity?,
    onUseInsightInStudio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by geminiViewModel.searchGroundingState.collectAsState()
    var query by remember { mutableStateOf("Trending new restaurants and cafes 2026") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_search_grounding_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B22)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.TravelExplore, contentDescription = null, tint = Color(0xFFD4A017))
                    Column {
                        Text(
                            text = "Live Web Intelligence (Google Search Grounding)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4A017)
                        )
                        Text(
                            text = "gemini-3.8-flash • Real-time verified web citations",
                            fontSize = 10.sp,
                            color = Color(0xFFB7D3C6)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Ask live Saudi food market trends", color = Color(0xFFD4A017)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFD4A017),
                    unfocusedBorderColor = Color(0xFF2E6B56)
                ),
                modifier = Modifier.fillMaxWidth().testTag("input_search_grounding")
            )

            Button(
                onClick = { geminiViewModel.runSearchGroundedIntel(query, selectedCity) },
                enabled = state !is GroundedIntelUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A017)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("btn_run_search_grounding")
            ) {
                if (state is GroundedIntelUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Searching Live Web...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Search Grounded Saudi Trends", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            when (val s = state) {
                is GroundedIntelUiState.Success -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Grounded Insight", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(s.generatedAt, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(s.summaryText, fontSize = 12.sp, lineHeight = 17.sp)
                            GroundingSourcesSection(sources = s.sources, searchWidgetHtml = s.searchWidgetHtml)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { onUseInsightInStudio(s.summaryText) }) {
                                    Text("Open Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                is GroundedIntelUiState.Error -> {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF4A1010), modifier = Modifier.fillMaxWidth()) {
                        Text(s.message, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(10.dp))
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun GoogleMapsGroundingCard(
    geminiViewModel: GeminiStudioViewModel,
    selectedCity: SaudiMapCity,
    modifier: Modifier = Modifier
) {
    val state by geminiViewModel.mapsGroundingState.collectAsState()
    var query by remember { mutableStateOf("Best specialty coffee and Saudi dining spots") }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_maps_grounding_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2B22))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFFD4A017), modifier = Modifier.size(18.dp))
                    Text(
                        text = "Gemini Google Maps Grounding (${selectedCity.englishName})",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD4A017)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Ask Google Maps via Gemini...", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD4A017),
                        unfocusedBorderColor = Color(0xFF2E6B56)
                    ),
                    modifier = Modifier.weight(1f).testTag("input_maps_grounding")
                )
                Button(
                    onClick = { geminiViewModel.runMapsGroundedQuery(query, selectedCity) },
                    enabled = state !is GroundedIntelUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A017)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("btn_run_maps_grounding")
                ) {
                    if (state is GroundedIntelUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.Black)
                    } else {
                        Text("Ask Maps", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            when (val s = state) {
                is GroundedIntelUiState.Success -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(s.summaryText, fontSize = 11.5.sp, lineHeight = 16.sp)
                            GroundingSourcesSection(sources = s.sources)
                        }
                    }
                }
                is GroundedIntelUiState.Error -> {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF4A1010), modifier = Modifier.fillMaxWidth()) {
                        Text(s.message, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                    }
                }
                else -> {}
            }
        }
    }
}
