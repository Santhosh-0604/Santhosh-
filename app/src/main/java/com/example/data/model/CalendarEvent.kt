package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val time: String, // e.g. "10:00 AM"
    val date: String, // e.g. "2026-06-09"
    val eventType: String = "Casual" // "Formal", "Casual", "Active", "Party"
)
