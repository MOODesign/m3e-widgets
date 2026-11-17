package com.example.m3ewidgets

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Reads from Health Connect. Google Health / Fitbit / Pixel Watch data shows up here
 * once the Google Health (Fitbit) app has "Health Connect" sync switched on.
 */
object Health {

    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND,
    )

    fun isAvailable(c: Context) =
        HealthConnectClient.getSdkStatus(c) == HealthConnectClient.SDK_AVAILABLE

    suspend fun read(c: Context): HealthData? {
        if (!isAvailable(c)) return null
        val client = HealthConnectClient.getOrCreate(c)
        val granted = client.permissionController.getGrantedPermissions()
        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val dayStart = LocalDate.now(zone).atStartOfDay(zone).toInstant()

        var steps = -1L
        if (HealthPermission.getReadPermission(StepsRecord::class) in granted) {
            steps = runCatching {
                client.aggregate(
                    AggregateRequest(
                        setOf(StepsRecord.COUNT_TOTAL),
                        TimeRangeFilter.between(dayStart, now),
                    )
                )[StepsRecord.COUNT_TOTAL] ?: 0L
            }.getOrDefault(-1L)
        }

        var bpm = -1
        if (HealthPermission.getReadPermission(HeartRateRecord::class) in granted) {
            bpm = runCatching {
                client.readRecords(
                    ReadRecordsRequest(
                        HeartRateRecord::class,
                        TimeRangeFilter.between(now.minus(Duration.ofHours(24)), now),
                        ascendingOrder = false,
                        pageSize = 5,
                    )
                ).records
                    .flatMap { it.samples }
                    .maxByOrNull { it.time }
                    ?.beatsPerMinute?.toInt() ?: -1
            }.getOrDefault(-1)
        }

        var sleepMin = -1
        if (HealthPermission.getReadPermission(SleepSessionRecord::class) in granted) {
            // Window: yesterday 18:00 -> now, so last night's sleep is included.
            val sleepStart = LocalDate.now(zone).minusDays(1)
                .atTime(LocalTime.of(18, 0)).atZone(zone).toInstant()
            sleepMin = runCatching {
                client.aggregate(
                    AggregateRequest(
                        setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL),
                        TimeRangeFilter.between(sleepStart, now),
                    )
                )[SleepSessionRecord.SLEEP_DURATION_TOTAL]?.toMinutes()?.toInt() ?: -1
            }.getOrDefault(-1)
        }

        return HealthData(steps, bpm, sleepMin, System.currentTimeMillis())
    }
}
