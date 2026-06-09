package com.example.api

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class LocalWeather(
    val locationName: String,
    val tempCelsius: Int,
    val weatherCondition: String,
    val precipitationMm: Double
)

object WeatherService {
    private const val TAG = "WeatherService"
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchWeatherForLocation(query: String): LocalWeather? {
        if (query.isBlank()) return null
        return try {
            // 1. Resolve Location using Open-Meteo Geocoding API
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val geocodingUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedQuery&count=1&language=en"
            
            val geoRequest = Request.Builder()
                .url(geocodingUrl)
                .get()
                .build()

            var lat = 37.7749
            var lon = -122.4194
            var resolvedName = query

            client.newCall(geoRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        val root = JSONObject(body)
                        if (root.has("results")) {
                            val results = root.getJSONArray("results")
                            if (results.length() > 0) {
                                val firstResult = results.getJSONObject(0)
                                lat = firstResult.optDouble("latitude", lat)
                                lon = firstResult.optDouble("longitude", lon)
                                val cityName = firstResult.optString("name", "")
                                val country = firstResult.optString("country", "")
                                resolvedName = if (country.isNotEmpty()) "$cityName, $country" else cityName
                            }
                        }
                    }
                }
            }

            // 2. Fetch current weather from Open-Meteo current weather API
            val forecastUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,precipitation,weather_code"
            val forecastRequest = Request.Builder()
                .url(forecastUrl)
                .get()
                .build()

            client.newCall(forecastRequest).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrEmpty()) {
                        val root = JSONObject(body)
                        val current = root.getJSONObject("current")
                        val temp = current.getDouble("temperature_2m").toInt()
                        val prec = current.optDouble("precipitation", 0.0)
                        val code = current.optInt("weather_code", 0)
                        
                        val condition = when (code) {
                            0 -> "Sunny"
                            1, 2, 3, 45, 48 -> "Cloudy"
                            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82, 95, 96, 99 -> "Rainy"
                            71, 73, 75, 77, 85, 86 -> "Snowy"
                            else -> if (prec > 0.0) "Rainy" else "Cloudy"
                        }

                        Log.d(TAG, "Fetched weather for $resolvedName: $temp°C, $condition, $prec mm")
                        return LocalWeather(
                            locationName = resolvedName,
                            tempCelsius = temp,
                            weatherCondition = condition,
                            precipitationMm = prec
                        )
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather for $query", e)
            null
        }
    }
}
