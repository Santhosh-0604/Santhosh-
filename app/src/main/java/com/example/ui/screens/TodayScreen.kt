package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.SuggestedOutfit
import com.example.data.model.ClothingItem
import com.example.ui.theme.*
import com.example.viewmodel.ClosetViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodayScreen(
    viewModel: ClosetViewModel,
    onNavigateToCloset: () -> Unit,
    onNavigateToStyle: () -> Unit,
    onNavigateToStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val suggestedOutfit by viewModel.suggestedOutfit.collectAsState()
    val isGenerating by viewModel.isGeneratingSuggestion.collectAsState()
    val clothes by viewModel.allClothing.collectAsState()
    val currentTemp by viewModel.currentTempCelsius.collectAsState()
    val currentCondition by viewModel.currentWeatherCondition.collectAsState()
    val calendarEvents by viewModel.allCalendarEvents.collectAsState()
    val stylePersonality by viewModel.stylePersonality.collectAsState()

    val dateStr = remember {
        val format = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        format.format(Date())
    }

    LaunchedEffect(clothes) {
        if (suggestedOutfit == null && clothes.isNotEmpty()) {
            viewModel.generateAiOutfitSuggestion()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Daily Recommendation Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(BrandRoseLight)
                .border(1.dp, BrandBorder, RoundedCornerShape(32.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Meta row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = when (currentCondition) {
                                    "Sunny" -> "☀️"
                                    "Cloudy" -> "☁️"
                                    "Rainy" -> "🌧️"
                                    "Snowy" -> "❄️"
                                    else -> "💨"
                                },
                                fontSize = 16.sp
                            )
                            Text(
                                text = "$currentCondition • $currentTemp°C",
                                color = BrandRose,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        val personalityLabel = stylePersonality?.let { " ($it)" } ?: ""
                        Text(
                            text = "Daily AI Pick$personalityLabel",
                            color = BrandDarkText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Calendar Sync Pill
                    val eventToday = calendarEvents.firstOrNull { 
                        // Simplified date match
                        true
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (eventToday != null) "Calendar: ${eventToday.title}" else "Calendar Sync",
                            color = BrandMutedText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                // AI Suggested Output display area
                if (isGenerating) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(BrandWhiteAccent, RoundedCornerShape(24.dp))
                            .border(1.dp, BrandBorder, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(color = BrandRose, modifier = Modifier.size(28.dp))
                            Text(
                                text = "Aura Engine coordinating clothes...",
                                color = BrandMutedText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else if (suggestedOutfit != null) {
                    val outfit = suggestedOutfit!!
                    val recommendedItems = clothes.filter { outfit.itemIds.contains(it.id) }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Outline Dashed representation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .background(BrandWhiteAccent, RoundedCornerShape(24.dp))
                                .border(
                                    width = 2.dp,
                                    color = BrandBorder,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "✨ ${outfit.outfitName}",
                                    color = BrandDarkText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                                ) {
                                    recommendedItems.forEach { item ->
                                        Box(
                                            modifier = Modifier
                                                .background(BrandRoseMutedLight, RoundedCornerShape(12.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${getCategoryEmoji(item.category)} ${item.name}",
                                                color = BrandRose,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                if (recommendedItems.isEmpty()) {
                                    Text(
                                        text = "Please add garments to Tops, Bottoms and Shoes in the Closet to compute outfits.",
                                        color = BrandMutedText,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Styling Insight
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Aura Insight (${outfit.engine})".uppercase(),
                                color = BrandRose,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = outfit.explanation,
                                color = BrandDarkText,
                                fontSize = 13.sp,
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 18.sp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Styling Tip: ${outfit.stylingTips}",
                                color = BrandMutedText,
                                fontSize = 11.sp,
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 15.sp)
                            )
                        }

                        // Confirmation Buttons and Overlapping Circles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Overlapping circles as styled in Geometric Balance
                            val demoCount = Math.min(3, recommendedItems.size)
                            Row(
                                modifier = Modifier.padding(start = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 0 until demoCount) {
                                    val itemColor = when (i) {
                                        0 -> BrandRose
                                        1 -> BrandMediumRose
                                        else -> BrandBorder
                                    }
                                    Box(
                                        modifier = Modifier
                                            .offset(x = (-i * 8).dp)
                                            .size(32.dp)
                                            .background(itemColor, CircleShape)
                                            .border(2.dp, BrandRoseLight, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "0${i + 1}",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.generateAiOutfitSuggestion(isAlternative = true) },
                                    modifier = Modifier
                                        .background(BrandWhiteAccent, CircleShape)
                                        .border(1.dp, BrandBorder, CircleShape)
                                        .size(44.dp)
                                        .testTag("today_refresh_alternative_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Regenerate Alternative Suggestion",
                                        tint = BrandRose
                                    )
                                }

                                Button(
                                    onClick = { viewModel.confirmSuggestedOutfit(outfit) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                                    shape = RoundedCornerShape(24.dp),
                                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                                    modifier = Modifier
                                        .testTag("confirm_outfit_button")
                                        .height(44.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Confirm",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Confirm Outfit",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Empty wardrobe helper state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(BrandWhiteAccent, RoundedCornerShape(24.dp))
                            .border(1.dp, BrandBorder, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "👚",
                                fontSize = 32.sp
                            )
                            Text(
                                text = "Configure your garments on the Closet page to initiate Daily Outfit picks.",
                                color = BrandMutedText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onNavigateToCloset,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text("Go to Closet", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Style Alignment Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(if (stylePersonality != null) BrandWhiteAccent else BrandRoseLight)
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
                .clickable { onNavigateToStyle() }
                .padding(16.dp)
                .testTag("today_alignment_banner")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White, CircleShape)
                            .size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (stylePersonality) {
                                "Minimalist" -> "📐"
                                "Bohemian" -> "🍃"
                                "Classic" -> "💼"
                                "Trendy" -> "⚡"
                                else -> "🎯"
                            },
                            fontSize = 18.sp
                        )
                    }

                    Column {
                        Text(
                            text = if (stylePersonality != null) "Aesthetic Profile Aligned" else "Aura Alignment Required",
                            color = BrandDarkText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (stylePersonality != null) "Aesthetic: $stylePersonality (Click to retake)" else "Configure matching preferences to refine AI outfit styles.",
                            color = BrandMutedText,
                            fontSize = 11.sp
                        )
                    }
                }

                Text(
                    text = "→",
                    color = BrandRose,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        // Section: Today's Sync Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BrandRoseMutedLight)
                    .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
                    .clickable { onNavigateToStats() }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📊", fontSize = 16.sp)
                    Text("+12%", color = BrandRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    val wornHistory by viewModel.allOutfitHistory.collectAsState()
                    Text(
                        text = "${wornHistory.size}",
                        color = BrandDarkText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "TOTAL WEARS SECURED",
                        color = BrandMutedText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Card 2
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BrandRoseMutedLight)
                    .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
                    .clickable { onNavigateToStats() }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("📦", fontSize = 16.sp)
                    Text("${clothes.size} total", color = BrandRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    val unworn = clothes.filter { it.wearCount == 0 }.size
                    val total = clothes.size
                    val util = if (total > 0) ((total - unworn).toFloat() / total * 100).toInt() else 0
                    Text(
                        text = "$util%",
                        color = BrandDarkText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "CLOSET UTILIZATION RATE",
                        color = BrandMutedText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Section: Live Feed / Recent Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(BrandWhiteAccent)
                .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Recent Outfit Log",
                color = BrandDarkText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            val histories by viewModel.allOutfitHistory.collectAsState()
            if (histories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No outfits logged yet. Wear an AI pick to record wear tracking!",
                        color = BrandMutedText,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                histories.take(4).forEach { log ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.summary, color = BrandDarkText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(log.agendaEvent, color = BrandMutedText, fontSize = 10.sp)
                        }
                        Text(
                            text = "${log.tempCelsius}°C • ${log.weatherCondition}",
                            color = BrandRose,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Divider(color = BrandBorder.copy(alpha = 0.5f))
                }
            }
        }
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category) {
        "Tops" -> "👕"
        "Bottoms" -> "👖"
        "Shoes" -> "👟"
        "Outerwear" -> "🧥"
        "Accessories" -> "🧣"
        else -> "👚"
    }
}
