package com.example.data.repository

import com.example.data.local.ClothingDao
import com.example.data.local.CalendarDao
import com.example.data.model.ClothingItem
import com.example.data.model.OutfitHistory
import com.example.data.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

class ClosetRepository(
    private val clothingDao: ClothingDao,
    private val calendarDao: CalendarDao
) {
    val allClothing: Flow<List<ClothingItem>> = clothingDao.getAllClothing()
    val allOutfitHistory: Flow<List<OutfitHistory>> = clothingDao.getAllOutfitHistory()
    val allEventsFlow: Flow<List<CalendarEvent>> = calendarDao.getAllEventsFlow()

    fun getClothingByCategory(category: String): Flow<List<ClothingItem>> =
        clothingDao.getClothingByCategory(category)

    suspend fun insertClothing(item: ClothingItem): Long =
        clothingDao.insertClothing(item)

    suspend fun updateClothing(item: ClothingItem) =
        clothingDao.updateClothing(item)

    suspend fun deleteClothing(item: ClothingItem) =
        clothingDao.deleteClothing(item)

    suspend fun getClothingById(id: Long): ClothingItem? =
        clothingDao.getClothingById(id)

    suspend fun getClothingItemsByIds(ids: List<Long>): List<ClothingItem> =
        clothingDao.getClothingItemsByIds(ids)

    suspend fun recordWear(id: Long, timestamp: Long) =
        clothingDao.incrementWearCount(id, timestamp)

    suspend fun insertOutfitHistory(history: OutfitHistory): Long =
        clothingDao.insertOutfitHistory(history)

    // Calendar
    suspend fun getAllEvents(): List<CalendarEvent> =
        calendarDao.getAllEvents()

    suspend fun insertEvent(event: CalendarEvent): Long =
        calendarDao.insertEvent(event)

    suspend fun deleteEvent(event: CalendarEvent) =
        calendarDao.deleteEvent(event)
}
