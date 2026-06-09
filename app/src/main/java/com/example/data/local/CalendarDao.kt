package com.example.data.local

import androidx.room.*
import com.example.data.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_events ORDER BY id DESC")
    fun getAllEventsFlow(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_events ORDER BY id DESC")
    suspend fun getAllEvents(): List<CalendarEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEvent): Long

    @Delete
    suspend fun deleteEvent(event: CalendarEvent)
}
