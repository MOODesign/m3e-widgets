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
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import java.text.NumberFormat

private const val STEP_GOAL = 10_000f

class HealthWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = Store.health(context)
        provideContent { GlanceTheme { HealthContent(data) } }
    }
}

class HealthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HealthWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        SyncWorker.schedule(context)
        SyncWorker.syncNow(context)
    }
}

@Composable
private fun HealthContent(h: HealthData?) {
    val c = GlanceTheme.colors
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(c.tertiaryContainer)
            .cornerRadius(WidgetCorner)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "Today",
                    modifier = GlanceModifier.defaultWeight(),
                    style = style(c.onTertiaryContainer, 18.sp, bold = true),
                )
                RefreshPill("↻")
            }
            Spacer(GlanceModifier.padding(top = 8.dp))

            if (h == null) {
                Text(
                    "Tap to open the app and allow Health Connect",
                    style = style(c.onTertiaryContainer, 13.sp),
                )
                return@Column
            }

            Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                // Steps
                Tile(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    title = "👟 Steps",
                    value = if (h.steps >= 0) NumberFormat.getIntegerInstance().format(h.steps) else "—",
                    onTile = c.onSurface,
                ) {
                    if (h.steps >= 0) {
                        LinearProgressIndicator(
                            progress = (h.steps / STEP_GOAL).coerceIn(0f, 1f),
                            modifier = GlanceModifier.fillMaxWidth(),
                            color = c.tertiary,
                            backgroundColor = c.surfaceVariant,
                        )
                    }
                }
                Spacer(GlanceModifier.width(8.dp))
                // Heart rate
                Tile(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    title = "❤️ BPM",
                    value = if (h.bpm >= 0) "${h.bpm}" else "—",
                    onTile = c.onSurface,
                )
                Spacer(GlanceModifier.width(8.dp))
                // Sleep
                Tile(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    title = "😴 Sleep",
                    value = if (h.sleepMin >= 0) "${h.sleepMin / 60}h ${h.sleepMin % 60}m" else "—",
                    onTile = c.onSurface,
                )
            }
        }
    }
}

@Composable
private fun Tile(
    modifier: GlanceModifier,
    title: String,
    value: String,
    onTile: ColorProvider,
    extra: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .background(GlanceTheme.colors.surface)
            .cornerRadius(22.dp)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = style(onTile, 11.sp))
        Text(value, style = style(onTile, 20.sp, bold = true))
        extra()
    }
}
