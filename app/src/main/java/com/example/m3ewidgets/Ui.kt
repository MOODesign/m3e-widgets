package com.example.m3ewidgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.GlanceId
import java.text.DateFormat
import java.util.Date

/** Tapping the refresh pill enqueues an immediate sync. */
class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        SyncWorker.syncNow(context)
    }
}

fun style(color: ColorProvider, size: TextUnit = 14.sp, bold: Boolean = false) = TextStyle(
    color = color,
    fontSize = size,
    fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
)

/** M3E-style fully rounded action pill. */
@Composable
fun RefreshPill(label: String = "↻  Refresh") {
    val c = GlanceTheme.colors
    Box(
        modifier = GlanceModifier
            .background(c.primary)
            .cornerRadius(100.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(actionRunCallback<RefreshAction>()),
    ) {
        Text(label, style = style(c.onPrimary, 13.sp, bold = true))
    }
}

fun timeLabel(millis: Long): String =
    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(millis))

val WidgetCorner: Dp = 32.dp
