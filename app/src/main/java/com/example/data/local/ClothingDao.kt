package com.example.data.local

import androidx.room.*
import com.example.data.model.ClothingItem
import com.example.data.model.OutfitHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {
    @Query("SELECT * FROM clothing_items ORDER BY id DESC")
    fun getAllClothing(): Flow<List<ClothingItem>>

    @Query("SELECT * FROM clothing_items WHERE category = :category ORDER BY id DESC")
    fun getClothingByCategory(category: String): Flow<List<ClothingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClothing(item: ClothingItem): Long

    @Update
    suspend fun updateClothing(item: ClothingItem)

    @Delete
    suspend fun deleteClothing(item: ClothingItem)

    @Query("SELECT * FROM clothing_items WHERE id IN (:ids)")
    suspend fun getClothingItemsByIds(ids: List<Long>): List<ClothingItem>

    @Query("UPDATE clothing_items SET wearCount = wearCount + 1, lastWornTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementWearCount(id: Long, timestamp: Long)

    @Query("SELECT * FROM clothing_items WHERE id = :id")
    suspend fun getClothingById(id: Long): ClothingItem?

    // Outfit History
    @Query("SELECT * FROM outfit_histories ORDER BY timestamp DESC")
    fun getAllOutfitHistory(): Flow<List<OutfitHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutfitHistory(history: OutfitHistory): Long
}
