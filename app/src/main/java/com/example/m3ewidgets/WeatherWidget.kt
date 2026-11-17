package com.example.m3ewidgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding

class WeatherWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = Store.weather(context)
        provideContent { GlanceTheme { WeatherContent(data) } }
    }
}

class WeatherWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        SyncWorker.schedule(context)
        SyncWorker.syncNow(context)
    }
}

@Composable
private fun WeatherContent(w: WeatherData?) {
    val c = GlanceTheme.colors
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(c.primaryContainer)
            .cornerRadius(WidgetCorner)
            .padding(18.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        if (w == null) {
            Column(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                androidx.glance.text.Text(
                    "Loading weather…",
                    style = style(c.onPrimaryContainer, 18.sp, bold = true),
                )
                androidx.glance.text.Text(
                    "Tap to open the app and allow location",
                    style = style(c.onPrimaryContainer, 13.sp),
                )
            }
            return@Box
        }

        val (emoji, label) = Weather.describe(w.code, w.isDay)
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    androidx.glance.text.Text(
                        "$label · feels ${w.feels}°",
                        style = style(c.onPrimaryContainer, 14.sp),
                    )
                    androidx.glance.text.Text(
                        "${w.temp}°",
                        style = style(c.onPrimaryContainer, 52.sp, bold = true),
                    )
                }
                androidx.glance.text.Text(emoji, style = style(c.onPrimaryContainer, 56.sp))
            }
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    androidx.glance.text.Text(
                        "H ${w.hi}°   L ${w.lo}°",
                        style = style(c.onPrimaryContainer, 15.sp, bold = true),
                    )
                    androidx.glance.text.Text(
                        "Updated ${timeLabel(w.updated)}",
                        style = style(c.onPrimaryContainer, 11.sp),
                    )
                }
                RefreshPill()
            }
        }
    }
}
