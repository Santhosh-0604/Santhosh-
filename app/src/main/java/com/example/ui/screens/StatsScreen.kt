package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClothingItem
import com.example.data.model.OutfitHistory
import com.example.ui.theme.*
import com.example.viewmodel.ClosetViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(
    viewModel: ClosetViewModel,
    modifier: Modifier = Modifier
) {
    val clothes by viewModel.allClothing.collectAsState()
    val wearHistory by viewModel.allOutfitHistory.collectAsState()

    // Deriving custom metrics for geometric stats panel
    val totalWears = wearHistory.size
    val totalClothingCount = clothes.size
    
    val unwornClothes = clothes.filter { it.wearCount == 0 }
    val unwornCount = unwornClothes.size
    val utilizationPercentage = if (totalClothingCount > 0) {
        ((totalClothingCount - unwornCount).toFloat() / totalClothingCount * 100).toInt()
    } else 0

    // Top 3 most worn items
    val topWornItems = remember(clothes) {
        clothes.filter { it.wearCount > 0 }.sortedByDescending { it.wearCount }.take(3)
    }

    // Sleepers (Items never worn or barely worn)
    val coldSleeperItems = remember(clothes) {
        clothes.filter { it.wearCount == 0 }.take(4)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High-fidelity Overview Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "AURA STATISTICS",
                    color = BrandRose,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Wardrobe Utilization",
                    color = BrandDarkText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Geometric grid layout for main KPI indicators
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Total items vs worn
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandWhiteAccent),
                    border = BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(BrandRoseMutedLight, CircleShape)
                                .size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📦", fontSize = 14.sp)
                        }
                        Column {
                            Text(
                                text = "$totalClothingCount items",
                                color = BrandDarkText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "DIGITIZED CLOSET",
                                color = BrandMutedText,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Card 2: Cost-per-wear equivalent / Utilization
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandWhiteAccent),
                    border = BorderStroke(1.dp, BrandBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(BrandRoseLight, CircleShape)
                                .size(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✨", fontSize = 14.sp)
                        }
                        Column {
                            Text(
                                text = "$utilizationPercentage%",
                                color = BrandRose,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "UTILIZATION INDEX",
                                color = BrandMutedText,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }

        // Top Worn Items Card List
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(BrandRoseLight)
                    .border(1.dp, BrandBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "🏆 Most Worn Fashion pieces",
                    color = BrandDarkText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                if (topWornItems.isEmpty()) {
                    Text(
                        text = "Once you wear and log your outfits today, your highest worn items will emerge here.",
                        color = BrandMutedText,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 16.sp)
                    )
                } else {
                    topWornItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.White, CircleShape)
                                        .size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "0${index + 1}", color = BrandRose, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text(item.name, color = BrandDarkText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(item.category + " • " + item.color, color = BrandMutedText, fontSize = 10.sp)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(BrandRose, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${item.wearCount} wears",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (index < topWornItems.lastIndex) {
                            Divider(color = BrandBorder.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Sleepers (Never worn items to promote rotation!)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BrandWhiteAccent)
                    .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "❄️ Underutilized 'Sleeper' Clothes",
                    color = BrandDarkText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                if (coldSleeperItems.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎉", fontSize = 20.sp)
                        Text(
                            text = "Incredible! 100% of your cabinet has been worn. Zero sleeper items detected.",
                            color = BrandMutedText,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Text(
                        text = "Balance your rotation. These items have not been worn yet:",
                        color = BrandMutedText,
                        fontSize = 11.sp
                    )

                    coldSleeperItems.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(getPresetEmoji(item.presetId, item.category), fontSize = 16.sp)
                                Text(
                                    text = item.name,
                                    color = BrandDarkText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(180.dp)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(BrandRoseMutedLight, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "0 wears",
                                    color = BrandMutedText,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Historical Ledger list header
        item {
            Text(
                text = "Full Wear History Log ($totalWears)",
                color = BrandDarkText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (wearHistory.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your outfits diary list is currently empty.",
                        color = BrandMutedText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(wearHistory) { log ->
                HistoryLogCard(log = log)
            }
        }
    }
}

@Composable
fun HistoryLogCard(log: OutfitHistory, modifier: Modifier = Modifier) {
    val dateStr = remember(log.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWhiteAccent),
        border = BorderStroke(1.dp, BrandBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    color = BrandMutedText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .background(BrandRoseLight, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${log.tempCelsius}°C (${log.weatherCondition})",
                        color = BrandRose,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = log.summary,
                color = BrandDarkText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🗓️",
                    fontSize = 10.sp
                )
                Text(
                    text = log.agendaEvent,
                    color = BrandMutedText,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
