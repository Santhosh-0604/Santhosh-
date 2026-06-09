package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outfit_histories")
data class OutfitHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val summary: String, // Description, e.g., "Casual Blazer, Linen Shirt & Chino Pants"
    val itemIds: String, // Comma-separated list of item IDs, e.g., "1,4"
    val weatherCondition: String, // e.g. "Sunny"
    val tempCelsius: Int, // e.g. 24
    val agendaEvent: String // e.g. "Calendar Sync • Work Presentation"
)
