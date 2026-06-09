package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.ui.theme.*
import com.example.viewmodel.ClosetViewModel
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainAppContainer(
    viewModel: ClosetViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("Today") } // Today, Closet, Stats, Plans
    var showAddGarmentDialog by remember { mutableStateOf(false) }

    val dateStr = remember {
        val format = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        format.format(Date())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BrandBackground,
        topBar = {
            HeaderRow(dateText = dateStr)
        },
        bottomBar = {
            CustomBottomBar(
                activeTab = activeTab,
                onTabSelect = { activeTab = it },
                onAddClick = { showAddGarmentDialog = true }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "Today" -> TodayScreen(
                    viewModel = viewModel,
                    onNavigateToCloset = { activeTab = "Closet" },
                    onNavigateToStyle = { activeTab = "Style" },
                    onNavigateToStats = { activeTab = "Stats" }
                )
                "Closet" -> ClosetScreen(
                    viewModel = viewModel
                )
                "Stats" -> StatsScreen(
                    viewModel = viewModel
                )
                "Plans" -> PlansScreen(
                    viewModel = viewModel
                )
                "Style" -> QuizScreen(
                    viewModel = viewModel,
                    onNavigateToToday = { activeTab = "Today" }
                )
            }
        }
    }

    if (showAddGarmentDialog) {
        DigitizeGarmentDialog(
            onDismiss = { showAddGarmentDialog = false },
            onConfirm = { name, category, color, season, warmth, presetId, imageBytes ->
                viewModel.addClothingItem(
                    name = name,
                    category = category,
                    color = color,
                    season = season,
                    warmthLevel = warmth,
                    presetId = presetId,
                    imageBytes = imageBytes
                )
                showAddGarmentDialog = false
            }
        )
    }
}

@Composable
fun HeaderRow(dateText: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = dateText.uppercase(),
                color = BrandRose,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Aura Closet",
                color = BrandDarkText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BrandRose)
                .border(2.dp, BrandRoseLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User profile account selector",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CustomBottomBar(
    activeTab: String,
    onTabSelect: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .height(84.dp)
            .background(Color.White)
            .border(width = 1.dp, color = BrandRoseMutedLight)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab: Today
            BottomBarItem(
                label = "Today",
                iconEmoji = "✨",
                isActive = activeTab == "Today",
                onClick = { onTabSelect("Today") }
            )

            // Tab: Closet
            BottomBarItem(
                label = "Closet",
                iconEmoji = "👚",
                isActive = activeTab == "Closet",
                onClick = { onTabSelect("Closet") }
            )

            // Space holder for absolute center action custom button
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BrandRose)
                    .clickable { onAddClick() }
                    .offset(y = (-14).dp)
                    .testTag("floating_photo_action"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📸", fontSize = 24.sp)
                    Text("DIGITIZE", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Tab: Style (Quiz Screen)
            BottomBarItem(
                label = "Style",
                iconEmoji = "🎯",
                isActive = activeTab == "Style",
                onClick = { onTabSelect("Style") }
            )

            // Tab: Plans
            BottomBarItem(
                label = "Plans",
                iconEmoji = "📅",
                isActive = activeTab == "Plans",
                onClick = { onTabSelect("Plans") }
            )
        }
    }
}

@Composable
fun BottomBarItem(
    label: String,
    iconEmoji: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(54.dp)
            .clickable { onClick() }
            .testTag("nav_tab_${label.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = iconEmoji,
            fontSize = 20.sp,
            modifier = Modifier.alpha(if (isActive) 1.0f else 0.40f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isActive) BrandRose else BrandMutedText.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DigitizeGarmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Int, String?, ByteArray?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Tops") }
    var color by remember { mutableStateOf("Cream") }
    var season by remember { mutableStateOf("All") }
    var warmthLevel by remember { mutableStateOf(3) }
    var presetId by remember { mutableStateOf<String?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }

    // Preset configurations for simulated mock scans
    val scanningPresets = listOf(
        Triple("Oversized Blazer", "Outerwear", "blazer_casual"),
        Triple("Piped Linen Shirt", "Tops", "shirt_linen"),
        Triple("Slim Designer Trousers", "Bottoms", "chinos_navy"),
        Triple("Japanese Denim Jacket", "Outerwear", "windbreaker_tech"),
        Triple("Waffle Knit Sweater", "Tops", "sweater_wool"),
        Triple("Suede Combat Boots", "Shoes", "boots_chelsea"),
        Triple("Vibram Sole Trainers", "Shoes", "sneakers_leather"),
        Triple("Cashmere Ribbed Scarf", "Accessories", "scarf_cashmere"),
        Triple("Classic leather Watch", "Accessories", "watch_chrono")
    )

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            presetId = null
            if (name.isBlank()) {
                name = "My Captured $category"
            }
        }
    }

    // Coroutine for scan simulations
    LaunchedEffect(isAnalyzing) {
        if (isAnalyzing) {
            scanProgress = 0f
            while (scanProgress < 1f) {
                kotlinx.coroutines.delay(100)
                scanProgress += 0.1f
            }
            // Auto fill with a preset
            val preset = scanningPresets.filter { it.second.equals(category, ignoreCase = true) }.randomOrNull() 
                ?: scanningPresets.random()
            name = preset.first
            category = preset.second
            presetId = preset.third
            color = listOf("Sand Cream", "Midnight Black", "Olive Drab", "Slate Grey", "Navy Blue").random()
            warmthLevel = (2..5).random()
            isAnalyzing = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Digitize Wardrobe Item",
                color = BrandDarkText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Photo Capture & Preview Box (Top primary visual target)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BrandRoseLight)
                        .border(BorderStroke(1.5.dp, BrandBorder), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAnalyzing) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = scanProgress,
                                color = BrandRose,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Aura AI scanning threads... ${(scanProgress * 100).toInt()}%",
                                color = BrandRose,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (capturedBitmap != null) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Captured Garment",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Label or overlay to retake
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .clickable { cameraLauncher.launch(null) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("📸 Retake", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (presetId != null) {
                        // Preset visual
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "✨ Ready!",
                                color = BrandRose,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Simulated $name",
                                color = BrandDarkText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Shade: $color • Preset $presetId",
                                color = BrandMutedText,
                                fontSize = 10.sp
                            )
                        }
                    } else {
                        // Default Empty State
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📸", fontSize = 32.sp)
                            Text(
                                text = "No item photo recorded yet",
                                color = BrandMutedText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Camera Action button bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("form_camera_capture_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📸", fontSize = 13.sp)
                            Text("Camera Take", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { isAnalyzing = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRoseMutedLight),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isAnalyzing,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("form_simulate_capture_button")
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔄", fontSize = 11.sp)
                            Text("Simulate AI", fontSize = 11.sp, color = BrandRose, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Divider(color = BrandBorder, thickness = 0.5.dp)

                // 2. Form Input fields
                Text(
                    text = "SPECIFICATIONS",
                    color = BrandRose,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                // Clothing Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Garment Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRose,
                        unfocusedBorderColor = BrandBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("digitize_item_name_input")
                )

                // Clothing category selection chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Clothing Category (Type)", color = BrandMutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    val categories = listOf("Tops", "Bottoms", "Shoes", "Outerwear", "Accessories")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = cat.equals(category, ignoreCase = true)
                            val containerCol = if (isSelected) BrandRose else BrandRoseMutedLight
                            val textCol = if (isSelected) Color.White else BrandMutedText
                            val emoji = getCategoryEmoji(cat)
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(containerCol)
                                    .clickable { category = cat }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("form_category_$cat"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(emoji, fontSize = 11.sp)
                                    Text(
                                        text = cat,
                                        color = textCol,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Primary Color field & quick select colors
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Primary Shade Color (e.g. Cream, Charcoal)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandRose,
                            unfocusedBorderColor = BrandBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("form_color_input")
                    )

                    // Quick Select palette colors!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickColors = listOf("Charcoal", "Cream", "Olive", "Navy", "White", "Rose")
                        quickColors.forEach { qColor ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandRoseLight)
                                    .clickable { color = qColor }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(qColor, color = BrandRose, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Season Section with beautiful segmented chips
                    Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Season Target", color = BrandMutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        val seasons = listOf("Spring", "Summer", "Winter", "All")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            seasons.forEach { s ->
                                val isSelected = s.equals(season, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) BrandRose else BrandRoseMutedLight)
                                        .clickable { season = s }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("form_season_$s"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = s,
                                        color = if (isSelected) Color.White else BrandMutedText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Warmth Level Section (1 to 5 stars selection)
                    Column(modifier = Modifier.weight(0.8f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Warmth Level", color = BrandMutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            (1..5).forEach { level ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(CircleShape)
                                        .background(if (level <= warmthLevel) BrandRose else BrandBorder)
                                        .clickable { warmthLevel = level }
                                        .aspectRatio(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$level",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        // Compress Bitmap to ByteArray
                        val byteArray = capturedBitmap?.let { bitmap ->
                            try {
                                val stream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                                stream.toByteArray()
                            } catch (e: Exception) {
                                null
                            }
                        }
                        onConfirm(name, category, color, season, warmthLevel, presetId, byteArray)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_digitize_button"),
                enabled = name.isNotBlank() && !isAnalyzing
            ) {
                Text("Save to Vault", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = BrandMutedText)) {
                Text("Dismiss")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}
