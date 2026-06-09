package com.example.data.repository

import com.example.data.dao.ClothingItemDao
import com.example.data.model.ClothingItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ClothingRepository(private val clothingItemDao: ClothingItemDao) {
    val allItems: Flow<List<ClothingItem>> = clothingItemDao.getAllItems()

    suspend fun insertItem(item: ClothingItem): Long {
        return clothingItemDao.insertItem(item)
    }

    suspend fun updateItem(item: ClothingItem) {
        clothingItemDao.updateItem(item)
    }

    suspend fun deleteItem(item: ClothingItem) {
        clothingItemDao.deleteItem(item)
    }

    suspend fun incrementWear(id: Long, timestamp: Long) {
        clothingItemDao.incrementWear(id, timestamp)
    }

    suspend fun getItemById(id: Long): ClothingItem? {
        return clothingItemDao.getItemById(id)
    }

    suspend fun populatePresetsIfEmpty() {
        val currentList = clothingItemDao.getAllItems().first()
        if (currentList.isEmpty()) {
            val presets = listOf(
                ClothingItem(
                    name = "White Linen Shirt",
                    category = "Tops",
                    color = "#FFFFFF",
                    season = "Summer",
                    presetId = "preset_linen_shirt",
                    warmthLevel = 2,
                    wearCount = 5,
                    lastWornTimestamp = System.currentTimeMillis() - 86400000 * 3
                ),
                ClothingItem(
                    name = "Navy Crewneck Knit",
                    category = "Tops",
                    color = "#1D2731",
                    season = "Fall",
                    presetId = "preset_navy_knit",
                    warmthLevel = 3,
                    wearCount = 8,
                    lastWornTimestamp = System.currentTimeMillis() - 86400000 * 1
                ),
                ClothingItem(
                    name = "Black Silk Turtleneck",
                    category = "Tops",
                    color = "#111111",
                    season = "Winter",
                    presetId = "preset_black_turtleneck",
                    warmthLevel = 4,
                    wearCount = 12,
                    lastWornTimestamp = System.currentTimeMillis() - 86400000 * 5
                ),
                ClothingItem(
                    name = "Casual Beige Blazer",
                    category = "Outerwear",
                    color = "#D9B48F",
                    season = "Spring",
                    presetId = "preset_blazer",
                    warmthLevel = 3,
                    wearCount = 6,
                    lastWornTimestamp = System.currentTimeMillis() - 86400000 * 2
                ),
                ClothingItem(
                    name = "Heavy Camel Trench",
                    category = "Outerwear",
                    color = "#C19A6B",
                    season = "Winter",
                    presetId = "preset_trench",
                    warmthLevel = 5,
                    wearCount = 3,
                    lastWornTimestamp = System.currentTimeMillis() - 86400000 * 10
                ),
                ClothingItem(
                    name = "Indigo Denim Jeans",
                    category = "Bottoms",
                    color = "#3B5998",
                    season = "All",
                    presetId = "preset_indigo_jeans",
                    warmthLevel = 3,
                    wearCount = 14,
                    lastWornTimestamp = System.currentTimeMillis() - 86400000 * 1
                ),
                ClothingItem(
                    name = "Charcoal Tailored Trousers",
                    category = "Bottoms",
                    color = "#2E3033",
                    season = "All",
                    presetId = "preset_charcoal_trousers",
                    warmthLevel = 3,
                    wearCount = 7,
                    lastWornTimestamp = System.currentTimeMillis() - 86400000 * 4
                ),
                ClothingItem(
                    name = "Pleated Cream Skirt",
                    category = "Bottoms",
                    color = "#F5F5DC",
                    season = "Summer",
                    presetId = "preset_cream_skirt",
                    warmthLevel = 2,
                    wearCount = 4,
                    lastWornTimestamp = System.currentTimeMillis() - 86400000 * 6
                ),
                ClothingItem(
                    name = "Brown Leather Boots",
                    category = "Shoes",
                    color = "#5C4033",
                    season = "Fall",
                    presetId = "preset_leather_boots",
                    warmthLevel = 3,
                    wearCount = 11,
                    lastWornTimestamp = System.currentTimeMillis() - 86400000 * 2
                ),
                ClothingItem(
                    name = "Minimalist White Sneakers",
                    category = "Shoes",
                    color = "#FFFFFF",
                    season = "Summer",
                    presetId = "preset_white_sneakers",
                    warmthLevel = 2,
                    wearCount = 18,
                    lastWornTimestamp = System.currentTimeMillis() - 86400000 * 1
                ),
                ClothingItem(
                    name = "Gold Accent Belt",
                    category = "Accessories",
                    color = "#FFD700",
                    season = "All",
                    presetId = "preset_belt",
                    warmthLevel = 1,
                    wearCount = 9,
                    lastWornTimestamp = System.currentTimeMillis() - 86400000 * 3
                ),
                ClothingItem(
                    name = "Burgundy Ribbed Scarf",
                    category = "Accessories",
                    color = "#800020",
                    season = "Winter",
                    presetId = "preset_scarf",
                    warmthLevel = 4,
                    wearCount = 2,
                    lastWornTimestamp = System.currentTimeMillis() - 86400000 * 12
                )
            )
            clothingItemDao.insertItems(presets)
        }
    }
}
