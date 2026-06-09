package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.model.ClothingItem
import com.example.ui.theme.*
import com.example.viewmodel.ClosetViewModel

@Composable
fun ClosetScreen(
    viewModel: ClosetViewModel,
    modifier: Modifier = Modifier
) {
    val clothes by viewModel.allClothing.collectAsState()
    val activeCategory by viewModel.selectedCategoryTab.collectAsState()

    val categories = remember {
        listOf("Tops", "Bottoms", "Shoes", "Outerwear", "Accessories")
    }

    val filteredClothes = clothes.filter {
        it.category.equals(activeCategory, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizontal scroll category selection
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(activeCategory),
            containerColor = Color.Transparent,
            contentColor = BrandRose,
            edgePadding = 0.dp,
            divider = {},
            indicator = { tabPositions ->
                // Custom design: blank or invisible indicator since we style buttons directly
            }
        ) {
            categories.forEachIndexed { index, cat ->
                val isSelected = cat == activeCategory
                val containerColor = if (isSelected) BrandRose else BrandRoseMutedLight
                val contentColor = if (isSelected) Color.White else BrandMutedText

                Box(
                    modifier = Modifier
                        .padding(end = 8.dp, bottom = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(containerColor)
                        .clickable { viewModel.setSelectedCategoryTab(cat) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("closet_category_tab_${cat.lowercase()}")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getCategoryEmoji(cat),
                            fontSize = 14.sp
                        )
                        Text(
                            text = cat,
                            color = contentColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Closet List grid
        if (filteredClothes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text("👔", fontSize = 48.sp)
                    Text(
                        text = "Your digital $activeCategory collection is empty.",
                        color = BrandMutedText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Use the float '+' photo button to digitize outfit presets or custom captured items.",
                        color = BrandMutedText.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredClothes) { item ->
                    ClothingItemCard(
                        item = item,
                        onDeleteClick = { viewModel.removeClothingItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun ClothingItemCard(
    item: ClothingItem,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clothing_item_card_${item.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWhiteAccent),
        border = BorderStroke(1.dp, BrandBorder)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Large visual preview inside clothing item
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp)
                        .background(BrandRoseMutedLight, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val bitmap = remember(item.imageBytes) {
                        item.imageBytes?.let { bytes ->
                            try {
                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                            } catch (e: Exception) {
                                null
                            }
                        }
                    }

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = item.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = getPresetEmoji(item.presetId, item.category),
                                fontSize = 32.sp
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color.White, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.color,
                                    color = BrandMutedText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Core item specifications
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.name,
                        color = BrandDarkText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                            for (i in 1..5) {
                                Text(
                                    text = "🔥",
                                    fontSize = 8.sp,
                                    modifier = Modifier.alpha(if (i <= item.warmthLevel) 1.0f else 0.2f)
                                )
                            }
                        }
                        Text(
                            text = "Warmth Lvl ${item.warmthLevel}",
                            color = BrandMutedText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Divider(color = BrandBorder.copy(alpha = 0.5f))

                // Stats wear counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (item.wearCount > 0) BrandRoseLight else BrandRoseMutedLight, 
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (item.wearCount == 0) "NEW/UNWORN" else "WORN: ${item.wearCount}x",
                            color = if (item.wearCount > 0) BrandRose else BrandMutedText,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Season: ${item.season}",
                        color = BrandMutedText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Absolute positioned deletion option
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
                    .border(1.dp, BrandBorder.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Item",
                    tint = BrandRose,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

fun getPresetEmoji(presetId: String?, category: String): String {
    return when (presetId) {
        "shirt_linen" -> "👕"
        "blazer_casual" -> "🧥"
        "sweater_wool" -> "🧶"
        "tshirt_oversized" -> "👕"
        "chinos_navy" -> "👖"
        "jeans_indigo" -> "👖"
        "cargo_tech" -> "👖"
        "shorts_fleece" -> "🩳"
        "boots_chelsea" -> "🥾"
        "sneakers_leather" -> "👟"
        "shoes_canvas" -> "👟"
        "coat_trench" -> "🧥"
        "windbreaker_tech" -> "🧥"
        "scarf_cashmere" -> "🧣"
        "watch_chrono" -> "⌚"
        else -> getCategoryEmoji(category)
    }
}
