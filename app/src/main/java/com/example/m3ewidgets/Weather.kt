package com.example.m3ewidgets

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

/** Open-Meteo: free, no API key. Pixel Weather exposes no public API to other apps. */
object Weather {

    // Used only if no location has ever been available. Change to your city.
    private const val DEFAULT_LAT = 30.0444
    private const val DEFAULT_LON = 31.2357

    suspend fun fetch(lat: Double, lon: Double): WeatherData = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$lat&longitude=$lon" +
            "&current=temperature_2m,apparent_temperature,weather_code,is_day" +
            "&daily=temperature_2m_max,temperature_2m_min" +
            "&timezone=auto&forecast_days=1"
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        try {
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val cur = json.getJSONObject("current")
            val daily = json.getJSONObject("daily")
            WeatherData(
                temp = cur.getDouble("temperature_2m").roundToInt(),
                feels = cur.getDouble("apparent_temperature").roundToInt(),
                hi = daily.getJSONArray("temperature_2m_max").getDouble(0).roundToInt(),
                lo = daily.getJSONArray("temperature_2m_min").getDouble(0).roundToInt(),
                code = cur.getInt("weather_code"),
                isDay = cur.getInt("is_day") == 1,
                updated = System.currentTimeMillis(),
            )
        } finally {
            conn.disconnect()
        }
    }

    /** Best available coordinates: live last-known fix, else cached, else default. */
    fun bestCoords(c: Context): Pair<Double, Double> =
        lastKnown(c) ?: Store.coords(c) ?: (DEFAULT_LAT to DEFAULT_LON)

    /** Call from the foreground (MainActivity) so background syncs have a cached fix. */
    fun cacheNow(c: Context) {
        lastKnown(c)?.let { Store.saveCoords(c, it.first, it.second) }
    }

    private fun lastKnown(c: Context): Pair<Double, Double>? {
        if (ContextCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return null
        val lm = c.getSystemService(LocationManager::class.java)
        return try {
            lm.getProviders(true)
                .mapNotNull { lm.getLastKnownLocation(it) }
                .maxByOrNull { it.time }
                ?.let { it.latitude to it.longitude }
        } catch (e: SecurityException) {
            null
        }
    }

    fun describe(code: Int, isDay: Boolean): Pair<String, String> = when (code) {
        0 -> (if (isDay) "☀️" else "🌙") to "Clear"
        1 -> (if (isDay) "🌤️" else "🌙") to "Mostly clear"
        2 -> "⛅" to "Partly cloudy"
        3 -> "☁️" to "Overcast"
        45, 48 -> "🌫️" to "Fog"
        in 51..57 -> "🌦️" to "Drizzle"
        in 61..67 -> "🌧️" to "Rain"
        in 71..77 -> "❄️" to "Snow"
        in 80..82 -> "🌧️" to "Showers"
        85, 86 -> "🌨️" to "Snow showers"
        in 95..99 -> "⛈️" to "Thunderstorm"
        else -> "🌡️" to "Weather"
    }
}
