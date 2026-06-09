package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.api.SuggestedOutfit
import com.example.api.WeeklyOutfit
import com.example.data.local.AppDatabase
import com.example.data.model.CalendarEvent
import com.example.data.model.ClothingItem
import com.example.data.model.OutfitHistory
import com.example.data.repository.ClosetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ClosetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ClosetRepository

    // Central database flows
    val allClothing: StateFlow<List<ClothingItem>>
    val allOutfitHistory: StateFlow<List<OutfitHistory>>
    val allCalendarEvents: StateFlow<List<CalendarEvent>>

    // UI Interactive States
    private val _currentLocation = MutableStateFlow("San Francisco, CA")
    val currentLocation = _currentLocation.asStateFlow()

    private val _currentWeatherCondition = MutableStateFlow("Sunny") // Sunny, Cloudy, Rainy, Snowy, Windy
    val currentWeatherCondition = _currentWeatherCondition.asStateFlow()

    private val _currentTempCelsius = MutableStateFlow(24)
    val currentTempCelsius = _currentTempCelsius.asStateFlow()

    private val _currentPrecipitationMm = MutableStateFlow(0.0)
    val currentPrecipitationMm = _currentPrecipitationMm.asStateFlow()

    private val _isFetchingWeather = MutableStateFlow(false)
    val isFetchingWeather = _isFetchingWeather.asStateFlow()

    // Style Personality states
    private val prefs = application.getSharedPreferences("aura_closet_pref", android.content.Context.MODE_PRIVATE)
    private val _stylePersonality = MutableStateFlow<String?>(prefs.getString("style_personality", null))
    val stylePersonality = _stylePersonality.asStateFlow()

    // Quiz states
    // -1: Info/Ready, 0-4: Questions, 5: Finished Result Screen
    private val _quizCurrentQuestionIndex = MutableStateFlow(-1)
    val quizCurrentQuestionIndex = _quizCurrentQuestionIndex.asStateFlow()

    private val _quizScores = MutableStateFlow<Map<String, Int>>(
        mapOf("Minimalist" to 0, "Bohemian" to 0, "Classic" to 0, "Trendy" to 0)
    )
    val quizScores = _quizScores.asStateFlow()

    fun updateStylePersonality(personality: String?) {
        _stylePersonality.value = personality
        if (personality == null) {
            prefs.edit().remove("style_personality").apply()
        } else {
            prefs.edit().putString("style_personality", personality).apply()
        }
        // Regenerate recommendation to align with user's aesthetic personality
        generateAiOutfitSuggestion()
    }

    fun startQuiz() {
        _quizCurrentQuestionIndex.value = 0
        _quizScores.value = mapOf("Minimalist" to 0, "Bohemian" to 0, "Classic" to 0, "Trendy" to 0)
    }

    fun answerQuizQuestion(selectedStyleOption: String) {
        val currentScores = _quizScores.value.toMutableMap()
        currentScores[selectedStyleOption] = (currentScores[selectedStyleOption] ?: 0) + 1
        _quizScores.value = currentScores

        val currentIndex = _quizCurrentQuestionIndex.value
        if (currentIndex < 4) {
            _quizCurrentQuestionIndex.value = currentIndex + 1
        } else {
            // Completed! Resolve personality
            val resolvedStyle = currentScores.maxByOrNull { it.value }?.key ?: "Minimalist"
            updateStylePersonality(resolvedStyle)
            _quizCurrentQuestionIndex.value = 5
        }
    }

    fun previousQuizQuestion() {
        val currentIndex = _quizCurrentQuestionIndex.value
        if (currentIndex > 0) {
            _quizCurrentQuestionIndex.value = currentIndex - 1
        } else {
            _quizCurrentQuestionIndex.value = -1
        }
    }

    fun resetQuiz() {
        _quizCurrentQuestionIndex.value = -1
        _quizScores.value = mapOf("Minimalist" to 0, "Bohemian" to 0, "Classic" to 0, "Trendy" to 0)
    }

    // Suggestion states
    private val _isGeneratingSuggestion = MutableStateFlow(false)
    val isGeneratingSuggestion = _isGeneratingSuggestion.asStateFlow()

    private val _suggestedOutfit = MutableStateFlow<SuggestedOutfit?>(null)
    val suggestedOutfit = _suggestedOutfit.asStateFlow()

    private val _weeklyOutfits = MutableStateFlow<List<WeeklyOutfit>?>(null)
    val weeklyOutfits = _weeklyOutfits.asStateFlow()

    private val _isGeneratingWeekly = MutableStateFlow(false)
    val isGeneratingWeekly = _isGeneratingWeekly.asStateFlow()

    private val _selectedCategoryTab = MutableStateFlow("Tops")
    val selectedCategoryTab = _selectedCategoryTab.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ClosetRepository(database.clothingDao(), database.calendarDao())

        allClothing = repository.allClothing
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allOutfitHistory = repository.allOutfitHistory
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        allCalendarEvents = repository.allEventsFlow
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        // Check and seed initial starter data if closet is empty
        viewModelScope.launch {
            allClothing.collectLatest { list ->
                if (list.isEmpty() && !_isGeneratingSuggestion.value) {
                    seedStarterWardrobe()
                } else if (list.isNotEmpty() && _weeklyOutfits.value == null && !_isGeneratingWeekly.value) {
                    generateWeeklyOutfitPlanner()
                }
            }
        }

        viewModelScope.launch {
            allCalendarEvents.collectLatest { list ->
                if (list.isEmpty()) {
                    seedStarterEvents()
                }
            }
        }
    }

    fun setWeather(condition: String, temp: Int, precipitation: Double = 0.0) {
        _currentWeatherCondition.value = condition
        _currentTempCelsius.value = temp
        _currentPrecipitationMm.value = precipitation
        generateAiOutfitSuggestion()
    }

    fun updateWeatherForLocation(query: String) {
        viewModelScope.launch {
            _isFetchingWeather.value = true
            try {
                val weather = withContext(Dispatchers.IO) {
                    com.example.api.WeatherService.fetchWeatherForLocation(query)
                }
                if (weather != null) {
                    _currentLocation.value = weather.locationName
                    _currentWeatherCondition.value = weather.weatherCondition
                    _currentTempCelsius.value = weather.tempCelsius
                    _currentPrecipitationMm.value = weather.precipitationMm
                    generateAiOutfitSuggestion()
                }
            } catch (e: Exception) {
                android.util.Log.e("ClosetViewModel", "Failed to fetch weather for $query", e)
            } finally {
                _isFetchingWeather.value = false
            }
        }
    }

    fun setSelectedCategoryTab(category: String) {
        _selectedCategoryTab.value = category
    }

    fun addClothingItem(
        name: String,
        category: String,
        color: String,
        season: String,
        warmthLevel: Int,
        presetId: String?,
        imageBytes: ByteArray?
    ) {
        viewModelScope.launch {
            val item = ClothingItem(
                name = name,
                category = category,
                color = color,
                season = season,
                presetId = presetId,
                imageBytes = imageBytes,
                warmthLevel = warmthLevel
            )
            repository.insertClothing(item)
        }
    }

    fun removeClothingItem(item: ClothingItem) {
        viewModelScope.launch {
            repository.deleteClothing(item)
            // Recalculate outfit suggestion if the recommended items got deleted
            val currentRec = _suggestedOutfit.value
            if (currentRec != null && currentRec.itemIds.contains(item.id)) {
                _suggestedOutfit.value = null
            }
        }
    }

    fun addCalendarEvent(title: String, time: String, date: String, eventType: String) {
        viewModelScope.launch {
            val event = CalendarEvent(title = title, time = time, date = date, eventType = eventType)
            repository.insertEvent(event)
        }
    }

    fun removeCalendarEvent(event: CalendarEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    /**
     * Triggers the server-side Gemini suggestion flow based on current wardrobe, weather, and agenda.
     */
    fun generateAiOutfitSuggestion(isAlternative: Boolean = false) {
        viewModelScope.launch {
            _isGeneratingSuggestion.value = true
            try {
                // Get today's active event (or first event in agenda)
                val activeEvent = allCalendarEvents.value.firstOrNull()
                val clothes = allClothing.value

                val result = withContext(Dispatchers.IO) {
                    GeminiClient.suggestOutfit(
                        weatherCondition = _currentWeatherCondition.value,
                        tempCelsius = _currentTempCelsius.value,
                        activeEvent = activeEvent,
                        availableClothes = clothes,
                        stylePersonality = _stylePersonality.value,
                        precipitationMm = _currentPrecipitationMm.value,
                        isAlternative = isAlternative
                    )
                }
                _suggestedOutfit.value = result
            } catch (e: Exception) {
                _suggestedOutfit.value = SuggestedOutfit(
                    explanation = "An error occurred generating suggestions: ${e.message}",
                    outfitName = "Engine Error",
                    itemIds = emptyList(),
                    stylingTips = "Could not reach Aura AI. Ensure your connection is active.",
                    engine = "Aura Error"
                )
            } finally {
                _isGeneratingSuggestion.value = false
            }
        }
    }

    /**
     * Generates a weekly 7-day outfit calendar plan, pulling data from Gemini AI service or falling back locally.
     */
    fun generateWeeklyOutfitPlanner() {
        viewModelScope.launch {
            _isGeneratingWeekly.value = true
            try {
                val events = allCalendarEvents.value
                val clothes = allClothing.value
                val result = withContext(Dispatchers.IO) {
                    GeminiClient.suggestWeeklyOutfits(
                        weatherCondition = _currentWeatherCondition.value,
                        tempCelsius = _currentTempCelsius.value,
                        events = events,
                        availableClothes = clothes,
                        stylePersonality = _stylePersonality.value
                    )
                }
                _weeklyOutfits.value = result
            } catch (e: Exception) {
                android.util.Log.e("ClosetViewModel", "Error in weekly planner", e)
                _weeklyOutfits.value = emptyList()
            } finally {
                _isGeneratingWeekly.value = false
            }
        }
    }

    /**
     * Confirms and logs wearing a weekly planner recommended outfit.
     */
    fun wearWeeklyOutfit(
        outfitItemIds: List<Long>,
        outfitSummary: String,
        dayWeatherCondition: String,
        dayTemp: Int,
        dayAgendaEvent: String
    ) {
        if (outfitItemIds.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val history = OutfitHistory(
                summary = outfitSummary,
                itemIds = outfitItemIds.joinToString(","),
                weatherCondition = dayWeatherCondition,
                tempCelsius = dayTemp,
                agendaEvent = dayAgendaEvent,
                timestamp = now
            )
            repository.insertOutfitHistory(history)

            // Increment wear counts for all selected items
            outfitItemIds.forEach { itemId ->
                repository.recordWear(itemId, now)
            }
        }
    }

    /**
     * Confirms the suggested daily outfit, adding wear records and incrementing wear counts.
     */
    fun confirmSuggestedOutfit(outfit: SuggestedOutfit) {
        if (outfit.itemIds.isEmpty()) return
        
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val activeEvent = allCalendarEvents.value.firstOrNull()
            val eventDesc = activeEvent?.let { "${it.title} (${it.eventType})" } ?: "Casual Day"

            // Insert into local wear history
            val history = OutfitHistory(
                summary = outfit.outfitName,
                itemIds = outfit.itemIds.joinToString(","),
                weatherCondition = _currentWeatherCondition.value,
                tempCelsius = _currentTempCelsius.value,
                agendaEvent = eventDesc,
                timestamp = now
            )

            repository.insertOutfitHistory(history)

            // Increment wear counts for all selected items
            outfit.itemIds.forEach { itemId ->
                repository.recordWear(itemId, now)
            }

            // Unset or refresh suggestion state so they can do a fresh daily picker later
            _suggestedOutfit.value = null
        }
    }

    private suspend fun seedStarterWardrobe() {
        val starters = listOf(
            ClothingItem(name = "Premium Cotton Linen Shirt", category = "Tops", color = "White", season = "Summer", warmthLevel = 2, presetId = "shirt_linen"),
            ClothingItem(name = "Casual Soft Blazer", category = "Tops", color = "Dusk Burgundy", season = "Spring", warmthLevel = 4, presetId = "blazer_casual"),
            ClothingItem(name = "Wool Knit Cozy Sweater", category = "Tops", color = "Oat Sand", season = "Winter", warmthLevel = 5, presetId = "sweater_wool"),
            ClothingItem(name = "Heavy Cotton Oversized T-Shirt", category = "Tops", color = "Black", season = "Summer", warmthLevel = 1, presetId = "tshirt_oversized"),
            
            ClothingItem(name = "Tailored Geometric Chinos", category = "Bottoms", color = "Navy Blue", season = "All", warmthLevel = 3, presetId = "chinos_navy"),
            ClothingItem(name = "Japanese Indigo Jeans", category = "Bottoms", color = "Classic Indigo", season = "All", warmthLevel = 3, presetId = "jeans_indigo"),
            ClothingItem(name = "Tech Utility Cargo Pants", category = "Bottoms", color = "Olive Green", season = "Fall", warmthLevel = 3, presetId = "cargo_tech"),
            ClothingItem(name = "Athletic Fleece Shorts", category = "Bottoms", color = "Heather Grey", season = "Summer", warmthLevel = 1, presetId = "shorts_fleece"),
            
            ClothingItem(name = "Retro Suede Leather Chelsea Boots", category = "Shoes", color = "Tan Brown", season = "Fall", warmthLevel = 4, presetId = "boots_chelsea"),
            ClothingItem(name = "Classic Leather Tennis Sneakers", category = "Shoes", color = "Minimalist Off-White", season = "All", warmthLevel = 2, presetId = "sneakers_leather"),
            ClothingItem(name = "Canvas Court Slip-Ons", category = "Shoes", color = "Coal Black", season = "Summer", warmthLevel = 2, presetId = "shoes_canvas"),

            ClothingItem(name = "Lightweight Raincoat Trench", category = "Outerwear", color = "Classic Khaki", season = "Winter", warmthLevel = 5, presetId = "coat_trench"),
            ClothingItem(name = "Technical Mountain Shell", category = "Outerwear", color = "Charcoal Grey", season = "Fall", warmthLevel = 4, presetId = "windbreaker_tech"),

            ClothingItem(name = "Cashmere Soft Scarf", category = "Accessories", color = "Dark Crimson", season = "Winter", warmthLevel = 4, presetId = "scarf_cashmere"),
            ClothingItem(name = "Hexagon Chronograph Watch", category = "Accessories", color = "Matte Gold Accent", season = "All", warmthLevel =  1, presetId = "watch_chrono")
        )

        starters.forEach { item ->
            repository.insertClothing(item)
        }
    }

    private suspend fun seedStarterEvents() {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(tomorrowCal.time)

        val events = listOf(
            CalendarEvent(title = "Product Strategy & Design Review", time = "10:30 AM", date = todayStr, eventType = "Formal"),
            CalendarEvent(title = "Evening Gym HIIT Session", time = "5:45 PM", date = todayStr, eventType = "Active"),
            CalendarEvent(title = "Brunch Meetup with Olivia", time = "11:00 AM", date = tomorrowStr, eventType = "Casual")
        )

        events.forEach { event ->
            repository.insertEvent(event)
        }
    }
}
