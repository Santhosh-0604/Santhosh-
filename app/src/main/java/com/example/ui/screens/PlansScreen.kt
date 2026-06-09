package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.GeminiClient
import com.example.data.model.CalendarEvent
import com.example.ui.theme.*
import com.example.viewmodel.ClosetViewModel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.shape.CircleShape

@Composable
fun PlansScreen(
    viewModel: ClosetViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.allCalendarEvents.collectAsState()
    val weatherCondition by viewModel.currentWeatherCondition.collectAsState()
    val tempCelsius by viewModel.currentTempCelsius.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val currentPrecipitationMm by viewModel.currentPrecipitationMm.collectAsState()
    val isFetchingWeather by viewModel.isFetchingWeather.collectAsState()

    var showAddEventDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BrandBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "AURA SCHEDULING",
                    color = BrandRose,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Environment & Agenda Sync",
                    color = BrandDarkText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Weekly Outfit Calendar Section
        item {
            WeeklyOutfitCalendarSection(
                viewModel = viewModel,
                weatherCondition = weatherCondition,
                tempCelsius = tempCelsius,
                events = events
            )
        }

        // Location & Weather Simulation Controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BrandWhiteAccent),
                border = BorderStroke(1.dp, BrandBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "🌤️ Local Weather & Climate Controller",
                        color = BrandDarkText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Connect with live Open-Meteo forecasts for any custom location, or query environment modifiers locally.",
                        color = BrandMutedText,
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 15.sp)
                    )

                    var searchCity by remember { mutableStateOf("") }
                    
                    // Live weather geocoding query field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Fetch Real-Time Local Weather",
                            color = BrandDarkText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchCity,
                                onValueChange = { searchCity = it },
                                placeholder = { Text("Enter city (e.g., Paris, Mumbai...)", fontSize = 11.sp, color = BrandMutedText) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("weather_city_search_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandRose,
                                    unfocusedBorderColor = BrandBorder,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                trailingIcon = {
                                    if (searchCity.isNotEmpty()) {
                                        IconButton(onClick = { searchCity = "" }, modifier = Modifier.size(20.dp)) {
                                            Text("✕", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandMutedText)
                                        }
                                    }
                                }
                            )

                            Button(
                                onClick = {
                                    if (searchCity.isNotBlank()) {
                                        viewModel.updateWeatherForLocation(searchCity)
                                    }
                                },
                                enabled = searchCity.isNotBlank() && !isFetchingWeather,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("weather_fetch_button"),
                                contentPadding = PaddingValues(horizontal = 14.dp)
                            ) {
                                if (isFetchingWeather) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Fetch", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Enriched active telemetries card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BrandRoseLight)
                            .border(1.dp, BrandBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📍 $currentLocation",
                                    color = BrandDarkText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(BrandRose)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Synced",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text("CONDITION", color = BrandMutedText, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = when (weatherCondition) {
                                            "Sunny" -> "☀️ Sunny"
                                            "Cloudy" -> "☁️ Cloudy"
                                            "Rainy" -> "🌧️ Rainy (Wet)"
                                            "Snowy" -> "❄️ Snowy"
                                            "Windy" -> "💨 Windy"
                                            else -> weatherCondition
                                        },
                                        color = BrandDarkText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("TEMPERATURE", color = BrandMutedText, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                    Text("$tempCelsius°C", color = BrandDarkText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text("PRECIPITATION", color = BrandMutedText, fontSize = 7.5.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "${String.format(java.util.Locale.US, "%.1f", currentPrecipitationMm)} mm",
                                        color = if (currentPrecipitationMm > 0.0) BrandRose else BrandDarkText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BrandBorder, thickness = 0.5.dp)

                    // Weather condition buttons
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Manual Simulation Preset Overrides",
                            color = BrandMutedText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val weathers = listOf("Sunny" to "☀️", "Cloudy" to "☁️", "Rainy" to "🌧️", "Snowy" to "❄️", "Windy" to "💨")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            weathers.forEach { (cond, emoji) ->
                                val isActive = cond.equals(weatherCondition, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isActive) BrandRose else BrandRoseMutedLight)
                                        .clickable { 
                                            val mockPrec = when (cond) {
                                                "Rainy" -> 4.5
                                                "Snowy" -> 1.2
                                                else -> 0.0
                                            }
                                            viewModel.setWeather(cond, tempCelsius, mockPrec) 
                                        }
                                        .padding(vertical = 10.dp)
                                        .testTag("weather_select_$cond"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(emoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = cond,
                                            color = if (isActive) Color.White else BrandMutedText,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Temperature slider
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Simulated Temperature Offset",
                                color = BrandDarkText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$tempCelsius°C",
                                color = BrandRose,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Slider(
                            value = tempCelsius.toFloat(),
                            onValueChange = { viewModel.setWeather(weatherCondition, it.toInt(), currentPrecipitationMm) },
                            valueRange = -5f..40f,
                            colors = SliderDefaults.colors(
                                thumbColor = BrandRose,
                                activeTrackColor = BrandRose,
                                inactiveTrackColor = BrandBorder
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("-5°C (Winter)", color = BrandMutedText, fontSize = 8.sp)
                            Text("18°C (Cool)", color = BrandMutedText, fontSize = 8.sp)
                            Text("40°C (Hot)", color = BrandMutedText, fontSize = 8.sp)
                        }
                    }
                }
            }
        }

        // Calendar Agenda
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sync Agenda Items (${events.size})",
                    color = BrandDarkText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showAddEventDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp).testTag("add_event_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(12.dp))
                        Text("Add Event", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Agenda items
        if (events.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your calendar is completely clear! Add an event to coordinate dressing styles.",
                        color = BrandMutedText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(events) { event ->
                CalendarEventRow(event = event, onDelete = { viewModel.removeCalendarEvent(event) })
            }
        }
    }

    if (showAddEventDialog) {
        AddCalendarEventDialog(
            onDismiss = { showAddEventDialog = false },
            onConfirm = { title, time, date, type ->
                viewModel.addCalendarEvent(title, time, date, type)
                showAddEventDialog = false
            }
        )
    }
}

@Composable
fun CalendarEventRow(
    event: CalendarEvent,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWhiteAccent),
        border = BorderStroke(1.dp, BrandBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(BrandRoseLight, CircleShape)
                            .size(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (event.eventType) {
                                "Formal" -> "💼"
                                "Active" -> "💪"
                                "Party" -> "🎉"
                                else -> "🛋️"
                            },
                            fontSize = 9.sp
                        )
                    }
                    Text(
                        text = event.eventType.uppercase(),
                        color = BrandRose,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.title,
                    color = BrandDarkText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Time: ${event.time} • Date: ${event.date}",
                    color = BrandMutedText,
                    fontSize = 10.sp
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .background(BrandRoseMutedLight, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove event",
                    tint = BrandMutedText,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun AddCalendarEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("12:00 PM") }
    var date by remember { mutableStateOf("Today") }
    var eventType by remember { mutableStateOf("Casual") }

    val types = listOf("Casual", "Formal", "Active", "Party")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sync Custom Event",
                color = BrandDarkText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Name (e.g. Job Interview)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRose,
                        unfocusedBorderColor = BrandBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_event_title_input")
                )

                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time (e.g. 5:30 PM)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRose,
                        unfocusedBorderColor = BrandBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (e.g. Tomorrow)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandRose,
                        unfocusedBorderColor = BrandBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Styling Intent Type", color = BrandMutedText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        types.forEach { type ->
                            val isActive = type == eventType
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isActive) BrandRose else BrandRoseMutedLight)
                                    .clickable { eventType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = if (isActive) Color.White else BrandMutedText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title, time, date, eventType) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = BrandMutedText)) {
                Text("Cancel")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun WeeklyOutfitCalendarSection(
    viewModel: ClosetViewModel,
    weatherCondition: String,
    tempCelsius: Int,
    events: List<CalendarEvent>
) {
    val weeklyOutfits by viewModel.weeklyOutfits.collectAsState()
    val isGeneratingWeekly by viewModel.isGeneratingWeekly.collectAsState()
    val allClothing by viewModel.allClothing.collectAsState()
    
    var selectedDayIndex by remember { mutableStateOf(0) }
    var wearSuccessMessage by remember { mutableStateOf<String?>(null) }
    
    // Auto-reset message after 3 seconds
    LaunchedEffect(wearSuccessMessage) {
        if (wearSuccessMessage != null) {
            kotlinx.coroutines.delay(3000)
            wearSuccessMessage = null
        }
    }

    val days = remember(weatherCondition, tempCelsius) {
        GeminiClient.getNext7Days(weatherCondition, tempCelsius)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BrandWhiteAccent),
        border = BorderStroke(1.dp, BrandBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "✨ WEEKLY PLANNER",
                        color = BrandRose,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "AI Outfit Coordinator",
                        color = BrandDarkText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Refresh button
                IconButton(
                    onClick = { viewModel.generateWeeklyOutfitPlanner() },
                    enabled = !isGeneratingWeekly,
                    modifier = Modifier
                        .size(36.dp)
                        .background(BrandRoseMutedLight, CircleShape)
                        .testTag("regenerate_weekly_planner_button")
                ) {
                    Text(
                        text = if (isGeneratingWeekly) "⏳" else "🔄",
                        fontSize = 14.sp
                    )
                }
            }

            if (isGeneratingWeekly) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = BrandRose,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp).testTag("weekly_loading_indicator")
                    )
                    Text(
                        text = "Aura AI is coordinating your weekly lookbook...",
                        color = BrandMutedText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (weeklyOutfits.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "No weekly lookbook generated yet.",
                        color = BrandMutedText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    
                    Button(
                        onClick = { viewModel.generateWeeklyOutfitPlanner() },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("generate_weekly_planner_button")
                    ) {
                        Text("Analyze Wardrobe & Agenda", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // 1. Horizontal Weekly Day Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    days.forEachIndexed { index, day ->
                        val isSelected = selectedDayIndex == index
                        val hasEvents = events.any { GeminiClient.isEventForDay(it, day.date, day.dayName, index) }
                        
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) BrandRose else BrandRoseLight)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) BrandRose else BrandBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedDayIndex = index }
                                .padding(vertical = 10.dp)
                                .testTag("calendar_day_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = if (index == 0) "TOD" else day.dayName.uppercase(),
                                    color = if (isSelected) Color.White else BrandMutedText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                val dayNum = day.date.split("-").lastOrNull() ?: ""
                                Text(
                                    text = dayNum,
                                    color = if (isSelected) Color.White else BrandDarkText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(2.dp))
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val emoji = when (day.weatherCondition) {
                                        "Sunny" -> "☀️"
                                        "Cloudy" -> "☁️"
                                        "Rainy" -> "🌧️"
                                        "Snowy" -> "❄️"
                                        "Windy" -> "💨"
                                        else -> "☀️"
                                    }
                                    Text(emoji, fontSize = 11.sp)
                                    
                                    // Visual event dot indicator
                                    if (hasEvents) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else BrandRose)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = BrandBorder, thickness = 0.5.dp)

                // 2. Beautiful Detailed Layout for Active Day
                val activeOutfit = weeklyOutfits?.getOrNull(selectedDayIndex)
                if (activeOutfit != null) {
                    val dayInfo = days.getOrNull(selectedDayIndex)
                    val activeDayEvents = events.filter { dayInfo?.let { d -> GeminiClient.isEventForDay(it, d.date, d.dayName, selectedDayIndex) } ?: false }
                    
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val dayLabel = when (selectedDayIndex) {
                                    0 -> "Today"
                                    1 -> "Tomorrow"
                                    else -> "Day ${selectedDayIndex}"
                                }
                                Text(
                                    text = "$dayLabel recommendation • ${activeOutfit.engine}".uppercase(),
                                    color = BrandRose,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeOutfit.outfitName,
                                    color = BrandDarkText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // Weather label
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandRoseLight)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${activeOutfit.tempCelsius}°C",
                                    color = BrandRose,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Agenda item link label
                        if (activeDayEvents.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BrandRoseLight)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📅", fontSize = 11.sp)
                                Text(
                                    text = activeDayEvents.joinToString(", ") { "${it.title} (${it.eventType})" },
                                    color = BrandRose,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Text explanation
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(BrandRoseLight.copy(alpha = 0.4f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = activeOutfit.explanation,
                                color = BrandMutedText,
                                fontSize = 11.4.sp,
                                style = TextStyle(lineHeight = 15.sp)
                            )
                        }

                        // Recommended closet items cards
                        Text(
                            text = "Itemized Coordinate Components",
                            color = BrandDarkText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        val dayClothes = activeOutfit.itemIds.mapNotNull { itemId ->
                            allClothing.find { it.id == itemId }
                        }

                        if (dayClothes.isEmpty()) {
                            Text(
                                text = "Could not locate recommended garments in cabinet.",
                                color = BrandMutedText,
                                fontSize = 11.sp
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                dayClothes.forEach { item ->
                                    Card(
                                        modifier = Modifier.width(132.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        border = BorderStroke(1.dp, BrandBorder),
                                        colors = CardDefaults.cardColors(containerColor = BrandWhiteAccent)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = when (item.category) {
                                                        "Tops" -> "👚"
                                                        "Bottoms" -> "👖"
                                                        "Shoes" -> "👟"
                                                        "Outerwear" -> "🧥"
                                                        else -> "👜"
                                                    },
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = item.category.uppercase(),
                                                    color = BrandRose,
                                                    fontSize = 7.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            
                                            Text(
                                                text = item.name,
                                                color = BrandDarkText,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                fontWeight = FontWeight.Bold
                                            )
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = item.color,
                                                    color = BrandMutedText,
                                                    fontSize = 8.sp
                                                )
                                                Text(
                                                    text = "🔄 ${item.wearCount}",
                                                    color = BrandRose,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Styling Tips Bullet
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BrandRoseMutedLight)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡", fontSize = 14.sp)
                            Column {
                                Text(
                                    text = "COORD STYLING ACCENT",
                                    color = BrandRose,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = activeOutfit.stylingTips,
                                    color = BrandMutedText,
                                    fontSize = 10.5.sp,
                                    style = TextStyle(lineHeight = 14.sp)
                                )
                            }
                        }

                        // Wear Outfit Action
                        Button(
                            onClick = {
                                val agendaText = activeDayEvents.firstOrNull()?.let { "${it.title} (${it.eventType})" } ?: "Relaxed day"
                                viewModel.wearWeeklyOutfit(
                                    outfitItemIds = activeOutfit.itemIds,
                                    outfitSummary = activeOutfit.outfitName,
                                    dayWeatherCondition = activeOutfit.weatherCondition,
                                    dayTemp = activeOutfit.tempCelsius,
                                    dayAgendaEvent = agendaText
                                )
                                wearSuccessMessage = "Outfit successfully logged to history!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRose),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("wear_weekly_outfit_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Confirm Outfit Worn",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        wearSuccessMessage?.let { msg ->
                            Text(
                                text = "✅ $msg",
                                color = Color(0xFF4CAF50),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
