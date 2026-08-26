package com.fiftycar.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.fiftycar.app.R

/**
 * 2x2 快捷控车小组件(RemoteViews 方案,通吃 Android 8~16)
 * 点击按钮 → 发送广播到应用层执行(此处留扩展点)
 */
class QuickControlWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> updateWidget(context, appWidgetManager, id) }
    }

    private fun updateWidget(context: Context, mgr: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_quick_control)
        views.setTextViewText(R.id.widget_status, "50Car 快捷控车")

        val lockIntent = PendingIntent.getBroadcast(
            context, 1,
            Intent(ACTION_WIDGET_CMD).setPackage(context.packageName).putExtra(EXTRA_CMD, "LOCK"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val unlockIntent = PendingIntent.getBroadcast(
            context, 2,
            Intent(ACTION_WIDGET_CMD).setPackage(context.packageName).putExtra(EXTRA_CMD, "UNLOCK"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        views.setOnClickPendingIntent(R.id.widget_lock_btn, lockIntent)
        views.setOnClickPendingIntent(R.id.widget_unlock_btn, unlockIntent)
        mgr.updateAppWidget(widgetId, views)
    }

    companion object {
        const val ACTION_WIDGET_CMD = "com.fiftycar.app.action.WIDGET_CMD"
        const val EXTRA_CMD = "cmd"
    }
}
