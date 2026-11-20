package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.viewmodel.AiImageAspectOption
import com.example.ui.viewmodel.AudioTranscribeState
import com.example.ui.viewmodel.GeminiStudioViewModel
import com.example.ui.viewmodel.ImageGenUiState
import kotlinx.coroutines.launch

@Composable
fun AiImageStudioCard(
    geminiViewModel: GeminiStudioViewModel,
    initialPrompt: String,
    onAttachGeneratedUri: (Uri) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageGenState by geminiViewModel.imageGenState.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    var useProModel by remember { mutableStateOf(false) }
    var selectedAspect by remember { mutableStateOf(AiImageAspectOption.SQUARE) }
    var promptText by remember(initialPrompt) { mutableStateOf(initialPrompt) }
    var editInstruction by remember {
        mutableStateOf("Enhance lighting with warm Riyadh evening ambiance, golden saffron garnish, and subtle steam rising")
    }
    var selectedSourceBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val editPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bmp = decodeScaledBitmapFromUri(context, uri, 1280)
            if (bmp != null) {
                selectedSourceBitmap = bmp
                isEditMode = true
                onShowMessage("Photo loaded for Nano Banana 2.1 editing.")
            } else {
                onShowMessage("Unable to decode selected photo.")
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_image_studio_card"),
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
                    Icon(Icons.Default.AutoFixHigh, contentDescription = "AI Image Studio", tint = Color(0xFFD4A017))
                    Column {
                        Text(
                            text = "AI Image Create & Edit Studio",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD4A017)
                        )
                        Text(
                            text = if (useProModel) "gemini-3-pro-image (Nano Banana Pro)" else "gemini-nano-banana-2.1",
                            fontSize = 10.sp,
                            color = Color(0xFFB7D3C6)
                        )
                    }
                }
                FilterChip(
                    selected = useProModel,
                    onClick = { useProModel = !useProModel },
                    label = { Text(if (useProModel) "Pro Model" else "2.1 Fast", fontSize = 10.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD4A017),
                        selectedLabelColor = Color.Black,
                        labelColor = Color.White
                    )
                )
            }

            // Create vs Edit Mode Toggle
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isEditMode,
                    onClick = { isEditMode = false },
                    label = { Text("Create from Prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(15.dp)) },
                    modifier = Modifier.weight(1f).testTag("chip_image_create_mode"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD4A017),
                        selectedLabelColor = Color.Black,
                        labelColor = Color.White
                    )
                )
                FilterChip(
                    selected = isEditMode,
                    onClick = { isEditMode = true },
                    label = { Text("Edit Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp)) },
                    modifier = Modifier.weight(1f).testTag("chip_image_edit_mode"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD4A017),
                        selectedLabelColor = Color.Black,
                        labelColor = Color.White
                    )
                )
            }

            // Aspect Ratio Selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(AiImageAspectOption.values()) { option ->
                    FilterChip(
                        selected = selectedAspect == option,
                        onClick = { selectedAspect = option },
                        label = { Text(option.label, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1B4D3E),
                            selectedLabelColor = Color(0xFFD4A017),
                            labelColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }

            if (!isEditMode) {
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    label = { Text("Food Photography Prompt", color = Color(0xFFD4A017)) },
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD4A017),
                        unfocusedBorderColor = Color(0xFF2E6B56)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_nano_banana_prompt")
                )

                Button(
                    onClick = {
                        geminiViewModel.generateFoodImage(
                            prompt = promptText,
                            aspectOption = selectedAspect,
                            useProModel = useProModel
                        )
                    },
                    enabled = imageGenState !is ImageGenUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A017)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_generate_ai_image")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate Food Image", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            } else {
                // Edit Photo flow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            editPhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.weight(1f).testTag("btn_pick_image_to_edit")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick Photo", fontSize = 11.sp)
                    }

                    val currentSuccess = imageGenState as? ImageGenUiState.Success
                    if (currentSuccess != null) {
                        OutlinedButton(
                            onClick = { selectedSourceBitmap = currentSuccess.bitmap },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD4A017)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use Generated", fontSize = 11.sp)
                        }
                    }
                }

                selectedSourceBitmap?.let { srcBmp ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF2E6B56), RoundedCornerShape(10.dp))
                    ) {
                        Image(
                            bitmap = srcBmp.asImageBitmap(),
                            contentDescription = "Source image to edit",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(bottomEnd = 8.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                "Source Image",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = editInstruction,
                    onValueChange = { editInstruction = it },
                    label = { Text("Edit Instruction (e.g., add steam, change plating)", color = Color(0xFFD4A017)) },
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFD4A017),
                        unfocusedBorderColor = Color(0xFF2E6B56)
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("input_nano_banana_edit")
                )

                Button(
                    onClick = {
                        val bmp = selectedSourceBitmap
                        if (bmp == null) {
                            onShowMessage("Select a source photo first using Pick Photo.")
                        } else {
                            geminiViewModel.editFoodImage(
                                inputBitmap = bmp,
                                editInstruction = editInstruction,
                                aspectOption = selectedAspect,
                                useProModel = useProModel
                            )
                        }
                    },
                    enabled = imageGenState !is ImageGenUiState.Loading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A017)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_run_ai_image_edit")
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply AI Image Edit", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // State Output Rendering (No fake fallbacks)
            when (val state = imageGenState) {
                is ImageGenUiState.Loading -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1B4D3E),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = Color(0xFFD4A017)
                            )
                            Text(state.statusLabel, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
                is ImageGenUiState.Success -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Image(
                            bitmap = state.bitmap.asImageBitmap(),
                            contentDescription = "AI Generated Culinary Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, Color(0xFFD4A017), RoundedCornerShape(12.dp))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val uri = geminiViewModel.saveGeneratedBitmapToFile(context, state.bitmap)
                                        if (uri != null) {
                                            onAttachGeneratedUri(uri)
                                            onShowMessage("Attached AI image to post preview!")
                                        } else {
                                            onShowMessage("Failed to save image file.")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                modifier = Modifier.weight(1f).testTag("btn_attach_ai_image")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Attach to Post", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    selectedSourceBitmap = state.bitmap
                                    isEditMode = true
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Refine Edit", fontSize = 11.sp)
                            }
                        }
                    }
                }
                is ImageGenUiState.Error -> {
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
                            Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = Color(0xFFFFCDD2))
                            Text(
                                text = state.message,
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { geminiViewModel.resetImageGenState() }) {
                                Text("Dismiss", color = Color(0xFFD4A017), fontSize = 10.sp)
                            }
                        }
                    }
                }
                is ImageGenUiState.Idle -> {}
            }
        }
    }
}

@Composable
fun AudioTranscriptionBar(
    geminiViewModel: GeminiStudioViewModel,
    onApplyArabicCaption: (String) -> Unit,
    onApplyEnglishCaption: (String) -> Unit,
    onApplyDishTopic: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioState by geminiViewModel.audioState.collectAsState()

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            geminiViewModel.startMicrophoneRecording(context)
        } else {
            onShowMessage("Microphone permission is required for voice transcription.")
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("audio_transcription_bar"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (audioState is AudioTranscribeState.Recording) Color(0xFFC62828)
                                else Color(0xFF0D472B)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (audioState is AudioTranscribeState.Recording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Voice Note Transcription",
                            tint = Color(0xFFD4A017),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Voice-to-Caption Mic (Gemini 3.8)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Speak in Saudi Arabic or English to transcribe & draft captions",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                when (val s = audioState) {
                    is AudioTranscribeState.Recording -> {
                        Button(
                            onClick = { geminiViewModel.stopAndTranscribeAudio() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_stop_transcribe")
                        ) {
                            Text("Stop (${s.elapsedSeconds}s)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    is AudioTranscribeState.Transcribing -> {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    }
                    else -> {
                        Button(
                            onClick = {
                                val hasPerm = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    geminiViewModel.startMicrophoneRecording(context)
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D472B)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_start_transcribe")
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFFD4A017), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Record", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            when (val s = audioState) {
                is AudioTranscribeState.Success -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Transcript:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(s.transcript, fontSize = 11.5.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onApplyDishTopic(s.transcript)
                                        onShowMessage("Filled Dish Topic from voice transcript")
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Dish Topic", fontSize = 10.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        onApplyArabicCaption(s.suggestedCaptionAr.ifBlank { s.transcript })
                                        onShowMessage("Applied Arabic caption")
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("AR Caption", fontSize = 10.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        onApplyEnglishCaption(s.suggestedCaptionEn.ifBlank { s.transcript })
                                        onShowMessage("Applied English caption")
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("EN Caption", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
                is AudioTranscribeState.Error -> {
                    Text(s.message, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
                else -> {}
            }
        }
    }
}

private fun decodeScaledBitmapFromUri(context: Context, uri: Uri, maxDim: Int): Bitmap? {
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val raw = BitmapFactory.decodeStream(stream) ?: return null
            val scale = minOf(maxDim.toFloat() / raw.width, maxDim.toFloat() / raw.height, 1f)
            if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    raw,
                    (raw.width * scale).toInt().coerceAtLeast(1),
                    (raw.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else raw
        }
    }.getOrNull()
}
