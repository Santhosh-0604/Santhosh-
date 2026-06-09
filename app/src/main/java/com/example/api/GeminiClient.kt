package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ClothingItem
import com.example.data.model.CalendarEvent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Recommends an outfit based on weather, calendar event, and available clothes list.
     * Returns a Triple of (Explanation/Reasoning, Suggestion title, List of recommended clothing item IDs)
     */
    suspend fun suggestOutfit(
        weatherCondition: String,
        tempCelsius: Int,
        activeEvent: CalendarEvent?,
        availableClothes: List<ClothingItem>,
        stylePersonality: String? = null,
        precipitationMm: Double? = null,
        isAlternative: Boolean = false
    ): SuggestedOutfit {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        // If API key is not configured or placeholder, use localized smart fallback
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "API Key is empty or placeholder, using offline intelligence engine")
            return suggestOutfitLocalFallback(weatherCondition, tempCelsius, activeEvent, availableClothes, "Offline Engine", stylePersonality, precipitationMm, isAlternative)
        }

        if (availableClothes.isEmpty()) {
            return SuggestedOutfit(
                explanation = "Your closet is empty. Digitize some clothes first!",
                outfitName = "No items available",
                itemIds = emptyList(),
                stylingTips = "Press the floating camera button to add clothes with photos or presets.",
                engine = "Aura Engine"
            )
        }

        try {
            // Prepare wardrobe summary for the prompt
            val clothingArray = JSONArray()
            availableClothes.forEach { item ->
                val clothingObj = JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("category", item.category)
                    put("color", item.color)
                    put("season", item.season)
                    put("wearCount", item.wearCount)
                    put("warmthLevel", item.warmthLevel)
                }
                clothingArray.put(clothingObj)
            }

            val eventStr = activeEvent?.let { "${it.title} (${it.eventType})" } ?: "No plans today (Casual)"
            
            val personalityContext = if (!stylePersonality.isNullOrEmpty()) {
                "User Style Personality Focus: $stylePersonality. Curate an assembly embodying this personality (Minimalist favors sleekness and monochrome; Bohemian likes loose folds and warmth; Classic likes elegant tailoring and structured clean looks; Trendy loves oversized silhouettes/cargo and bold cuts)."
            } else {
                ""
            }

            val weatherDetails = if (precipitationMm != null && precipitationMm > 0.0) {
                "$weatherCondition, $tempCelsius°C, Precipitation: $precipitationMm mm (wet weather conditions active)"
            } else {
                "$weatherCondition, $tempCelsius°C"
            }

            val alternativeContext = if (isAlternative) {
                "Note: This is an ALTERNATIVE suggestion request. DO NOT recommend the same combination as previous picks. Recommend a different, highly distinct outfit style, color combination, or layer coordination from the available wardrobe items for today's context."
            } else {
                ""
            }

            val prompt = """
                Recommend the single best clothing combination (outfit) for today.
                
                Context:
                - Weather: $weatherDetails
                - Calendar Event: $eventStr
                $personalityContext
                $alternativeContext
                - Available Wardrobe: $clothingArray

                Requirements:
                1. Select items *strictly* from the available wardrobe. Try to pair:
                   - Exactly one 'Tops'
                   - Exactly one 'Bottoms'
                   - Exactly one 'Shoes'
                   - Optionally one 'Outerwear' (recommended if temp is cold, e.g., below 18°C)
                   - Optionally one 'Accessories'
                2. Balance the wear count: try to pick under-utilized garments (lower wearCount) if they fit the weather/event to improve closet utilization.
                3. Return your response as a strict JSON object with this shape:
                   {
                     "explanation": "A stylish explanation of why this outfit was selected, matching the weather and event",
                     "outfitName": "The name of the outfit combination",
                     "selectedItemIds": [list of numbers corresponding to selected garment IDs],
                     "stylingTips": "One direct styling tip or aura note"
                   }
                Do not include markdown or backticks (no ```json ... ``` tags). Code ONLY.
            """.trimIndent()

            // Construct payload manually for bulletproof compatibility
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", if (isAlternative) 0.8 else 0.3)
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed: ${response.code} ${response.message}")
                    return suggestOutfitLocalFallback(weatherCondition, tempCelsius, activeEvent, availableClothes, "Aura Local (API error)", stylePersonality, precipitationMm, isAlternative)
                }

                val responseBodyStr = response.body?.string() ?: ""
                Log.d(TAG, "Gemini raw response: $responseBodyStr")

                val candidateObj = JSONObject(responseBodyStr)
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                
                val textResponse = candidateObj.getString("text").trim()
                
                // Clean markdown response wrappers in case Gemini ignores JSON response format directive
                val cleanedJson = if (textResponse.startsWith("```")) {
                    textResponse.substringAfter("json").substringBefore("```").trim()
                } else {
                    textResponse
                }

                val outfitJson = JSONObject(cleanedJson)
                val explanation = outfitJson.getString("explanation")
                val outfitName = outfitJson.getString("outfitName")
                val itemIdsJson = outfitJson.getJSONArray("selectedItemIds")
                val stylingTips = outfitJson.optString("stylingTips", "Accessorize to express your unique aura.")

                val selectedIds = mutableListOf<Long>()
                for (i in 0 until itemIdsJson.length()) {
                    selectedIds.add(itemIdsJson.getLong(i))
                }

                return SuggestedOutfit(
                    explanation = explanation,
                    outfitName = outfitName,
                    itemIds = selectedIds,
                    stylingTips = stylingTips,
                    engine = if (isAlternative) "Aura Alternate AI" else "Aura AI"
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Gemini API error, falling back locally", e)
            return suggestOutfitLocalFallback(weatherCondition, tempCelsius, activeEvent, availableClothes, "Aura Local (Offline fallback)", stylePersonality, precipitationMm, isAlternative)
        }
    }

    /**
     * High-fidelity rules-based offline outfit recommendation engine
     */
    private fun suggestOutfitLocalFallback(
        weatherCondition: String,
        tempCelsius: Int,
        activeEvent: CalendarEvent?,
        availableClothes: List<ClothingItem>,
        engineName: String,
        stylePersonality: String? = null,
        precipitationMm: Double? = null,
        isAlternative: Boolean = false
    ): SuggestedOutfit {
        if (availableClothes.isEmpty()) {
            return SuggestedOutfit(
                explanation = "Your closet is empty. Tap the floating button to add some fashion pieces!",
                outfitName = "Empty Wardrobe",
                itemIds = emptyList(),
                stylingTips = "Add a mix of Tops, Bottoms, and Shoes to unlock smart coordination.",
                engine = engineName
            )
        }

        // Categorize items
        val tops = availableClothes.filter { it.category.equals("Tops", ignoreCase = true) }
        val bottoms = availableClothes.filter { it.category.equals("Bottoms", ignoreCase = true) }
        val shoes = availableClothes.filter { it.category.equals("Shoes", ignoreCase = true) }
        val outerwear = availableClothes.filter { it.category.equals("Outerwear", ignoreCase = true) }
        val accessories = availableClothes.filter { it.category.equals("Accessories", ignoreCase = true) }

        if (tops.isEmpty() || bottoms.isEmpty() || shoes.isEmpty()) {
            // Pick whatever exists
            val selected = mutableListOf<Long>()
            val descParts = mutableListOf<String>()
            
            tops.firstOrNull()?.let { selected.add(it.id); descParts.add(it.name) }
            bottoms.firstOrNull()?.let { selected.add(it.id); descParts.add(it.name) }
            shoes.firstOrNull()?.let { selected.add(it.id); descParts.add(it.name) }

            return SuggestedOutfit(
                explanation = "A minimal offline preset combination. Add more Tops, Bottoms, and Shoes for optimized coords.",
                outfitName = if (descParts.isNotEmpty()) descParts.joinToString(" & ") else "Starter Coord",
                itemIds = selected,
                stylingTips = "Digitize at least one item in Tops, Bottoms, and Shoes categories to enable full ensemble recommendations.",
                engine = engineName
            )
        }

        // 1. Scoring logic to choose best candidates
        // We evaluate: Warmth match (temp), Style match (event), and Wear balance (low wear count)
        val eventType = activeEvent?.eventType ?: "Casual"

        // Helper to score an item
        fun scoreItem(item: ClothingItem): Double {
            var score = 100.0
            
            // A: Warmth check
            // If cold (< 15), prefer high warmth items (4, 5). If warm (> 24), prefer cool items (1, 2)
            val idealWarmth = when {
                tempCelsius < 12 -> 5
                tempCelsius < 18 -> 4
                tempCelsius < 23 -> 3
                tempCelsius < 28 -> 2
                else -> 1
            }
            val warmthDiff = Math.abs(item.warmthLevel - idealWarmth)
            score -= (warmthDiff * 20.0) // Deduct 20 points per deviation

            // B: Stylistic event match
            val isFormalEvent = eventType.equals("Formal", ignoreCase = true)
            val isActiveEvent = eventType.equals("Active", ignoreCase = true)
            val isPartyEvent = eventType.equals("Party", ignoreCase = true)

            val nameLower = item.name.lowercase()
            if (isFormalEvent) {
                if (nameLower.contains("suit") || nameLower.contains("blazer") || nameLower.contains("shirt") || nameLower.contains("chino") || nameLower.contains("dress")) {
                    score += 50.0
                }
            } else if (isActiveEvent) {
                if (nameLower.contains("sport") || nameLower.contains("run") || nameLower.contains("t-shirt") || nameLower.contains("track") || nameLower.contains("sneaker") || nameLower.contains("shorts")) {
                    score += 50.0
                }
            } else if (isPartyEvent) {
                if (nameLower.contains("designer") || nameLower.contains("fancy") || nameLower.contains("leather") || nameLower.contains("jeans") || nameLower.contains("heels")) {
                    score += 50.0
                }
            }

            // C: Balance wear count
            // Deduct score for highly worn items to promote unused items
            score -= (item.wearCount * 3.0)

            // D: Style Personality score booster
            if (stylePersonality != null) {
                val styleLower = stylePersonality.lowercase()
                if (styleLower == "minimalist") {
                    if (nameLower.contains("clean") || nameLower.contains("plain") || nameLower.contains("simple") || nameLower.contains("minimal") || nameLower.contains("linen")) {
                        score += 30.0
                    }
                } else if (styleLower == "bohemian") {
                    if (nameLower.contains("cozy") || nameLower.contains("wool") || nameLower.contains("soft") || nameLower.contains("knit") || nameLower.contains("warmth")) {
                        score += 30.0
                    }
                } else if (styleLower == "classic") {
                    if (nameLower.contains("premium") || nameLower.contains("tailored") || nameLower.contains("chelsea") || nameLower.contains("leather") || nameLower.contains("classic")) {
                        score += 30.0
                    }
                } else if (styleLower == "trendy") {
                    if (nameLower.contains("oversized") || nameLower.contains("heavy") || nameLower.contains("cargo") || nameLower.contains("tech")) {
                        score += 30.0
                    }
                }
            }

            // E: Precipitation adaptability check
            if (precipitationMm != null && precipitationMm > 0.0) {
                if (item.category.equals("Outerwear", ignoreCase = true)) {
                    if (nameLower.contains("rain") || nameLower.contains("hood") || nameLower.contains("windbreaker") || nameLower.contains("water") || nameLower.contains("jacket") || nameLower.contains("denim")) {
                        score += 40.0
                    }
                }
                if (item.category.equals("Shoes", ignoreCase = true)) {
                    // Boots or water-resistant shoes get a boost
                    if (nameLower.contains("boot") || nameLower.contains("leather") || nameLower.contains("trainer") || nameLower.contains("sneaker")) {
                        score += 35.0
                    } else if (nameLower.contains("sandal") || nameLower.contains("canvas") || nameLower.contains("heel")) {
                        score -= 30.0 // reduce score for sandals/heels in wet conditions
                    }
                }
                if (item.category.equals("Accessories", ignoreCase = true)) {
                    if (nameLower.contains("umbrella") || nameLower.contains("cap") || nameLower.contains("hat") || nameLower.contains("scarf")) {
                        score += 30.0
                    }
                }
            }

            return score
        }

        // Best picks corresponding to scores
        val bestTop = if (isAlternative && tops.size > 1) {
            tops.sortedByDescending { scoreItem(it) }[1]
        } else {
            tops.maxByOrNull { scoreItem(it) }!!
        }

        val bestBottom = if (isAlternative && bottoms.size > 1) {
            bottoms.sortedByDescending { scoreItem(it) }[1]
        } else {
            bottoms.maxByOrNull { scoreItem(it) }!!
        }

        val bestShoes = if (isAlternative && shoes.size > 1) {
            shoes.sortedByDescending { scoreItem(it) }[1]
        } else {
            shoes.maxByOrNull { scoreItem(it) }!!
        }

        val selectedItems = mutableListOf<ClothingItem>().apply {
            add(bestTop)
            add(bestBottom)
            add(bestShoes)
        }

        // High warmth or precipitation protection outerwear option
        var pickedOuterwear: ClothingItem? = null
        val needsOuter = tempCelsius < 18 || (precipitationMm != null && precipitationMm > 0.0)
        if (needsOuter && outerwear.isNotEmpty()) {
            val bestOuter = outerwear.maxByOrNull { scoreItem(it) }!!
            selectedItems.add(bestOuter)
            pickedOuterwear = bestOuter
        }

        // Casual accessory or rain guard option
        var pickedAccessory: ClothingItem? = null
        val wantsAccessory = (precipitationMm != null && precipitationMm > 0.0) || (accessories.isNotEmpty() && Math.random() < 0.6)
        if (wantsAccessory && accessories.isNotEmpty()) {
            val bestAccessory = accessories.maxByOrNull { scoreItem(it) }!!
            selectedItems.add(bestAccessory)
            pickedAccessory = bestAccessory
        }

        val nameDesc = buildString {
            append("${bestTop.name} & ${bestBottom.name}")
            if (pickedOuterwear != null) {
                append(" paired with ${pickedOuterwear.name}")
            }
            if (pickedAccessory != null && pickedAccessory.name.lowercase().contains("umbrella")) {
                append(" and ${pickedAccessory.name}")
            }
        }

        val explanation = buildString {
            append("An exquisite offline choice for today's weather of $tempCelsius°C ")
            if (precipitationMm != null && precipitationMm > 0.0) {
                append("with active precipitation ($precipitationMm mm) ")
            }
            append("and your ")
            if (activeEvent != null) {
                append("agenda item, '${activeEvent.title}'. ")
            } else {
                append("relaxed day. ")
            }
            append("This outfit pairs the ${bestTop.name} with ${bestBottom.name} ")
            if (pickedOuterwear != null && precipitationMm != null && precipitationMm > 0.0) {
                append("along with ${pickedOuterwear.name} to buffer against the elements, ")
            }
            append("to maximize ")
            if (bestTop.wearCount > 3 || bestBottom.wearCount > 3) {
                append("comfort.")
            } else {
                append("your wardrobe's untapped potential.")
            }
        }

        val tip = when {
            precipitationMm != null && precipitationMm > 0.0 -> {
                "Rain or moisture detected ($precipitationMm mm). Prioritize water-resistant footwear and keep your umbrella close by."
            }
            tempCelsius < 15 -> "Perfectly cozy! Stay bundled in the cooler weather with structured layering."
            tempCelsius > 25 -> "Super lightweight. Keep accessories minimal to beat the warm sun."
            eventType.equals("Formal", ignoreCase = true) -> "A structured outfit designed to leave an impact. Stay graceful!"
            eventType.equals("Active", ignoreCase = true) -> "Optimized for maximum mobility and athletic styling."
            else -> "Classic casual balance. Roll up your sleeves slightly for an effortless geometric line styling."
        }

        return SuggestedOutfit(
            explanation = explanation,
            outfitName = nameDesc,
            itemIds = selectedItems.map { it.id },
            stylingTips = tip,
            engine = engineName
        )
    }

    /**
     * Recommends outfits for the entire week (next 7 days starting today) based on weather and calendar events.
     */
    suspend fun suggestWeeklyOutfits(
        weatherCondition: String,
        tempCelsius: Int,
        events: List<CalendarEvent>,
        availableClothes: List<ClothingItem>,
        stylePersonality: String? = null
    ): List<WeeklyOutfit> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        // If API key is not configured or placeholder, use localized fallback
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "API Key is empty or placeholder, using offline intelligence engine for weekly planner")
            return suggestWeeklyOutfitsLocalFallback(weatherCondition, tempCelsius, events, availableClothes, "Offline Engine", stylePersonality)
        }

        if (availableClothes.isEmpty()) {
            return emptyList()
        }

        try {
            val clothingArray = JSONArray()
            availableClothes.forEach { item ->
                clothingArray.put(JSONObject().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("category", item.category)
                    put("color", item.color)
                    put("season", item.season)
                    put("wearCount", item.wearCount)
                    put("warmthLevel", item.warmthLevel)
                })
            }

            // Generateforecast descriptions for the week
            val daysInfo = getNext7Days(weatherCondition, tempCelsius)
            val forecastBuilder = StringBuilder()
            daysInfo.forEachIndexed { index, day ->
                val dayEvents = events.filter { isEventForDay(it, day.date, day.dayName, index) }
                val eventStr = if (dayEvents.isNotEmpty()) {
                    dayEvents.joinToString(", ") { "${it.title} (${it.eventType})" }
                } else {
                    "No plans scheduled (Casual)"
                }
                forecastBuilder.append("Day $index: ${day.dayName} (${day.date}) - Weather: ${day.weatherCondition}, ${day.tempCelsius}°C. Agenda: $eventStr\n")
            }

            val personalityContext = if (!stylePersonality.isNullOrEmpty()) {
                "User Style Personality Focus: $stylePersonality. Curate stylish matching pieces embodying this aesthetic style preference (Minimalist, Bohemian, Classic, Trendy)."
            } else {
                ""
            }

            val prompt = """
                You are a premium AI fashion stylist from Aura Closet.
                Generate a beautiful, personalized, and highly unified 7-day weekly outfit calendar program based on the available closet items, the upcoming weather, and the scheduled events for the upcoming week.
                
                Strict Constraint: You MUST recommend a unique outfit combination for each of the next 7 days starting from today.
                
                Style Personality Focus: ${stylePersonality ?: "Classic/Minimalist"}. Ensure all 7 outfit assemblies embody this aesthetic style.
                $personalityContext

                Available Wardrobe Cabinet: $clothingArray
                
                Weekly Forecast and Agenda:
                $forecastBuilder
                
                For each of the 7 days (numbered 0 to 6), return your recommendation as elements of a strict JSON object with this exact shape:
                {
                  "weeklyPlanner": [
                    {
                      "date": "yyyy-MM-dd formatted date matching the day",
                      "dayName": "3-letter day name matching the day",
                      "outfitName": "The aesthetic name of this daily ensemble",
                      "explanation": "A chic, visual stylist's explanation of why this outfit fits this day's weather, temperature, and scheduled event.",
                      "selectedItemIds": [list of numbers corresponding to selected garment IDs],
                      "stylingTips": "One highly actionable styling tip or visual alignment guidance for this specific coord.",
                      "weatherCondition": "The predicted weather condition provided in forecast",
                      "tempCelsius": tempCelsiusNumberProvidedInForecast
                    }
                  ]
                }
                
                Requirements:
                1. Select items *strictly* from the available wardrobe. Try to pair:
                   - Exactly one 'Tops'
                   - Exactly one 'Bottoms'
                   - Exactly one 'Shoes'
                   - Optionally one 'Outerwear' (recommended if temp is cold, e.g., below 18°C)
                   - Optionally one 'Accessories'
                2. Do not recommend the exact same outfit on multiple consecutive days. Ensure variety and rotation across the week, balancing lower wearCount items.
                3. Do not include markdown or backticks (no ```json ... ``` tags). Output JSON code ONLY.
            """.trimIndent()

            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed: ${response.code} ${response.message}")
                    return suggestWeeklyOutfitsLocalFallback(weatherCondition, tempCelsius, events, availableClothes, "Aura Local (API error)", stylePersonality)
                }

                val responseBodyStr = response.body?.string() ?: ""
                Log.d(TAG, "Gemini weekly raw response: $responseBodyStr")

                val candidateObj = JSONObject(responseBodyStr)
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                
                val textResponse = candidateObj.getString("text").trim()
                
                val cleanedJson = if (textResponse.startsWith("```")) {
                    textResponse.substringAfter("json").substringBefore("```").trim()
                } else {
                    textResponse
                }

                val plannerArray = JSONObject(cleanedJson).getJSONArray("weeklyPlanner")
                val recommendations = mutableListOf<WeeklyOutfit>()
                
                for (i in 0 until plannerArray.length()) {
                    val dayObj = plannerArray.getJSONObject(i)
                    val date = dayObj.getString("date")
                    val dayName = dayObj.getString("dayName")
                    val outfitName = dayObj.getString("outfitName")
                    val explanation = dayObj.getString("explanation")
                    val stylingTips = dayObj.optString("stylingTips", "Accessorize to express your unique aura.")
                    
                    val itemIdsJson = dayObj.getJSONArray("selectedItemIds")
                    val selectedIds = mutableListOf<Long>()
                    for (j in 0 until itemIdsJson.length()) {
                        selectedIds.add(itemIdsJson.getLong(j))
                    }
                    
                    val condition = dayObj.optString("weatherCondition", weatherCondition)
                    val temp = dayObj.optInt("tempCelsius", tempCelsius)
                    
                    recommendations.add(
                        WeeklyOutfit(
                            date = date,
                            dayName = dayName,
                            outfitName = outfitName,
                            explanation = explanation,
                            itemIds = selectedIds,
                            stylingTips = stylingTips,
                            tempCelsius = temp,
                            weatherCondition = condition,
                            engine = "Aura AI"
                        )
                    )
                }
                return recommendations
            }

        } catch (e: Exception) {
            Log.e(TAG, "Gemini API weekly error, falling back locally", e)
            return suggestWeeklyOutfitsLocalFallback(weatherCondition, tempCelsius, events, availableClothes, "Aura Local (Offline fallback)", stylePersonality)
        }
    }

    /**
     * High-fidelity rules-based offline outfit recommendation engine for the entire week.
     */
    fun suggestWeeklyOutfitsLocalFallback(
        weatherCondition: String,
        tempCelsius: Int,
        events: List<CalendarEvent>,
        availableClothes: List<ClothingItem>,
        engineName: String,
        stylePersonality: String? = null
    ): List<WeeklyOutfit> {
        val days = getNext7Days(weatherCondition, tempCelsius)
        val recommendations = mutableListOf<WeeklyOutfit>()
        
        // Track garment usages to balance rotation and prevent repeats
        val categoryUsedCounts = mutableMapOf<Long, Int>()
        
        days.forEachIndexed { index, day ->
            val dayEvents = events.filter { isEventForDay(it, day.date, day.dayName, index) }
            val activeEvent = dayEvents.firstOrNull()
            
            // Categorize items
            val tops = availableClothes.filter { it.category.equals("Tops", ignoreCase = true) }
            val bottoms = availableClothes.filter { it.category.equals("Bottoms", ignoreCase = true) }
            val shoes = availableClothes.filter { it.category.equals("Shoes", ignoreCase = true) }
            val outerwear = availableClothes.filter { it.category.equals("Outerwear", ignoreCase = true) }
            val accessories = availableClothes.filter { it.category.equals("Accessories", ignoreCase = true) }
            
            if (tops.isEmpty() || bottoms.isEmpty() || shoes.isEmpty()) {
                recommendations.add(
                    WeeklyOutfit(
                        date = day.date,
                        dayName = day.dayName,
                        outfitName = "Starter Ensemble",
                        explanation = "A clean minimal wardrobe combination for your day. Add more Tops, Bottoms, and Shoes to unlock smart coordination.",
                        itemIds = availableClothes.take(3).map { it.id },
                        stylingTips = "Digitize more items to enable full coordinate recommendations.",
                        tempCelsius = day.tempCelsius,
                        weatherCondition = day.weatherCondition,
                        engine = engineName
                    )
                )
                return@forEachIndexed
            }
            
            val eventType = activeEvent?.eventType ?: "Casual"
            
            // Score items, accounting for warmth match, event type, style personality, and penalty for repeating items too much
            fun scoreItem(item: ClothingItem): Double {
                var score = 100.0
                
                // A: Warmth check
                val idealWarmth = when {
                    day.tempCelsius < 12 -> 5
                    day.tempCelsius < 18 -> 4
                    day.tempCelsius < 23 -> 3
                    day.tempCelsius < 28 -> 2
                    else -> 1
                }
                val warmthDiff = Math.abs(item.warmthLevel - idealWarmth)
                score -= (warmthDiff * 25.0)
                
                // B: Stylistic event match
                val isFormalEvent = eventType.equals("Formal", ignoreCase = true)
                val isActiveEvent = eventType.equals("Active", ignoreCase = true)
                val isPartyEvent = eventType.equals("Party", ignoreCase = true)
                
                val nameLower = item.name.lowercase()
                if (isFormalEvent) {
                    if (nameLower.contains("suit") || nameLower.contains("blazer") || nameLower.contains("shirt") || nameLower.contains("chino") || nameLower.contains("dress")) {
                        score += 50.0
                    }
                } else if (isActiveEvent) {
                    if (nameLower.contains("sport") || nameLower.contains("run") || nameLower.contains("t-shirt") || nameLower.contains("track") || nameLower.contains("sneaker") || nameLower.contains("shorts")) {
                        score += 50.0
                    }
                } else if (isPartyEvent) {
                    if (nameLower.contains("designer") || nameLower.contains("fancy") || nameLower.contains("leather") || nameLower.contains("jeans") || nameLower.contains("heels")) {
                        score += 50.0
                    }
                }
                
                // C: Wear count balance
                score -= (item.wearCount * 2.0)
                
                // D: Repeated usage penalty in this weekly planner
                val timesUsed = categoryUsedCounts[item.id] ?: 0
                score -= (timesUsed * 35.0) // Strong penalty to promote variation across the week!
                
                // E: Style Personality score booster
                if (stylePersonality != null) {
                    val styleLower = stylePersonality.lowercase()
                    if (styleLower == "minimalist") {
                        if (nameLower.contains("clean") || nameLower.contains("plain") || nameLower.contains("simple") || nameLower.contains("minimal") || nameLower.contains("linen")) {
                            score += 30.0
                        }
                    } else if (styleLower == "bohemian") {
                        if (nameLower.contains("cozy") || nameLower.contains("wool") || nameLower.contains("soft") || nameLower.contains("knit") || nameLower.contains("warmth")) {
                            score += 30.0
                        }
                    } else if (styleLower == "classic") {
                        if (nameLower.contains("premium") || nameLower.contains("tailored") || nameLower.contains("chelsea") || nameLower.contains("leather") || nameLower.contains("classic")) {
                            score += 30.0
                        }
                    } else if (styleLower == "trendy") {
                        if (nameLower.contains("oversized") || nameLower.contains("heavy") || nameLower.contains("cargo") || nameLower.contains("tech")) {
                            score += 30.0
                        }
                    }
                }
                
                return score
            }
            
            val bestTop = tops.maxByOrNull { scoreItem(it) }!!
            categoryUsedCounts[bestTop.id] = (categoryUsedCounts[bestTop.id] ?: 0) + 1
            
            val bestBottom = bottoms.maxByOrNull { scoreItem(it) }!!
            categoryUsedCounts[bestBottom.id] = (categoryUsedCounts[bestBottom.id] ?: 0) + 1
            
            val bestShoes = shoes.maxByOrNull { scoreItem(it) }!!
            categoryUsedCounts[bestShoes.id] = (categoryUsedCounts[bestShoes.id] ?: 0) + 1
            
            val selectedItems = mutableListOf<ClothingItem>().apply {
                add(bestTop)
                add(bestBottom)
                add(bestShoes)
            }
            
            var pickedOuterwear: ClothingItem? = null
            if (day.tempCelsius < 18 && outerwear.isNotEmpty()) {
                val bestOuter = outerwear.maxByOrNull { scoreItem(it) }!!
                selectedItems.add(bestOuter)
                categoryUsedCounts[bestOuter.id] = (categoryUsedCounts[bestOuter.id] ?: 0) + 1
                pickedOuterwear = bestOuter
            }
            
            var pickedAccessory: ClothingItem? = null
            if (accessories.isNotEmpty() && Math.random() < 0.6) {
                val bestAccessory = accessories.maxByOrNull { scoreItem(it) }!!
                selectedItems.add(bestAccessory)
                categoryUsedCounts[bestAccessory.id] = (categoryUsedCounts[bestAccessory.id] ?: 0) + 1
                pickedAccessory = bestAccessory
            }
            
            val nameDesc = buildString {
                append("${bestTop.name} & ${bestBottom.name}")
                if (pickedOuterwear != null) {
                    append(" with ${pickedOuterwear.name}")
                }
            }
            
            val explanation = buildString {
                append("An active coordination chosen for ${day.dayName}'s weather of ${day.tempCelsius}°C (${day.weatherCondition}). ")
                if (activeEvent != null) {
                    append("Perfectly matches your scheduled plans: '${activeEvent.title}'. ")
                } else {
                    append("Ideal for a balanced, styled ${day.dayName}. ")
                }
                append("The styling anchors the ${bestTop.name} and ${bestBottom.name} to maximize ")
                if (selectedItems.any { it.wearCount == 0 }) {
                    append("closet rotation and uncover unworn elements.")
                } else {
                    append("personal aesthetic elegance.")
                }
            }
            
            val tip = when {
                day.tempCelsius < 15 -> "Perfectly styled for cooler temps! Bundle with modern, comfortable layering."
                day.tempCelsius > 25 -> "Chic summer aesthetic. Keep lines minimal and breathable to handle the heat."
                eventType.equals("Formal", ignoreCase = true) -> "A structured formal assembly designed to elevate presence."
                eventType.equals("Active", ignoreCase = true) -> "Active style composition prioritizing performance and mobility."
                else -> "Relaxed silhouette lines. Roll up cuffs or tuck hem slightly for an artistic visual line."
            }
            
            recommendations.add(
                WeeklyOutfit(
                    date = day.date,
                    dayName = day.dayName,
                    outfitName = nameDesc,
                    explanation = explanation,
                    itemIds = selectedItems.map { it.id },
                    stylingTips = tip,
                    tempCelsius = day.tempCelsius,
                    weatherCondition = day.weatherCondition,
                    engine = engineName
                )
            )
        }
        
        return recommendations
    }

    /**
     * Generates weather condition and temperature for the upcoming 7 days, anchored by today's local climate.
     */
    fun getNext7Days(baseCondition: String, baseTemp: Int): List<DayInfo> {
        val list = mutableListOf<DayInfo>()
        val cal = Calendar.getInstance()
        
        val conditions = listOf("Sunny", "Cloudy", "Rainy", "Windy", "Sunny", "Cloudy", "Rainy")
        
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfDay = SimpleDateFormat("EEE", Locale.getDefault())
        
        for (i in 0 until 7) {
            val dateStr = sdfDate.format(cal.time)
            val dayName = sdfDay.format(cal.time)
            
            // Natural dynamic temperature drift
            val tempVar = when (i) {
                0 -> 0
                1 -> if (baseTemp > 20) -2 else 2
                2 -> if (baseTemp > 25) -3 else 1
                3 -> if (baseTemp < 10) 3 else -1
                4 -> 2
                5 -> -2
                else -> 1
            }
            val nextTemp = (baseTemp + tempVar).coerceIn(-5, 40)
            
            val nextCondition = if (i == 0) {
                baseCondition
            } else {
                val baseIndex = conditions.indexOf(baseCondition).coerceAtLeast(0)
                conditions[(baseIndex + i) % conditions.size]
            }
            
            list.add(DayInfo(dateStr, dayName, nextCondition, nextTemp))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    /**
     * Matches scheduled event date with upcoming target date.
     */
    fun isEventForDay(event: CalendarEvent, dateStr: String, dayName: String, relativeIndex: Int): Boolean {
        val cleanDateInput = event.date.lowercase().trim()
        val cleanTargetDate = dateStr.lowercase().trim()
        
        if (relativeIndex == 0 && (cleanDateInput == "today" || cleanDateInput == "today's")) return true
        if (relativeIndex == 1 && (cleanDateInput == "tomorrow")) return true
        
        return cleanDateInput.contains(cleanTargetDate) || cleanDateInput == cleanTargetDate
    }
}

/**
 * Result wrapper for Daily pick recommendation
 */
data class SuggestedOutfit(
    val explanation: String,
    val outfitName: String,
    val itemIds: List<Long>,
    val stylingTips: String,
    val engine: String // "Aura AI" or "Aura Local" for beautiful user feedback
)

/**
 * Result wrapper for Weekly Outfit Planner Day
 */
data class WeeklyOutfit(
    val date: String,             // "2026-06-09"
    val dayName: String,          // "Mon", "Tue"
    val outfitName: String,
    val explanation: String,
    val itemIds: List<Long>,
    val stylingTips: String,
    val tempCelsius: Int,
    val weatherCondition: String,
    val engine: String
)

/**
 * Helper container for week weather info
 */
data class DayInfo(
    val date: String,
    val dayName: String,
    val weatherCondition: String,
    val tempCelsius: Int
)
