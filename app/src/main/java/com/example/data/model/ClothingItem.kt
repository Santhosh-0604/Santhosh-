package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clothing_items")
data class ClothingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "Tops", "Bottoms", "Shoes", "Outerwear", "Accessories"
    val color: String,
    val season: String = "All", // "Spring", "Summer", "Fall", "Winter", "All"
    val presetId: String? = null, // Preset identifier, e.g., "preset_blazer", "preset_shirt", etc.
    val imageBytes: ByteArray? = null, // Image taken by camera
    val wearCount: Int = 0,
    val lastWornTimestamp: Long = 0L,
    val warmthLevel: Int = 3 // Rate warmth 1 (coolest, e.g., t-shirt) to 5 (warmest, e.g., heavy coat)
)
