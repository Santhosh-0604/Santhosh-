package com.example.data.dao

import androidx.room.*
import com.example.data.model.ClothingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingItemDao {
    @Query("SELECT * FROM clothing_items ORDER BY lastWornTimestamp DESC")
    fun getAllItems(): Flow<List<ClothingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ClothingItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ClothingItem>)

    @Update
    suspend fun updateItem(item: ClothingItem)

    @Delete
    suspend fun deleteItem(item: ClothingItem)

    @Query("UPDATE clothing_items SET wearCount = wearCount + 1, lastWornTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementWear(id: Long, timestamp: Long)

    @Query("SELECT * FROM clothing_items WHERE id = :id")
    suspend fun getItemById(id: Long): ClothingItem?
}
