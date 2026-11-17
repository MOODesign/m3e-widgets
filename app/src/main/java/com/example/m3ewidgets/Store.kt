package com.example.m3ewidgets

import android.content.Context

data class WeatherData(
    val temp: Int,
    val feels: Int,
    val hi: Int,
    val lo: Int,
    val code: Int,
    val isDay: Boolean,
    val updated: Long,
)

/** steps = -1, bpm = -1, sleepMin = -1 mean "not available / no permission". */
data class HealthData(
    val steps: Long,
    val bpm: Int,
    val sleepMin: Int,
    val updated: Long,
)

/** Tiny SharedPreferences cache the widgets read from. */
object Store {
    private fun p(c: Context) = c.getSharedPreferences("m3e_widgets", Context.MODE_PRIVATE)

    fun saveWeather(c: Context, w: WeatherData) = p(c).edit()
        .putInt("w_temp", w.temp).putInt("w_feels", w.feels)
        .putInt("w_hi", w.hi).putInt("w_lo", w.lo)
        .putInt("w_code", w.code).putBoolean("w_day", w.isDay)
        .putLong("w_updated", w.updated).apply()

    fun weather(c: Context): WeatherData? {
        val s = p(c)
        if (!s.contains("w_updated")) return null
        return WeatherData(
            s.getInt("w_temp", 0), s.getInt("w_feels", 0),
            s.getInt("w_hi", 0), s.getInt("w_lo", 0),
            s.getInt("w_code", 0), s.getBoolean("w_day", true),
            s.getLong("w_updated", 0),
        )
    }

    fun saveHealth(c: Context, h: HealthData) = p(c).edit()
        .putLong("h_steps", h.steps).putInt("h_bpm", h.bpm)
        .putInt("h_sleep", h.sleepMin).putLong("h_updated", h.updated).apply()

    fun health(c: Context): HealthData? {
        val s = p(c)
        if (!s.contains("h_updated")) return null
        return HealthData(
            s.getLong("h_steps", -1), s.getInt("h_bpm", -1),
            s.getInt("h_sleep", -1), s.getLong("h_updated", 0),
        )
    }

    fun saveCoords(c: Context, lat: Double, lon: Double) = p(c).edit()
        .putString("lat", lat.toString()).putString("lon", lon.toString()).apply()

    fun coords(c: Context): Pair<Double, Double>? {
        val s = p(c)
        val lat = s.getString("lat", null)?.toDoubleOrNull() ?: return null
        val lon = s.getString("lon", null)?.toDoubleOrNull() ?: return null
        return lat to lon
    }
}
