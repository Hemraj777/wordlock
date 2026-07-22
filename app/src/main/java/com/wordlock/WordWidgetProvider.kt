package com.wordlock

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class WordWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, WordWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.wordlock.ACTION_REFRESH_WIDGET"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, WordWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val word = WordProvider.getRandomWord(context)
            val views = RemoteViews(context.packageName, R.layout.widget_word)

            views.setTextViewText(R.id.widgetWord, word.word)
            views.setTextViewText(R.id.widgetPron, word.pronunciation)
            views.setTextViewText(R.id.widgetMeaning, word.meaning)
            views.setTextViewText(R.id.widgetMeaningNP, word.meaningNP)
            views.setTextViewText(R.id.widgetExample, "\u201C${word.example}\u201D")

            val refreshIntent = Intent(context, WordWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                context, 0, refreshIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetWord, pendingIntent)
            views.setOnClickPendingIntent(R.id.widgetMeaning, pendingIntent)
            views.setOnClickPendingIntent(R.id.widgetMeaningNP, pendingIntent)

            manager.updateAppWidget(id, views)
        }
    }
}
